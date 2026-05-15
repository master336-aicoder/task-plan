package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.FileResource;
import com.taskplan.service.FileResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileResourceController {

    @Autowired
    private FileResourceService fileResourceService;

    private static final String UPLOAD_DIR = "./uploads";

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

    @PostMapping("/upload")
    public Result<FileResource> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam(required = false) Long projectId) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(originalFilename);
        file.transferTo(filePath.toFile());

        FileResource fileResource = new FileResource();
        fileResource.setName(originalFilename);
        fileResource.setPath(filePath.toString());
        fileResource.setSize(file.getSize());
        fileResource.setProjectId(projectId);

        return Result.success(fileResourceService.saveFile(fileResource));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        fileResourceService.deleteFile(id);
        return Result.success();
    }
}