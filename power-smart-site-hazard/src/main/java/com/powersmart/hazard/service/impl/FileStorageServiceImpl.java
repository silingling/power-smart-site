package com.powersmart.hazard.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.hazard.entity.FileRecord;
import com.powersmart.hazard.mapper.FileRecordMapper;
import com.powersmart.hazard.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 本地文件存储实现
 *
 * <p>文件按日期分目录存储，支持扩展为 OSS/S3。
 * 上传目录通过配置 ${file.upload.dir:./uploads} 指定。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:52428800}")
    private long maxFileSize;           // 默认 50MB

    private final FileRecordMapper fileRecordMapper;

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("文件上传目录已创建: {}", dir.getAbsolutePath());
        }
        log.info("文件存储服务初始化完成, 上传目录: {}, 最大文件: {}MB",
                dir.getAbsolutePath(), maxFileSize / 1024 / 1024);
    }

    @Override
    public Long uploadFile(MultipartFile file, String bizType, Long bizId, Long uploadBy, String uploadByName) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超出限制（最大 " + (maxFileSize / 1024 / 1024) + "MB）");
        }
        try {
            return uploadFile(file.getBytes(), file.getOriginalFilename(), bizType, bizId, uploadBy, uploadByName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Long uploadFile(byte[] data, String originalName, String bizType, Long bizId, Long uploadBy, String uploadByName) {
        // 生成存储文件名
        String ext = FileUtil.extName(originalName);
        String storedName = IdUtil.fastSimpleUUID() + (StrUtil.isNotBlank(ext) ? "." + ext : "");
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + storedName;
        Path targetPath = Paths.get(uploadDir, relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, data);

            // 写入数据库记录
            FileRecord record = new FileRecord();
            record.setFileName(originalName);
            record.setFilePath(relativePath);
            record.setFileSize((long) data.length);
            record.setFileType(StrUtil.isNotBlank(ext) ? ext.toLowerCase() : "unknown");
            record.setMimeType(guessMimeType(ext));
            record.setStorageType("local");
            record.setBizType(bizType);
            record.setBizId(bizId);
            record.setUploadBy(uploadBy);
            record.setUploadByName(uploadByName);
            fileRecordMapper.insert(record);

            log.info("文件上传成功: {} ({}B) → {}", originalName, data.length, relativePath);
            return record.getId();
        } catch (IOException e) {
            log.error("文件存储失败: {}", originalName, e);
            throw new RuntimeException("文件存储失败", e);
        }
    }

    @Override
    public String getFilePath(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) return null;
        return Paths.get(uploadDir, record.getFilePath()).toAbsolutePath().toString();
    }

    @Override
    public FileRecord getFileRecord(Long fileId) {
        return fileRecordMapper.selectById(fileId);
    }

    @Override
    public List<FileRecord> getFilesByBiz(String bizType, Long bizId) {
        return fileRecordMapper.selectList(new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getBizType, bizType)
                .eq(FileRecord::getBizId, bizId)
                .eq(FileRecord::getIsDeleted, 0)
                .orderByDesc(FileRecord::getCreatedAt));
    }

    @Override
    public void deleteFile(Long fileId) {
        fileRecordMapper.deleteById(fileId); // 逻辑删除（@TableLogic）
        log.info("文件已逻辑删除: fileId={}", fileId);
    }

    @Override
    public String getUploadDir() {
        return uploadDir;
    }

    /** MIME 类型猜测 */
    private String guessMimeType(String ext) {
        if (ext == null) return "application/octet-stream";
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            default -> "application/octet-stream";
        };
    }
}
