package com.powersmart.hazard.service;

import com.powersmart.hazard.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    /**
     * 上传文件（返回文件记录ID）
     */
    Long uploadFile(MultipartFile file, String bizType, Long bizId, Long uploadBy, String uploadByName);

    /**
     * 上传文件（字节数组）
     */
    Long uploadFile(byte[] data, String originalName, String bizType, Long bizId, Long uploadBy, String uploadByName);

    /**
     * 获取文件的绝对路径
     */
    String getFilePath(Long fileId);

    /**
     * 获取文件记录
     */
    FileRecord getFileRecord(Long fileId);

    /**
     * 获取业务关联的文件列表
     */
    List<FileRecord> getFilesByBiz(String bizType, Long bizId);

    /**
     * 删除文件（逻辑删除）
     */
    void deleteFile(Long fileId);

    /**
     * 获取上传目录
     */
    String getUploadDir();
}
