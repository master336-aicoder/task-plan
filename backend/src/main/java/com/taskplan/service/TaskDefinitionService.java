package com.taskplan.service;

import com.taskplan.entity.TaskDefinition;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.TaskDefinitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TaskDefinitionService {

    @Autowired
    private TaskDefinitionRepository taskDefinitionRepository;

    @Transactional
    public TaskDefinition createTask(TaskDefinition task) {
        validateTask(task);
        if (taskDefinitionRepository.existsByProjectIdAndName(task.getProjectId(), task.getName())) {
            throw new BusinessException("任务名称已存在");
        }
        return taskDefinitionRepository.save(task);
    }

    @Transactional
    public TaskDefinition updateTask(Long id, TaskDefinition task) {
        TaskDefinition existing = getTask(id);
        
        if (task.getName() != null && !task.getName().equals(existing.getName())) {
            if (taskDefinitionRepository.existsByProjectIdAndName(existing.getProjectId(), task.getName())) {
                throw new BusinessException("任务名称已存在");
            }
            existing.setName(task.getName());
        }
        if (task.getTaskType() != null) existing.setTaskType(task.getTaskType());
        if (task.getParams() != null) existing.setParams(task.getParams());
        if (task.getEnvParams() != null) existing.setEnvParams(task.getEnvParams());
        if (task.getTaskPriority() != null) existing.setTaskPriority(task.getTaskPriority());
        if (task.getWorkerGroup() != null) existing.setWorkerGroup(task.getWorkerGroup());
        if (task.getFailRetryTimes() != null) existing.setFailRetryTimes(task.getFailRetryTimes());
        if (task.getFailRetryInterval() != null) existing.setFailRetryInterval(task.getFailRetryInterval());
        if (task.getTimeoutFlag() != null) existing.setTimeoutFlag(task.getTimeoutFlag());
        if (task.getTimeoutNotifyStrategy() != null) existing.setTimeoutNotifyStrategy(task.getTimeoutNotifyStrategy());
        if (task.getTimeout() != null) existing.setTimeout(task.getTimeout());
        if (task.getOwner() != null) existing.setOwner(task.getOwner());
        
        return taskDefinitionRepository.save(existing);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskDefinitionRepository.existsById(id)) {
            throw new BusinessException("任务不存在");
        }
        taskDefinitionRepository.deleteById(id);
    }

    public TaskDefinition getTask(Long id) {
        return taskDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
    }

    public Page<TaskDefinition> listTasks(Long projectId, String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return taskDefinitionRepository.findByProjectIdAndNameContaining(projectId, name, pageable);
        }
        return taskDefinitionRepository.findByProjectId(projectId, pageable);
    }

    public List<TaskDefinition> listTasksByProject(Long projectId) {
        return taskDefinitionRepository.findByProjectId(projectId);
    }

    private void validateTask(TaskDefinition task) {
        if (task.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            throw new BusinessException("任务名称不能为空");
        }
        if (task.getTaskType() == null || task.getTaskType().trim().isEmpty()) {
            throw new BusinessException("任务类型不能为空");
        }
    }
}
