package com.powersmart.hazard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.hazard.entity.QualMaterialChangelog;
import com.powersmart.hazard.mapper.QualMaterialChangelogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class QualMaterialChangelogService {

    private final QualMaterialChangelogMapper mapper;

    public void add(QualMaterialChangelog entity) {
        mapper.insert(entity);
    }

    public List<QualMaterialChangelog> selectByPid(Long pid) {
        return mapper.selectList(
                new LambdaQueryWrapper<QualMaterialChangelog>()
                        .eq(QualMaterialChangelog::getMaterialId, pid)
                        .orderByDesc(QualMaterialChangelog::getCreateTime));
    }
}
