package com.powersmart.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.device.entity.SafetyFence;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SafetyFenceMapper extends BaseMapper<SafetyFence> {

    @Select("SELECT * FROM safety_fence WHERE project_id = #{projectId} AND enabled = 1 AND is_deleted = 0")
    List<SafetyFence> selectActiveByProject(Long projectId);

    @Select("SELECT * FROM safety_fence WHERE enabled = 1 AND is_deleted = 0")
    List<SafetyFence> selectAllActive();
}
