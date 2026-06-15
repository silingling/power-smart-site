package com.powersmart.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powersmart.system.entity.EvalLevel;
import com.powersmart.system.mapper.EvalLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class EvalLevelService {

    private final EvalLevelMapper evalLevelMapper;

    public List<EvalLevel> selectList(Map<String, Object> params) {
        LambdaQueryWrapper<EvalLevel> wrapper = new LambdaQueryWrapper<>();
        if (params != null && params.containsKey("projectId") && params.get("projectId") != null)
            wrapper.eq(EvalLevel::getProjectId, Long.valueOf(params.get("projectId").toString()));
        wrapper.orderByAsc(EvalLevel::getScoreMin);
        return evalLevelMapper.selectList(wrapper);
    }
}
