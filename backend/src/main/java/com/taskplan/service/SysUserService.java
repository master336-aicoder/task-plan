package com.taskplan.service;

import com.taskplan.entity.SysUser;
import com.taskplan.entity.SysRole;
import com.taskplan.entity.SysUserRole;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.SysUserRepository;
import com.taskplan.repository.SysUserRoleRepository;
import com.taskplan.repository.SysRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysUserService {

    @Autowired
    private SysUserRepository sysUserRepository;
    
    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;
    
    @Autowired
    private SysRoleRepository sysRoleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public SysUser getUserByUsername(String username) {
        return sysUserRepository.findActiveUserByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    public SysUser getUserById(Long id) {
        return sysUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public List<SysUser> listUsers() {
        return sysUserRepository.findByIsDeleted(0);
    }

    @Transactional
    public SysUser createUser(SysUser user) {
        if (sysUserRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsDeleted(0);
        return sysUserRepository.save(user);
    }

    @Transactional
    public SysUser updateUser(Long id, SysUser user) {
        SysUser existing = getUserById(id);
        
        if (user.getRealName() != null) existing.setRealName(user.getRealName());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getStatus() != null) existing.setStatus(user.getStatus());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return sysUserRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        SysUser user = getUserById(id);
        user.setIsDeleted(1);
        sysUserRepository.save(user);
    }
    
    public List<SysRole> getUserRoles(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(userId);
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return roleIds.stream()
                .map(id -> sysRoleRepository.findById(id).orElse(null))
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        sysUserRoleRepository.deleteByUserId(userId);
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleRepository.save(userRole);
        }
    }
}
