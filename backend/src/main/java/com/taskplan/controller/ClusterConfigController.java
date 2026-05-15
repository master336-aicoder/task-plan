package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ClusterConfig;
import com.taskplan.service.ClusterConfigService;
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
@RequestMapping("/api/cluster-configs")
public class ClusterConfigController {

    @Autowired
    private ClusterConfigService clusterConfigService;

    @GetMapping
    public Result<Map<String, Object>> listConfigs(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ClusterConfig> configPage = clusterConfigService.listConfigs(name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", configPage.getContent());
        result.put("total", configPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<ClusterConfig>> listAll() {
        return Result.success(clusterConfigService.listAll());
    }

    @GetMapping("/type/{type}")
    public Result<List<ClusterConfig>> listByType(@PathVariable String type) {
        return Result.success(clusterConfigService.listByType(type));
    }

    @GetMapping("/{id}")
    public Result<ClusterConfig> getConfig(@PathVariable Long id) {
        return Result.success(clusterConfigService.getConfig(id));
    }

    @PostMapping
    public Result<ClusterConfig> createConfig(@RequestBody ClusterConfig config) {
        return Result.success(clusterConfigService.createConfig(config));
    }

    @PutMapping("/{id}")
    public Result<ClusterConfig> updateConfig(@PathVariable Long id, @RequestBody ClusterConfig config) {
        return Result.success(clusterConfigService.updateConfig(id, config));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        clusterConfigService.deleteConfig(id);
        return Result.success();
    }
}