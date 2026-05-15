package com.taskplan.service;

import com.taskplan.entity.Project;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public Project createProject(Project project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new BusinessException("项目名称不能为空");
        }
        if (projectRepository.existsByName(project.getName())) {
            throw new BusinessException("项目名称已存在");
        }
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, Project project) {
        Project existing = getProject(id);
        if (project.getName() != null && !project.getName().equals(existing.getName())) {
            if (projectRepository.existsByName(project.getName())) {
                throw new BusinessException("项目名称已存在");
            }
            existing.setName(project.getName());
        }
        if (project.getDescription() != null) {
            existing.setDescription(project.getDescription());
        }
        return projectRepository.save(existing);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new BusinessException("项目不存在");
        }
        projectRepository.deleteById(id);
    }

    public Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("项目不存在"));
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }

    public List<Project> searchProjects(String keyword) {
        return projectRepository.findByNameContaining(keyword);
    }
}
