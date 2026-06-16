package com.powersmart.progress.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.auth.SecurityContext;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.progress.entity.ElectronicSignature;
import com.powersmart.progress.mapper.ElectronicSignatureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "电子签名")
public class ElectronicSignatureService {

    private final ElectronicSignatureMapper electronicSignatureMapper;

    /**
     * 分页查询电子签名
     */
    public PageResult<ElectronicSignature> list(Map<String, Object> params) {
        IPage<ElectronicSignature> pageParam = PageHelper.of(params);
        LambdaQueryWrapper<ElectronicSignature> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            if (StrUtil.isNotBlank((String) params.get("bizType"))) {
                wrapper.eq(ElectronicSignature::getBizType, params.get("bizType"));
            }
            if (params.get("bizId") != null) {
                wrapper.eq(ElectronicSignature::getBizId, params.get("bizId"));
            }
            if (params.get("signerId") != null) {
                wrapper.eq(ElectronicSignature::getSignerId, params.get("signerId"));
            }
        }
        wrapper.orderByDesc(ElectronicSignature::getId);
        return PageResult.from(electronicSignatureMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 根据 ID 查询电子签名
     */
    public ElectronicSignature getById(Long id) {
        return electronicSignatureMapper.selectById(id);
    }

    /**
     * 签署（新增电子签名）
     */
    @OperateLog(action = "sign", description = "电子签名")
    public ElectronicSignature sign(ElectronicSignature entity) {
        if (StrUtil.isBlank(entity.getBizType())) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        if (entity.getBizId() == null) {
            throw new IllegalArgumentException("业务ID不能为空");
        }
        entity.setSignerId(SecurityContext.getCurrentUserId());
        entity.setSignerName(SecurityContext.getCurrentUsername());
        entity.setSignedAt(LocalDateTime.now());
        electronicSignatureMapper.insert(entity);
        return entity;
    }

    /**
     * 获取业务记录的所有签名
     */
    public List<ElectronicSignature> getByBiz(String bizType, Long bizId) {
        return electronicSignatureMapper.selectList(new LambdaQueryWrapper<ElectronicSignature>()
                .eq(ElectronicSignature::getBizType, bizType)
                .eq(ElectronicSignature::getBizId, bizId)
                .orderByAsc(ElectronicSignature::getSignedAt));
    }

    /**
     * 校验签名是否存在并返回签署人详情
     */
    public Map<String, Object> verify(Long id) {
        ElectronicSignature signature = electronicSignatureMapper.selectById(id);
        if (signature == null) {
            throw new IllegalArgumentException("签名记录不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("signerName", signature.getSignerName());
        result.put("signerRole", signature.getSignerRole());
        result.put("signedAt", signature.getSignedAt());
        result.put("bizType", signature.getBizType());
        result.put("bizId", signature.getBizId());
        return result;
    }
}
