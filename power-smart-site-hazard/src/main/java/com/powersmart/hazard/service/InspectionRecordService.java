package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.InspectionRecord;
import com.powersmart.hazard.entity.InspectionTask;
import com.powersmart.hazard.mapper.InspectionRecordMapper;
import com.powersmart.hazard.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "巡检记录管理")
public class InspectionRecordService {

    private final InspectionRecordMapper inspectionRecordMapper;
    private final InspectionTaskMapper inspectionTaskMapper;

    public PageResult<InspectionRecord> list(Map<String, Object> params) {
        Page<InspectionRecord> page = PageHelper.of(params);
        LambdaQueryWrapper<InspectionRecord> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("taskId") && params.get("taskId") != null)
                wrapper.eq(InspectionRecord::getTaskId, Long.valueOf(params.get("taskId").toString()));
            if (params.containsKey("inspectorId") && params.get("inspectorId") != null)
                wrapper.eq(InspectionRecord::getInspectorId, Long.valueOf(params.get("inspectorId").toString()));
            if (params.containsKey("checkResult") && params.get("checkResult") != null)
                wrapper.eq(InspectionRecord::getCheckResult, params.get("checkResult").toString());
        }
        wrapper.orderByAsc(InspectionRecord::getPointIndex, InspectionRecord::getId);
        return PageResult.from(inspectionRecordMapper.selectPage(page, wrapper));
    }

    public InspectionRecord getById(Long id) {
        return inspectionRecordMapper.selectById(id);
    }

    /**
     * 批量提交巡检记录
     */
    @Transactional(rollbackFor = Exception.class)
    @OperateLog(description = "提交巡检记录")
    public void submitRecords(Long taskId, List<InspectionRecord> records) {
        InspectionTask task = inspectionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("巡检任务不存在");
        }
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("巡检记录不能为空");
        }

        Long currentUserId = SecurityContext.getCurrentUserId();
        String currentUsername = SecurityContext.getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();

        int completedCount = 0;
        boolean hasFail = false;
        boolean allPass = true;

        for (InspectionRecord record : records) {
            record.setTaskId(taskId);
            record.setInspectorId(currentUserId);
            record.setInspectorName(currentUsername);
            record.setCheckTime(now);
            inspectionRecordMapper.insert(record);

            completedCount++;

            if ("fail".equals(record.getCheckResult())) {
                hasFail = true;
                allPass = false;
            } else if ("na".equals(record.getCheckResult())) {
                allPass = false;
            }
        }

        // 更新任务完成点数
        int totalCompleted = inspectionRecordMapper.selectCount(
                new LambdaQueryWrapper<InspectionRecord>()
                        .eq(InspectionRecord::getTaskId, taskId));
        task.setCompletedPoints(totalCompleted);

        // 判断任务是否全部完成
        if (totalCompleted >= task.getTotalPoints()) {
            if (allPass) {
                task.setResult("pass");
            } else if (hasFail) {
                task.setResult("fail");
            } else {
                task.setResult("conditional_pass");
            }
            task.setStatus("completed");
            task.setEndTime(now);
            task.setCompletedAt(now);
        } else {
            // 更新状态为进行中
            if (!"in_progress".equals(task.getStatus())) {
                task.setStartTime(now);
            }
            task.setStatus("in_progress");
        }

        inspectionTaskMapper.updateById(task);
        log.info("Submitted {} records for task [{}], completedPoints={}/{}", completedCount, taskId,
                task.getCompletedPoints(), task.getTotalPoints());
    }

    public List<InspectionRecord> getByTask(Long taskId) {
        return inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<InspectionRecord>()
                        .eq(InspectionRecord::getTaskId, taskId)
                        .orderByAsc(InspectionRecord::getPointIndex, InspectionRecord::getId));
    }
}
