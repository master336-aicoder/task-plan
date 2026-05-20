package com.taskplan.service;

import com.taskplan.entity.SysMenu;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.SysMenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysMenuService {

    @Autowired
    private SysMenuRepository sysMenuRepository;

    public SysMenu getMenuById(Long id) {
        return sysMenuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
    }

    public List<SysMenu> listMenus() {
        return sysMenuRepository.findAll();
    }

    public List<SysMenu> listActiveMenus() {
        return sysMenuRepository.findAllActiveMenus();
    }

    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = sysMenuRepository.findAllActiveMenus();
        return buildMenuTree(allMenus, null);
    }
    
    public List<SysMenu> getUserMenus(List<Long> menuIds) {
        List<SysMenu> allMenus = sysMenuRepository.findAllById(menuIds);
        return buildMenuTree(allMenus, null);
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> allMenus, Long parentId) {
        return allMenus.stream()
                .filter(menu -> (parentId == null && menu.getParentId() == null) || 
                               (parentId != null && menu.getParentId() != null && menu.getParentId().equals(parentId)))
                .sorted((m1, m2) -> {
                    if (m1.getSortOrder() == null) return 1;
                    if (m2.getSortOrder() == null) return -1;
                    return m1.getSortOrder().compareTo(m2.getSortOrder());
                })
                .peek(menu -> {
                    List<SysMenu> children = buildMenuTree(allMenus, menu.getId());
                    if (!children.isEmpty()) {
                        menu.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public SysMenu createMenu(SysMenu menu) {
        menu.setStatus(1);
        return sysMenuRepository.save(menu);
    }

    @Transactional
    public SysMenu updateMenu(Long id, SysMenu menu) {
        SysMenu existing = getMenuById(id);
        
        if (menu.getMenuName() != null) existing.setMenuName(menu.getMenuName());
        if (menu.getMenuCode() != null) existing.setMenuCode(menu.getMenuCode());
        if (menu.getMenuType() != null) existing.setMenuType(menu.getMenuType());
        if (menu.getPath() != null) existing.setPath(menu.getPath());
        if (menu.getComponent() != null) existing.setComponent(menu.getComponent());
        if (menu.getParentId() != null) existing.setParentId(menu.getParentId());
        if (menu.getIcon() != null) existing.setIcon(menu.getIcon());
        if (menu.getSortOrder() != null) existing.setSortOrder(menu.getSortOrder());
        if (menu.getStatus() != null) existing.setStatus(menu.getStatus());
        if (menu.getPermission() != null) existing.setPermission(menu.getPermission());
        
        return sysMenuRepository.save(existing);
    }

    @Transactional
    public void deleteMenu(Long id) {
        List<SysMenu> children = sysMenuRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("请先删除子菜单");
        }
        sysMenuRepository.deleteById(id);
    }
}
