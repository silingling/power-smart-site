package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 评价等级（对应tongye build/evalLevel）
 */
@Data
@TableName("eval_level")
public class EvalLevel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String levelName;
    private String levelType;
    private BigDecimal scoreMin;
    private BigDecimal scoreMax;
    private String color;
    private String remark;
    private Integer isDeleted;
    private LocalDateTime createTime;
}
