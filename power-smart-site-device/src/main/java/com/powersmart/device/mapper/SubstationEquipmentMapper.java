package com.powersmart.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.device.entity.SubstationEquipment;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SubstationEquipmentMapper extends BaseMapper<SubstationEquipment> {

    @Select("SELECT DISTINCT device_type FROM substation_equipment WHERE project_id = #{projectId} AND is_deleted = 0")
    List<String> selectDistinctDeviceTypes(Long projectId);

    @Select("SELECT * FROM substation_equipment WHERE device_type = #{deviceType} AND is_deleted = 0 AND status != 'retired'")
    List<SubstationEquipment> selectActiveByType(String deviceType);
}
