package com.powersmart.hazard.controller;

import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.ConstructionArea;
import com.powersmart.hazard.service.ConstructionAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build/constructionArea")
public class ConstructionAreaController {

    private final ConstructionAreaService constructionAreaService;

    @PostMapping("/list")
    public Result<PageResult<ConstructionArea>> list(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(constructionAreaService.list(params));
    }

    @PostMapping("/get/{id}")
    public Result<ConstructionArea> getById(@PathVariable Long id) {
        return Result.ok(constructionAreaService.getById(id));
    }

    @OperateLog(description = "新增作业区域")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ConstructionArea entity) {
        constructionAreaService.add(entity);
        return Result.ok();
    }

    @OperateLog(description = "修改作业区域")
    @PostMapping("/set")
    public Result<Void> update(@RequestBody ConstructionArea entity) {
        constructionAreaService.update(entity);
        return Result.ok();
    }

    @OperateLog(description = "删除作业区域")
    @PostMapping("/del/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        constructionAreaService.delete(id);
        return Result.ok();
    }

    @PostMapping("/getActiveByProject/{projectId}")
    public Result<List<ConstructionArea>> getActiveByProject(@PathVariable Long projectId) {
        return Result.ok(constructionAreaService.getActiveByProject(projectId));
    }
}
