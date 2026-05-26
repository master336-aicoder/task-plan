package com.taskplan.service;

import com.taskplan.entity.EnvVariable;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.EnvVariableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvVariableService {

    private final EnvVariableRepository envVariableRepository;
    private final EnvConfigGroupService envConfigGroupService;

    @Transactional
    public EnvVariable createEnvVariable(EnvVariable envVariable) {
        validateEnvVariable(envVariable);
        if (envVariableRepository.existsByNameAndGroupId(envVariable.getName(), envVariable.getGroupId())) {
            throw new BusinessException("同配置组内变量名已存在");
        }
        EnvVariable saved = envVariableRepository.save(envVariable);
        envConfigGroupService.refreshVariableCount(envVariable.getGroupId());
        return saved;
    }

    @Transactional
    public EnvVariable updateEnvVariable(Long id, EnvVariable envVariable) {
        EnvVariable existing = getEnvVariable(id);
        Long oldGroupId = existing.getGroupId();
        
        if (!existing.getName().equals(envVariable.getName()) && 
            envVariableRepository.existsByNameAndGroupId(envVariable.getName(), envVariable.getGroupId())) {
            throw new BusinessException("同配置组内变量名已存在");
        }
        
        existing.setGroupId(envVariable.getGroupId());
        existing.setName(envVariable.getName());
        existing.setValue(envVariable.getValue());
        existing.setDescription(envVariable.getDescription());
        
        EnvVariable saved = envVariableRepository.save(existing);
        
        envConfigGroupService.refreshVariableCount(oldGroupId);
        if (!oldGroupId.equals(envVariable.getGroupId())) {
            envConfigGroupService.refreshVariableCount(envVariable.getGroupId());
        }
        return saved;
    }

    @Transactional
    public void deleteEnvVariable(Long id) {
        EnvVariable existing = getEnvVariable(id);
        Long groupId = existing.getGroupId();
        envVariableRepository.deleteById(id);
        envConfigGroupService.refreshVariableCount(groupId);
    }

    public EnvVariable getEnvVariable(Long id) {
        return envVariableRepository.findById(id)
                .orElseThrow(() -> new BusinessException("环境变量不存在"));
    }

    public Page<EnvVariable> listEnvVariables(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return envVariableRepository.findByNameContaining(name, pageable);
        }
        return envVariableRepository.findAll(pageable);
    }

    public List<EnvVariable> listAll() {
        return envVariableRepository.findAll();
    }

    public List<EnvVariable> listByGroupId(Long groupId) {
        return envVariableRepository.findByGroupId(groupId);
    }

    public List<EnvVariable> listByGroupIds(List<Long> groupIds) {
        return envVariableRepository.findByGroupIdIn(groupIds);
    }

    @Transactional
    public List<EnvVariable> createBatch(List<EnvVariable> variables) {
        for (EnvVariable variable : variables) {
            validateEnvVariable(variable);
        }
        return envVariableRepository.saveAll(variables);
    }

    @Transactional
    public void deleteByGroupId(Long groupId) {
        envVariableRepository.deleteByGroupId(groupId);
    }

    private void validateEnvVariable(EnvVariable envVariable) {
        if (envVariable.getName() == null || envVariable.getName().trim().isEmpty()) {
            throw new BusinessException("环境变量名称不能为空");
        }
    }
}