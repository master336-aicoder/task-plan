package com.taskplan.service;

import com.taskplan.entity.SysMenu;
import com.taskplan.entity.SysRole;
import com.taskplan.entity.SysRoleMenu;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.SysMenuRepository;
import com.taskplan.repository.SysRoleMenuRepository;
import com.taskplan.repository.SysRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysRoleService {

    @Autowired
    private SysRoleRepository sysRoleRepository;
    
    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;
    
    @Autowired
    private SysMenuRepository sysMenuRepository;

    public SysRole getRoleById(Long id) {
        return sysRoleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
    }

    public List<SysRole> listRoles() {
        return sysRoleRepository.findAll();
    }

    public List<SysRole> listActiveRoles() {
        return sysRoleRepository.findByStatus(1);
    }

    @Transactional
    public SysRole createRole(SysRole role) {
        if (sysRoleRepository.findByRoleCode(role.getRoleCode()).isPresent()) {
            throw new BusinessException("角色代码已存在");
        }
        role.setStatus(1);
        return sysRoleRepository.save(role);
    }

    @Transactional
    public SysRole updateRole(Long id, SysRole role) {
        SysRole existing = getRoleById(id);
        
        if (role.getRoleName() != null) existing.setRoleName(role.getRoleName());
        if (role.getDescription() != null) existing.setDescription(role.getDescription());
        if (role.getStatus() != null) existing.setStatus(role.getStatus());
        
        return sysRoleRepository.save(existing);
    }

    @Transactional
    public void deleteRole(Long id) {
        if (!sysRoleRepository.existsById(id)) {
            throw new BusinessException("角色不存在");
        }
        sysRoleMenuRepository.deleteByRoleId(id);
        sysRoleRepository.deleteById(id);
    }
    
    public List<SysMenu> getRoleMenus(Long roleId) {
        List<SysRoleMenu> roleMenus = sysRoleMenuRepository.findByRoleId(roleId);
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return sysMenuRepository.findAllById(menuIds);
    }
    
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        sysRoleMenuRepository.deleteByRoleId(roleId);
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            sysRoleMenuRepository.save(roleMenu);
        }
    }
}
