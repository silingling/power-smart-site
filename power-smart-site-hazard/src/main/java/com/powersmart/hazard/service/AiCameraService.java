package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.AiCamera;
import com.powersmart.hazard.mapper.AiCameraMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 摄像头 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCameraService {

    private final AiCameraMapper aiCameraMapper;

    public void add(AiCamera camera) {
        camera.setStatus(0); // 默认离线
        camera.setCreatedAt(LocalDateTime.now());
        aiCameraMapper.insert(camera);
    }

    public void edit(AiCamera camera) {
        camera.setUpdatedAt(LocalDateTime.now());
        aiCameraMapper.updateById(camera);
    }

    public void delete(Long id) {
        aiCameraMapper.deleteById(id);
    }

    public AiCamera getById(Long id) {
        return aiCameraMapper.selectById(id);
    }

    public PageResult<AiCamera> queryPage(Map<String, Object> params) {
        Page<AiCamera> page = PageHelper.of(params);
        LambdaQueryWrapper<AiCamera> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(AiCamera::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("status") && params.get("status") != null)
                wrapper.eq(AiCamera::getStatus, Integer.valueOf(params.get("status").toString()));
            if (params.containsKey("cameraName") && params.get("cameraName") != null && StrUtil.isNotBlank(params.get("cameraName").toString()))
                wrapper.like(AiCamera::getCameraName, params.get("cameraName").toString());
            if (params.containsKey("cameraType") && params.get("cameraType") != null && StrUtil.isNotBlank(params.get("cameraType").toString()))
                wrapper.eq(AiCamera::getCameraType, params.get("cameraType").toString());
        }
        wrapper.orderByDesc(AiCamera::getCreatedAt);
        return PageResult.from(aiCameraMapper.selectPage(page, wrapper));
    }

    public List<AiCamera> getByProject(Long projectId) {
        return aiCameraMapper.selectList(
                new LambdaQueryWrapper<AiCamera>()
                        .eq(AiCamera::getProjectId, projectId)
                        .orderByDesc(AiCamera::getCreatedAt));
    }

    public void updateStatus(Long id, Integer status) {
        AiCamera camera = aiCameraMapper.selectById(id);
        if (camera != null) {
            camera.setStatus(status);
            if (status == 1) {
                camera.setLastHeartbeat(LocalDateTime.now());
            }
            camera.setUpdatedAt(LocalDateTime.now());
            aiCameraMapper.updateById(camera);
        }
    }
}
