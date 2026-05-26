package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ProcessDefinition;
import com.taskplan.entity.ServerNode;
import com.taskplan.repository.ProcessDefinitionRepository;
import com.taskplan.repository.ServerNodeRepository;
import com.taskplan.service.ScheduleExecutorService;
import com.taskplan.service.SshService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/remote-execute")
public class RemoteExecuteController {

    @Autowired
    private SshService sshService;

    @Autowired
    private ServerNodeRepository serverNodeRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/command")
    public Result<Map<String, Object>> executeCommand(@RequestBody Map<String, Object> params) {
        Long serverId = params.get("serverId") != null ? ((Number) params.get("serverId")).longValue() : null;
        String command = (String) params.get("command");

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

        if (server == null) {
            return Result.error("没有可用的服务器节点");
        }

        log.info("执行远程命令: server={}, command={}", server.getHostname(), command);
        Map<String, Object> result = sshService.executeCommand(server, command);
        return Result.success(result);
    }

    @PostMapping("/script")
    public Result<Map<String, Object>> executeScript(@RequestBody Map<String, Object> params) {
        Long serverId = params.get("serverId") != null ? ((Number) params.get("serverId")).longValue() : null;
        String script = (String) params.get("script");
        String scriptType = params.get("scriptType") != null ? (String) params.get("scriptType") : "SHELL";

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

        if (server == null) {
            return Result.error("没有可用的服务器节点");
        }

        log.info("执行远程脚本: server={}, type={}", server.getHostname(), scriptType);
        Map<String, Object> result = sshService.executeScript(server, script, scriptType);
        return Result.success(result);
    }

    @PostMapping("/process/{processDefinitionId}")
    public Result<String> executeProcess(@PathVariable Long processDefinitionId) {
        try {
            ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                    .orElseThrow(() -> new RuntimeException("工作流定义不存在"));

            scheduleExecutorService.executeProcess(processDefinitionId);
            return Result.success("工作流执行已启动");

        } catch (Exception e) {
            log.error("执行工作流失败: {}", e.getMessage());
            return Result.error("执行工作流失败: " + e.getMessage());
        }
    }

    @GetMapping("/servers/test")
    public Result<Map<String, Object>> testServerConnection(@RequestParam Long serverId) {
        ServerNode server = serverNodeRepository.findById(serverId).orElse(null);
        if (server == null) {
            return Result.error("服务器不存在");
        }

        boolean success = sshService.testSshConnection(server);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("serverId", serverId);
        result.put("hostname", server.getHostname());
        result.put("message", success ? "SSH连接成功" : "SSH连接失败");

        if (success) {
            server.setStatus("ONLINE");
        } else {
            server.setStatus("OFFLINE");
        }
        serverNodeRepository.save(server);

        return Result.success(result);
    }
}