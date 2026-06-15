package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.VideoMonitor;
import com.powersmart.device.mapper.VideoMonitorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 视频监控管理 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoMonitorService {

    private final VideoMonitorMapper videoMonitorMapper;

    public Map<String, String> getAccessToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        return Map.of(
                "accessToken", token,
                "expireTime", "7200"
        );
    }

    public List<VideoMonitor> queryByParentId(Long parentId) {
        LambdaQueryWrapper<VideoMonitor> wrapper = new LambdaQueryWrapper<VideoMonitor>()
                .eq(VideoMonitor::getLocationId, parentId)
                .orderByDesc(VideoMonitor::getCreatedAt);
        return videoMonitorMapper.selectList(wrapper);
    }

    public void add(VideoMonitor entity) {
        videoMonitorMapper.insert(entity);
    }

    public void edit(VideoMonitor entity) {
        videoMonitorMapper.updateById(entity);
    }

    public void delete(Long id) {
        videoMonitorMapper.deleteById(id);
    }
}
