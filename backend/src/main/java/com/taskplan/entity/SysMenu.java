package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "sys_menu")
public class SysMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100)
    private String menuName;
    
    @Column(length = 50)
    private String menuCode;
    
    @Column(length = 50)
    private String menuType;
    
    @Column(length = 255)
    private String path;
    
    @Column(length = 255)
    private String component;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(length = 100)
    private String icon;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(length = 255)
    private String permission;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Transient
    private List<SysMenu> children;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
