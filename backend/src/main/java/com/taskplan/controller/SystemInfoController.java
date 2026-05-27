package com.taskplan.controller;

import com.taskplan.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    @GetMapping("/jvm-info")
    public Result<Map<String, Object>> getJvmInfo() {
        Map<String, Object> result = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("javaVendor", System.getProperty("java.vendor"));
        result.put("javaHome", System.getProperty("java.home"));
        result.put("javaVmVersion", System.getProperty("java.vm.version"));
        result.put("javaVmName", System.getProperty("java.vm.name"));
        result.put("javaVmVendor", System.getProperty("java.vm.vendor"));
        result.put("osName", System.getProperty("os.name"));
        result.put("osVersion", System.getProperty("os.version"));
        result.put("osArch", System.getProperty("os.arch"));
        result.put("userDir", System.getProperty("user.dir"));
        result.put("userName", System.getProperty("user.name"));
        result.put("fileEncoding", System.getProperty("file.encoding"));
        result.put("pathSeparator", System.getProperty("path.separator"));
        result.put("lineSeparator", System.getProperty("line.separator"));

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memory = new HashMap<>();
        memory.put("maxMemory", maxMemory);
        memory.put("maxMemoryFormatted", formatMemory(maxMemory));
        memory.put("totalMemory", totalMemory);
        memory.put("totalMemoryFormatted", formatMemory(totalMemory));
        memory.put("freeMemory", freeMemory);
        memory.put("freeMemoryFormatted", formatMemory(freeMemory));
        memory.put("usedMemory", usedMemory);
        memory.put("usedMemoryFormatted", formatMemory(usedMemory));
        memory.put("usedMemoryPercent", String.format("%.2f", (double) usedMemory / maxMemory * 100));
        result.put("memory", memory);

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> os = new HashMap<>();
        os.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        os.put("systemLoadAverage", String.format("%.2f", osBean.getSystemLoadAverage()));
        
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            os.put("processCpuLoad", String.format("%.2f", sunOsBean.getProcessCpuLoad() * 100));
            os.put("systemCpuLoad", String.format("%.2f", sunOsBean.getSystemCpuLoad() * 100));
            os.put("totalPhysicalMemorySize", sunOsBean.getTotalPhysicalMemorySize());
            os.put("totalPhysicalMemoryFormatted", formatMemory(sunOsBean.getTotalPhysicalMemorySize()));
            os.put("freePhysicalMemorySize", sunOsBean.getFreePhysicalMemorySize());
            os.put("freePhysicalMemoryFormatted", formatMemory(sunOsBean.getFreePhysicalMemorySize()));
            os.put("committedVirtualMemorySize", sunOsBean.getCommittedVirtualMemorySize());
            os.put("committedVirtualMemoryFormatted", formatMemory(sunOsBean.getCommittedVirtualMemorySize()));
            os.put("processCpuTime", sunOsBean.getProcessCpuTime());
        }
        result.put("os", os);

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("threadCount", threadBean.getThreadCount());
        threads.put("peakThreadCount", threadBean.getPeakThreadCount());
        threads.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        threads.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
        result.put("threads", threads);

        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        Map<String, Object> classes = new HashMap<>();
        classes.put("loadedClassCount", classBean.getLoadedClassCount());
        classes.put("totalLoadedClassCount", classBean.getTotalLoadedClassCount());
        classes.put("unloadedClassCount", classBean.getUnloadedClassCount());
        result.put("classes", classes);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> heapMemory = new HashMap<>();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        heapMemory.put("init", heapUsage.getInit());
        heapMemory.put("initFormatted", formatMemory(heapUsage.getInit()));
        heapMemory.put("used", heapUsage.getUsed());
        heapMemory.put("usedFormatted", formatMemory(heapUsage.getUsed()));
        heapMemory.put("committed", heapUsage.getCommitted());
        heapMemory.put("committedFormatted", formatMemory(heapUsage.getCommitted()));
        heapMemory.put("max", heapUsage.getMax());
        heapMemory.put("maxFormatted", formatMemory(heapUsage.getMax()));
        heapMemory.put("usedPercent", String.format("%.2f", (double) heapUsage.getUsed() / heapUsage.getMax() * 100));
        result.put("heapMemory", heapMemory);

        Map<String, Object> nonHeapMemory = new HashMap<>();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        nonHeapMemory.put("init", nonHeapUsage.getInit());
        nonHeapMemory.put("initFormatted", formatMemory(nonHeapUsage.getInit()));
        nonHeapMemory.put("used", nonHeapUsage.getUsed());
        nonHeapMemory.put("usedFormatted", formatMemory(nonHeapUsage.getUsed()));
        nonHeapMemory.put("committed", nonHeapUsage.getCommitted());
        nonHeapMemory.put("committedFormatted", formatMemory(nonHeapUsage.getCommitted()));
        nonHeapMemory.put("max", nonHeapUsage.getMax());
        nonHeapMemory.put("maxFormatted", formatMemory(nonHeapUsage.getMax()));
        result.put("nonHeapMemory", nonHeapMemory);

        return Result.success(result);
    }

    @GetMapping("/env-variables")
    public Result<Map<String, String>> getEnvVariables() {
        Map<String, String> env = new LinkedHashMap<>();
        
        Map<String, String> systemEnv = System.getenv();
        systemEnv.forEach((key, value) -> {
            if (key.toLowerCase().contains("java") || 
                key.toLowerCase().contains("path") || 
                key.toLowerCase().contains("home") ||
                key.toLowerCase().contains("classpath") ||
                key.toLowerCase().contains("maven") ||
                key.toLowerCase().contains("gradle") ||
                key.toLowerCase().contains("jdk") ||
                key.toLowerCase().contains("jre") ||
                key.toLowerCase().contains("spring") ||
                key.toLowerCase().contains("server") ||
                key.toLowerCase().contains("port")) {
                env.put(key, value);
            }
        });

        Properties systemProps = System.getProperties();
        systemProps.forEach((key, value) -> {
            String keyStr = String.valueOf(key);
            if (keyStr.toLowerCase().contains("java") || 
                keyStr.toLowerCase().contains("user") ||
                keyStr.toLowerCase().contains("file") ||
                keyStr.toLowerCase().contains("path") ||
                keyStr.toLowerCase().contains("os")) {
                env.put(keyStr, String.valueOf(value));
            }
        });

        return Result.success(env);
    }

    private String formatMemory(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}