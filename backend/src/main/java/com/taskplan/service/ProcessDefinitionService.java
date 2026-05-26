package com.taskplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskplan.entity.ProcessDefinition;
import com.taskplan.entity.ProcessDefinitionHistory;
import com.taskplan.entity.ProcessDefinitionVersionDiff;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ProcessDefinitionHistoryRepository;
import com.taskplan.repository.ProcessDefinitionRepository;
import com.taskplan.repository.ProcessScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessDefinitionService {

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private ProcessDefinitionHistoryRepository historyRepository;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ProcessScheduleRepository processScheduleRepository;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ProcessDefinition createProcess(ProcessDefinition process) {
        validateProcess(process);
        if (processDefinitionRepository.existsByProjectIdAndName(process.getProjectId(), process.getName())) {
            throw new BusinessException("工作流名称已存在");
        }
        process.setVersion(1);
        ProcessDefinition saved = processDefinitionRepository.save(process);
        saveToHistory(saved);
        return saved;
    }

    @Transactional
    public ProcessDefinition createProcessWithNodes(Long projectId, String name, String releaseState, 
            List<Map<String, Object>> taskNodes, List<Map<String, Object>> connections) {
        return saveProcessWithNodes(null, projectId, name, releaseState, taskNodes, connections);
    }

    @Transactional
    public ProcessDefinition saveProcessWithNodes(Long id, Long projectId, String name, String releaseState, 
            List<Map<String, Object>> taskNodes, List<Map<String, Object>> connections) {
        validateProcessInput(projectId, name);
        
        ProcessDefinition process;
        boolean isNew = false;
        
        if (id != null) {
            process = getProcess(id);
            saveToHistory(process);
            process.setVersion(process.getVersion() + 1);
            process.setName(name);
            process.setProjectId(projectId);
        } else {
            process = processDefinitionRepository.findByProjectIdAndName(projectId, name);
            if (process == null) {
                process = new ProcessDefinition();
                process.setProjectId(projectId);
                process.setName(name);
                process.setVersion(1);
                isNew = true;
            } else {
                saveToHistory(process);
                process.setVersion(process.getVersion() + 1);
            }
        }
        
        process.setReleaseState(releaseState);
        
        try {
            Map<String, Object> definition = new HashMap<>();
            definition.put("nodes", taskNodes);
            definition.put("connections", connections);
            process.setDefinitionJson(objectMapper.writeValueAsString(definition));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize process definition", e);
            throw new BusinessException("保存失败");
        }
        
        ProcessDefinition saved = processDefinitionRepository.save(process);
        
        if (isNew) {
            saveToHistory(saved);
        }
        
        log.info("Process saved with nodes: id={}, name={}, version={}", saved.getId(), saved.getName(), saved.getVersion());
        return saved;
    }

    private void validateProcessInput(Long projectId, String name) {
        if (projectId == null) {
            throw new BusinessException("项目ID不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("工作流名称不能为空");
        }
    }

    @Transactional
    public ProcessDefinition updateProcess(Long id, ProcessDefinition process) {
        ProcessDefinition existing = getProcess(id);
        
        if (process.getName() != null && !process.getName().equals(existing.getName())) {
            if (processDefinitionRepository.existsByProjectIdAndName(existing.getProjectId(), process.getName())) {
                throw new BusinessException("工作流名称已存在");
            }
            existing.setName(process.getName());
        }
        if (process.getDescription() != null) existing.setDescription(process.getDescription());
        if (process.getDefinitionJson() != null) existing.setDefinitionJson(process.getDefinitionJson());
        if (process.getProcessType() != null) existing.setProcessType(process.getProcessType());
        if (process.getOwner() != null) existing.setOwner(process.getOwner());
        
        return processDefinitionRepository.save(existing);
    }

    @Transactional
    public ProcessDefinition releaseProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        
        saveToHistory(process);
        
        process.setVersion(process.getVersion() + 1);
        process.setReleaseState("ONLINE");
        
        ProcessDefinition saved = processDefinitionRepository.save(process);
        
        log.info("Process released: id={}, name={}, version={}", id, process.getName(), process.getVersion());
        return saved;
    }

    @Transactional
    public ProcessDefinition offlineProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        process.setReleaseState("OFFLINE");
        return processDefinitionRepository.save(process);
    }

    private void saveToHistory(ProcessDefinition process) {
        ProcessDefinitionHistory history = new ProcessDefinitionHistory();
        history.setProcessDefinitionId(process.getId());
        history.setProjectId(process.getProjectId());
        history.setName(process.getName());
        history.setDescription(process.getDescription());
        history.setDefinitionJson(process.getDefinitionJson());
        history.setProcessType(process.getProcessType());
        history.setReleaseState(process.getReleaseState());
        history.setVersion(process.getVersion());
        history.setUserId(process.getUserId());
        history.setOwner(process.getOwner());
        history.setCreateTime(process.getCreateTime());
        history.setHistoryTime(LocalDateTime.now());
        
        historyRepository.save(history);
        log.info("Saved process history: processId={}, version={}", process.getId(), process.getVersion());
    }

    @Transactional
    public void deleteProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        
        log.info("Deleting process definition: id={}, name={}", id, process.getName());
        
        historyRepository.deleteByProcessDefinitionId(id);
        
        List<com.taskplan.entity.ProcessSchedule> schedules = processScheduleRepository.findByProcessDefinitionId(id);
        for (com.taskplan.entity.ProcessSchedule schedule : schedules) {
            log.info("Deleting schedule: id={} for process: {}", schedule.getId(), id);
            try {
                scheduleExecutorService.removeScheduleJob(schedule.getId());
            } catch (Exception e) {
                log.warn("Failed to remove schedule job: {}", e.getMessage());
            }
            processScheduleRepository.deleteById(schedule.getId());
        }
        
        processDefinitionRepository.deleteById(id);
        log.info("Process definition deleted: id={}", id);
    }

    @Transactional
    public void executeProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        if (!"ONLINE".equals(process.getReleaseState())) {
            throw new BusinessException("工作流未发布，无法执行");
        }
        processInstanceService.startProcess(process);
    }

    public ProcessDefinition getProcess(Long id) {
        return processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工作流不存在"));
    }

    public Page<ProcessDefinition> listProcesses(Long projectId, String name, Pageable pageable) {
        if (projectId != null) {
            if (name != null && !name.isEmpty()) {
                return processDefinitionRepository.findByProjectIdAndNameContaining(projectId, name, pageable);
            }
            return processDefinitionRepository.findByProjectId(projectId, pageable);
        } else {
            if (name != null && !name.isEmpty()) {
                return processDefinitionRepository.findByNameContaining(name, pageable);
            }
            return processDefinitionRepository.findAll(pageable);
        }
    }

    public List<ProcessDefinition> listProcessesByProject(Long projectId) {
        return processDefinitionRepository.findByProjectId(projectId);
    }

    public List<ProcessDefinition> listOnlineProcesses(Long projectId) {
        return processDefinitionRepository.findByProjectIdAndReleaseState(projectId, "ONLINE");
    }

    private void validateProcess(ProcessDefinition process) {
        if (process.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        if (process.getName() == null || process.getName().trim().isEmpty()) {
            throw new BusinessException("工作流名称不能为空");
        }
    }

    public List<ProcessDefinitionHistory> getHistoryByProcessId(Long processDefinitionId) {
        return historyRepository.findByProcessDefinitionIdOrderByVersionDesc(processDefinitionId);
    }

    public Page<ProcessDefinitionHistory> getHistoryByProcessId(Long processDefinitionId, Pageable pageable) {
        return historyRepository.findByProcessDefinitionId(processDefinitionId, pageable);
    }

    public ProcessDefinitionHistory getHistoryByVersion(Long processDefinitionId, Integer version) {
        return historyRepository.findByProcessDefinitionIdAndVersion(processDefinitionId, version)
                .orElseThrow(() -> new BusinessException("历史版本不存在"));
    }

    @Transactional
    public ProcessDefinition rollbackToVersion(Long processDefinitionId, Integer version) {
        ProcessDefinitionHistory history = getHistoryByVersion(processDefinitionId, version);
        
        ProcessDefinition current = getProcess(processDefinitionId);
        
        saveToHistory(current);
        
        current.setName(history.getName());
        current.setDescription(history.getDescription());
        current.setDefinitionJson(history.getDefinitionJson());
        current.setProcessType(history.getProcessType());
        current.setOwner(history.getOwner());
        current.setVersion(current.getVersion() + 1);
        
        ProcessDefinition saved = processDefinitionRepository.save(current);
        
        log.info("Process rolled back: id={}, name={}, rolled back to version={}, new version={}", 
                processDefinitionId, current.getName(), version, current.getVersion());
        
        return saved;
    }

    public ProcessDefinitionVersionDiff compareVersions(Long processDefinitionId, Integer sourceVersion, Integer targetVersion) {
        ProcessDefinitionHistory sourceHistory = historyRepository.findByProcessDefinitionIdAndVersion(processDefinitionId, sourceVersion)
                .orElseThrow(() -> new BusinessException("源版本不存在"));
        
        ProcessDefinitionHistory targetHistory = historyRepository.findByProcessDefinitionIdAndVersion(processDefinitionId, targetVersion)
                .orElseThrow(() -> new BusinessException("目标版本不存在"));
        
        ProcessDefinitionVersionDiff diff = new ProcessDefinitionVersionDiff();
        diff.setProcessDefinitionId(sourceHistory.getProcessDefinitionId());
        diff.setSourceVersion(sourceHistory.getVersion());
        diff.setTargetVersion(targetHistory.getVersion());
        
        if (!Objects.equals(sourceHistory.getName(), targetHistory.getName())) {
            diff.setNameChanged(sourceHistory.getName() + " -> " + targetHistory.getName());
        }
        if (!Objects.equals(sourceHistory.getDescription(), targetHistory.getDescription())) {
            diff.setDescriptionChanged("已修改");
        }
        if (!Objects.equals(sourceHistory.getProcessType(), targetHistory.getProcessType())) {
            diff.setProcessTypeChanged(sourceHistory.getProcessType() + " -> " + targetHistory.getProcessType());
        }
        
        diff.setNodesDiff(compareNodes(sourceHistory.getDefinitionJson(), targetHistory.getDefinitionJson()));
        diff.setConnectionsDiff(compareConnections(sourceHistory.getDefinitionJson(), targetHistory.getDefinitionJson()));
        
        return diff;
    }

    public ProcessDefinitionVersionDiff compareWithCurrent(Long processDefinitionId, Integer version) {
        ProcessDefinition current = getProcess(processDefinitionId);
        ProcessDefinitionHistory history = historyRepository.findByProcessDefinitionIdAndVersion(processDefinitionId, version)
                .orElseThrow(() -> new BusinessException("历史版本不存在"));
        
        return compareProcessDefinitions(history, current);
    }

    private ProcessDefinitionVersionDiff compareProcessDefinitions(ProcessDefinitionHistory source, ProcessDefinition target) {
        ProcessDefinitionVersionDiff diff = new ProcessDefinitionVersionDiff();
        diff.setProcessDefinitionId(source.getProcessDefinitionId());
        diff.setSourceVersion(source.getVersion());
        diff.setTargetVersion(target.getVersion());
        diff.setCurrentVersion(target.getVersion());
        
        if (!Objects.equals(source.getName(), target.getName())) {
            diff.setNameChanged(source.getName() + " -> " + target.getName());
        }
        if (!Objects.equals(source.getDescription(), target.getDescription())) {
            diff.setDescriptionChanged("已修改");
        }
        if (!Objects.equals(source.getProcessType(), target.getProcessType())) {
            diff.setProcessTypeChanged(source.getProcessType() + " -> " + target.getProcessType());
        }
        
        try {
            Map<String, Object> sourceDef = parseDefinitionJson(source.getDefinitionJson());
            Map<String, Object> targetDef = parseDefinitionJson(target.getDefinitionJson());
            
            diff.setTargetNodes((List<Map<String, Object>>) sourceDef.getOrDefault("nodes", new ArrayList<>()));
            diff.setCurrentNodes((List<Map<String, Object>>) targetDef.getOrDefault("nodes", new ArrayList<>()));
            diff.setTargetConnections((List<Map<String, Object>>) sourceDef.getOrDefault("connections", new ArrayList<>()));
            diff.setCurrentConnections((List<Map<String, Object>>) targetDef.getOrDefault("connections", new ArrayList<>()));
            
            diff.setNodesDiff(compareNodes(source.getDefinitionJson(), target.getDefinitionJson()));
            diff.setConnectionsDiff(compareConnections(source.getDefinitionJson(), target.getDefinitionJson()));
        } catch (Exception e) {
            log.error("Failed to compare process definitions", e);
        }
        
        return diff;
    }

    private ProcessDefinitionVersionDiff compareProcessDefinitions(ProcessDefinitionHistory source, ProcessDefinitionHistory target) {
        ProcessDefinitionVersionDiff diff = new ProcessDefinitionVersionDiff();
        diff.setProcessDefinitionId(source.getProcessDefinitionId());
        diff.setSourceVersion(source.getVersion());
        diff.setTargetVersion(target.getVersion());
        
        if (!Objects.equals(source.getName(), target.getName())) {
            diff.setNameChanged(source.getName() + " -> " + target.getName());
        }
        if (!Objects.equals(source.getDescription(), target.getDescription())) {
            diff.setDescriptionChanged("已修改");
        }
        if (!Objects.equals(source.getProcessType(), target.getProcessType())) {
            diff.setProcessTypeChanged(source.getProcessType() + " -> " + target.getProcessType());
        }
        
        diff.setNodesDiff(compareNodes(source.getDefinitionJson(), target.getDefinitionJson()));
        diff.setConnectionsDiff(compareConnections(source.getDefinitionJson(), target.getDefinitionJson()));
        
        return diff;
    }

    private ProcessDefinitionVersionDiff.NodeDiff compareNodes(String sourceJson, String targetJson) {
        ProcessDefinitionVersionDiff.NodeDiff nodeDiff = new ProcessDefinitionVersionDiff.NodeDiff();
        nodeDiff.setAdded(new ArrayList<>());
        nodeDiff.setRemoved(new ArrayList<>());
        nodeDiff.setModified(new ArrayList<>());
        
        try {
            Map<String, Object> sourceDef = parseDefinitionJson(sourceJson);
            Map<String, Object> targetDef = parseDefinitionJson(targetJson);
            
            List<Map<String, Object>> sourceNodes = (List<Map<String, Object>>) sourceDef.getOrDefault("nodes", new ArrayList<>());
            List<Map<String, Object>> targetNodes = (List<Map<String, Object>>) targetDef.getOrDefault("nodes", new ArrayList<>());
            
            Map<String, Map<String, Object>> sourceNodeMap = sourceNodes.stream()
                    .collect(Collectors.toMap(n -> String.valueOf(n.get("id")), n -> n));
            Map<String, Map<String, Object>> targetNodeMap = targetNodes.stream()
                    .collect(Collectors.toMap(n -> String.valueOf(n.get("id")), n -> n));
            
            for (String nodeId : targetNodeMap.keySet()) {
                if (!sourceNodeMap.containsKey(nodeId)) {
                    Map<String, Object> node = targetNodeMap.get(nodeId);
                    ProcessDefinitionVersionDiff.NodeChange change = new ProcessDefinitionVersionDiff.NodeChange();
                    change.setId(nodeId);
                    change.setName(String.valueOf(node.getOrDefault("name", "未知")));
                    change.setTaskType(String.valueOf(node.getOrDefault("taskType", "未知")));
                    change.setProperties(node);
                    change.setAdded(true);
                    nodeDiff.getAdded().add(change);
                } else {
                    Map<String, Object> sourceNode = sourceNodeMap.get(nodeId);
                    Map<String, Object> targetNode = targetNodeMap.get(nodeId);
                    if (!sourceNode.equals(targetNode)) {
                        ProcessDefinitionVersionDiff.NodeChange change = new ProcessDefinitionVersionDiff.NodeChange();
                        change.setId(nodeId);
                        change.setName(String.valueOf(targetNode.getOrDefault("name", "未知")));
                        change.setTaskType(String.valueOf(targetNode.getOrDefault("taskType", "未知")));
                        change.setProperties(targetNode);
                        change.setModified(true);
                        nodeDiff.getModified().add(change);
                    }
                }
            }
            
            for (String nodeId : sourceNodeMap.keySet()) {
                if (!targetNodeMap.containsKey(nodeId)) {
                    Map<String, Object> node = sourceNodeMap.get(nodeId);
                    ProcessDefinitionVersionDiff.NodeChange change = new ProcessDefinitionVersionDiff.NodeChange();
                    change.setId(nodeId);
                    change.setName(String.valueOf(node.getOrDefault("name", "未知")));
                    change.setTaskType(String.valueOf(node.getOrDefault("taskType", "未知")));
                    change.setProperties(node);
                    change.setRemoved(true);
                    nodeDiff.getRemoved().add(change);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to compare nodes", e);
        }
        
        return nodeDiff;
    }

    private ProcessDefinitionVersionDiff.ConnectionDiff compareConnections(String sourceJson, String targetJson) {
        ProcessDefinitionVersionDiff.ConnectionDiff connectionDiff = new ProcessDefinitionVersionDiff.ConnectionDiff();
        connectionDiff.setAdded(new ArrayList<>());
        connectionDiff.setRemoved(new ArrayList<>());
        
        try {
            Map<String, Object> sourceDef = parseDefinitionJson(sourceJson);
            Map<String, Object> targetDef = parseDefinitionJson(targetJson);
            
            List<Map<String, Object>> sourceConnections = (List<Map<String, Object>>) sourceDef.getOrDefault("connections", new ArrayList<>());
            List<Map<String, Object>> targetConnections = (List<Map<String, Object>>) targetDef.getOrDefault("connections", new ArrayList<>());
            
            Set<String> sourceConnSet = sourceConnections.stream()
                    .map(this::connectionToString)
                    .collect(Collectors.toSet());
            Set<String> targetConnSet = targetConnections.stream()
                    .map(this::connectionToString)
                    .collect(Collectors.toSet());
            
            for (String connStr : targetConnSet) {
                if (!sourceConnSet.contains(connStr)) {
                    ProcessDefinitionVersionDiff.ConnectionChange change = parseConnectionString(connStr);
                    change.setAdded(true);
                    connectionDiff.getAdded().add(change);
                }
            }
            
            for (String connStr : sourceConnSet) {
                if (!targetConnSet.contains(connStr)) {
                    ProcessDefinitionVersionDiff.ConnectionChange change = parseConnectionString(connStr);
                    change.setRemoved(true);
                    connectionDiff.getRemoved().add(change);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to compare connections", e);
        }
        
        return connectionDiff;
    }

    private Map<String, Object> parseDefinitionJson(String json) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    private String connectionToString(Map<String, Object> connection) {
        String source = String.valueOf(connection.getOrDefault("source", ""));
        String target = String.valueOf(connection.getOrDefault("target", ""));
        String sourceHandle = String.valueOf(connection.getOrDefault("sourceHandle", ""));
        String targetHandle = String.valueOf(connection.getOrDefault("targetHandle", ""));
        return source + ":" + sourceHandle + "->" + target + ":" + targetHandle;
    }

    private ProcessDefinitionVersionDiff.ConnectionChange parseConnectionString(String connStr) {
        ProcessDefinitionVersionDiff.ConnectionChange change = new ProcessDefinitionVersionDiff.ConnectionChange();
        String[] parts = connStr.split("->");
        if (parts.length == 2) {
            String[] sourceParts = parts[0].split(":");
            String[] targetParts = parts[1].split(":");
            change.setSourceNodeId(sourceParts.length > 0 ? sourceParts[0] : "");
            change.setSourceHandle(sourceParts.length > 1 ? sourceParts[1] : "");
            change.setTargetNodeId(targetParts.length > 0 ? targetParts[0] : "");
            change.setTargetHandle(targetParts.length > 1 ? targetParts[1] : "");
        }
        return change;
    }
}