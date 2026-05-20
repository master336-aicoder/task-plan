package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.SysConfig;
import com.taskplan.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sys-config")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping
    public Result<Map<String, String>> getAllConfigs() {
        return Result.success(sysConfigService.getAllConfigMap());
    }

    @GetMapping("/list")
    public Result<List<SysConfig>> listAll() {
        return Result.success(sysConfigService.listAll());
    }

    @GetMapping("/{configKey}")
    public Result<SysConfig> getConfig(@PathVariable String configKey) {
        return Result.success(sysConfigService.getConfig(configKey));
    }

    @PostMapping
    public Result<SysConfig> saveConfig(@RequestBody Map<String, String> request) {
        String configKey = request.get("configKey");
        String configValue = request.get("configValue");
        String description = request.get("description");
        return Result.success(sysConfigService.saveConfig(configKey, configValue, description));
    }

    @PutMapping("/{id}")
    public Result<SysConfig> updateConfig(@PathVariable Long id, @RequestBody Map<String, String> request) {
        SysConfig config = sysConfigService.getConfigById(id);
        if (config != null) {
            config.setConfigValue(request.get("configValue"));
            config.setDescription(request.get("description"));
            return Result.success(sysConfigService.updateConfig(config));
        }
        return Result.error("配置不存在");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return Result.success();
    }

    @GetMapping("/paths")
    public Result<Map<String, Object>> getPaths() {
        Map<String, Object> paths = new HashMap<>();
        paths.put("uploadPath", sysConfigService.getUploadPath());
        paths.put("scheduleLogPath", sysConfigService.getScheduleLogPath());
        paths.put("taskLogPath", sysConfigService.getTaskLogPath());
        paths.put("uploadAllowedTypes", sysConfigService.getUploadAllowedTypes());
        paths.put("uploadMaxSizeMB", sysConfigService.getUploadMaxSizeMB());
        paths.put("uploadMaxChunks", sysConfigService.getUploadMaxChunks());
        return Result.success(paths);
    }
}
