package com.taskplan.service;

import com.taskplan.entity.TaskTypeConfig;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.TaskTypeConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TaskTypeConfigService {

    @Autowired
    private TaskTypeConfigRepository taskTypeConfigRepository;

    @Transactional
    public TaskTypeConfig createConfig(TaskTypeConfig config) {
        validateConfig(config);
        if (taskTypeConfigRepository.existsByName(config.getName())) {
            throw new BusinessException("任务类型配置名称已存在");
        }
        return taskTypeConfigRepository.save(config);
    }

    @Transactional
    public TaskTypeConfig updateConfig(Long id, TaskTypeConfig config) {
        TaskTypeConfig existing = getConfig(id);
        
        if (!existing.getName().equals(config.getName()) && 
            taskTypeConfigRepository.existsByName(config.getName())) {
            throw new BusinessException("任务类型配置名称已存在");
        }
        
        existing.setName(config.getName());
        existing.setTaskType(config.getTaskType());
        existing.setProgramPath(config.getProgramPath());
        existing.setDefaultParams(config.getDefaultParams());
        existing.setEnvParams(config.getEnvParams());
        existing.setDescription(config.getDescription());
        
        return taskTypeConfigRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(Long id) {
        if (!taskTypeConfigRepository.existsById(id)) {
            throw new BusinessException("任务类型配置不存在");
        }
        taskTypeConfigRepository.deleteById(id);
    }

    public TaskTypeConfig getConfig(Long id) {
        return taskTypeConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务类型配置不存在"));
    }

    public Page<TaskTypeConfig> listConfigs(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return taskTypeConfigRepository.findByNameContaining(name, pageable);
        }
        return taskTypeConfigRepository.findAll(pageable);
    }

    public List<TaskTypeConfig> listByTaskType(String taskType) {
        return taskTypeConfigRepository.findByTaskType(taskType);
    }

    public List<TaskTypeConfig> listAll() {
        return taskTypeConfigRepository.findAll();
    }

    private void validateConfig(TaskTypeConfig config) {
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            throw new BusinessException("配置名称不能为空");
        }
        if (config.getTaskType() == null || config.getTaskType().trim().isEmpty()) {
            throw new BusinessException("任务类型不能为空");
        }
    }
}