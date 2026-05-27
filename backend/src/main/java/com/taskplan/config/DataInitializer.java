package com.taskplan.config;

import com.taskplan.entity.*;
import com.taskplan.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserRepository sysUserRepository;
    
    @Autowired
    private SysRoleRepository sysRoleRepository;
    
    @Autowired
    private SysMenuRepository sysMenuRepository;
    
    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;
    
    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FileResourceRepository fileResourceRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Override
    public void run(String... args) {
        initializeData();
    }

    private void initializeData() {
        log.info("Initializing system data...");
        
        // 先清理旧数据
        try {
            sysRoleMenuRepository.deleteAll();
            sysUserRoleRepository.deleteAll();
            sysMenuRepository.deleteAll();
            sysRoleRepository.deleteAll();
            sysUserRepository.deleteAll();
            fileResourceRepository.deleteAll();
            sysConfigRepository.deleteAll();
            log.info("Cleaned existing data");
        } catch (Exception e) {
            log.warn("Cleanup failed, proceeding anyway: {}", e.getMessage());
        }

        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("系统管理员");
        admin.setEmail("admin@taskplan.com");
        admin.setStatus(1);
        admin.setIsDeleted(0);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        admin = sysUserRepository.save(admin);

        SysRole adminRole = new SysRole();
        adminRole.setRoleCode("ADMIN");
        adminRole.setRoleName("系统管理员");
        adminRole.setDescription("系统管理员角色，拥有所有权限");
        adminRole.setStatus(1);
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        adminRole = sysRoleRepository.save(adminRole);

        SysRole userRole = new SysRole();
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setDescription("普通用户角色");
        userRole.setStatus(1);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        userRole = sysRoleRepository.save(userRole);

        SysUserRole adminUserRole = new SysUserRole();
        adminUserRole.setUserId(admin.getId());
        adminUserRole.setRoleId(adminRole.getId());
        sysUserRoleRepository.save(adminUserRole);

        // 创建菜单
        SysMenu dashboard = createMenu("工作台", "dashboard", "C", "/dashboard", "Layout", null, "Odometer", 1, null);
        
        SysMenu projectMenu = createMenu("项目管理", "project", "C", "/projects", null, null, "FolderOpened", 2, null);
        
        SysMenu workflowMenu = createMenu("工作流管理", "workflow", "C", "/workflows", null, null, "Connection", 3, null);
        
        SysMenu scheduleMenu = createMenu("调度管理", "schedule", "C", "/schedules", null, null, "Clock", 4, null);
        
        SysMenu executionMenu = createMenu("执行记录", "execution", "C", "/executions", null, null, "List", 5, null);
        
        SysMenu datasourceMenu = createMenu("数据源管理", "datasource", "C", "/datasources", null, null, "Coin", 6, null);
        
        SysMenu serverMenu = createMenu("服务器管理", "server", "C", "/servers", null, null, "Cpu", 7, null);
        
        SysMenu fileMenu = createMenu("文件管理", "file", "C", "/files", null, null, "Folder", 8, null);
        
        SysMenu clusterMenu = createMenu("集群配置", "cluster", "C", "/cluster-configs", null, null, "Grid", 9, null);
        
        SysMenu taskTypeMenu = createMenu("任务类型", "tasktype", "C", "/task-types", null, null, "Briefcase", 10, null);
        
        SysMenu envMenu = createMenu("环境变量", "env", "C", "/env-variables", null, null, "Setting", 11, null);
        
        SysMenu systemMenu = createMenu("系统管理", "system", "C", "/system", null, null, "Tools", 12, null);
        SysMenu logsMenu = createMenu("日志管理", "logs", "C", "/logs", null, null, "FileText", 13, null);
        
        createMenu("用户管理", "system:user", "F", "/system/users", "views/UserList.vue", systemMenu.getId(), null, 1, "system:user");
        createMenu("角色管理", "system:role", "F", "/system/roles", "views/RoleList.vue", systemMenu.getId(), null, 2, "system:role");
        createMenu("菜单管理", "system:menu", "F", "/system/menus", "views/MenuList.vue", systemMenu.getId(), null, 3, "system:menu");
        createMenu("系统配置", "system:config", "F", "/system/config", "views/SysConfigList.vue", systemMenu.getId(), null, 4, "system:config");
        createMenu("登录日志", "system:loginlogs", "F", "/system/login-logs", "views/LoginLogList.vue", systemMenu.getId(), null, 5, "system:loginlogs");
        createMenu("操作日志", "system:operationlogs", "F", "/system/operation-logs", "views/OperationLogList.vue", systemMenu.getId(), null, 6, "system:operationlogs");
        createMenu("调度日志", "logs:schedule", "F", "/logs/schedule", "views/ScheduleLogList.vue", logsMenu.getId(), null, 1, "logs:schedule");
        createMenu("作业日志", "logs:task", "F", "/logs/task-execution", "views/TaskExecutionLogList.vue", logsMenu.getId(), null, 2, "logs:task");

        // 为管理员角色分配所有菜单
        for (SysMenu menu : sysMenuRepository.findAll()) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(adminRole.getId());
            roleMenu.setMenuId(menu.getId());
            sysRoleMenuRepository.save(roleMenu);
        }

        // 初始化文件管理数据
        initializeFileResources();

        log.info("System data initialization completed. Admin user: admin/123456");
    }

    private void initializeFileResources() {
        log.info("Initializing file resources...");
        
        try {
            // 创建uploads目录 - 使用绝对路径
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("./uploads").toAbsolutePath().normalize();
            log.info("Upload directory: {}", uploadDir);
            
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }
            
            // 创建根目录下的目录
            FileResource shellDir = new FileResource();
            shellDir.setName("shell");
            shellDir.setPath(uploadDir.resolve("shell").toString());
            shellDir.setSize(0L);
            shellDir.setParentId(null);
            shellDir.setIsDirectory(1);
            shellDir.setType("DIRECTORY");
            shellDir.setCreateTime(LocalDateTime.now());
            shellDir.setUpdateTime(LocalDateTime.now());
            shellDir = fileResourceRepository.save(shellDir);
            
            java.nio.file.Path shellPath = uploadDir.resolve("shell");
            if (!java.nio.file.Files.exists(shellPath)) {
                java.nio.file.Files.createDirectory(shellPath);
            }

            FileResource pythonDir = new FileResource();
            pythonDir.setName("python");
            pythonDir.setPath(uploadDir.resolve("python").toString());
            pythonDir.setSize(0L);
            pythonDir.setParentId(null);
            pythonDir.setIsDirectory(1);
            pythonDir.setType("DIRECTORY");
            pythonDir.setCreateTime(LocalDateTime.now());
            pythonDir.setUpdateTime(LocalDateTime.now());
            pythonDir = fileResourceRepository.save(pythonDir);
            
            java.nio.file.Path pythonPath = uploadDir.resolve("python");
            if (!java.nio.file.Files.exists(pythonPath)) {
                java.nio.file.Files.createDirectory(pythonPath);
            }

            FileResource dataDir = new FileResource();
            dataDir.setName("data");
            dataDir.setPath(uploadDir.resolve("data").toString());
            dataDir.setSize(0L);
            dataDir.setParentId(null);
            dataDir.setIsDirectory(1);
            dataDir.setType("DIRECTORY");
            dataDir.setCreateTime(LocalDateTime.now());
            dataDir.setUpdateTime(LocalDateTime.now());
            dataDir = fileResourceRepository.save(dataDir);
            
            java.nio.file.Path dataPath = uploadDir.resolve("data");
            if (!java.nio.file.Files.exists(dataPath)) {
                java.nio.file.Files.createDirectory(dataPath);
            }

            // 创建示例脚本文件
            java.nio.file.Path helloShPath = shellPath.resolve("hello.sh");
            String helloShContent = "#!/bin/bash\necho \"Hello, Task Plan!\"";
            java.nio.file.Files.write(helloShPath, helloShContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            FileResource helloSh = new FileResource();
            helloSh.setName("hello.sh");
            helloSh.setPath(helloShPath.toString());
            helloSh.setSize((long) helloShContent.length());
            helloSh.setParentId(shellDir.getId());
            helloSh.setIsDirectory(0);
            helloSh.setType("SCRIPT");
            helloSh.setCreateTime(LocalDateTime.now());
            helloSh.setUpdateTime(LocalDateTime.now());
            fileResourceRepository.save(helloSh);

            java.nio.file.Path testPyPath = pythonPath.resolve("test.py");
            String testPyContent = "#!/usr/bin/env python3\nprint(\"Hello from Python!\")";
            java.nio.file.Files.write(testPyPath, testPyContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            FileResource testPy = new FileResource();
            testPy.setName("test.py");
            testPy.setPath(testPyPath.toString());
            testPy.setSize((long) testPyContent.length());
            testPy.setParentId(pythonDir.getId());
            testPy.setIsDirectory(0);
            testPy.setType("SCRIPT");
            testPy.setCreateTime(LocalDateTime.now());
            testPy.setUpdateTime(LocalDateTime.now());
            fileResourceRepository.save(testPy);

            log.info("File resources initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize file resources", e);
        }
        
        initializeSysConfig();
    }
    
    private void initializeSysConfig() {
        log.info("Initializing system config...");
        
        SysConfig uploadPath = new SysConfig();
        uploadPath.setConfigKey("upload.path");
        uploadPath.setConfigValue("./uploads");
        uploadPath.setDescription("文件上传路径");
        uploadPath.setCreateTime(LocalDateTime.now());
        uploadPath.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(uploadPath);

        SysConfig scheduleLogPath = new SysConfig();
        scheduleLogPath.setConfigKey("schedule.log.path");
        scheduleLogPath.setConfigValue("./logs/scheduler");
        scheduleLogPath.setDescription("调度日志路径");
        scheduleLogPath.setCreateTime(LocalDateTime.now());
        scheduleLogPath.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(scheduleLogPath);

        SysConfig taskLogPath = new SysConfig();
        taskLogPath.setConfigKey("task.log.path");
        taskLogPath.setConfigValue("./logs/task");
        taskLogPath.setDescription("任务日志路径");
        taskLogPath.setCreateTime(LocalDateTime.now());
        taskLogPath.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(taskLogPath);

        SysConfig allowedTypes = new SysConfig();
        allowedTypes.setConfigKey("upload.allowedTypes");
        allowedTypes.setConfigValue("txt,doc,docx,xls,xlsx,ppt,pptx,pdf,jpg,jpeg,png,gif,bmp,zip,rar,7z,tgz,tar,sh,py,pl,jar,sql");
        allowedTypes.setDescription("允许上传的文件类型");
        allowedTypes.setCreateTime(LocalDateTime.now());
        allowedTypes.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(allowedTypes);

        SysConfig maxSizeMB = new SysConfig();
        maxSizeMB.setConfigKey("upload.maxSizeMB");
        maxSizeMB.setConfigValue("100");
        maxSizeMB.setDescription("最大上传文件大小(MB)");
        maxSizeMB.setCreateTime(LocalDateTime.now());
        maxSizeMB.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(maxSizeMB);

        SysConfig maxChunks = new SysConfig();
        maxChunks.setConfigKey("upload.maxChunks");
        maxChunks.setConfigValue("100");
        maxChunks.setDescription("最大分块数");
        maxChunks.setCreateTime(LocalDateTime.now());
        maxChunks.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(maxChunks);

        SysConfig taskInstancePath = new SysConfig();
        taskInstancePath.setConfigKey("task.instance.path");
        taskInstancePath.setConfigValue("./instances");
        taskInstancePath.setDescription("任务实例保存路径");
        taskInstancePath.setCreateTime(LocalDateTime.now());
        taskInstancePath.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(taskInstancePath);

        SysConfig mailHost = new SysConfig();
        mailHost.setConfigKey("mail.host");
        mailHost.setConfigValue("smtp.example.com");
        mailHost.setDescription("邮件服务器地址");
        mailHost.setCreateTime(LocalDateTime.now());
        mailHost.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(mailHost);

        SysConfig mailPort = new SysConfig();
        mailPort.setConfigKey("mail.port");
        mailPort.setConfigValue("587");
        mailPort.setDescription("邮件服务器端口");
        mailPort.setCreateTime(LocalDateTime.now());
        mailPort.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(mailPort);

        SysConfig mailUsername = new SysConfig();
        mailUsername.setConfigKey("mail.username");
        mailUsername.setConfigValue("");
        mailUsername.setDescription("邮件服务器用户名");
        mailUsername.setCreateTime(LocalDateTime.now());
        mailUsername.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(mailUsername);

        SysConfig mailPassword = new SysConfig();
        mailPassword.setConfigKey("mail.password");
        mailPassword.setConfigValue("");
        mailPassword.setDescription("邮件服务器密码");
        mailPassword.setCreateTime(LocalDateTime.now());
        mailPassword.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(mailPassword);

        SysConfig mailFrom = new SysConfig();
        mailFrom.setConfigKey("mail.from");
        mailFrom.setConfigValue("");
        mailFrom.setDescription("发件人邮箱地址");
        mailFrom.setCreateTime(LocalDateTime.now());
        mailFrom.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(mailFrom);

        SysConfig mailSsl = new SysConfig();
        mailSsl.setConfigKey("mail.ssl");
        mailSsl.setConfigValue("true");
        mailSsl.setDescription("是否启用SSL");
        mailSsl.setCreateTime(LocalDateTime.now());
        mailSsl.setUpdateTime(LocalDateTime.now());
        sysConfigRepository.save(mailSsl);

        log.info("System config initialization completed");
    }

    private SysMenu createMenu(String name, String code, String type, String path, String component, Long parentId, String icon, int sort, String permission) {
        SysMenu menu = new SysMenu();
        menu.setMenuName(name);
        menu.setMenuCode(code);
        menu.setMenuType(type);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setParentId(parentId);
        menu.setIcon(icon);
        menu.setSortOrder(sort);
        menu.setStatus(1);
        menu.setPermission(permission);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        return sysMenuRepository.save(menu);
    }
}
