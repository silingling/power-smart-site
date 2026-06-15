package com.powersmart.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.SafetyFence;
import com.powersmart.device.service.SafetyFenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 安全围栏管理 — /build/safetyFence/*
 *
 * <p>围栏 CRUD、启用/禁用、定位检测</p>
 */
@RestController
@RequiredArgsConstructor
public class SafetyFenceController {

    private final SafetyFenceService service;

    /** 分页查询围栏列表 */
    @PostMapping("/build/safetyFence/querySafetyFenceList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SafetyFence> page = service.queryPage(params);
        List<Map<String, Object>> list = page.getRecords().stream()
                .map(service::toMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 获取围栏详情 */
    @PostMapping("/build/safetyFence/getSafetyFence/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        SafetyFence fence = service.getById(id);
        if (fence == null) return Result.fail("围栏不存在");
        return Result.ok(service.toMap(fence));
    }

    /** 新增围栏 */
    @PostMapping("/build/safetyFence/addSafetyFence")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        SafetyFence fence = service.buildFromParams(null, params);
        service.add(fence);
        return Result.ok();
    }

    /** 更新围栏 */
    @PostMapping("/build/safetyFence/setSafetyFence")
    public Result<Void> set(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        SafetyFence existing = service.getById(id);
        if (existing == null) return Result.fail("围栏不存在");
        service.update(service.buildFromParams(existing, params));
        return Result.ok();
    }

    /** 删除围栏 */
    @PostMapping("/build/safetyFence/delSafetyFence/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    /** 启用/禁用围栏 */
    @PostMapping("/build/safetyFence/setSafetyFenceEnabled")
    public Result<Void> setEnabled(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        Integer enabled = params.containsKey("enabled") ? Integer.parseInt(params.get("enabled").toString()) : null;
        if (id == null || enabled == null) return Result.fail("id 和 enabled 不能为空");
        service.toggleEnabled(id, enabled);
        return Result.ok();
    }

    /** 查询某项目下所有启用的围栏 */
    @PostMapping("/build/safetyFence/getActiveFenceList")
    public Result<List<Map<String, Object>>> getActiveList(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        if (projectId == null) return Result.fail("projectId 不能为空");
        List<SafetyFence> fences = service.getActiveFences(projectId);
        List<Map<String, Object>> list = fences.stream().map(service::toMap).collect(Collectors.toList());
        return Result.ok(list);
    }

    /** 检查定位点是否在围栏内（巡检/定位检测用） */
    @PostMapping("/build/safetyFence/checkPointInFences")
    public Result<Map<String, Object>> checkPoint(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        BigDecimal lat = params.containsKey("lat") ? new BigDecimal(params.get("lat").toString()) : null;
        BigDecimal lng = params.containsKey("lng") ? new BigDecimal(params.get("lng").toString()) : null;
        if (projectId == null || lat == null || lng == null)
            return Result.fail("projectId, lat, lng 不能为空");
        return Result.ok(service.checkPointInFences(projectId, lat, lng));
    }

    // ==================== 帮助方法 ====================

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
