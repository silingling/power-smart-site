package com.powersmart.worker.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.worker.entity.Worker;
import com.powersmart.worker.entity.WorkerCertificate;
import com.powersmart.worker.mapper.WorkerCertificateMapper;
import com.powersmart.worker.mapper.WorkerMapper;
import com.powersmart.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {

    private final WorkerCertificateMapper certificateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImport(List<Worker> workers) {
        if (CollUtil.isEmpty(workers)) return 0;
        workers.forEach(w -> {
            w.setId(null);
            w.setEntryDate(LocalDate.now());
            w.setStatus(1);
        });
        saveBatch(workers);
        return workers.size();
    }

    @Override
    public boolean validateCertificates(Long workerId) {
        List<WorkerCertificate> certs = certificateMapper.selectList(
                new LambdaQueryWrapper<WorkerCertificate>()
                        .eq(WorkerCertificate::getWorkerId, workerId));
        if (CollUtil.isEmpty(certs)) return false;
        // 任意证书有效期内即通过
        return certs.stream().anyMatch(c ->
                c.getExpireDate() != null && c.getExpireDate().isAfter(LocalDate.now()));
    }

    @Override
    public List<WorkerCertificate> getExpiringCertificates(int aheadDays) {
        LocalDate deadline = LocalDate.now().plusDays(aheadDays);
        return certificateMapper.selectList(
                new LambdaQueryWrapper<WorkerCertificate>()
                        .le(WorkerCertificate::getExpireDate, deadline)
                        .ge(WorkerCertificate::getExpireDate, LocalDate.now()));
    }

    @Override
    public List<Worker> getByProject(Long projectId, Long teamId, Integer status) {
        LambdaQueryWrapper<Worker> qw = new LambdaQueryWrapper<Worker>()
                .eq(Worker::getProjectId, projectId);
        if (teamId != null) qw.eq(Worker::getTeamId, teamId);
        if (status != null) qw.eq(Worker::getStatus, status);
        return list(qw);
    }
}
