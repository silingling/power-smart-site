package com.powersmart.progress.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.progress.entity.ProgressTask;
import com.powersmart.progress.entity.ProgressReport;

import java.util.List;

public interface ProgressTaskService extends IService<ProgressTask> {
    /**
     * 获取项目工序树（按层级组织）
     */
    List<ProgressTask> getTaskTree(Long projectId);

    /**
     * 进度偏差分析：返回滞后的工序
     */
    List<ProgressTask> getDelayedTasks(Long projectId);
}
