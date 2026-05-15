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

import java.util.List;

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
        if (!fileResourceRepository.existsById(id)) {
            throw new BusinessException("文件不存在");
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
}