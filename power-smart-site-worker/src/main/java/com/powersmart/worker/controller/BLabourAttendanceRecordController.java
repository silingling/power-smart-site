package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/build/bLabourAttendanceRecord")
@RequiredArgsConstructor
public class BLabourAttendanceRecordController {

    private final AttendanceService attendanceService;

    @PostMapping("/excelImport")
    public Result<Map<String, Object>> excelImport(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(attendanceService.excelImport(params));
    }
}
