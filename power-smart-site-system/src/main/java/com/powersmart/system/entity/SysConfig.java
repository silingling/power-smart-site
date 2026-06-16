package com.powersmart.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置（对应tongye adminConfig）
 */
@Data
@TableName("sys_config")
public class SysConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configName;
    private String configValue;
    private String configDesc;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
