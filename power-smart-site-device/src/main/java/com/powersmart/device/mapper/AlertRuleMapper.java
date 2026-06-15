package com.powersmart.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.device.entity.AlertRule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AlertRuleMapper extends BaseMapper<AlertRule> {

    /** 查询所有启用的告警规则（含通用规则） */
    @Select("SELECT * FROM alert_rule WHERE enabled = 1 AND is_deleted = 0")
    List<AlertRule> selectEnabledRules();

    /** 查询匹配指定设备类型的告警规则 */
    @Select("SELECT * FROM alert_rule WHERE enabled = 1 AND is_deleted = 0 " +
            "AND (device_type = #{deviceType} OR device_type IS NULL OR device_type = '')")
    List<AlertRule> selectRulesByDeviceType(String deviceType);
}
