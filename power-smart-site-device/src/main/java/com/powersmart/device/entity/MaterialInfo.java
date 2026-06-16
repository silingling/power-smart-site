package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("material_info")
public class MaterialInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long categoryId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String unit;

    private BigDecimal unitPrice;

    private Integer currentQuantity;

    private Integer minQuantity;

    private String location;

    private String supplier;

    private String contactPhone;

    private String remark;

    private String status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
