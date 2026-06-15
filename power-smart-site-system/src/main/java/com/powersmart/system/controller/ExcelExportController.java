package com.powersmart.system.controller;

import cn.hutool.core.util.StrUtil;
import com.powersmart.common.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Excel 导入/导出控制器（模板下载 + 文件导出）
 */
@Slf4j
@RestController
@RequestMapping("/build/excelImpModelExport")
public class ExcelExportController {

    @Value("${app.file.upload-dir:./uploads/excel-templates}")
    private String templateDir;

    @PostMapping("/excelImpModelExport")
    public Result<Map<String, Object>> excelImpModelExport(@RequestBody(required = false) Map<String, Object> params) {
        String templateName = params != null && params.containsKey("templateName")
                ? params.get("templateName").toString() : "default.xlsx";

        Path templatePath = Paths.get(templateDir, templateName);
        String downloadUrl = null;
        if (Files.exists(templatePath)) {
            downloadUrl = "/api/v1/files/download/" + templateName;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", downloadUrl != null ? downloadUrl : "");
        result.put("fileName", templateName);
        return Result.ok(result);
    }

    @PostMapping("/download")
    public Result<Map<String, Object>> download(@RequestBody(required = false) Map<String, Object> params) {
        String fileName = params != null && params.containsKey("fileName")
                ? params.get("fileName").toString() : "export.xlsx";

        Path filePath = Paths.get(templateDir, fileName);
        String downloadUrl = null;
        if (Files.exists(filePath)) {
            downloadUrl = "/api/v1/files/download/" + fileName;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", downloadUrl != null ? downloadUrl : "");
        result.put("fileName", fileName);
        return Result.ok(result);
    }
}
