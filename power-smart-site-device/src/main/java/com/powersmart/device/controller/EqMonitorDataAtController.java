package com.powersmart.device.controller;

import com.powersmart.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 设备监测环境数据 — 同业电力前端 build/eqMonitorDataAt/*
 */
@RestController
@RequestMapping("/build/eqMonitorDataAt")
@RequiredArgsConstructor
public class EqMonitorDataAtController {

    @PostMapping("/selectEnvDataAt")
    public Result<Map<String, Object>> selectEnvDataAt(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pm25", 35.2);
        data.put("noise", 62.5);
        data.put("temperature", 26.8);
        data.put("humidity", 58.3);
        data.put("updateTime", new Date());
        return Result.ok(data);
    }
}
