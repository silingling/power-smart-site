package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.SupplyPoint;
import com.powersmart.device.mapper.SupplyPointMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 水电供应点管理 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyPointService {

    private final SupplyPointMapper supplyPointMapper;

    public PageResult<SupplyPoint> list(Map<String, Object> params) {
        Page<SupplyPoint> page = PageHelper.of(params);
        LambdaQueryWrapper<SupplyPoint> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(SupplyPoint::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("pointType") && params.get("pointType") != null && StrUtil.isNotBlank(params.get("pointType").toString()))
                wrapper.eq(SupplyPoint::getPointType, params.get("pointType").toString());
        }
        wrapper.orderByDesc(SupplyPoint::getCreateTime);
        return PageResult.from(supplyPointMapper.selectPage(page, wrapper));
    }

    public void add(SupplyPoint entity) {
        supplyPointMapper.insert(entity);
    }

    public void edit(SupplyPoint entity) {
        supplyPointMapper.updateById(entity);
    }

    public void delete(Long id) {
        supplyPointMapper.deleteById(id);
    }

    public Map<String, Object> waterRealTimeData(Long pointId) {
        SupplyPoint point = supplyPointMapper.selectById(pointId);
        Map<String, Object> data = new LinkedHashMap<>();
        if (point != null) {
            data.put("pointId", point.getId());
            data.put("pointName", point.getPointName());
            data.put("currentReading", point.getCurrentReading());
        } else {
            data.put("pointId", pointId);
            data.put("currentReading", 0);
        }
        data.put("updateTime", new Date());
        return data;
    }

    public List<Map<String, Object>> getHistoryCurveData(Map<String, Object> params) {
        // TODO: 实现历史曲线数据查询
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getHistoryReportData(Map<String, Object> params) {
        // TODO: 实现历史报表数据查询
        return new ArrayList<>();
    }
}
