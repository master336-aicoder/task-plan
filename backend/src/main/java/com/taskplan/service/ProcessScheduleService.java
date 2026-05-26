package com.taskplan.service;

import com.taskplan.entity.ProcessSchedule;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ProcessScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProcessScheduleService {

    @Autowired
    private ProcessScheduleRepository processScheduleRepository;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    @Transactional
    public ProcessSchedule createSchedule(ProcessSchedule schedule) {
        validateSchedule(schedule);
        ProcessSchedule saved = processScheduleRepository.save(schedule);
        
        if (saved.getIsValid() == 1) {
            try {
                scheduleExecutorService.addScheduleJob(saved);
                log.info("新调度任务已注册到Quartz: id={}", saved.getId());
            } catch (SchedulerException e) {
                log.error("注册调度任务失败: id={}, error={}", saved.getId(), e.getMessage());
            }
        }
        
        return saved;
    }

    @Transactional
    public ProcessSchedule updateSchedule(Long id, ProcessSchedule schedule) {
        log.info("更新调度: id={}, processDefinitionId={}, cronExpression={}", 
                 id, schedule.getProcessDefinitionId(), schedule.getCronExpression());
        try {
            ProcessSchedule existing = getSchedule(id);
            log.info("找到现有调度: id={}, projectId={}, processDefinitionId={}", 
                     existing.getId(), existing.getProjectId(), existing.getProcessDefinitionId());
            
            boolean needReschedule = false;
            
            if (schedule.getCronExpression() != null && !schedule.getCronExpression().equals(existing.getCronExpression())) {
                existing.setCronExpression(schedule.getCronExpression());
                log.info("更新 cronExpression: {}", schedule.getCronExpression());
                needReschedule = true;
            }
            if (schedule.getStartTime() != null) {
                existing.setStartTime(schedule.getStartTime());
                log.info("更新 startTime: {}", schedule.getStartTime());
            }
            if (schedule.getEndTime() != null) {
                existing.setEndTime(schedule.getEndTime());
                log.info("更新 endTime: {}", schedule.getEndTime());
            }
            if (schedule.getTimezone() != null && !schedule.getTimezone().equals(existing.getTimezone())) {
                existing.setTimezone(schedule.getTimezone());
                log.info("更新 timezone: {}", schedule.getTimezone());
                needReschedule = true;
            }
            if (schedule.getDelayTime() != null) existing.setDelayTime(schedule.getDelayTime());
            if (schedule.getWarningType() != null) existing.setWarningType(schedule.getWarningType());
            if (schedule.getWarningGroupId() != null) existing.setWarningGroupId(schedule.getWarningGroupId());
            if (schedule.getFailRetryTimes() != null) existing.setFailRetryTimes(schedule.getFailRetryTimes());
            if (schedule.getFailRetryInterval() != null) existing.setFailRetryInterval(schedule.getFailRetryInterval());
            if (schedule.getIsValid() != null) {
                existing.setIsValid(schedule.getIsValid());
                log.info("更新 isValid: {}", schedule.getIsValid());
                needReschedule = true;
            }
            if (schedule.getOwner() != null) {
                existing.setOwner(schedule.getOwner());
                log.info("更新 owner: {}", schedule.getOwner());
            }
            
            ProcessSchedule saved = processScheduleRepository.save(existing);
            log.info("调度更新成功: id={}", saved.getId());
            
            if (needReschedule) {
                try {
                    scheduleExecutorService.removeScheduleJob(id);
                    if (saved.getIsValid() == 1) {
                        scheduleExecutorService.addScheduleJob(saved);
                        log.info("调度任务已重新注册到Quartz: id={}, cron={}", saved.getId(), saved.getCronExpression());
                    }
                } catch (SchedulerException e) {
                    log.error("重新注册调度任务失败: id={}, error={}", id, e.getMessage());
                }
            }
            
            return saved;
        } catch (Exception e) {
            log.error("更新调度失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ProcessSchedule enableSchedule(Long id) {
        ProcessSchedule schedule = getSchedule(id);
        
        if (schedule.getIsValid() == 1) {
            log.warn("调度任务已经是启用状态: id={}", id);
            return schedule;
        }
        
        schedule.setIsValid(1);
        ProcessSchedule saved = processScheduleRepository.save(schedule);
        
        try {
            scheduleExecutorService.addScheduleJob(saved);
            log.info("调度任务已启用并注册到Quartz: id={}, cron={}", id, saved.getCronExpression());
        } catch (SchedulerException e) {
            log.error("注册调度任务失败: id={}, error={}", id, e.getMessage());
            throw new BusinessException("启用调度失败: " + e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public ProcessSchedule disableSchedule(Long id) {
        ProcessSchedule schedule = getSchedule(id);
        
        if (schedule.getIsValid() == 0) {
            log.warn("调度任务已经是禁用状态: id={}", id);
            return schedule;
        }
        
        try {
            scheduleExecutorService.removeScheduleJob(id);
            log.info("调度任务已从Quartz移除: id={}", id);
        } catch (SchedulerException e) {
            log.error("移除调度任务失败: id={}, error={}", id, e.getMessage());
            throw new BusinessException("禁用调度失败: " + e.getMessage());
        }
        
        schedule.setIsValid(0);
        ProcessSchedule saved = processScheduleRepository.save(schedule);
        
        log.info("调度任务已禁用: id={}", id);
        return saved;
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!processScheduleRepository.existsById(id)) {
            throw new BusinessException("调度不存在");
        }
        processScheduleRepository.deleteById(id);
    }

    public ProcessSchedule getSchedule(Long id) {
        return processScheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("调度不存在"));
    }

    public Page<ProcessSchedule> listSchedules(Long projectId, Long processDefinitionId, Pageable pageable) {
        if (projectId != null && processDefinitionId != null) {
            return processScheduleRepository.findByProjectIdAndProcessDefinitionId(projectId, processDefinitionId, pageable);
        } else if (projectId != null) {
            return processScheduleRepository.findByProjectId(projectId, pageable);
        } else if (processDefinitionId != null) {
            return processScheduleRepository.findByProcessDefinitionId(processDefinitionId, pageable);
        }
        return processScheduleRepository.findAll(pageable);
    }

    public List<ProcessSchedule> listSchedulesByProcess(Long processDefinitionId) {
        return processScheduleRepository.findByProcessDefinitionId(processDefinitionId);
    }

    public List<ProcessSchedule> listValidSchedules() {
        return processScheduleRepository.findByIsValid(1);
    }

    private void validateSchedule(ProcessSchedule schedule) {
        if (schedule.getProjectId() == null || schedule.getProjectId() <= 0) {
            throw new BusinessException("项目ID不能为空");
        }
        if (schedule.getProcessDefinitionId() == null || schedule.getProcessDefinitionId() <= 0) {
            throw new BusinessException("请选择工作流");
        }
    }
}
