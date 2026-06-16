package com.powersmart.device.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.MaterialCategory;
import com.powersmart.device.service.MaterialCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/materialCategory")
public class MaterialCategoryController {

    private final MaterialCategoryService materialCategoryService;

    @PostMapping("/list")
    public Result<PageResult<MaterialCategory>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(materialCategoryService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<MaterialCategory> getById(@PathVariable Long id) {
        return Result.ok(materialCategoryService.getById(id));
    }

    @PostMapping("/add")
    @OperateLog(action = "add", description = "新增物料分类")
    public Result<Void> add(@RequestBody MaterialCategory entity) {
        return materialCategoryService.add(entity);
    }

    @PostMapping("/set")
    @OperateLog(action = "update", description = "更新物料分类")
    public Result<Void> set(@RequestBody MaterialCategory entity) {
        return materialCategoryService.update(entity);
    }

    @PostMapping("/del/{id}")
    @OperateLog(action = "delete", description = "删除物料分类")
    public Result<Void> del(@PathVariable Long id) {
        return materialCategoryService.delete(id);
    }

    @PostMapping("/getTree")
    public Result<List<MaterialCategory>> getTree(@RequestBody Map<String, Object> params) {
        Long projectId = params != null && params.get("projectId") != null
                ? Long.valueOf(params.get("projectId").toString()) : null;
        return Result.ok(materialCategoryService.getTree(projectId));
    }
}
