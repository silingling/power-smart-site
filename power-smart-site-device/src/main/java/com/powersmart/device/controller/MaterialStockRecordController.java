package com.powersmart.device.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.MaterialStockRecord;
import com.powersmart.device.service.MaterialStockRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/materialStockRecord")
public class MaterialStockRecordController {

    private final MaterialStockRecordService materialStockRecordService;

    @PostMapping("/list")
    public Result<PageResult<MaterialStockRecord>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(materialStockRecordService.list(params));
    }

    @PostMapping("/stockIn")
    @OperateLog(action = "stockIn", description = "入库")
    public Result<Void> stockIn(@RequestBody MaterialStockRecord entity) {
        return materialStockRecordService.stockIn(entity);
    }

    @PostMapping("/stockOut")
    @OperateLog(action = "stockOut", description = "出库")
    public Result<Void> stockOut(@RequestBody MaterialStockRecord entity) {
        return materialStockRecordService.stockOut(entity);
    }

    @PostMapping("/stockReturn")
    @OperateLog(action = "stockReturn", description = "退库")
    public Result<Void> stockReturn(@RequestBody MaterialStockRecord entity) {
        return materialStockRecordService.stockReturn(entity);
    }

    @PostMapping("/getByMaterial/{materialId}")
    public Result<List<MaterialStockRecord>> getByMaterial(@PathVariable Long materialId) {
        return Result.ok(materialStockRecordService.getByMaterial(materialId));
    }
}
