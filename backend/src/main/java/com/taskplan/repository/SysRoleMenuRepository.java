package com.taskplan.repository;

import com.taskplan.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {
    List<SysRoleMenu> findByRoleId(Long roleId);
    
    void deleteByRoleId(Long roleId);
    
    @Query("SELECT rm.menuId FROM SysRoleMenu rm WHERE rm.roleId IN :roleIds")
    List<Long> findMenuIdsByRoleIds(List<Long> roleIds);
}
