package com.taskplan.service;

import com.taskplan.entity.ProcessDefinition;
import com.taskplan.entity.ProcessInstance;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ProcessDefinitionRepository;
import com.taskplan.repository.ProcessInstanceRepository;
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
public class ProcessInstanceService {

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Transactional
    public ProcessInstance startProcess(Long processDefinitionId, String commandType) {
        ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        return startProcess(processDefinition, commandType);
    }

    @Transactional
    public ProcessInstance startProcess(ProcessDefinition processDefinition) {
        return startProcess(processDefinition, "MANUAL");
    }

    @Transactional
    public ProcessInstance startProcess(ProcessDefinition processDefinition, String commandType) {
        if (!"ONLINE".equals(processDefinition.getReleaseState())) {
            throw new BusinessException("工作流未上线，无法启动");
        }

        ProcessInstance instance = new ProcessInstance();
        instance.setProjectId(processDefinition.getProjectId());
        instance.setProcessDefinitionId(processDefinition.getId());
        instance.setProcessName(processDefinition.getName());
        instance.setState("RUNNING_EXECUTION");
        instance.setCommandType(commandType);
        instance.setOwner(processDefinition.getOwner());
        
        return processInstanceRepository.save(instance);
    }

    @Transactional
    public ProcessInstance endProcess(Long id, String state) {
        ProcessInstance instance = getInstance(id);
        instance.setState(state);
        instance.setEndTime(LocalDateTime.now());
        
        if (instance.getStartTime() != null) {
            instance.setDuration(Duration.between(instance.getStartTime(), instance.getEndTime()).getSeconds());
        }
        
        return processInstanceRepository.save(instance);
    }

    public ProcessInstance getInstance(Long id) {
        return processInstanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程实例不存在"));
    }

    public Page<ProcessInstance> listInstances(Long projectId, String state, Long processDefinitionId, Pageable pageable) {
        if (processDefinitionId != null) {
            return processInstanceRepository.findByProcessDefinitionId(processDefinitionId, pageable);
        }
        if (state != null && !state.isEmpty()) {
            return processInstanceRepository.findByProjectIdAndState(projectId, state, pageable);
        }
        return processInstanceRepository.findByProjectId(projectId, pageable);
    }

    public List<ProcessInstance> listRunningInstances() {
        return processInstanceRepository.findByState("RUNNING_EXECUTION");
    }
}
