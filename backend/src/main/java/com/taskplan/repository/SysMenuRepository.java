package com.taskplan.repository;

import com.taskplan.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {
    List<SysMenu> findByStatus(Integer status);
    
    List<SysMenu> findByParentId(Long parentId);
    
    List<SysMenu> findByParentIdOrderBySortOrder(Long parentId);
    
    @Query("SELECT m FROM SysMenu m WHERE m.status = 1 ORDER BY m.sortOrder")
    List<SysMenu> findAllActiveMenus();
    
    @Query("SELECT m FROM SysMenu m WHERE m.status = 1 AND m.parentId = :parentId ORDER BY m.sortOrder")
    List<SysMenu> findActiveMenusByParentId(Long parentId);
}
