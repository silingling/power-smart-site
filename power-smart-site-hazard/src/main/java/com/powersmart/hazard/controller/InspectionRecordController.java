package com.powersmart.hazard.controller;

import com.alibaba.fastjson2.JSON;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.InspectionRecord;
import com.powersmart.hazard.service.InspectionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/inspectionRecord")
public class InspectionRecordController {

    private final InspectionRecordService inspectionRecordService;

    @PostMapping("/list")
    public Result<PageResult<InspectionRecord>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(inspectionRecordService.list(params));
    }

    @PostMapping("/submitRecords")
    @OperateLog(module = "巡检记录管理", action = "insert", description = "批量提交巡检记录", recordResult = false)
    public Result<Void> submitRecords(@RequestBody Map<String, Object> params) {
        Long taskId = params.get("taskId") != null
                ? Long.valueOf(params.get("taskId").toString()) : null;
        if (taskId == null) {
            return Result.fail("任务ID不能为空");
        }

        List<InspectionRecord> records;
        Object recordsObj = params.get("records");
        if (recordsObj instanceof List) {
            records = JSON.parseArray(JSON.toJSONString(recordsObj), InspectionRecord.class);
        } else {
            return Result.fail("巡检记录参数格式错误");
        }

        inspectionRecordService.submitRecords(taskId, records);
        return Result.ok();
    }

    @PostMapping("/getByTask/{taskId}")
    public Result<List<InspectionRecord>> getByTask(@PathVariable Long taskId) {
        return Result.ok(inspectionRecordService.getByTask(taskId));
    }
}
