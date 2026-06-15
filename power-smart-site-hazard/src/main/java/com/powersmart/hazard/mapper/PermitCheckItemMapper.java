package com.powersmart.hazard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.hazard.entity.PermitCheckItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermitCheckItemMapper extends BaseMapper<PermitCheckItem> {

    @Select("SELECT * FROM permit_check_item WHERE permit_type = #{permitType} AND enabled = 1 ORDER BY sort_order ASC")
    List<PermitCheckItem> selectByPermitType(String permitType);

    @Select("SELECT DISTINCT permit_type FROM permit_check_item WHERE enabled = 1 ORDER BY permit_type")
    List<String> selectDistinctPermitTypes();
}
