package com.powersmart.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.device.entity.TransmissionTower;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TransmissionTowerMapper extends BaseMapper<TransmissionTower> {

    @Select("SELECT id, tower_code, tower_name, tower_type FROM transmission_tower " +
            "WHERE project_id = #{projectId} AND is_deleted = 0 AND status != 'retired' " +
            "ORDER BY tower_code")
    List<TransmissionTower> selectActiveTowers(Long projectId);
}
