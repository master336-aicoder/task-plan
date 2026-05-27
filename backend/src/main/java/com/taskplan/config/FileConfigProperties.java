package com.taskplan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileConfigProperties {
    
    private String rootpath = "./";
    
    public String getRootpath() {
        if (rootpath == null || rootpath.trim().isEmpty()) {
            return "./";
        }
        String path = rootpath;
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path = path + "/";
        }
        return path.replace("\\", "/");
    }
}