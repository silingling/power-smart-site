package com.powersmart.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.system.entity.SingleBuildingInfo;
import com.powersmart.system.mapper.SingleBuildingInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单体楼栋信息服务 — 封装 SingleBuildingInfoController 的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SingleBuildingInfoService {

    private final SingleBuildingInfoMapper mapper;

    /**
     * 新增楼栋信息
     */
    public void add(SingleBuildingInfo entity) {
        mapper.insert(entity);
    }

    /**
     * 根据 ID 查询楼栋信息
     */
    public SingleBuildingInfo selectById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 根据 projectId 查询楼栋列表
     */
    public List<SingleBuildingInfo> selectByProjectId(Long projectId) {
        return mapper.selectList(
                new LambdaQueryWrapper<SingleBuildingInfo>()
                        .eq(SingleBuildingInfo::getProjectId, projectId)
                        .orderByDesc(SingleBuildingInfo::getCreateTime));
    }

    /**
     * 删除楼栋信息
     */
    public void removeById(Long id) {
        mapper.deleteById(id);
    }
}
