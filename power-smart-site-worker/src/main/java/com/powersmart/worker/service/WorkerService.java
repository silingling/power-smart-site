package com.powersmart.worker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powersmart.worker.entity.Worker;
import com.powersmart.worker.entity.WorkerCertificate;

import java.util.List;

public interface WorkerService extends IService<Worker> {
    /**
     * 批量导入人员（Excel）
     */
    int batchImport(List<Worker> workers);

    /**
     * 校验人员资质是否有效
     */
    boolean validateCertificates(Long workerId);

    /**
     * 查询即将过期的资质（提前30天预警）
     */
    List<WorkerCertificate> getExpiringCertificates(int aheadDays);

    /**
     * 根据项目ID查询人员列表
     */
    List<Worker> getByProject(Long projectId, Long teamId, Integer status);
}
