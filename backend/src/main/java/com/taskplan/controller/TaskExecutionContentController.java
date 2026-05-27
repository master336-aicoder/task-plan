package com.taskplan.controller;

import com.taskplan.entity.TaskExecutionContent;
import com.taskplan.service.TaskExecutionContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/task-execution-content")
public class TaskExecutionContentController {

    @Autowired
    private TaskExecutionContentService taskExecutionContentService;

    @GetMapping("/process/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> getByProcessInstanceId(@PathVariable Long processInstanceId) {
        List<TaskExecutionContent> contents = taskExecutionContentService.findByProcessInstanceId(processInstanceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200000);
        response.put("msg", "success");
        response.put("data", contents);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/process/{processInstanceId}/task/{taskNodeId}")
    public ResponseEntity<Map<String, Object>> getByProcessAndTask(@PathVariable Long processInstanceId, @PathVariable String taskNodeId) {
        TaskExecutionContent content = taskExecutionContentService.findByProcessInstanceIdAndTaskNodeId(processInstanceId, taskNodeId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200000);
        response.put("msg", "success");
        response.put("data", content);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        TaskExecutionContent content = taskExecutionContentService.findById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200000);
        response.put("msg", "success");
        response.put("data", content);
        
        return ResponseEntity.ok(response);
    }
}
