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
        paths.put("uploadPath", sysConfigService.getConfigValue(SysConfigService.KEY_UPLOAD_PATH, "未配置"));
        paths.put("scheduleLogPath", sysConfigService.getConfigValue(SysConfigService.KEY_SCHEDULE_LOG_PATH, "未配置"));
        paths.put("taskLogPath", sysConfigService.getConfigValue(SysConfigService.KEY_TASK_LOG_PATH, "未配置"));
        paths.put("taskInstancePath", sysConfigService.getConfigValue(SysConfigService.KEY_TASK_INSTANCE_PATH, "./instances"));
        paths.put("uploadAllowedTypes", sysConfigService.getConfigValue(SysConfigService.KEY_UPLOAD_ALLOWED_TYPES, "未配置"));
        paths.put("uploadMaxSizeMB", sysConfigService.getConfigValue(SysConfigService.KEY_UPLOAD_MAX_SIZE_MB, "100"));
        paths.put("uploadMaxChunks", sysConfigService.getConfigValue(SysConfigService.KEY_UPLOAD_MAX_CHUNKS, "100"));
        return Result.success(paths);
    }

    @GetMapping("/mail")
    public Result<Map<String, Object>> getMailConfig() {
        Map<String, Object> mailConfig = new HashMap<>();
        mailConfig.put("host", sysConfigService.getConfigValue("mail.host", ""));
        mailConfig.put("port", sysConfigService.getConfigValue("mail.port", "587"));
        mailConfig.put("username", sysConfigService.getConfigValue("mail.username", ""));
        mailConfig.put("password", sysConfigService.getConfigValue("mail.password", ""));
        mailConfig.put("from", sysConfigService.getConfigValue("mail.from", ""));
        mailConfig.put("ssl", sysConfigService.getConfigValue("mail.ssl", "true"));
        return Result.success(mailConfig);
    }

    @PutMapping("/mail")
    public Result<Void> updateMailConfig(@RequestBody Map<String, String> request) {
        sysConfigService.saveConfig("mail.host", request.get("host"), "邮件服务器地址");
        sysConfigService.saveConfig("mail.port", request.get("port"), "邮件服务器端口");
        sysConfigService.saveConfig("mail.username", request.get("username"), "邮件服务器用户名");
        sysConfigService.saveConfig("mail.password", request.get("password"), "邮件服务器密码");
        sysConfigService.saveConfig("mail.from", request.get("from"), "发件人邮箱地址");
        sysConfigService.saveConfig("mail.ssl", request.get("ssl"), "是否启用SSL");
        return Result.success();
    }
}
