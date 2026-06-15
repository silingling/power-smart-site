package com.powersmart.hazard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.hazard.entity.SpecialWorkPermit;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SpecialWorkPermitMapper extends BaseMapper<SpecialWorkPermit> {

    @Select("SELECT MAX(permit_no) FROM special_work_permit WHERE permit_no LIKE CONCAT('SP-', DATE_FORMAT(NOW(), '%Y-%m-%d'), '-%')")
    String selectMaxPermitNoToday();

    @Select("SELECT * FROM special_work_permit WHERE project_id = #{projectId} AND status IN ('approved', 'active') AND is_deleted = 0")
    List<SpecialWorkPermit> selectActivePermits(Long projectId);

    @Select("SELECT COUNT(*) FROM special_work_permit WHERE project_id = #{projectId} AND status = #{status} AND is_deleted = 0")
    Long countByStatus(Long projectId, String status);
}
