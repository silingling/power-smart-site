package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单 / 权限点
 *
 * menu_type: 1-目录 2-菜单 3-按钮/权限点
 * permission_key: 如 "build:safetyMaterial:list"
 */
@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private String permissionKey;
    private String path;
    private String icon;
    private Integer menuType;     // 1-目录 2-菜单 3-按钮
    private Integer visible;      // 1-显示 0-隐藏
    private Integer sortOrder;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非持久化：子菜单树 */
    @TableField(exist = false)
    private List<SysMenu> children;
}
