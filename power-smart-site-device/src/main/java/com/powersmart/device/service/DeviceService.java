package com.powersmart.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.device.entity.Device;
import java.util.List;

public interface DeviceService extends IService<Device> {
    /**
     * 根据项目ID查询设备列表
     */
    List<Device> getByProject(Long projectId, String deviceType, Integer status);

    /**
     * 查询需要年检或维保的设备
     */
    List<Device> getMaintenanceDue(int aheadDays);
}
