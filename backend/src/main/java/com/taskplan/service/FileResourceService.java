package com.taskplan.service;

import com.taskplan.entity.FileResource;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.FileResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FileResourceService {

    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Transactional
    public FileResource saveFile(FileResource file) {
        if (file.getName() == null || file.getName().trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        
        String fileName = file.getName().toLowerCase();
        String fileType = "OTHER";
        if (fileName.endsWith(".sh")) {
            fileType = "SCRIPT";
        } else if (fileName.endsWith(".py")) {
            fileType = "SCRIPT";
        } else if (fileName.endsWith(".pl")) {
            fileType = "SCRIPT";
        } else if (fileName.endsWith(".jar")) {
            fileType = "JAR";
        }
        file.setType(fileType);
        
        return fileResourceRepository.save(file);
    }

    @Transactional
    public void deleteFile(Long id) {
        FileResource file = getFile(id);
        if (file.getIsDirectory() == 1) {
            List<FileResource> children = fileResourceRepository.findByParentId(id);
            if (!children.isEmpty()) {
                throw new BusinessException("请先删除目录内的文件和子目录");
            }
        }
        fileResourceRepository.deleteById(id);
    }

    public FileResource getFile(Long id) {
        return fileResourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文件不存在"));
    }

    public Page<FileResource> listFiles(String name, Long projectId, Pageable pageable) {
        if (projectId != null) {
            return fileResourceRepository.findByProjectId(projectId, pageable);
        }
        if (name != null && !name.isEmpty()) {
            return fileResourceRepository.findByNameContaining(name, pageable);
        }
        return fileResourceRepository.findAll(pageable);
    }

    public List<FileResource> listByType(String type) {
        return fileResourceRepository.findByType(type);
    }

    public List<FileResource> listAll() {
        return fileResourceRepository.findAll();
    }

    @Transactional
    public FileResource createDirectory(String name, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("目录名不能为空");
        }
        
        FileResource existing = fileResourceRepository.findByNameAndParentId(name, parentId);
        if (existing != null) {
            throw new BusinessException("目录已存在");
        }
        
        FileResource directory = new FileResource();
        directory.setName(name);
        directory.setIsDirectory(1);
        directory.setParentId(parentId);
        directory.setSize(0L);
        directory.setType("DIRECTORY");
        
        return fileResourceRepository.save(directory);
    }

    public List<FileResource> listDirectory(Long parentId) {
        return fileResourceRepository.findByParentIdOrderByNameAsc(parentId);
    }

    public List<FileResource> listDirectories(Long parentId) {
        return fileResourceRepository.findDirectoriesByParentId(parentId);
    }

    public List<FileResource> listFilesInDirectory(Long parentId) {
        return fileResourceRepository.findFilesByParentId(parentId);
    }

    public Map<String, Object> getDirectoryTree(Long parentId) {
        Map<String, Object> result = new HashMap<>();
        List<FileResource> directories = listDirectories(parentId);
        List<FileResource> files = listFilesInDirectory(parentId);
        
        result.put("directories", directories);
        result.put("files", files);
        return result;
    }

    public String getFileContent(Long id) {
        FileResource file = getFile(id);
        if (file.getIsDirectory() == 1) {
            throw new BusinessException("不能读取目录内容");
        }
        
        try {
            String pathStr = file.getPath();
            log.info("尝试读取文件[{}]: {}", file.getName(), pathStr);
            
            Path filePath = Paths.get(pathStr);
            
            if (!filePath.isAbsolute()) {
                String backendPath = System.getProperty("user.dir");
                filePath = Paths.get(backendPath, pathStr);
                log.info("相对路径转换为绝对路径: {}", filePath.toAbsolutePath());
            }
            
            log.info("文件路径(绝对路径): {}", filePath.toAbsolutePath());
            log.info("文件是否存在: {}", Files.exists(filePath));
            
            if (!Files.exists(filePath)) {
                throw new BusinessException("文件不存在: " + filePath.toAbsolutePath());
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            log.info("文件读取成功，内容长度: {}", content.length());
            return content.toString();
        } catch (IOException e) {
            log.error("读取文件内容失败", e);
            throw new RuntimeException("读取文件内容失败: " + e.getMessage());
        }
    }

    @Transactional
    public void updateFileContent(Long id, String content) {
        FileResource file = getFile(id);
        if (file.getIsDirectory() == 1) {
            throw new BusinessException("不能编辑目录");
        }
        
        try {
            Path filePath = Paths.get(file.getPath());
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(content);
            }
            file.setSize((long) content.getBytes().length);
            fileResourceRepository.save(file);
            log.info("文件内容更新成功: ID={}", id);
        } catch (IOException e) {
            log.error("写入文件内容失败", e);
            throw new RuntimeException("写入文件内容失败: " + e.getMessage());
        }
    }

    @Transactional
    public FileResource createFile(String name, String content, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        
        FileResource existing = fileResourceRepository.findByNameAndParentId(name, parentId);
        if (existing != null) {
            throw new BusinessException("文件已存在");
        }
        
        try {
            String basePath = System.getProperty("user.dir");
            String relativePath = "data/files";
            if (parentId != null) {
                FileResource parentDir = getFile(parentId);
                if (parentDir.getIsDirectory() != 1) {
                    throw new BusinessException("父路径必须是目录");
                }
                relativePath = parentDir.getPath() != null ? parentDir.getPath() : "data/files";
            }
            
            Path filePath = Paths.get(basePath, relativePath, name);
            Path parentPath = filePath.getParent();
            
            if (!Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
                log.info("创建目录: {}", parentPath.toAbsolutePath());
            }
            
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                if (content != null) {
                    writer.write(content);
                }
            }
            
            FileResource fileResource = new FileResource();
            fileResource.setName(name);
            fileResource.setPath(filePath.toAbsolutePath().toString());
            fileResource.setSize(content != null ? (long) content.getBytes().length : 0L);
            fileResource.setParentId(parentId);
            fileResource.setIsDirectory(0);
            
            String fileName = name.toLowerCase();
            String fileType = "OTHER";
            if (fileName.endsWith(".sh") || fileName.endsWith(".py") || fileName.endsWith(".pl")) {
                fileType = "SCRIPT";
            } else if (fileName.endsWith(".jar")) {
                fileType = "JAR";
            } else if (fileName.endsWith(".sql")) {
                fileType = "DATA";
            }
            fileResource.setType(fileType);
            
            FileResource savedFile = fileResourceRepository.save(fileResource);
            log.info("文件创建成功: ID={}, path={}", savedFile.getId(), filePath.toAbsolutePath());
            return savedFile;
        } catch (IOException e) {
            log.error("创建文件失败", e);
            throw new RuntimeException("创建文件失败: " + e.getMessage());
        }
    }
}
