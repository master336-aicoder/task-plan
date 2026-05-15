package com.taskplan.config;

import com.taskplan.entity.ClusterConfig;
import com.taskplan.entity.TaskTypeConfig;
import com.taskplan.repository.ClusterConfigRepository;
import com.taskplan.repository.TaskTypeConfigRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final TaskTypeConfigRepository taskTypeConfigRepository;
    private final ClusterConfigRepository clusterConfigRepository;

    public DataInitializer(TaskTypeConfigRepository taskTypeConfigRepository, 
                          ClusterConfigRepository clusterConfigRepository) {
        this.taskTypeConfigRepository = taskTypeConfigRepository;
        this.clusterConfigRepository = clusterConfigRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initTaskTypeConfigs();
        initClusterConfigs();
    }

    private void initTaskTypeConfigs() {
        List<TaskTypeConfig> configs = Arrays.asList(
            createTaskTypeConfig("Shell默认", "SHELL", "/bin/bash", "默认Shell脚本执行器"),
            createTaskTypeConfig("Python3", "PYTHON", "/usr/bin/python3", "Python3脚本执行器"),
            createTaskTypeConfig("Python2", "PYTHON", "/usr/bin/python2", "Python2脚本执行器"),
            createTaskTypeConfig("Perl", "PERL", "/usr/bin/perl", "Perl脚本执行器"),
            createTaskTypeConfig("Java8", "JAVA", "/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "Java8运行环境"),
            createTaskTypeConfig("Java11", "JAVA", "/usr/lib/jvm/java-11-openjdk-amd64/bin/java", "Java11运行环境"),
            createTaskTypeConfig("Java17", "JAVA", "/usr/lib/jvm/java-17-openjdk-amd64/bin/java", "Java17运行环境"),
            createTaskTypeConfig("Python3.9", "PYTHON", "/usr/bin/python3.9", "Python3.9脚本执行器")
        );

        for (TaskTypeConfig config : configs) {
            if (!taskTypeConfigRepository.existsByName(config.getName())) {
                taskTypeConfigRepository.save(config);
            }
        }
    }

    private void initClusterConfigs() {
        List<ClusterConfig> configs = Arrays.asList(
            createClusterConfig("Local YARN", "YARN", "本地YARN集群"),
            createClusterConfig("Local Flink", "FLINK", "本地Flink集群"),
            createClusterConfig("Local Spark", "SPARK", "本地Spark配置")
        );

        for (ClusterConfig config : configs) {
            if (!clusterConfigRepository.existsByName(config.getName())) {
                clusterConfigRepository.save(config);
            }
        }
    }

    private TaskTypeConfig createTaskTypeConfig(String name, String taskType, String programPath, String description) {
        TaskTypeConfig config = new TaskTypeConfig();
        config.setName(name);
        config.setTaskType(taskType);
        config.setProgramPath(programPath);
        config.setDescription(description);
        return config;
    }

    private ClusterConfig createClusterConfig(String name, String type, String description) {
        ClusterConfig config = new ClusterConfig();
        config.setName(name);
        config.setType(type);
        config.setDescription(description);
        return config;
    }
}
