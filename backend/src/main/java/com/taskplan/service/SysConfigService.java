package com.taskplan.service;

import com.taskplan.entity.SysConfig;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.SysConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysConfigService {

    @Autowired
    private SysConfigRepository sysConfigRepository;

    public static final String KEY_UPLOAD_PATH = "upload.path";
    public static final String KEY_SCHEDULE_LOG_PATH = "schedule.log.path";
    public static final String KEY_TASK_LOG_PATH = "task.log.path";
    public static final String KEY_UPLOAD_ALLOWED_TYPES = "upload.allowedTypes";
    public static final String KEY_UPLOAD_MAX_SIZE_MB = "upload.maxSizeMB";
    public static final String KEY_UPLOAD_MAX_CHUNKS = "upload.maxChunks";

    @Transactional
    public SysConfig saveConfig(String configKey, String configValue, String description) {
        SysConfig config = sysConfigRepository.findByConfigKey(configKey)
                .orElse(new SysConfig());
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setDescription(description);
        return sysConfigRepository.save(config);
    }

    public SysConfig getConfig(String configKey) {
        return sysConfigRepository.findByConfigKey(configKey)
                .orElse(null);
    }

    public SysConfig getConfigById(Long id) {
        return sysConfigRepository.findById(id).orElse(null);
    }

    @Transactional
    public SysConfig updateConfig(SysConfig config) {
        return sysConfigRepository.save(config);
    }

    public String getConfigValue(String configKey) {
        return sysConfigRepository.findByConfigKey(configKey)
                .map(SysConfig::getConfigValue)
                .orElse(null);
    }

    public String getConfigValue(String configKey, String defaultValue) {
        return sysConfigRepository.findByConfigKey(configKey)
                .map(SysConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void deleteConfig(Long id) {
        if (!sysConfigRepository.existsById(id)) {
            throw new BusinessException("配置不存在");
        }
        sysConfigRepository.deleteById(id);
    }

    public List<SysConfig> listAll() {
        return sysConfigRepository.findAll();
    }

    public Map<String, String> getAllConfigMap() {
        Map<String, String> configMap = new HashMap<>();
        sysConfigRepository.findAll().forEach(config -> {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        });
        return configMap;
    }

    public String getUploadPath() {
        String path = getConfigValue(KEY_UPLOAD_PATH);
        if (path == null || path.trim().isEmpty()) {
            path = System.getProperty("user.dir") + "/uploads";
        }
        return path;
    }

    public String getScheduleLogPath() {
        String path = getConfigValue(KEY_SCHEDULE_LOG_PATH);
        if (path == null || path.trim().isEmpty()) {
            path = System.getProperty("user.dir") + "/logs/schedule";
        }
        return path;
    }

    public String getTaskLogPath() {
        String path = getConfigValue(KEY_TASK_LOG_PATH);
        if (path == null || path.trim().isEmpty()) {
            path = System.getProperty("user.dir") + "/logs/task";
        }
        return path;
    }

    public String getUploadAllowedTypes() {
        String types = getConfigValue(KEY_UPLOAD_ALLOWED_TYPES);
        if (types == null || types.trim().isEmpty()) {
            // 默认允许常见文件类型
            types = "txt,doc,docx,xls,xlsx,ppt,pptx,pdf,zip,rar,7z,jpg,jpeg,png,gif,mp3,mp4,avi,py,sh,bat,jar,sql,json,xml,csv";
        }
        return types;
    }

    public int getUploadMaxSizeMB() {
        String sizeStr = getConfigValue(KEY_UPLOAD_MAX_SIZE_MB);
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return 100; // 默认100MB
        }
        try {
            return Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid max size config, using default 100MB");
            return 100;
        }
    }

    public int getUploadMaxChunks() {
        String chunksStr = getConfigValue(KEY_UPLOAD_MAX_CHUNKS);
        if (chunksStr == null || chunksStr.trim().isEmpty()) {
            return 100; // 默认100个分片
        }
        try {
            return Integer.parseInt(chunksStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid max chunks config, using default 100");
            return 100;
        }
    }

    public boolean isFileTypeAllowed(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        String allowedTypes = getUploadAllowedTypes().toLowerCase();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            // 没有扩展名，不允许
            return false;
        }
        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        return allowedTypes.contains(extension);
    }

    public boolean isFileSizeAllowed(long sizeBytes) {
        int maxSizeMB = getUploadMaxSizeMB();
        long maxSizeBytes = maxSizeMB * 1024L * 1024L;
        return sizeBytes <= maxSizeBytes;
    }
}
