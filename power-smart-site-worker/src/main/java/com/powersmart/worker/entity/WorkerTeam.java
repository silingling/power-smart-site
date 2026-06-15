package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 施工班组
 */
@Data
@TableName("worker_team")
public class WorkerTeam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String teamName;
    private String leaderName;
    private String leaderPhone;
    private String workType;        // 班组工种（电气安装/土建/调试等）
    private Integer memberCount;
    private Integer status;         // 1-正常 0-解散
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
