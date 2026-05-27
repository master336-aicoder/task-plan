package com.taskplan.service;

import com.taskplan.entity.TaskExecutionContent;
import com.taskplan.repository.TaskExecutionContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TaskExecutionContentService {

    @Autowired
    private TaskExecutionContentRepository taskExecutionContentRepository;

    @Transactional
    public TaskExecutionContent saveContent(TaskExecutionContent content) {
        TaskExecutionContent saved = taskExecutionContentRepository.save(content);
        log.info("任务执行内容已保存: processInstanceId={}, taskNodeId={}", 
            content.getProcessInstanceId(), content.getTaskNodeId());
        return saved;
    }

    public List<TaskExecutionContent> findByProcessInstanceId(Long processInstanceId) {
        return taskExecutionContentRepository.findByProcessInstanceId(processInstanceId);
    }

    public TaskExecutionContent findByProcessInstanceIdAndTaskNodeId(Long processInstanceId, String taskNodeId) {
        List<TaskExecutionContent> contents = taskExecutionContentRepository.findByProcessInstanceIdAndTaskNodeId(processInstanceId, taskNodeId);
        return contents.isEmpty() ? null : contents.get(0);
    }
}
