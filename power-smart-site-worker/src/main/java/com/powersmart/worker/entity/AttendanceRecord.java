package com.powersmart.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员考勤记录
 */
@Data
@TableName("attendance_record")
public class AttendanceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;
    private Long projectId;
    private LocalDate attendDate;
    private String checkInTime;       // HH:mm 签到时间
    private String checkOutTime;      // HH:mm 签退时间
    private String attendType;        // 人脸/刷卡/手动
    private Integer status;           // 1-正常 2-迟到 3-早退 4-缺勤

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
