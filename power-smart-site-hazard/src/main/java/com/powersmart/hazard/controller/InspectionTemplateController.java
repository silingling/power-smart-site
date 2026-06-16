package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.InspectionTemplate;
import com.powersmart.hazard.service.InspectionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/inspectionTemplate")
public class InspectionTemplateController {

    private final InspectionTemplateService inspectionTemplateService;

    @PostMapping("/list")
    public Result<PageResult<InspectionTemplate>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(inspectionTemplateService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<InspectionTemplate> get(@PathVariable Long id) {
        InspectionTemplate template = inspectionTemplateService.getById(id);
        if (template == null) {
            return Result.fail("巡检模板不存在");
        }
        return Result.ok(template);
    }

    @PostMapping("/add")
    @OperateLog(module = "巡检模板管理", action = "insert", description = "新增巡检模板", recordResult = false)
    public Result<Void> add(@RequestBody InspectionTemplate entity) {
        inspectionTemplateService.add(entity);
        return Result.ok();
    }

    @PostMapping("/set")
    @OperateLog(module = "巡检模板管理", action = "update", description = "修改巡检模板")
    public Result<Void> set(@RequestBody InspectionTemplate entity) {
        inspectionTemplateService.update(entity);
        return Result.ok();
    }

    @PostMapping("/del/{id}")
    @OperateLog(module = "巡检模板管理", action = "delete", description = "删除巡检模板 #{{id}}", targetId = "{{id}}")
    public Result<Void> del(@PathVariable Long id) {
        inspectionTemplateService.delete(id);
        return Result.ok();
    }
}
