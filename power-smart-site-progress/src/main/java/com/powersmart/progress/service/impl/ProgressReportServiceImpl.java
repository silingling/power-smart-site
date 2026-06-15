package com.powersmart.progress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powersmart.progress.entity.ProgressReport;
import com.powersmart.progress.mapper.ProgressReportMapper;
import com.powersmart.progress.service.ProgressReportService;
import org.springframework.stereotype.Service;

@Service
public class ProgressReportServiceImpl extends ServiceImpl<ProgressReportMapper, ProgressReport> implements ProgressReportService {
}
