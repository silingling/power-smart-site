package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.EmergencySupplyRecord;
import com.powersmart.hazard.service.EmergencySupplyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/emergencySupplyRecord")
@RequiredArgsConstructor
public class EmergencySupplyRecordController {

    private final EmergencySupplyRecordService emergencySupplyRecordService;

    @PostMapping("/list")
    public Result<PageResult<EmergencySupplyRecord>> list(@RequestBody Map<String, Object> params) {
        return Result.ok(emergencySupplyRecordService.list(params));
    }

    @PostMapping("/add")
    @OperateLog(module = "应急管理", action = "insert", description = "新增出入库记录")
    public Result<Void> add(@RequestBody EmergencySupplyRecord entity) {
        emergencySupplyRecordService.add(entity);
        return Result.ok();
    }

    @PostMapping("/getBySupply/{supplyId}")
    public Result<List<EmergencySupplyRecord>> getBySupply(@PathVariable Long supplyId) {
        return Result.ok(emergencySupplyRecordService.getBySupply(supplyId));
    }
}
