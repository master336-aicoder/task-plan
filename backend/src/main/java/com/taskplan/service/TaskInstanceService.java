package com.taskplan.service;

import com.taskplan.entity.TaskDefinition;
import com.taskplan.entity.TaskInstance;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.TaskDefinitionRepository;
import com.taskplan.repository.TaskInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TaskInstanceService {

    @Autowired
    private TaskInstanceRepository taskInstanceRepository;

    @Autowired
    private TaskDefinitionRepository taskDefinitionRepository;

    @Transactional
    public TaskInstance createTaskInstance(Long processInstanceId, Long taskDefinitionId) {
        TaskDefinition taskDefinition = taskDefinitionRepository.findById(taskDefinitionId)
                .orElseThrow(() -> new BusinessException("任务定义不存在"));

        TaskInstance instance = new TaskInstance();
        instance.setProjectId(taskDefinition.getProjectId());
        instance.setProcessInstanceId(processInstanceId);
        instance.setTaskDefinitionId(taskDefinitionId);
        instance.setTaskName(taskDefinition.getName());
        instance.setTaskType(taskDefinition.getTaskType());
        instance.setState("SUBMITTED_SUCCESS");
        instance.setOwner(taskDefinition.getOwner());

        return taskInstanceRepository.save(instance);
    }

    @Transactional
    public TaskInstance startTask(Long id) {
        TaskInstance instance = getInstance(id);
        instance.setState("RUNNING_EXECUTION");
        instance.setStartTime(LocalDateTime.now());
        return taskInstanceRepository.save(instance);
    }

    @Transactional
    public TaskInstance completeTask(Long id, String state, String errorMsg) {
        TaskInstance instance = getInstance(id);
        instance.setState(state);
        instance.setEndTime(LocalDateTime.now());
        
        if (instance.getStartTime() != null) {
            instance.setDuration(Duration.between(instance.getStartTime(), instance.getEndTime()).getSeconds());
        }
        
        if (errorMsg != null) {
            instance.setErrorMsg(errorMsg);
        }
        
        return taskInstanceRepository.save(instance);
    }

    @Transactional
    public TaskInstance retryTask(Long id) {
        TaskInstance instance = getInstance(id);
        instance.setState("SUBMITTED_SUCCESS");
        instance.setRetryTimes(instance.getRetryTimes() + 1);
        instance.setStartTime(null);
        instance.setEndTime(null);
        instance.setDuration(null);
        instance.setErrorMsg(null);
        return taskInstanceRepository.save(instance);
    }

    public TaskInstance getInstance(Long id) {
        return taskInstanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务实例不存在"));
    }

    public Page<TaskInstance> listTaskInstances(Long projectId, Long processInstanceId, Pageable pageable) {
        if (processInstanceId != null) {
            return taskInstanceRepository.findByProcessInstanceId(processInstanceId, pageable);
        }
        return taskInstanceRepository.findByProjectId(projectId, pageable);
    }

    public List<TaskInstance> listByProcessInstance(Long processInstanceId) {
        return taskInstanceRepository.findByProcessInstanceId(processInstanceId);
    }
}
