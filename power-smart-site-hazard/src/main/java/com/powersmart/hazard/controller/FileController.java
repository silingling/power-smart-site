package com.powersmart.hazard.controller;

import cn.hutool.core.util.StrUtil;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.Result;
import com.powersmart.hazard.entity.FileRecord;
import com.powersmart.hazard.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件上传/下载 — 同业电力前端 build/ApiFile/*
 *
 * 统一文件管理接口，支持本地存储，可扩展为 OSS。
 */
@Slf4j
@RestController
@RequestMapping("/build/ApiFile")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 文件上传
     *
     * POST /build/ApiFile/upload
     * Content-Type: multipart/form-data
     *
     * @param file    上传的文件
     * @param bizType 业务类型（safety_material/rectification_image / ...）
     * @param bizId   业务 ID（资料 ID / 工单 ID）
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "") String bizType,
            @RequestParam(required = false) Long bizId) {

        if (file.isEmpty()) return Result.fail("文件不能为空");

        String uploadByName = SecurityContext.getCurrentUsername();
        if (StrUtil.isBlank(uploadByName)) uploadByName = "未知用户";

        Long fileId = fileStorageService.uploadFile(
                file, bizType, bizId,
                SecurityContext.getCurrentUserId(),
                uploadByName);

        FileRecord record = fileStorageService.getFileRecord(fileId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileId", fileId);
        result.put("fileName", record.getFileName());
        result.put("fileSize", record.getFileSize());
        result.put("fileType", record.getFileType());
        result.put("url", "/build/ApiFile/download/" + fileId);
        return Result.ok(result);
    }

    /**
     * 文件下载/预览
     *
     * GET /build/ApiFile/download/{fileId}
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        FileRecord record = fileStorageService.getFileRecord(fileId);
        if (record == null) return ResponseEntity.notFound().build();

        String absPath = fileStorageService.getFilePath(fileId);
        if (absPath == null) return ResponseEntity.notFound().build();

        File file = new File(absPath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        String encodedFileName = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        StrUtil.isNotBlank(record.getMimeType()) ? record.getMimeType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    /**
     * 删除文件
     */
    @PostMapping("/delete")
    public Result<Void> deleteFile(@RequestBody Map<String, Object> params) {
        Long fileId = Long.parseLong(params.getOrDefault("fileId", "0").toString());
        fileStorageService.deleteFile(fileId);
        return Result.ok();
    }

    /**
     * 导出模板（向下兼容）
     */
    @PostMapping("/export")
    public Result<Map<String, Object>> export(@RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(Map.of("url", ""));
    }
}
