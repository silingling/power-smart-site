privatepackage com.powersmart.hazard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全资料目录（树形）
 */
@Data
@TableName("safety_material_catalog")
public class SafetyMaterialCatalog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long parentId;              // 上级目录ID
    private String name;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
