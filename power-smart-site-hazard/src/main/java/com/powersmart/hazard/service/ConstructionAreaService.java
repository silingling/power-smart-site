package com.powersmart.hazard.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.annotation.OperateLog;
import com.powersmart.common.entity.PageResult;
import com.powersmart.common.util.PageHelper;
import com.powersmart.hazard.entity.ConstructionArea;
import com.powersmart.hazard.mapper.ConstructionAreaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@OperateLog(module = "作业区域管理")
public class ConstructionAreaService {

    private final ConstructionAreaMapper constructionAreaMapper;

    public PageResult<ConstructionArea> list(Map<String, Object> params) {
        Page<ConstructionArea> page = PageHelper.of(params);
        LambdaQueryWrapper<ConstructionArea> wrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            Object projectId = params.get("projectId");
            if (projectId != null) {
                wrapper.eq(ConstructionArea::getProjectId, Long.valueOf(projectId.toString()));
            }
            Object areaType = params.get("areaType");
            if (areaType != null && StrUtil.isNotBlank(areaType.toString())) {
                wrapper.eq(ConstructionArea::getAreaType, areaType.toString());
            }
            Object riskLevel = params.get("riskLevel");
            if (riskLevel != null && StrUtil.isNotBlank(riskLevel.toString())) {
                wrapper.eq(ConstructionArea::getRiskLevel, riskLevel.toString());
            }
            Object status = params.get("status");
            if (status != null) {
                wrapper.eq(ConstructionArea::getStatus, Integer.valueOf(status.toString()));
            }
        }
        wrapper.orderByDesc(ConstructionArea::getCreatedAt);
        return PageResult.from(constructionAreaMapper.selectPage(page, wrapper));
    }

    public ConstructionArea getById(Long id) {
        return constructionAreaMapper.selectById(id);
    }

    @OperateLog(description = "新增作业区域")
    public void add(ConstructionArea entity) {
        if (StrUtil.isBlank(entity.getAreaName())) {
            throw new IllegalArgumentException("区域名称不能为空");
        }
        if (entity.getProjectId() == null) {
            throw new IllegalArgumentException("所属项目不能为空");
        }
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
        constructionAreaMapper.insert(entity);
    }

    @OperateLog(description = "修改作业区域")
    public void update(ConstructionArea entity) {
        ConstructionArea existing = constructionAreaMapper.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("作业区域不存在");
        }
        if (StrUtil.isNotBlank(entity.getAreaName())) existing.setAreaName(entity.getAreaName());
        if (StrUtil.isNotBlank(entity.getAreaType())) existing.setAreaType(entity.getAreaType());
        if (StrUtil.isNotBlank(entity.getRiskLevel())) existing.setRiskLevel(entity.getRiskLevel());
        if (entity.getFencePoints() != null) existing.setFencePoints(entity.getFencePoints());
        if (entity.getResponsiblePersonId() != null) existing.setResponsiblePersonId(entity.getResponsiblePersonId());
        if (entity.getResponsibleTeamId() != null) existing.setResponsibleTeamId(entity.getResponsibleTeamId());
        if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
        if (entity.getProjectId() != null) existing.setProjectId(entity.getProjectId());
        constructionAreaMapper.updateById(existing);
    }

    @OperateLog(description = "删除作业区域")
    public void delete(Long id) {
        constructionAreaMapper.deleteById(id);
    }

    public List<ConstructionArea> getActiveByProject(Long projectId) {
        LambdaQueryWrapper<ConstructionArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConstructionArea::getProjectId, projectId);
        wrapper.eq(ConstructionArea::getStatus, 1);
        wrapper.orderByAsc(ConstructionArea::getAreaName);
        return constructionAreaMapper.selectList(wrapper);
    }
}
