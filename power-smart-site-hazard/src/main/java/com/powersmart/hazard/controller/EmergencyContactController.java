package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.EmergencyContact;
import com.powersmart.hazard.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/build/emergencyContact")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    @PostMapping("/list")
    public Result<PageResult<EmergencyContact>> list(@RequestBody Map<String, Object> params) {
        return Result.ok(emergencyContactService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<EmergencyContact> get(@PathVariable Long id) {
        return Result.ok(emergencyContactService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(module = "应急管理", action = "insert", description = "新增应急联系人")
    public Result<Void> add(@RequestBody EmergencyContact entity) {
        emergencyContactService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "应急管理", action = "update", description = "修改应急联系人")
    public Result<Void> set(@RequestBody EmergencyContact entity) {
        emergencyContactService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "应急管理", action = "delete", description = "删除应急联系人 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        emergencyContactService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getByRole")
    public Result<List<EmergencyContact>> getByRole(@RequestBody Map<String, Object> params) {
        Long projectId = params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        String role = params.get("contactRole") != null ? params.get("contactRole").toString() : null;
        return Result.ok(emergencyContactService.getByRole(projectId, role));
    }
}
