package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("material_check_item")
public class MaterialCheckItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private Long materialId;

    private String materialName;

    private String specification;

    private String unit;

    private BigDecimal bookQuantity;

    private BigDecimal actualQuantity;

    private BigDecimal difference;

    private BigDecimal unitPrice;

    private BigDecimal differenceAmount;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
