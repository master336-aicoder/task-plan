package com.taskplan.service;

import com.taskplan.entity.ProcessSchedule;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ProcessScheduleRepository;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public ProcessSchedule createSchedule(ProcessSchedule schedule) {
        validateSchedule(schedule);
        return processScheduleRepository.save(schedule);
    }

    @Transactional
    public ProcessSchedule updateSchedule(Long id, ProcessSchedule schedule) {
        ProcessSchedule existing = getSchedule(id);
        
        if (schedule.getCronExpression() != null) existing.setCronExpression(schedule.getCronExpression());
        if (schedule.getStartTime() != null) existing.setStartTime(schedule.getStartTime());
        if (schedule.getEndTime() != null) existing.setEndTime(schedule.getEndTime());
        if (schedule.getTimezone() != null) existing.setTimezone(schedule.getTimezone());
        if (schedule.getDelayTime() != null) existing.setDelayTime(schedule.getDelayTime());
        if (schedule.getWarningType() != null) existing.setWarningType(schedule.getWarningType());
        if (schedule.getWarningGroupId() != null) existing.setWarningGroupId(schedule.getWarningGroupId());
        if (schedule.getFailRetryTimes() != null) existing.setFailRetryTimes(schedule.getFailRetryTimes());
        if (schedule.getFailRetryInterval() != null) existing.setFailRetryInterval(schedule.getFailRetryInterval());
        if (schedule.getIsValid() != null) existing.setIsValid(schedule.getIsValid());
        if (schedule.getOwner() != null) existing.setOwner(schedule.getOwner());
        
        return processScheduleRepository.save(existing);
    }

    @Transactional
    public ProcessSchedule enableSchedule(Long id) {
        ProcessSchedule schedule = getSchedule(id);
        schedule.setIsValid(1);
        return processScheduleRepository.save(schedule);
    }

    @Transactional
    public ProcessSchedule disableSchedule(Long id) {
        ProcessSchedule schedule = getSchedule(id);
        schedule.setIsValid(0);
        return processScheduleRepository.save(schedule);
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
        if (processDefinitionId != null) {
            return processScheduleRepository.findByProjectIdAndProcessDefinitionId(projectId, processDefinitionId, pageable);
        }
        return processScheduleRepository.findByProjectId(projectId, pageable);
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
