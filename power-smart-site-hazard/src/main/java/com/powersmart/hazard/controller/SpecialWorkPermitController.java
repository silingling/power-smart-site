package com.powersmart.hazard.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.PermitCheckItem;
import com.powersmart.hazard.entity.SpecialWorkPermit;
import com.powersmart.hazard.service.SpecialWorkPermitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 特种作业票管理 — /build/specialWorkPermit/*
 *
 * <p>完整生命周期：草稿→提交→审核→签发→作业→完工→归档</p>
 */
@RestController
@RequiredArgsConstructor
public class SpecialWorkPermitController {

    private final SpecialWorkPermitService permitService;

    // ==================== CRUD ====================

    /** 分页查询作业票列表 */
    @PostMapping("/build/specialWorkPermit/querySpecialWorkPermitList")
    public Result<PageResult<Map<String, Object>>> queryList(@RequestBody(required = false) Map<String, Object> params) {
        Page<SpecialWorkPermit> page = permitService.queryPage(params);
        List<Map<String, Object>> list = page.getRecords().stream()
                .map(permitService::toMap).collect(Collectors.toList());
        return Result.ok(PageResult.of(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    /** 获取作业票详情 */
    @PostMapping("/build/specialWorkPermit/getSpecialWorkPermit/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        SpecialWorkPermit wp = permitService.getById(id);
        if (wp == null) return Result.fail("作业票不存在");
        return Result.ok(permitService.toMap(wp));
    }

    /** 新增作业票（草稿） */
    @PostMapping("/build/specialWorkPermit/addSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "insert", description = "新增作业票")
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> params) {
        SpecialWorkPermit wp = permitService.buildFromParams(null, params);
        permitService.add(wp);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", wp.getId());
        result.put("permitNo", wp.getPermitNo());
        return Result.ok(result);
    }

    /** 更新作业票 */
    @PostMapping("/build/specialWorkPermit/setSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "update", description = "修改作业票")
    public Result<Void> set(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        SpecialWorkPermit existing = permitService.getById(id);
        if (existing == null) return Result.fail("作业票不存在");
        permitService.update(permitService.buildFromParams(existing, params));
        return Result.ok();
    }

    /** 删除作业票 */
    @PostMapping("/build/specialWorkPermit/delSpecialWorkPermit/{id}")
    @OperateLog(module = "特种作业票", action = "delete", description = "删除作业票 #{{id}}", targetId = "{{id}}")
    public Result<Void> delete(@PathVariable Long id) {
        permitService.delete(id);
        return Result.ok();
    }

    // ==================== 状态流转 ====================

    /** 提交审批: draft → submitted */
    @PostMapping("/build/specialWorkPermit/submitSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "submit", description = "提交作业票审批")
    public Result<Void> submit(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        return permitService.submit(id);
    }

    /** 审核通过/签发: submitted → approved */
    @PostMapping("/build/specialWorkPermit/approveSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "approve", description = "审核通过作业票")
    public Result<Void> approve(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        String issuerName = params.containsKey("issuerName") ? params.get("issuerName").toString() : null;
        String issuerSignature = params.containsKey("issuerSignature") ? params.get("issuerSignature").toString() : null;
        if (id == null) return Result.fail("id 不能为空");
        return permitService.approve(id, issuerName, issuerSignature);
    }

    /** 驳回: submitted → rejected */
    @PostMapping("/build/specialWorkPermit/rejectSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "reject", description = "驳回作业票")
    public Result<Void> reject(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        String reason = params.containsKey("reason") ? params.get("reason").toString() : "";
        if (id == null) return Result.fail("id 不能为空");
        return permitService.reject(id, reason);
    }

    /** 开始作业: approved → active */
    @PostMapping("/build/specialWorkPermit/startSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "start", description = "开始作业")
    public Result<Void> startWork(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        if (id == null) return Result.fail("id 不能为空");
        return permitService.startWork(id);
    }

    /** 完工: active → completed */
    @PostMapping("/build/specialWorkPermit/completeSpecialWorkPermit")
    @OperateLog(module = "特种作业票", action = "complete", description = "完成作业")
    public Result<Void> complete(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        String note = params.containsKey("completionNote") ? params.get("completionNote").toString() : null;
        String closerName = params.containsKey("closerName") ? params.get("closerName").toString() : null;
        String closerSignature = params.containsKey("closerSignature") ? params.get("closerSignature").toString() : null;
        if (id == null) return Result.fail("id 不能为空");
        return permitService.complete(id, note, closerName, closerSignature);
    }

    /** 归档: completed → closed */
    @PostMapping("/build/specialWorkPermit/closeSpecialWorkPermit")
    public Result<Void> close(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        String closerName = params.containsKey("closerName") ? params.get("closerName").toString() : null;
        if (id == null) return Result.fail("id 不能为空");
        return permitService.close(id, closerName);
    }

    /** 作废 */
    @PostMapping("/build/specialWorkPermit/cancelSpecialWorkPermit")
    public Result<Void> cancel(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        String reason = params.containsKey("reason") ? params.get("reason").toString() : "";
        if (id == null) return Result.fail("id 不能为空");
        return permitService.cancel(id, reason);
    }

    // ==================== 延期 ====================

    /** 作业延期 */
    @PostMapping("/build/specialWorkPermit/extendSpecialWorkPermit")
    public Result<Void> extend(@RequestBody Map<String, Object> params) {
        Long id = safeLong(params.get("id"));
        LocalDateTime newEndTime = params.containsKey("newEndTime")
                ? LocalDateTime.parse(params.get("newEndTime").toString()) : null;
        String reason = params.containsKey("reason") ? params.get("reason").toString() : "";
        if (id == null || newEndTime == null) return Result.fail("id 和 newEndTime 不能为空");
        return permitService.extendPermit(id, newEndTime, reason);
    }

    // ==================== 查询/统计 ====================

    /** 获取作业票类型列表 */
    @PostMapping("/build/specialWorkPermit/getPermitTypeList")
    public Result<List<Map<String, Object>>> getPermitTypes() {
        List<Map<String, Object>> list = SpecialWorkPermitService.PERMIT_TYPE_NAMES.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", e.getKey());
                    m.put("name", e.getValue());
                    return m;
                }).collect(Collectors.toList());
        return Result.ok(list);
    }

    /** 获取某项目的作业票状态统计 */
    @PostMapping("/build/specialWorkPermit/getPermitStatusStats")
    public Result<Map<String, Long>> getStatusStats(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        if (projectId == null) return Result.fail("projectId 不能为空");
        return Result.ok(permitService.getStatusStats(projectId));
    }

    /** 获取某项目进行中的作业票 */
    @PostMapping("/build/specialWorkPermit/getActivePermitList")
    public Result<List<Map<String, Object>>> getActiveList(@RequestBody Map<String, Object> params) {
        Long projectId = safeLong(params.get("projectId"));
        if (projectId == null) return Result.fail("projectId 不能为空");
        List<SpecialWorkPermit> list = permitService.getActivePermits(projectId);
        return Result.ok(list.stream().map(permitService::toMap).collect(Collectors.toList()));
    }

    // ==================== 检查项管理 ====================

    /** 获取检查项列表（可按类型过滤） */
    @PostMapping("/build/specialWorkPermit/getPermitCheckItems")
    public Result<List<PermitCheckItem>> getCheckItems(@RequestBody(required = false) Map<String, Object> params) {
        String permitType = params != null && params.containsKey("permitType")
                ? params.get("permitType").toString() : null;
        return Result.ok(permitService.getCheckItems(permitType));
    }

    /** 新增检查项 */
    @PostMapping("/build/specialWorkPermit/addPermitCheckItem")
    public Result<Void> addCheckItem(@RequestBody Map<String, Object> params) {
        PermitCheckItem item = new PermitCheckItem();
        item.setPermitType(safeStr(params.get("permitType")));
        item.setItemName(safeStr(params.get("itemName")));
        item.setItemCategory(safeStr(params.get("itemCategory"), "measure"));
        item.setRequired(params.containsKey("required") ? safeInt(params.get("required")) : 1);
        item.setSortOrder(params.containsKey("sortOrder") ? safeInt(params.get("sortOrder")) : 0);
        item.setEnabled(1);
        permitService.addCheckItem(item);
        return Result.ok();
    }

    // ==================== 帮助方法 ====================

    private Long safeLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private Integer safeInt(Object v) {
        if (v == null) return null;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }
    private String safeStr(Object v) {
        return v != null && !v.toString().isEmpty() ? v.toString() : null;
    }
    private String safeStr(Object v, String def) {
        String s = safeStr(v);
        return s != null ? s : def;
    }
}
