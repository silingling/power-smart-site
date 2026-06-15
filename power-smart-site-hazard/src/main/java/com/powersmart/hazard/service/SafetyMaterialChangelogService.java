package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.hazard.entity.SafetyMaterialChangelog;
import com.powersmart.hazard.mapper.SafetyMaterialChangelogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SafetyMaterialChangelogService {

    private final SafetyMaterialChangelogMapper mapper;

    public void add(SafetyMaterialChangelog entity) {
        mapper.insert(entity);
    }

    public List<SafetyMaterialChangelog> selectByPid(Long pid) {
        return mapper.selectList(
                new LambdaQueryWrapper<SafetyMaterialChangelog>()
                        .eq(SafetyMaterialChangelog::getMaterialId, pid)
                        .orderByDesc(SafetyMaterialChangelog::getCreateTime));
    }
}
