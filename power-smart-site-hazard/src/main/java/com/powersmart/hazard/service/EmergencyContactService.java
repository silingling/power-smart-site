package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.EmergencyContact;
import com.powersmart.hazard.mapper.EmergencyContactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog
public class EmergencyContactService {

    private final EmergencyContactMapper emergencyContactMapper;

    public PageResult<EmergencyContact> list(Map<String, Object> params) {
        Page<EmergencyContact> page = PageHelper.of(params);
        LambdaQueryWrapper<EmergencyContact> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (params.containsKey("projectId") && params.get("projectId") != null)
                wrapper.eq(EmergencyContact::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("contactRole") && params.get("contactRole") != null)
                wrapper.eq(EmergencyContact::getContactRole, params.get("contactRole").toString());
            if (params.containsKey("keyword") && StrUtil.isNotBlank(params.get("keyword").toString())) {
                String keyword = params.get("keyword").toString();
                wrapper.and(w -> w.like(EmergencyContact::getContactName, keyword)
                        .or().like(EmergencyContact::getOrganization, keyword)
                        .or().like(EmergencyContact::getDepartment, keyword)
                        .or().like(EmergencyContact::getPhone, keyword));
            }
        }
        wrapper.orderByAsc(EmergencyContact::getSortOrder);
        return PageResult.from(emergencyContactMapper.selectPage(page, wrapper));
    }

    public EmergencyContact getById(Long id) {
        return emergencyContactMapper.selectById(id);
    }

    public void add(EmergencyContact entity) {
        if (StrUtil.isBlank(entity.getContactName())) {
            throw new IllegalArgumentException("联系人姓名不能为空");
        }
        if (StrUtil.isBlank(entity.getPhone()) && StrUtil.isBlank(entity.getLandline())) {
            throw new IllegalArgumentException("联系人电话不能为空");
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("active");
        }
        emergencyContactMapper.insert(entity);
    }

    public void update(EmergencyContact entity) {
        EmergencyContact existing = emergencyContactMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("联系人不存在");
        }
        if (StrUtil.isNotBlank(entity.getContactName())) {
            existing.setContactName(entity.getContactName());
        }
        if (StrUtil.isNotBlank(entity.getContactRole())) {
            existing.setContactRole(entity.getContactRole());
        }
        if (entity.getOrganization() != null) {
            existing.setOrganization(entity.getOrganization());
        }
        if (entity.getDepartment() != null) {
            existing.setDepartment(entity.getDepartment());
        }
        if (entity.getPosition() != null) {
            existing.setPosition(entity.getPosition());
        }
        if (StrUtil.isNotBlank(entity.getPhone())) {
            existing.setPhone(entity.getPhone());
        }
        if (StrUtil.isNotBlank(entity.getLandline())) {
            existing.setLandline(entity.getLandline());
        }
        if (entity.getEmail() != null) {
            existing.setEmail(entity.getEmail());
        }
        if (entity.getDuty() != null) {
            existing.setDuty(entity.getDuty());
        }
        if (entity.getSortOrder() != null) {
            existing.setSortOrder(entity.getSortOrder());
        }
        if (StrUtil.isNotBlank(entity.getStatus())) {
            existing.setStatus(entity.getStatus());
        }
        emergencyContactMapper.updateById(existing);
    }

    public void delete(Long id) {
        emergencyContactMapper.deleteById(id);
    }

    public List<EmergencyContact> getByRole(Long projectId, String role) {
        return emergencyContactMapper.selectList(
                new LambdaQueryWrapper<EmergencyContact>()
                        .eq(EmergencyContact::getProjectId, projectId)
                        .eq(EmergencyContact::getContactRole, role)
                        .eq(EmergencyContact::getStatus, "active")
                        .orderByAsc(EmergencyContact::getSortOrder));
    }
}
