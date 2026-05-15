package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.Project;
import com.taskplan.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public Result<List<Project>> listProjects(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return Result.success(projectService.searchProjects(keyword));
        }
        return Result.success(projectService.listProjects());
    }

    @GetMapping("/{id}")
    public Result<Project> getProject(@PathVariable Long id) {
        return Result.success(projectService.getProject(id));
    }

    @PostMapping
    public Result<Project> createProject(@RequestBody Project project) {
        return Result.success(projectService.createProject(project));
    }

    @PutMapping("/{id}")
    public Result<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        return Result.success(projectService.updateProject(id, project));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }
}
