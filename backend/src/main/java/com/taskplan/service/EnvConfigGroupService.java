package com.taskplan.service;

import com.taskplan.entity.EnvConfigGroup;
import com.taskplan.repository.EnvConfigGroupRepository;
import com.taskplan.repository.EnvVariableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvConfigGroupService {

    private final EnvConfigGroupRepository envConfigGroupRepository;
    private final EnvVariableRepository envVariableRepository;

    public List<EnvConfigGroup> listAll() {
        List<EnvConfigGroup> groups = envConfigGroupRepository.findAll();
        refreshVariableCount(groups);
        return groups;
    }

    @Transactional
    public void refreshVariableCount(List<EnvConfigGroup> groups) {
        for (EnvConfigGroup group : groups) {
            long count = envVariableRepository.countByGroupId(group.getId());
            if (count != (group.getVariableCount() != null ? group.getVariableCount() : 0)) {
                group.setVariableCount((int) count);
                envConfigGroupRepository.save(group);
            }
        }
    }

    @Transactional
    public void refreshVariableCount(Long groupId) {
        envConfigGroupRepository.findById(groupId).ifPresent(group -> {
            long count = envVariableRepository.countByGroupId(groupId);
            group.setVariableCount((int) count);
            envConfigGroupRepository.save(group);
        });
    }

    public Optional<EnvConfigGroup> findById(Long id) {
        return envConfigGroupRepository.findById(id);
    }

    public Optional<EnvConfigGroup> findByName(String name) {
        return envConfigGroupRepository.findByName(name);
    }

    @Transactional
    public EnvConfigGroup create(EnvConfigGroup group) {
        if (envConfigGroupRepository.existsByName(group.getName())) {
            throw new IllegalArgumentException("配置组名称已存在");
        }
        return envConfigGroupRepository.save(group);
    }

    @Transactional
    public EnvConfigGroup update(Long id, EnvConfigGroup group) {
        EnvConfigGroup existing = envConfigGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("配置组不存在"));
        
        if (!existing.getName().equals(group.getName()) && 
            envConfigGroupRepository.existsByNameAndIdNot(group.getName(), id)) {
            throw new IllegalArgumentException("配置组名称已存在");
        }
        
        existing.setName(group.getName());
        existing.setDescription(group.getDescription());
        existing.setOwner(group.getOwner());
        
        return envConfigGroupRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!envConfigGroupRepository.existsById(id)) {
            throw new IllegalArgumentException("配置组不存在");
        }
        envVariableRepository.deleteByGroupId(id);
        envConfigGroupRepository.deleteById(id);
    }
}