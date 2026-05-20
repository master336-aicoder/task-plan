package com.taskplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskplan.entity.ProcessDefinition;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ProcessDefinitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProcessDefinitionService {

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ProcessDefinition createProcess(ProcessDefinition process) {
        validateProcess(process);
        if (processDefinitionRepository.existsByProjectIdAndName(process.getProjectId(), process.getName())) {
            throw new BusinessException("工作流名称已存在");
        }
        return processDefinitionRepository.save(process);
    }

    @Transactional
    public ProcessDefinition createProcessWithNodes(Long projectId, String name, String releaseState, 
            List<Map<String, Object>> taskNodes, List<Map<String, Object>> connections) {
        return saveProcessWithNodes(null, projectId, name, releaseState, taskNodes, connections);
    }

    @Transactional
    public ProcessDefinition saveProcessWithNodes(Long id, Long projectId, String name, String releaseState, 
            List<Map<String, Object>> taskNodes, List<Map<String, Object>> connections) {
        validateProcessInput(projectId, name);
        
        ProcessDefinition process;
        
        if (id != null) {
            process = getProcess(id);
            process.setName(name);
            process.setProjectId(projectId);
        } else {
            process = processDefinitionRepository.findByProjectIdAndName(projectId, name);
            if (process == null) {
                process = new ProcessDefinition();
                process.setProjectId(projectId);
                process.setName(name);
            }
        }
        
        process.setReleaseState(releaseState);
        
        try {
            Map<String, Object> definition = new HashMap<>();
            definition.put("nodes", taskNodes);
            definition.put("connections", connections);
            process.setDefinitionJson(objectMapper.writeValueAsString(definition));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize process definition", e);
            throw new BusinessException("保存失败");
        }
        
        return processDefinitionRepository.save(process);
    }

    private void validateProcessInput(Long projectId, String name) {
        if (projectId == null) {
            throw new BusinessException("项目ID不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("工作流名称不能为空");
        }
    }

    @Transactional
    public ProcessDefinition updateProcess(Long id, ProcessDefinition process) {
        ProcessDefinition existing = getProcess(id);
        
        if (process.getName() != null && !process.getName().equals(existing.getName())) {
            if (processDefinitionRepository.existsByProjectIdAndName(existing.getProjectId(), process.getName())) {
                throw new BusinessException("工作流名称已存在");
            }
            existing.setName(process.getName());
        }
        if (process.getDescription() != null) existing.setDescription(process.getDescription());
        if (process.getDefinitionJson() != null) existing.setDefinitionJson(process.getDefinitionJson());
        if (process.getProcessType() != null) existing.setProcessType(process.getProcessType());
        if (process.getReleaseState() != null) existing.setReleaseState(process.getReleaseState());
        if (process.getOwner() != null) existing.setOwner(process.getOwner());
        
        return processDefinitionRepository.save(existing);
    }

    @Transactional
    public ProcessDefinition releaseProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        process.setReleaseState("ONLINE");
        return processDefinitionRepository.save(process);
    }

    @Transactional
    public ProcessDefinition offlineProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        process.setReleaseState("OFFLINE");
        return processDefinitionRepository.save(process);
    }

    @Transactional
    public void deleteProcess(Long id) {
        if (!processDefinitionRepository.existsById(id)) {
            throw new BusinessException("工作流不存在");
        }
        processDefinitionRepository.deleteById(id);
    }

    @Transactional
    public void executeProcess(Long id) {
        ProcessDefinition process = getProcess(id);
        if (!"ONLINE".equals(process.getReleaseState())) {
            throw new BusinessException("工作流未发布，无法执行");
        }
        processInstanceService.startProcess(process);
    }

    public ProcessDefinition getProcess(Long id) {
        return processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工作流不存在"));
    }

    public Page<ProcessDefinition> listProcesses(Long projectId, String name, Pageable pageable) {
        if (projectId != null) {
            if (name != null && !name.isEmpty()) {
                return processDefinitionRepository.findByProjectIdAndNameContaining(projectId, name, pageable);
            }
            return processDefinitionRepository.findByProjectId(projectId, pageable);
        } else {
            if (name != null && !name.isEmpty()) {
                return processDefinitionRepository.findByNameContaining(name, pageable);
            }
            return processDefinitionRepository.findAll(pageable);
        }
    }

    public List<ProcessDefinition> listProcessesByProject(Long projectId) {
        return processDefinitionRepository.findByProjectId(projectId);
    }

    public List<ProcessDefinition> listOnlineProcesses(Long projectId) {
        return processDefinitionRepository.findByProjectIdAndReleaseState(projectId, "ONLINE");
    }

    private void validateProcess(ProcessDefinition process) {
        if (process.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        if (process.getName() == null || process.getName().trim().isEmpty()) {
            throw new BusinessException("工作流名称不能为空");
        }
    }
}
