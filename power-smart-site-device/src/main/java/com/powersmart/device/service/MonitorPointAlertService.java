package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.MonitorPointAlert;
import com.powersmart.device.mapper.MonitorPointAlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 监测点告警 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorPointAlertService {

    private final MonitorPointAlertMapper monitorPointAlertMapper;

    public Long selectNumber(Map<String, Object> params) {
        LambdaQueryWrapper<MonitorPointAlert> wrapper = new LambdaQueryWrapper<MonitorPointAlert>()
                .eq(MonitorPointAlert::getStatus, 0);
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(MonitorPointAlert::getProjectId, Long.valueOf(params.get("projectId").toString()));
        return monitorPointAlertMapper.selectCount(wrapper);
    }

    public List<Map<String, Object>> selectState(Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 按 status 分组统计
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        for (MonitorPointAlert alert : monitorPointAlertMapper.selectList(new LambdaQueryWrapper<>())) {
            int status = alert.getStatus() != null ? alert.getStatus() : 0;
            countMap.merge(status, 1L, Long::sum);
        }
        for (Map.Entry<Integer, Long> entry : countMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    public PageResult<MonitorPointAlert> selectUntreatedDataList(Map<String, Object> params) {
        Page<MonitorPointAlert> page = PageHelper.of(params);
        LambdaQueryWrapper<MonitorPointAlert> wrapper = new LambdaQueryWrapper<MonitorPointAlert>()
                .eq(MonitorPointAlert::getStatus, 0);
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(MonitorPointAlert::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.orderByDesc(MonitorPointAlert::getCreateTime);
        return PageResult.from(monitorPointAlertMapper.selectPage(page, wrapper));
    }
}
