package com.powersmart.progress.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.progress.entity.ElectronicSignature;
import com.powersmart.progress.service.ElectronicSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/electronicSignature")
@RequiredArgsConstructor
public class ElectronicSignatureController {

    private final ElectronicSignatureService electronicSignatureService;

    @PostMapping("/list")
    public Result<PageResult<ElectronicSignature>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(electronicSignatureService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<ElectronicSignature> get(@PathVariable Long id) {
        return Result.ok(electronicSignatureService.getById(id));
    }

    @PostMapping("/sign")
    @OperateLog(module = "电子签名", action = "sign", description = "电子签名")
    public Result<ElectronicSignature> sign(@RequestBody ElectronicSignature entity) {
        return Result.ok(electronicSignatureService.sign(entity));
    }

    @PostMapping("/getByBiz")
    public Result<List<ElectronicSignature>> getByBiz(@RequestBody Map<String, Object> params) {
        String bizType = (String) params.get("bizType");
        Long bizId = Long.valueOf(params.get("bizId").toString());
        return Result.ok(electronicSignatureService.getByBiz(bizType, bizId));
    }

    @PostMapping("/verify/{id}")
    public Result<Map<String, Object>> verify(@PathVariable Long id) {
        return Result.ok(electronicSignatureService.verify(id));
    }
}
