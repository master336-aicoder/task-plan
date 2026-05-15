package com.taskplan.service;

import com.taskplan.entity.EnvVariable;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.EnvVariableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class EnvVariableService {

    @Autowired
    private EnvVariableRepository envVariableRepository;

    @Transactional
    public EnvVariable createEnvVariable(EnvVariable envVariable) {
        validateEnvVariable(envVariable);
        if (envVariableRepository.existsByName(envVariable.getName())) {
            throw new BusinessException("环境变量名称已存在");
        }
        return envVariableRepository.save(envVariable);
    }

    @Transactional
    public EnvVariable updateEnvVariable(Long id, EnvVariable envVariable) {
        EnvVariable existing = getEnvVariable(id);
        
        if (!existing.getName().equals(envVariable.getName()) && 
            envVariableRepository.existsByName(envVariable.getName())) {
            throw new BusinessException("环境变量名称已存在");
        }
        
        existing.setName(envVariable.getName());
        existing.setValue(envVariable.getValue());
        existing.setDescription(envVariable.getDescription());
        existing.setCategory(envVariable.getCategory());
        
        return envVariableRepository.save(existing);
    }

    @Transactional
    public void deleteEnvVariable(Long id) {
        if (!envVariableRepository.existsById(id)) {
            throw new BusinessException("环境变量不存在");
        }
        envVariableRepository.deleteById(id);
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

    public List<EnvVariable> listByCategory(String category) {
        return envVariableRepository.findByCategory(category);
    }

    public List<EnvVariable> listAll() {
        return envVariableRepository.findAll();
    }

    private void validateEnvVariable(EnvVariable envVariable) {
        if (envVariable.getName() == null || envVariable.getName().trim().isEmpty()) {
            throw new BusinessException("环境变量名称不能为空");
        }
    }
}
