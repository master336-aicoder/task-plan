package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ServerNode;
import com.taskplan.service.ServerNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
public class ServerNodeController {

    @Autowired
    private ServerNodeService serverNodeService;

    @GetMapping
    public Result<Map<String, Object>> listServers(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ServerNode> serverPage = serverNodeService.listServers(name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", serverPage.getContent());
        result.put("total", serverPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<ServerNode>> listAll() {
        return Result.success(serverNodeService.listAll());
    }

    @GetMapping("/online")
    public Result<List<ServerNode>> listOnline() {
        return Result.success(serverNodeService.listOnline());
    }

    @GetMapping("/group/{groupName}")
    public Result<List<ServerNode>> listByGroup(@PathVariable String groupName) {
        return Result.success(serverNodeService.listByGroup(groupName));
    }

    @GetMapping("/{id}")
    public Result<ServerNode> getServer(@PathVariable Long id) {
        return Result.success(serverNodeService.getServer(id));
    }

    @PostMapping
    public Result<ServerNode> createServer(@RequestBody ServerNode server) {
        return Result.success(serverNodeService.createServer(server));
    }

    @PutMapping("/{id}")
    public Result<ServerNode> updateServer(@PathVariable Long id, @RequestBody ServerNode server) {
        return Result.success(serverNodeService.updateServer(id, server));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteServer(@PathVariable Long id) {
        serverNodeService.deleteServer(id);
        return Result.success();
    }

    @PostMapping("/{id}/ping")
    public Result<Boolean> pingServer(@PathVariable Long id) {
        boolean success = serverNodeService.pingServer(id);
        return Result.success(success);
    }

    @PostMapping("/test-connection")
    public Result<Map<String, Object>> testConnection(@RequestBody ServerNode server) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = serverNodeService.testConnection(server);
            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return Result.success(result);
    }
}