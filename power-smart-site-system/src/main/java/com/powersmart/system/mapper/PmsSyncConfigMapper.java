package com.powersmart.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.system.entity.PmsSyncConfig;
import org.apache.ibatis.annotations.Select;

public interface PmsSyncConfigMapper extends BaseMapper<PmsSyncConfig> {
    @Select("SELECT config_value FROM pms_sync_config WHERE config_key = #{configKey} AND enabled = 1 LIMIT 1")
    String selectConfigValue(String configKey);
}
