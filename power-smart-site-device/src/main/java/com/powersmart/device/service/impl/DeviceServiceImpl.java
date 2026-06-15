package com.powersmart.device.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.device.entity.Device;
import com.powersmart.device.mapper.DeviceMapper;
import com.powersmart.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    @Override
    public List<Device> getByProject(Long projectId, String deviceType, Integer status) {
        LambdaQueryWrapper<Device> qw = new LambdaQueryWrapper<Device>()
                .eq(Device::getProjectId, projectId);
        if (deviceType != null) qw.eq(Device::getDeviceType, deviceType);
        if (status != null) qw.eq(Device::getStatus, status);
        return list(qw);
    }

    @Override
    public List<Device> getMaintenanceDue(int aheadDays) {
        LocalDate deadline = LocalDate.now().plusDays(aheadDays);
        return list(new LambdaQueryWrapper<Device>()
                .le(Device::getNextMaintenanceDate, deadline)
                .or()
                .le(Device::getNextInspectionDate, deadline));
    }
}
