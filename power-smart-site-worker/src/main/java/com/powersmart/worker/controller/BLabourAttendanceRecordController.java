package com.powersmart.worker.controller;

import com.powersmart.common.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/build/bLabourAttendanceRecord")
public class BLabourAttendanceRecordController {

    @PostMapping("/excelImport")
    public Result<Map<String, Object>> excelImport(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("count", 0, "errorCount", 0));
    }
}
