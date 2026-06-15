package com.powersmart.hazard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powersmart.hazard.entity.ApprovalNode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    /** 按业务类型查询启用的审批节点（有序） */
    @Select("SELECT * FROM approval_node WHERE biz_type = #{bizType} AND status = 1 ORDER BY node_order ASC")
    List<ApprovalNode> selectByBizType(@Param("bizType") String bizType);

    /** 查询指定序号的下一个节点 */
    @Select("SELECT * FROM approval_node WHERE biz_type = #{bizType} AND node_order = #{nextOrder} AND status = 1")
    ApprovalNode selectByBizTypeAndOrder(@Param("bizType") String bizType, @Param("nextOrder") Integer nextOrder);
}
