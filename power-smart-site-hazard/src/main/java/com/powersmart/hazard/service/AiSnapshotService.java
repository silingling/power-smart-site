package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.AiSnapshot;
import com.powersmart.hazard.mapper.AiSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSnapshotService {

    private final AiSnapshotMapper aiSnapshotMapper;

    public PageResult<AiSnapshot> list(Map<String, Object> params) {
        Page<AiSnapshot> page = PageHelper.of(params);
        LambdaQueryWrapper<AiSnapshot> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            Object projectId = params.get("projectId");
            if (projectId != null)
                wrapper.eq(AiSnapshot::getProjectId, Long.valueOf(projectId.toString()));
            Object cameraId = params.get("cameraId");
            if (cameraId != null)
                wrapper.eq(AiSnapshot::getCameraId, Long.valueOf(cameraId.toString()));
            Object violationType = params.get("violationType");
            if (violationType != null)
                wrapper.eq(AiSnapshot::getViolationType, violationType.toString());
        }
        wrapper.orderByDesc(AiSnapshot::getCreatedAt);
        return PageResult.from(aiSnapshotMapper.selectPage(page, wrapper));
    }

    public AiSnapshot getById(Long id) {
        return aiSnapshotMapper.selectById(id);
    }
}
