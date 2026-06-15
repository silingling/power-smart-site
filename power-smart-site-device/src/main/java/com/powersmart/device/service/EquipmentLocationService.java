package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.common.entity.Result;
import com.powersmart.device.entity.EquipmentLocation;
import com.powersmart.device.mapper.EquipmentLocationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备位置台账树 服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentLocationService {

    private final EquipmentLocationMapper equipmentLocationMapper;

    public List<EquipmentLocation> queryTreeListByParentId(Long parentId, EquipmentLocation query) {
        LambdaQueryWrapper<EquipmentLocation> wrapper = new LambdaQueryWrapper<EquipmentLocation>()
                .eq(parentId != null, EquipmentLocation::getParentId, parentId != null ? parentId : 0)
                .orderByAsc(EquipmentLocation::getSortOrder);

        if (query != null && query.getProjectId() != null) {
            wrapper.eq(EquipmentLocation::getProjectId, query.getProjectId());
        }
        return equipmentLocationMapper.selectList(wrapper);
    }

    public void add(EquipmentLocation entity) {
        equipmentLocationMapper.insert(entity);
    }

    public void edit(EquipmentLocation entity) {
        equipmentLocationMapper.updateById(entity);
    }

    public void delete(Long id) {
        equipmentLocationMapper.deleteById(id);
    }

    public List<Map<String, Object>> list() {
        List<EquipmentLocation> all = equipmentLocationMapper.selectList(
                new LambdaQueryWrapper<EquipmentLocation>().orderByAsc(EquipmentLocation::getSortOrder));
        // 构建树形结构
        return buildTree(all, 0L);
    }

    private List<Map<String, Object>> buildTree(List<EquipmentLocation> all, Long parentId) {
        return all.stream()
                .filter(n -> n.getParentId() != null && n.getParentId().equals(parentId))
                .map(n -> {
                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("id", n.getId());
                    node.put("label", n.getLocationName());
                    node.put("locationType", n.getLocationType());
                    List<Map<String, Object>> children = buildTree(all, n.getId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    return node;
                }).collect(Collectors.toList());
    }
}
