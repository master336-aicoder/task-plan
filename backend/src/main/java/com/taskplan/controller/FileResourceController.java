package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.FileResource;
import com.taskplan.service.FileResourceService;
import com.taskplan.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileResourceController {

    @Autowired
    private FileResourceService fileResourceService;

    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping
    public Result<Map<String, Object>> listFiles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FileResource> filePage = fileResourceService.listFiles(name, projectId, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", filePage.getContent());
        result.put("total", filePage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<FileResource>> listAll() {
        return Result.success(fileResourceService.listAll());
    }

    @GetMapping("/type/{type}")
    public Result<List<FileResource>> listByType(@PathVariable String type) {
        return Result.success(fileResourceService.listByType(type));
    }

    @GetMapping("/{id}")
    public Result<FileResource> getFile(@PathVariable Long id) {
        return Result.success(fileResourceService.getFile(id));
    }

    @GetMapping("/directory")
    public Result<Map<String, Object>> listDirectory(@RequestParam(required = false) Long parentId) {
        return Result.success(fileResourceService.getDirectoryTree(parentId));
    }

    @GetMapping("/directories")
    public Result<List<FileResource>> listDirectories(@RequestParam(required = false) Long parentId) {
        return Result.success(fileResourceService.listDirectories(parentId));
    }

    @PostMapping("/directory")
    public Result<FileResource> createDirectory(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? Long.parseLong(request.get("parentId").toString()) : null;
        return Result.success(fileResourceService.createDirectory(name, parentId));
    }

    @PostMapping("/upload")
    public Result<FileResource> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam(required = false) Long projectId,
                                           @RequestParam(required = false) Long parentId) {
        log.info("收到上传请求: file={}, size={}, projectId={}, parentId={}", 
                 (file != null ? file.getOriginalFilename() : "null"), 
                 (file != null ? file.getSize() : 0), projectId, parentId);
                 
        if (file == null || file.isEmpty()) {
            log.warn("上传文件为空");
            throw new RuntimeException("请选择要上传的文件");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new RuntimeException("文件名不能为空");
            }
            
            // 检查文件类型
            if (!sysConfigService.isFileTypeAllowed(originalFilename)) {
                log.warn("文件类型不允许: {}", originalFilename);
                String allowedTypes = sysConfigService.getUploadAllowedTypes();
                throw new RuntimeException("文件类型不允许，允许的类型: " + allowedTypes);
            }
            
            // 检查文件大小
            if (!sysConfigService.isFileSizeAllowed(file.getSize())) {
                log.warn("文件大小超出限制: {}, 限制: {}MB", file.getSize(), sysConfigService.getUploadMaxSizeMB());
                throw new RuntimeException("文件大小超出限制，最大允许: " + sysConfigService.getUploadMaxSizeMB() + "MB");
            }
            
            // 获取上传路径
            String uploadPathStr = sysConfigService.getUploadPath();
            log.info("上传路径: {}", uploadPathStr);
            
            // 创建上传目录
            Path uploadPath = Paths.get(uploadPathStr);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }
            
            // 使用安全的文件名处理
            String safeFileName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path filePath = uploadPath.resolve(safeFileName);
            
            // 保存文件
            file.transferTo(filePath.toFile());
            log.info("文件保存成功: {}", filePath.toAbsolutePath());

            // 创建文件记录
            FileResource fileResource = new FileResource();
            fileResource.setName(originalFilename);
            fileResource.setPath(filePath.toAbsolutePath().toString());
            fileResource.setSize(file.getSize());
            fileResource.setProjectId(projectId);
            fileResource.setParentId(parentId);
            fileResource.setIsDirectory(0);

            FileResource savedFile = fileResourceService.saveFile(fileResource);
            log.info("文件记录保存成功: ID={}, name={}", savedFile.getId(), savedFile.getName());
            return Result.success(savedFile);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        fileResourceService.deleteFile(id);
        return Result.success();
    }

    @GetMapping("/{id}/content")
    public Result<String> getFileContent(@PathVariable Long id) {
        return Result.success(fileResourceService.getFileContent(id));
    }

    @PutMapping("/{id}/content")
    public Result<Void> updateFileContent(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String content = request.get("content");
        fileResourceService.updateFileContent(id, content);
        return Result.success();
    }

    @PostMapping("/file")
    public Result<FileResource> createFile(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String content = (String) request.get("content");
        Long parentId = request.get("parentId") != null ? Long.parseLong(request.get("parentId").toString()) : null;
        return Result.success(fileResourceService.createFile(name, content, parentId));
    }
}
