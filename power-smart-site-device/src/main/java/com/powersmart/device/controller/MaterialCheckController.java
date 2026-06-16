package com.powersmart.device.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.MaterialCheck;
import com.powersmart.device.entity.MaterialCheckItem;
import com.powersmart.device.service.MaterialCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/materialCheck")
public class MaterialCheckController {

    private final MaterialCheckService materialCheckService;

    @PostMapping("/list")
    public Result<PageResult<MaterialCheck>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(materialCheckService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<MaterialCheck> getById(@PathVariable Long id) {
        return Result.ok(materialCheckService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(action = "add", description = "新增盘点")
    public Result<Void> add(@RequestBody MaterialCheck entity) {
        return materialCheckService.add(entity);
    }

    @PostMapping("/set")
    @OperateLog(action = "update", description = "更新盘点")
    public Result<Void> set(@RequestBody MaterialCheck entity) {
        return materialCheckService.update(entity);
    }

    @PostMapping("/del/{id}")
    @OperateLog(action = "delete", description = "删除盘点")
    public Result<Void> del(@PathVariable Long id) {
        return materialCheckService.delete(id);
    }

    @PostMapping("/submit")
    @OperateLog(action = "submit", description = "提交盘点单")
    public Result<Void> submit(@RequestBody Map<String, Object> params) {
        Long checkId = params != null && params.get("checkId") != null
                ? Long.valueOf(params.get("checkId").toString()) : null;
        if (checkId == null) {
            return Result.fail("checkId不能为空");
        }
        return materialCheckService.submit(checkId);
    }

    @PostMapping("/submitCheckItems")
    @OperateLog(action = "submitCheckItems", description = "提交盘点明细")
    public Result<Void> submitCheckItems(@RequestBody Map<String, Object> params) {
        Long checkId = params != null && params.get("checkId") != null
                ? Long.valueOf(params.get("checkId").toString()) : null;
        if (checkId == null) {
            return Result.fail("checkId不能为空");
        }
        Object itemsObj = params != null ? params.get("items") : null;
        if (!(itemsObj instanceof List)) {
            return Result.fail("items不能为空");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) itemsObj;
        List<MaterialCheckItem> items = itemMaps.stream().map(m -> {
            MaterialCheckItem item = new MaterialCheckItem();
            item.setMaterialId(m.get("materialId") != null ? Long.valueOf(m.get("materialId").toString()) : null);
            item.setMaterialName(m.get("materialName") != null ? m.get("materialName").toString() : null);
            item.setSpecification(m.get("specification") != null ? m.get("specification").toString() : null);
            item.setUnit(m.get("unit") != null ? m.get("unit").toString() : null);
            item.setBookQuantity(m.get("bookQuantity") != null
                    ? new java.math.BigDecimal(m.get("bookQuantity").toString()) : null);
            item.setActualQuantity(m.get("actualQuantity") != null
                    ? new java.math.BigDecimal(m.get("actualQuantity").toString()) : null);
            item.setUnitPrice(m.get("unitPrice") != null
                    ? new java.math.BigDecimal(m.get("unitPrice").toString()) : null);
            item.setRemark(m.get("remark") != null ? m.get("remark").toString() : null);
            return item;
        }).collect(java.util.stream.Collectors.toList());
        return materialCheckService.submitCheckItems(checkId, items);
    }

    @PostMapping("/approveCheck")
    @OperateLog(action = "approveCheck", description = "审批盘点")
    public Result<Void> approveCheck(@RequestBody Map<String, Object> params) {
        Long checkId = params != null && params.get("checkId") != null
                ? Long.valueOf(params.get("checkId").toString()) : null;
        if (checkId == null) {
            return Result.fail("checkId不能为空");
        }
        return materialCheckService.approveCheck(checkId);
    }

    @PostMapping("/getCheckItems/{checkId}")
    public Result<List<MaterialCheckItem>> getCheckItems(@PathVariable Long checkId) {
        return Result.ok(materialCheckService.getCheckItems(checkId));
    }
}
