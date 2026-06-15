package com.powersmart.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.system.entity.PmsSyncLog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PmsSyncLogMapper extends BaseMapper<PmsSyncLog> {
    @Select("SELECT * FROM pms_sync_log WHERE status = 'running' ORDER BY created_at DESC LIMIT 5")
    List<PmsSyncLog> selectRunningLogs();

    @Select("SELECT entity_type, COUNT(*) total, SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) fails FROM pms_sync_log WHERE created_at > DATE_SUB(NOW(), INTERVAL 7 DAY) GROUP BY entity_type")
    List<java.util.Map<String, Object>> selectWeekStats();
}
