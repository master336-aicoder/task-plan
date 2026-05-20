package com.taskplan.service;

import com.taskplan.entity.SysUser;
import com.taskplan.entity.SysUserRole;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.SysRoleRepository;
import com.taskplan.repository.SysRoleMenuRepository;
import com.taskplan.repository.SysUserRoleRepository;
import com.taskplan.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysMenuService sysMenuService;
    
    @Autowired
    private SysRoleRepository sysRoleRepository;
    
    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;
    
    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    public Map<String, Object> login(String username, String password) {
        try {
            SysUser user = sysUserService.getUserByUsername(username);
            
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BusinessException("密码错误");
            }
            
            if (user.getStatus() != 1) {
                throw new BusinessException("用户已被禁用");
            }
            
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            
            List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(user.getId());
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            
            List<Long> menuIds = sysRoleMenuRepository.findMenuIdsByRoleIds(roleIds);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("roles", roleIds);
            result.put("menus", sysMenuService.getUserMenus(menuIds));
            
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login error", e);
            throw new BusinessException("登录失败");
        }
    }
}
