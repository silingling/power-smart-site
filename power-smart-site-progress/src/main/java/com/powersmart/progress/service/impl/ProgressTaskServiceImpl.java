package com.powersmart.progress.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.progress.entity.ProgressTask;
import com.powersmart.progress.mapper.ProgressTaskMapper;
import com.powersmart.progress.service.ProgressTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressTaskServiceImpl extends ServiceImpl<ProgressTaskMapper, ProgressTask> implements ProgressTaskService {

    @Override
    public List<ProgressTask> getTaskTree(Long projectId) {
        return list(new LambdaQueryWrapper<ProgressTask>()
                .eq(ProgressTask::getProjectId, projectId)
                .orderByAsc(ProgressTask::getSortOrder));
    }

    @Override
    public List<ProgressTask> getDelayedTasks(Long projectId) {
        // 已超计划结束日期且未完成的工序
        return list(new LambdaQueryWrapper<ProgressTask>()
                .eq(ProgressTask::getProjectId, projectId)
                .in(ProgressTask::getStatus, 0, 1)   // 未开始或进行中
                .lt(ProgressTask::getPlanEndDate, LocalDate.now()));
    }
}
