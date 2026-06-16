package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.InspectionPlan;
import com.powersmart.hazard.entity.InspectionTask;
import com.powersmart.hazard.mapper.InspectionPlanMapper;
import com.powersmart.hazard.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "巡检任务管理")
public class InspectionTaskService {

    private final InspectionTaskMapper inspectionTaskMapper;
    private final InspectionPlanMapper inspectionPlanMapper;

    public PageResult<InspectionTask> list(Map<String, Object> params) {
        Page<InspectionTask> page = PageHelper.of(params);
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(InspectionTask::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("planId") && params.get("planId") != null)
                wrapper.eq(InspectionTask::getPlanId, Long.valueOf(params.get("planId").toString()));
            if (params.containsKey("assigneeId") && params.get("assigneeId") != null)
                wrapper.eq(InspectionTask::getAssigneeId, Long.valueOf(params.get("assigneeId").toString()));
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(InspectionTask::getStatus, params.get("status").toString());
            if (params.containsKey("startDate") && params.get("startDate") != null)
                wrapper.ge(InspectionTask::getScheduledDate, LocalDate.parse(params.get("startDate").toString()));
            if (params.containsKey("endDate") && params.get("endDate") != null)
                wrapper.le(InspectionTask::getScheduledDate, LocalDate.parse(params.get("endDate").toString()));
        }
        wrapper.orderByDesc(InspectionTask::getCreatedAt);
        return PageResult.from(inspectionTaskMapper.selectPage(page, wrapper));
    }

    public InspectionTask getById(Long id) {
        return inspectionTaskMapper.selectById(id);
    }

    @OperateLog(description = "新增巡检任务")
    public void add(InspectionTask entity) {
        if (StrUtil.isBlank(entity.getTaskCode())) {
            entity.setTaskCode(generateTaskCode());
        }
        if (entity.getCompletedPoints() == null) {
            entity.setCompletedPoints(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("pending");
        }
        inspectionTaskMapper.insert(entity);
    }

    @OperateLog(description = "修改巡检任务")
    public void update(InspectionTask entity) {
        InspectionTask existing = inspectionTaskMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("巡检任务不存在");
        }
        if (StrUtil.isNotBlank(entity.getTaskName())) existing.setTaskName(entity.getTaskName());
        if (entity.getAssigneeId() != null) existing.setAssigneeId(entity.getAssigneeId());
        if (StrUtil.isNotBlank(entity.getAssigneeName())) existing.setAssigneeName(entity.getAssigneeName());
        if (entity.getScheduledDate() != null) existing.setScheduledDate(entity.getScheduledDate());
        if (entity.getDeadline() != null) existing.setDeadline(entity.getDeadline());
        if (StrUtil.isNotBlank(entity.getStatus())) existing.setStatus(entity.getStatus());
        if (StrUtil.isNotBlank(entity.getRemark())) existing.setRemark(entity.getRemark());
        inspectionTaskMapper.updateById(existing);
    }

    @OperateLog(description = "删除巡检任务")
    public void delete(Long id) {
        inspectionTaskMapper.deleteById(id);
    }

    /**
     * 根据计划生成指定日期的巡检任务
     */
    @Transactional(rollbackFor = Exception.class)
    @OperateLog(description = "生成巡检任务")
    public int generateTasks(Long planId, LocalDate date) {
        InspectionPlan plan = inspectionPlanMapper.selectById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("巡检计划不存在");
        }
        if (!"active".equals(plan.getStatus())) {
            throw new IllegalArgumentException("巡检计划未启动");
        }

        // 检查日期是否在计划范围内
        if (date.isBefore(plan.getStartDate()) || date.isAfter(plan.getEndDate())) {
            throw new IllegalArgumentException("日期不在计划有效期内");
        }

        // 检查是否已存在当天任务 — 已存在则跳过(幂等)
        long existingCount = inspectionTaskMapper.selectCount(
                new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getPlanId, planId)
                        .eq(InspectionTask::getScheduledDate, date));
        if (existingCount > 0) {
            log.info("Task already exists for plan [{}] on date [{}], skipping", planId, date);
            return 0;
        }

        InspectionTask task = new InspectionTask();
        task.setProjectId(plan.getProjectId());
        task.setPlanId(planId);
        task.setTaskCode(generateTaskCode());
        task.setTaskName(plan.getPlanName() + "-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        task.setAssigneeId(plan.getAssignedTo());
        task.setAssigneeName(plan.getAssigneeName());
        task.setScheduledDate(date);
        task.setDeadline(LocalDateTime.of(date, LocalTime.of(18, 0)));
        task.setTotalPoints(plan.getTotalPoints() != null ? plan.getTotalPoints() : 0);
        task.setCompletedPoints(0);
        task.setStatus("pending");

        inspectionTaskMapper.insert(task);
        log.info("Generated inspection task [{}] from plan [{}] for date [{}]", task.getTaskCode(), planId, date);

        return 1;
    }

    /**
     * 每日定时任务：为所有活跃的日/周计划生成今天的任务
     */
    @Transactional(rollbackFor = Exception.class)
    @OperateLog(description = "批量生成每日巡检任务")
    public int generateDailyTasks(Long projectId) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<InspectionPlan>()
                .eq(InspectionPlan::getStatus, "active")
                .le(InspectionPlan::getStartDate, today)
                .ge(InspectionPlan::getEndDate, today);

        if (projectId != null) {
            wrapper.eq(InspectionPlan::getProjectId, projectId);
        }

        List<InspectionPlan> activePlans = inspectionPlanMapper.selectList(wrapper);
        int count = 0;

        for (InspectionPlan plan : activePlans) {
            boolean shouldGenerate = false;

            if ("daily".equals(plan.getPlanType())) {
                shouldGenerate = true;
            } else if ("weekly".equals(plan.getPlanType())) {
                // 周计划：检查今天是否符合该周期的巡检日
                if (StrUtil.isNotBlank(plan.getFrequency())) {
                    shouldGenerate = matchWeeklyDay(plan.getFrequency(), today);
                }
            }

            if (shouldGenerate) {
                try {
                    count += generateTasks(plan.getId(), today);
                } catch (Exception e) {
                    log.warn("Skip generating task for plan [{}]: {}", plan.getId(), e.getMessage());
                }
            }
        }

        log.info("Generated {} tasks for project [{}] on [{}]", count, projectId, today);
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    @OperateLog(description = "更新任务状态")
    public void updateStatus(Long id, String status) {
        InspectionTask task = inspectionTaskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("巡检任务不存在");
        }

        task.setStatus(status);
        if ("in_progress".equals(status)) {
            task.setStartTime(LocalDateTime.now());
        } else if ("completed".equals(status)) {
            task.setEndTime(LocalDateTime.now());
            task.setCompletedAt(LocalDateTime.now());
        } else if ("overdue".equals(status)) {
            if (task.getEndTime() == null) {
                task.setEndTime(LocalDateTime.now());
            }
        }
        inspectionTaskMapper.updateById(task);
    }

    public List<InspectionTask> getMyPendingTasks(Long userId) {
        return inspectionTaskMapper.selectList(
                new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getAssigneeId, userId)
                        .in(InspectionTask::getStatus, "pending", "in_progress")
                        .orderByAsc(InspectionTask::getScheduledDate));
    }

    public Map<String, Object> getTaskStats(Long projectId) {
        LambdaQueryWrapper<InspectionTask> baseWrapper = new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getProjectId, projectId);

        long total = inspectionTaskMapper.selectCount(baseWrapper);
        long pending = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getStatus, "pending"));
        long inProgress = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getStatus, "in_progress"));
        long completed = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getStatus, "completed"));
        long overdue = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getStatus, "overdue"));
        long passCount = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getResult, "pass"));
        long conditionalPass = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getResult, "conditional_pass"));
        long failCount = inspectionTaskMapper.selectCount(
                baseWrapper.clone().eq(InspectionTask::getResult, "fail"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("overdue", overdue);
        stats.put("passCount", passCount);
        stats.put("conditionalPass", conditionalPass);
        stats.put("failCount", failCount);
        return stats;
    }

    /**
     * 生成任务编号：INS-{yyyyMMdd}-{xxxx}
     * 使用 synchronized 防止并发下重复编号
     */
    private synchronized String generateTaskCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 查询当天最后一个任务编号
        InspectionTask lastTask = inspectionTaskMapper.selectOne(
                new LambdaQueryWrapper<InspectionTask>()
                        .likeRight(InspectionTask::getTaskCode, "INS-" + datePart)
                        .orderByDesc(InspectionTask::getId)
                        .last("LIMIT 1"));
        int seq = 1;
        if (lastTask != null && StrUtil.isNotBlank(lastTask.getTaskCode())) {
            String[] parts = lastTask.getTaskCode().split("-");
            if (parts.length == 3) {
                try {
                    seq = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("INS-%s-%04d", datePart, seq);
    }

    /**
     * 检查今天是否匹配周计划频率配置
     * frequency 格式：用逗号分隔的英文星期缩写，如 "Mon,Wed,Fri"
     */
    private boolean matchWeeklyDay(String frequency, LocalDate date) {
        String dayAbbr = date.getDayOfWeek().name().substring(0, 3);
        // 支持中英文星期名称
        String[] days = frequency.split(",");
        for (String day : days) {
            day = day.trim();
            if (day.equalsIgnoreCase(dayAbbr)) {
                return true;
            }
        }
        // 也支持数字格式 1-7 (周一=1, 周日=7)
        try {
            int dayNum = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
            for (String day : days) {
                day = day.trim();
                if (day.equals(String.valueOf(dayNum))) {
                    return true;
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return false;
    }
}
