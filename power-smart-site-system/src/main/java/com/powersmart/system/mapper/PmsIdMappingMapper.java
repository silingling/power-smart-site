package com.powersmart.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.system.entity.PmsIdMapping;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface PmsIdMappingMapper extends BaseMapper<PmsIdMapping> {
    @Select("SELECT pms_id FROM pms_id_mapping WHERE entity_type = #{entityType} AND local_id = #{localId} LIMIT 1")
    String selectPmsId(String entityType, String localId);

    @Select("SELECT local_id FROM pms_id_mapping WHERE entity_type = #{entityType} AND pms_id = #{pmsId} LIMIT 1")
    String selectLocalId(String entityType, String pmsId);

    @Update("UPDATE pms_id_mapping SET last_sync_time = NOW(), sync_status = 'synced' WHERE entity_type = #{entityType} AND local_id = #{localId}")
    void updateSyncTime(String entityType, String localId);
}
