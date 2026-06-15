package com.powersmart.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.device.entity.TransmissionSpan;
import com.powersmart.device.entity.TransmissionTower;
import com.powersmart.device.mapper.TransmissionSpanMapper;
import com.powersmart.device.mapper.TransmissionTowerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransmissionLineService {

    private final TransmissionTowerMapper towerMapper;
    private final TransmissionSpanMapper spanMapper;

    // ================== 杆塔 ==================

    public Page<TransmissionTower> queryTowerPage(Map<String, Object> params) {
        int pn = 1, ps = 20;
        if (params != null) {
            if (params.containsKey("page")) pn = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize"))
                ps = Integer.parseInt(params.get("pageSize").toString());
        }
        Page<TransmissionTower> page = new Page<>(pn, ps);
        LambdaQueryWrapper<TransmissionTower> w = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.containsKey("projectId"))
                w.eq(TransmissionTower::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("towerType"))
                w.eq(TransmissionTower::getTowerType, params.get("towerType").toString());
            if (params.containsKey("status"))
                w.eq(TransmissionTower::getStatus, params.get("status").toString());
            if (params.containsKey("keyword")) {
                String kw = "%" + params.get("keyword") + "%";
                w.and(x -> x.like(TransmissionTower::getTowerCode, kw)
                        .or().like(TransmissionTower::getTowerName, kw)
                        .or().like(TransmissionTower::getModel, kw));
            }
        }
        w.orderByAsc(TransmissionTower::getTowerCode);
        return towerMapper.selectPage(page, w);
    }

    public TransmissionTower getTowerById(Long id) {
        return towerMapper.selectById(id);
    }

    public void addTower(TransmissionTower tower) {
        towerMapper.insert(tower);
    }

    public void updateTower(TransmissionTower tower) {
        towerMapper.updateById(tower);
    }

    public void deleteTower(Long id) {
        towerMapper.deleteById(id);
    }

    public List<TransmissionTower> getActiveTowers(Long projectId) {
        return towerMapper.selectActiveTowers(projectId);
    }

    // ================== 档距/弧垂 ==================

    public Page<TransmissionSpan> querySpanPage(Map<String, Object> params) {
        int pn = 1, ps = 20;
        if (params != null) {
            if (params.containsKey("page")) pn = Integer.parseInt(params.get("page").toString());
            if (params.containsKey("pageSize"))
                ps = Integer.parseInt(params.get("pageSize").toString());
        }
        Page<TransmissionSpan> page = new Page<>(pn, ps);
        LambdaQueryWrapper<TransmissionSpan> w = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.containsKey("projectId"))
                w.eq(TransmissionSpan::getProjectId, Long.valueOf(params.get("projectId").toString()));
            if (params.containsKey("status"))
                w.eq(TransmissionSpan::getStatus, params.get("status").toString());
            if (params.containsKey("fromTowerId"))
                w.eq(TransmissionSpan::getFromTowerId, Long.valueOf(params.get("fromTowerId").toString()));
            if (params.containsKey("toTowerId"))
                w.eq(TransmissionSpan::getToTowerId, Long.valueOf(params.get("toTowerId").toString()));
        }
        w.orderByAsc(TransmissionSpan::getSpanCode);
        return spanMapper.selectPage(page, w);
    }

    public TransmissionSpan getSpanById(Long id) {
        return spanMapper.selectById(id);
    }

    public void addSpan(TransmissionSpan span) {
        spanMapper.insert(span);
    }

    public void updateSpan(TransmissionSpan span) {
        spanMapper.updateById(span);
    }

    public void deleteSpan(Long id) {
        spanMapper.deleteById(id);
    }
}
