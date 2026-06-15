package com.powersmart.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.device.entity.FenceAlertEvent;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FenceAlertEventMapper extends BaseMapper<FenceAlertEvent> {

    @Select("SELECT * FROM fence_alert_event WHERE fence_id = #{fenceId} AND target_id = #{targetId} AND event_type = #{eventType} AND status = 'pending'")
    List<FenceAlertEvent> selectPendingForTarget(Long fenceId, String targetId, String eventType);

    @Select("SELECT * FROM fence_alert_event WHERE status = 'pending' ORDER BY created_at DESC")
    List<FenceAlertEvent> selectAllPending();
}
