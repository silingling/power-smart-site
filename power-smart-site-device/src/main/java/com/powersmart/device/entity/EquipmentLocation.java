package com.powersmart.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备位置台账树（树形结构）
 */
@Data
@TableName("equipment_location")
public class EquipmentLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long parentId;             // 父节点ID，0=根节点
    private String locationName;       // 位置名称 e.g. "1号塔吊区" / "A区-配电室"
    private String locationType;       // 区域/点位
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
