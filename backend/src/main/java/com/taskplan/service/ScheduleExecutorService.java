package com.taskplan.service;

import com.taskplan.entity.EnvVariable;
import com.taskplan.entity.ProcessDefinition;
import com.taskplan.entity.ProcessInstance;
import com.taskplan.entity.ProcessSchedule;
import com.taskplan.entity.ServerNode;
import com.taskplan.entity.TaskExecutionLog;
import com.taskplan.repository.ProcessDefinitionRepository;
import com.taskplan.repository.ProcessScheduleRepository;
import com.taskplan.repository.ServerNodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ScheduleExecutorService {

    @Autowired
    private ProcessScheduleRepository processScheduleRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ServerNodeRepository serverNodeRepository;

    @Autowired
    private SshService sshService;

    @Autowired
    private ScheduleLogService scheduleLogService;

    @Autowired
    private TaskExecutionLogService taskExecutionLogService;

    @Autowired
    private EnvVariableService envVariableService;

    @Autowired
    private DatasourceService datasourceService;

    private Scheduler scheduler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.getContext().put("scheduleExecutorService", this);
            scheduler.start();
            log.info("Quartz调度器启动成功");
            loadSchedules();
        } catch (SchedulerException e) {
            log.error("Quartz调度器启动失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                log.info("Quartz调度器已关闭");
            } catch (SchedulerException e) {
                log.error("Quartz调度器关闭失败: {}", e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void loadSchedules() {
        List<ProcessSchedule> schedules = processScheduleRepository.findByIsValid(1);
        log.info("加载调度计划数量: {}", schedules.size());

        for (ProcessSchedule schedule : schedules) {
            try {
                String jobKey = "process_schedule_" + schedule.getId();
                JobKey jobKeyObj = new JobKey(jobKey);

                if (!scheduler.checkExists(jobKeyObj)) {
                    addScheduleJob(schedule);
                    scheduleLogService.info(schedule.getId(), schedule.getProcessDefinitionId(),
                        String.format("调度任务已加载 - ID: %d, 工作流: %d, Cron: %s",
                            schedule.getId(), schedule.getProcessDefinitionId(), schedule.getCronExpression()));
                }
            } catch (Exception e) {
                log.error("加载调度计划失败: id={}, error={}", schedule.getId(), e.getMessage());
                scheduleLogService.error(schedule.getId(), schedule.getProcessDefinitionId(),
                    String.format("加载调度任务失败: %s", e.getMessage()));
            }
        }
    }

    public void addScheduleJob(ProcessSchedule schedule) throws SchedulerException {
        String jobKey = "process_schedule_" + schedule.getId();

        JobDetail job = JobBuilder.newJob(ScheduleJob.class)
                .withIdentity(jobKey)
                .usingJobData("scheduleId", schedule.getId())
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + schedule.getId())
                .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronExpression())
                        .inTimeZone(java.util.TimeZone.getTimeZone(schedule.getTimezone())))
                .build();

        scheduler.scheduleJob(job, trigger);
        log.info("调度任务已添加: {}", jobKey);
    }

    public void removeScheduleJob(Long scheduleId) throws SchedulerException {
        String jobKey = "process_schedule_" + scheduleId;
        scheduler.deleteJob(new JobKey(jobKey));
        log.info("调度任务已删除: {}", jobKey);
    }

    public void executeProcess(Long processDefinitionId) {
        executeProcessWithInstance(processDefinitionId, null);
    }

    public void executeProcessWithInstance(Long processDefinitionId, Long instanceId) {
        try {
            ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                    .orElseThrow(() -> new RuntimeException("工作流定义不存在"));

            if (!"ONLINE".equals(processDefinition.getReleaseState())) {
                log.warn("工作流未上线，跳过执行: id={}", processDefinitionId);
                return;
            }

            ProcessInstance instance;
            if (instanceId != null) {
                instance = processInstanceService.getInstance(instanceId);
            } else {
                instance = processInstanceService.startProcess(processDefinition, "SCHEDULER");
            }
            log.info("工作流实例创建成功: id={}, processName={}", instance.getId(), instance.getProcessName());

            scheduleLogService.info(null, processDefinitionId,
                String.format("工作流执行开始 - 实例ID: %d, 工作流名称: %s, 时间: %s",
                    instance.getId(), processDefinition.getName(), LocalDateTime.now().format(DATE_FORMATTER)));

            executeTasks(instance.getId(), processDefinition.getDefinitionJson());

        } catch (Exception e) {
            log.error("执行工作流失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage(), e);
            scheduleLogService.error(null, processDefinitionId,
                String.format("工作流执行失败: %s", e.getMessage()));
        }
    }

    public void executeProcess(Long scheduleId, Long processDefinitionId) {
        try {
            ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                    .orElseThrow(() -> new RuntimeException("工作流定义不存在"));

            if (!"ONLINE".equals(processDefinition.getReleaseState())) {
                log.warn("工作流未上线，跳过执行: id={}", processDefinitionId);
                scheduleLogService.warn(scheduleId, processDefinitionId,
                    String.format("工作流未上线，跳过执行: %s", processDefinition.getName()));
                return;
            }

            ProcessInstance instance = processInstanceService.startProcess(processDefinition, "SCHEDULER");
            log.info("工作流实例创建成功: id={}, processName={}", instance.getId(), instance.getProcessName());

            scheduleLogService.info(scheduleId, processDefinitionId,
                String.format("[调度触发] 工作流执行开始 - 调度ID: %d, 实例ID: %d, 工作流名称: %s, 时间: %s",
                    scheduleId, instance.getId(), processDefinition.getName(), LocalDateTime.now().format(DATE_FORMATTER)));

            executeTasks(scheduleId, instance.getId(), processDefinition.getDefinitionJson());

        } catch (Exception e) {
            log.error("执行工作流失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage(), e);
            scheduleLogService.error(scheduleId, processDefinitionId,
                String.format("[调度触发] 工作流执行失败: %s", e.getMessage()));
        }
    }

    private void executeTasks(Long scheduleId, Long processInstanceId, String definitionJson) {
        boolean allSuccess = true;
        try {
            JsonNode rootNode = objectMapper.readTree(definitionJson);
            JsonNode nodesNode = rootNode.get("nodes");

            if (nodesNode != null && nodesNode.isArray()) {
                for (JsonNode node : nodesNode) {
                    try {
                        executeTask(scheduleId, processInstanceId, node);
                    } catch (Exception e) {
                        allSuccess = false;
                        log.error("执行单个任务失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            allSuccess = false;
            log.error("解析工作流定义失败: {}", e.getMessage());
            scheduleLogService.error(scheduleId, processInstanceId,
                String.format("[工作流执行失败] 工作流实例ID: %d, 解析工作流定义失败: %s",
                    processInstanceId, e.getMessage()));
        } finally {
            try {
                String endState = allSuccess ? "SUCCESS" : "FAILURE";
                processInstanceService.endProcess(processInstanceId, endState);

                scheduleLogService.info(scheduleId, processInstanceId,
                    String.format("[工作流执行完成] 工作流实例ID: %d, 状态: %s, 时间: %s",
                        processInstanceId, endState, LocalDateTime.now().format(DATE_FORMATTER)));

                log.info("工作流执行完成: processInstanceId={}, state={}", processInstanceId, endState);
            } catch (Exception e) {
                log.error("更新工作流实例状态失败: {}", e.getMessage());
            }
        }
    }

    private void executeTasks(Long processInstanceId, String definitionJson) {
        executeTasks(null, processInstanceId, definitionJson);
    }

    private void executeTask(Long scheduleId, Long processInstanceId, JsonNode node) {
        try {
            String taskType = node.has("taskType") ? node.get("taskType").asText() : "SHELL";
            String script = node.has("script") ? node.get("script").asText() : "";
            String taskName = node.has("name") ? node.get("name").asText() : "Unknown";
            String taskNodeId = node.has("id") ? node.get("id").asText() : "Unknown";
            Long serverId = node.has("serverId") && !node.get("serverId").isNull() ? node.get("serverId").asLong() : null;

            Map<String, String> envVariables = loadEnvVariables(node);
            String processedScript = replaceEnvVariables(script, envVariables);

            scheduleLogService.info(scheduleId, processInstanceId,
                String.format("[任务执行开始] 工作流实例ID: %d, 任务节点ID: %s, 任务名称: %s, 任务类型: %s, 时间: %s",
                    processInstanceId, taskNodeId, taskName, taskType, LocalDateTime.now().format(DATE_FORMATTER)));

            boolean success;
            String output;
            String error;
            String serverIp = null;
            Long actualServerId = null;

            if ("SQL".equalsIgnoreCase(taskType)) {
                Long datasourceId = node.has("datasourceId") && !node.get("datasourceId").isNull() ? node.get("datasourceId").asLong() : null;

                if (datasourceId != null) {
                    try {
                        output = datasourceService.executeSql(datasourceId, processedScript);
                        success = !output.startsWith("SQL执行失败:");
                        error = success ? null : output;
                        serverIp = "localhost (Direct DB Connection)";
                        actualServerId = datasourceId;

                        log.info("SQL任务执行结果: success={}, outputLength={}", success, output.length());
                    } catch (Exception e) {
                        success = false;
                        output = "SQL执行异常: " + e.getMessage();
                        error = output;
                        serverIp = "localhost (Direct DB Connection)";
                        actualServerId = datasourceId;
                        log.error("SQL任务执行失败: {}", e.getMessage(), e);
                    }
                } else {
                    success = false;
                    output = "SQL任务缺少数据源配置 (datasourceId)";
                    error = output;
                    log.warn("SQL任务缺少datasourceId配置");
                }

                String logMessage = String.format("[SQL任务执行完成] 工作流实例ID: %d, 任务节点ID: %s, 任务名称: %s, 状态: %s\n%s",
                    processInstanceId, taskNodeId, taskName,
                    success ? "成功" : "失败",
                    output.length() > 500 ? output.substring(0, 500) + "\n...(结果已截断)" : output);

                if (success) {
                    scheduleLogService.info(scheduleId, processInstanceId, logMessage);
                } else {
                    scheduleLogService.error(scheduleId, processInstanceId, logMessage);
                }

                saveTaskLog(processInstanceId, taskNodeId, taskName, taskType,
                        actualServerId, serverIp, success, output, error);
            } else {
                ServerNode server = null;
                if (serverId != null) {
                    server = serverNodeRepository.findById(serverId).orElse(null);
                }

                if (server == null) {
                    List<ServerNode> onlineServers = serverNodeRepository.findByStatus("ONLINE");
                    if (!onlineServers.isEmpty()) {
                        server = onlineServers.get(0);
                    }
                }

                if (server != null) {
                    log.info("执行任务: type={}, server={}", taskType, server.getHostname());

                    Map<String, Object> result = sshService.executeScript(server, processedScript, taskType);

                    success = (Boolean) result.get("success");
                    output = (String) result.get("output");
                    error = (String) result.get("error");
                    serverIp = server.getHostname();
                    actualServerId = server.getId();

                    log.info("任务执行结果: success={}, output={}, error={}", success, output, error);

                    String logMessage = String.format("[任务执行完成] 工作流实例ID: %d, 任务节点ID: %s, 任务名称: %s, 任务类型: %s, 服务器: %s, 状态: %s, 输出: %s",
                        processInstanceId, taskNodeId, taskName, taskType, server.getHostname(),
                        success ? "成功" : "失败",
                        output != null && output.length() > 200 ? output.substring(0, 200) + "..." : (output != null ? output : "无"));

                    if (success) {
                        scheduleLogService.info(scheduleId, processInstanceId, logMessage);
                    } else {
                        scheduleLogService.error(scheduleId, processInstanceId,
                            logMessage + (error != null ? ", 错误: " + error : ""));
                    }

                    saveTaskLog(processInstanceId, taskNodeId, taskName, taskType,
                            server.getId(), server.getHostname(), success, output, error);
                } else {
                    log.warn("没有可用的服务器节点来执行任务");
                    scheduleLogService.warn(scheduleId, processInstanceId,
                        String.format("[任务执行异常] 工作流实例ID: %d, 任务名称: %s, 没有可用的服务器节点来执行任务",
                            processInstanceId, taskName));
                }
            }

        } catch (Exception e) {
            log.error("执行任务失败: {}", e.getMessage());
            scheduleLogService.error(scheduleId, processInstanceId,
                String.format("[任务执行异常] 工作流实例ID: %d, 任务名称: %s, 错误: %s",
                    processInstanceId, node.has("name") ? node.get("name").asText() : "Unknown", e.getMessage()));
        }
    }

    private Map<String, String> loadEnvVariables(JsonNode node) {
        Map<String, String> envMap = new HashMap<>();
        try {
            if (node.has("envConfigGroupIds") && node.get("envConfigGroupIds").isArray()) {
                List<Long> groupIds = new ArrayList<>();
                for (JsonNode groupIdNode : node.get("envConfigGroupIds")) {
                    groupIds.add(groupIdNode.asLong());
                }
                if (!groupIds.isEmpty()) {
                    List<EnvVariable> variables = envVariableService.listByGroupIds(groupIds);
                    for (EnvVariable var : variables) {
                        if (var.getName() != null && var.getValue() != null) {
                            envMap.put(var.getName(), var.getValue());
                        }
                    }
                    log.info("加载环境变量配置: groupIds={}, count={}", groupIds, variables.size());
                }
            }
        } catch (Exception e) {
            log.warn("加载环境变量失败: {}", e.getMessage());
        }
        return envMap;
    }

    private String replaceEnvVariables(String script, Map<String, String> envVariables) {
        if (script == null || script.isEmpty() || envVariables.isEmpty()) {
            return script;
        }

        String result = script;
        for (Map.Entry<String, String> entry : envVariables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, entry.getValue());
            }
        }
        return result;
    }

    private void executeTask(Long processInstanceId, JsonNode node) {
        executeTask(null, processInstanceId, node);
    }

    private void saveTaskLog(Long processInstanceId, String taskNodeId, String taskName,
                            String taskType, Long serverId, String serverIp,
                            boolean success, String output, String error) {
        try {
            TaskExecutionLog logEntity = new TaskExecutionLog();
            logEntity.setProcessInstanceId(processInstanceId);
            logEntity.setTaskNodeId(taskNodeId);
            logEntity.setTaskName(taskName);
            logEntity.setTaskType(taskType);
            logEntity.setServerId(serverId);
            logEntity.setServerIp(serverIp);
            logEntity.setStatus(success ? "SUCCESS" : "FAILED");
            logEntity.setOutput(output);
            logEntity.setError(error);
            logEntity.setCreateTime(LocalDateTime.now());

            taskExecutionLogService.saveLog(logEntity);
            log.info("任务执行日志已保存: processInstanceId={}, taskNodeId={}, success={}",
                    processInstanceId, taskNodeId, success);
        } catch (Exception e) {
            log.error("保存任务执行日志失败: {}", e.getMessage());
        }
    }

    @DisallowConcurrentExecution
    public static class ScheduleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Long scheduleId = context.getJobDetail().getJobDataMap().getLong("scheduleId");

            ScheduleExecutorService executorService = null;
            try {
                executorService = (ScheduleExecutorService)
                        context.getScheduler().getContext().get("scheduleExecutorService");
            } catch (SchedulerException e) {
                log.error("获取调度器上下文失败: {}", e.getMessage());
                return;
            }

            if (executorService != null) {
                try {
                    ProcessSchedule schedule = executorService.processScheduleRepository.findById(scheduleId).orElse(null);
                    if (schedule != null && schedule.getIsValid() == 1) {
                        executorService.scheduleLogService.info(scheduleId, schedule.getProcessDefinitionId(),
                            String.format("[定时触发] 调度任务触发 - 调度ID: %d, 工作流ID: %d, Cron: %s, 时间: %s",
                                scheduleId, schedule.getProcessDefinitionId(), schedule.getCronExpression(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                        executorService.executeProcess(scheduleId, schedule.getProcessDefinitionId());
                    }
                } catch (Exception e) {
                    log.error("调度任务执行失败: scheduleId={}, error={}", scheduleId, e.getMessage());
                    executorService.scheduleLogService.error(scheduleId, null,
                        String.format("调度任务执行失败: %s", e.getMessage()));
                }
            }
        }
    }
}
