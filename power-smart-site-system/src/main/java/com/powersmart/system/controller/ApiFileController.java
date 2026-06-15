package com.powersmart.system.controller;

import com.powersmart.common.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/build/ApiFile")
public class ApiFileController {

    @PostMapping("/export")
    public Result<Map<String, Object>> export(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("url", ""));
    }
}
