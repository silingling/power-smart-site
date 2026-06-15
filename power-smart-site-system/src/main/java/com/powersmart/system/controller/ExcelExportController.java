package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.powersmart.common.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/build/excelImpModelExport")
public class ExcelExportController {

    @PostMapping("/excelImpModelExport")
    public Result<Map<String, Object>> excelImpModelExport(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("url", "", "fileName", ""));
    }

    @PostMapping("/download")
    public Result<Map<String, Object>> download(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("url", "", "fileName", ""));
    }
}
