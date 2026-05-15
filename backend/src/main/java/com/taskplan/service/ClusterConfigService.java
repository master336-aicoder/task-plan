package com.taskplan.service;

import com.taskplan.entity.ClusterConfig;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ClusterConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ClusterConfigService {

    @Autowired
    private ClusterConfigRepository clusterConfigRepository;

    @Transactional
    public ClusterConfig createConfig(ClusterConfig config) {
        validateConfig(config);
        if (clusterConfigRepository.existsByName(config.getName())) {
            throw new BusinessException("配置名称已存在");
        }
        return clusterConfigRepository.save(config);
    }

    @Transactional
    public ClusterConfig updateConfig(Long id, ClusterConfig config) {
        ClusterConfig existing = getConfig(id);
        
        if (!existing.getName().equals(config.getName()) && 
            clusterConfigRepository.existsByName(config.getName())) {
            throw new BusinessException("配置名称已存在");
        }
        
        existing.setName(config.getName());
        existing.setType(config.getType());
        existing.setConfigJson(config.getConfigJson());
        existing.setDescription(config.getDescription());
        
        return clusterConfigRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(Long id) {
        if (!clusterConfigRepository.existsById(id)) {
            throw new BusinessException("配置不存在");
        }
        clusterConfigRepository.deleteById(id);
    }

    public ClusterConfig getConfig(Long id) {
        return clusterConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配置不存在"));
    }

    public Page<ClusterConfig> listConfigs(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return clusterConfigRepository.findByNameContaining(name, pageable);
        }
        return clusterConfigRepository.findAll(pageable);
    }

    public List<ClusterConfig> listByType(String type) {
        return clusterConfigRepository.findByType(type);
    }

    public List<ClusterConfig> listAll() {
        return clusterConfigRepository.findAll();
    }

    private void validateConfig(ClusterConfig config) {
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            throw new BusinessException("配置名称不能为空");
        }
        if (config.getType() == null || config.getType().trim().isEmpty()) {
            throw new BusinessException("配置类型不能为空");
        }
    }
}