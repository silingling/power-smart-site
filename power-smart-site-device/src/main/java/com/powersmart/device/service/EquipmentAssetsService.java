package com.powersmart.device.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.common.util.PageHelper;
import com.powersmart.device.entity.EquipmentAssets;
import com.powersmart.device.mapper.EquipmentAssetsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 设备资产管理 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentAssetsService {

    private final EquipmentAssetsMapper equipmentAssetsMapper;

    public PageResult<EquipmentAssets> list(Map<String, Object> params) {
        Page<EquipmentAssets> page = PageHelper.of(params);
        LambdaQueryWrapper<EquipmentAssets> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EquipmentAssets::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("deviceType") && params.get("deviceType") != null && StrUtil.isNotBlank(params.get("deviceType").toString()))
                wrapper.eq(EquipmentAssets::getDeviceType, params.get("deviceType").toString());
        }
        wrapper.orderByDesc(EquipmentAssets::getCreateTime);
        return PageResult.from(equipmentAssetsMapper.selectPage(page, wrapper));
    }

    public List<EquipmentAssets> selectVideoMonitor(Map<String, Object> params) {
        LambdaQueryWrapper<EquipmentAssets> wrapper = new LambdaQueryWrapper<EquipmentAssets>()
                .gt(EquipmentAssets::getVideoMonitorId, 0);
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(EquipmentAssets::getProjectId, Long.valueOf(params.get("projectId").toString()));
        return equipmentAssetsMapper.selectList(wrapper);
    }

    public EquipmentAssets getDeviceId(Long id) {
        return equipmentAssetsMapper.selectById(id);
    }

    public void add(EquipmentAssets entity) {
        equipmentAssetsMapper.insert(entity);
    }

    public void edit(EquipmentAssets entity) {
        equipmentAssetsMapper.updateById(entity);
    }

    public void delete(Long id) {
        equipmentAssetsMapper.deleteById(id);
    }
}
