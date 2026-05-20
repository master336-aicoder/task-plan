package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "sys_role_menu")
@IdClass(SysRoleMenuId.class)
public class SysRoleMenu {
    @Id
    @Column(name = "role_id")
    private Long roleId;
    
    @Id
    @Column(name = "menu_id")
    private Long menuId;
}

class SysRoleMenuId implements Serializable {
    private Long roleId;
    private Long menuId;
}
