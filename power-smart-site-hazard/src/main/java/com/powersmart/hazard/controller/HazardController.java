package com.powersmart.hazard.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.HazardReport;
import com.powersmart.hazard.entity.HazardWorkOrder;
import com.powersmart.hazard.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hazards")
@RequiredArgsConstructor
public class HazardController {

    private final HazardService hazardService;

    @PostMapping
    public Result<HazardReport> create(@RequestBody HazardReport report,
                                       @RequestParam(required = false) Long assigneeId) {
        return Result.ok(hazardService.reportHazard(report, assigneeId));
    }

    @GetMapping
    public Result<List<HazardReport>> list(
            @RequestParam Long projectId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(hazardService.lambdaQuery()
                .eq(HazardReport::getProjectId, projectId)
                .eq(status != null, HazardReport::getStatus, status)
                .orderByDesc(HazardReport::getCreatedAt)
                .list());
    }

    @GetMapping("/{id}")
    public Result<HazardReport> getById(@PathVariable Long id) {
        return Result.ok(hazardService.getById(id));
    }

    @PostMapping("/{id}/work-order")
    public Result<HazardWorkOrder> createWorkOrder(
            @PathVariable Long id,
            @RequestParam Long assigneeId,
            @RequestParam(defaultValue = "4") int deadlineHours) {
        return Result.ok(hazardService.createWorkOrder(id, assigneeId, null, deadlineHours));
    }

    @PutMapping("/work-orders/{orderId}/rectify")
    public Result<HazardWorkOrder> rectify(
            @PathVariable Long orderId,
            @RequestParam String note,
            @RequestParam(required = false) String images) {
        return Result.ok(hazardService.submitRectification(orderId, note, images));
    }

    @PutMapping("/work-orders/{orderId}/verify")
    public Result<HazardWorkOrder> verify(
            @PathVariable Long orderId,
            @RequestParam Long verifierId,
            @RequestParam boolean passed,
            @RequestParam(required = false) String note) {
        return Result.ok(hazardService.verifyWorkOrder(orderId, verifierId, passed, note));
    }
}
