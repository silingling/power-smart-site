package com.powersmart.system.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powersmart.common.util.PageHelper;
import com.powersmart.system.entity.ReportExportTask;
import com.powersmart.system.entity.ReportTemplate;
import com.powersmart.system.mapper.ReportExportTaskMapper;
import com.powersmart.system.mapper.ReportTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表导出引擎 — 模板化 Excel/CSV 导出服务
 *
 * <p>基于 ReportTemplate 配置（columns_config + query_config），
 * 通过 JdbcTemplate 动态查询数据并生成 Excel 文件。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ReportExportService {

    /** 导出文件本地存储根目录 */
    private static final String EXPORT_DIR = "./exports/reports/";

    /** 默认过期天数 */
    private static final int DEFAULT_EXPIRE_DAYS = 7;

    private final ReportTemplateMapper templateMapper;
    private final ReportExportTaskMapper taskMapper;
    private final JdbcTemplate jdbcTemplate;

    // ==================== 模板管理 ====================

    /**
     * 按模块查询可用模板列表
     */
    public List<ReportTemplate> listTemplates(String module) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<ReportTemplate>()
                .eq(ReportTemplate::getEnabled, 1);
        if (StrUtil.isNotBlank(module)) {
            wrapper.eq(ReportTemplate::getModule, module);
        }
        wrapper.orderByAsc(ReportTemplate::getModule, ReportTemplate::getId);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 获取模板详情
     */
    public ReportTemplate getTemplate(Long id) {
        return templateMapper.selectById(id);
    }

    /**
     * 分页查询所有模板（管理后台用，含禁用模板）
     */
    public Page<ReportTemplate> listAllTemplates(LambdaQueryWrapper<ReportTemplate> wrapper, Page<ReportTemplate> page) {
        return templateMapper.selectPage(page, wrapper);
    }

    // ==================== 数据查询 ====================

    /**
     * 添加报表模板
     */
    public void addTemplate(ReportTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        if (template.getEnabled() == null) {
            template.setEnabled(1);
        }
        templateMapper.insert(template);
    }

    /**
     * 更新报表模板
     */
    public void updateTemplate(ReportTemplate template) {
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);
    }

    /**
     * 删除报表模板
     */
    public void deleteTemplate(Long id) {
        templateMapper.deleteById(id);
    }

    /**
     * 基于模板配置查询导出数据
     *
     * @param templateId 模板 ID
     * @param params     查询参数（覆盖/补充 query_config 中的占位符）
     * @return 查询结果列表（每行一个 Map）
     */
    public List<Map<String, Object>> queryExportData(Long templateId, Map<String, Object> params) {
        ReportTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("报表模板不存在: " + templateId);
        }

        // 解析 queryConfig 获取 SQL
        String sql = buildDynamicSql(template.getQueryConfig(), params);
        log.debug("ReportExport query SQL: {}", sql);

        // 执行查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // 根据 columnsConfig 筛选列
        return filterColumns(rows, template.getColumnsConfig());
    }

    /**
     * 从 queryConfig JSON 构建动态 SQL
     *
     * <p>queryConfig 格式示例：</p>
     * <pre>
     * {
     *   "sql": "SELECT * FROM project WHERE deleted = 0 ${conditions} ORDER BY ${orderBy}",
     *   "conditions": {
     *     "projectName": "AND project_name LIKE '%${projectName}%'",
     *     "status": "AND status = '${status}'",
     *     "createTimeStart": "AND created_at >= '${createTimeStart}'",
     *     "createTimeEnd": "AND created_at <= '${createTimeEnd}'"
     *   },
     *   "orderBy": "created_at DESC",
     *   "limit": 10000
     * }
     * </pre>
     */
    private String buildDynamicSql(String queryConfigJson, Map<String, Object> params) {
        JSONObject config = JSONUtil.parseObj(queryConfigJson);
        String baseSql = config.getStr("sql");

        // 构建条件片段
        StringBuilder conditions = new StringBuilder();
        JSONObject conditionDefs = config.getJSONObject("conditions");
        if (conditionDefs != null && params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String paramKey = entry.getKey();
                Object paramValue = entry.getValue();
                if (paramValue == null || StrUtil.isBlankIfStr(paramValue)) {
                    continue;
                }
                String conditionTemplate = conditionDefs.getStr(paramKey);
                if (StrUtil.isNotBlank(conditionTemplate)) {
                    String resolved = conditionTemplate.replace("${" + paramKey + "}",
                            paramValue.toString());
                    conditions.append(" ").append(resolved);
                }
            }
        }

        // 替换占位符
        String sql = baseSql.replace("${conditions}", conditions.toString());

        // 替换排序
        String orderBy = config.getStr("orderBy", "id DESC");
        sql = sql.replace("${orderBy}", orderBy);

        // 追加行数限制
        Integer limit = config.getInt("limit");
        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }

        return sql;
    }

    /**
     * 根据 columnsConfig 过滤查询结果列
     *
     * <p>columnsConfig 格式示例：</p>
     * <pre>
     * [
     *   {"key": "id", "title": "ID", "width": 10, "type": "text"},
     *   {"key": "projectName", "title": "项目名称", "width": 30, "type": "text"},
     *   {"key": "status", "title": "状态", "width": 15, "type": "enum"},
     *   {"key": "budget", "title": "预算(万元)", "width": 15, "type": "number"}
     * ]
     * </pre>
     */
    private List<Map<String, Object>> filterColumns(List<Map<String, Object>> rows, String columnsConfigJson) {
        JSONArray columns = JSONUtil.parseArray(columnsConfigJson);
        List<String> columnKeys = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            columnKeys.add(columns.getJSONObject(i).getStr("key"));
        }

        return rows.stream().map(row -> {
            Map<String, Object> filtered = new LinkedHashMap<>();
            for (String key : columnKeys) {
                filtered.put(key, row.getOrDefault(key, ""));
            }
            return filtered;
        }).collect(Collectors.toList());
    }

    // ==================== Excel 导出 ====================

    /**
     * 触发 Excel 导出，返回任务 ID
     *
     * @param templateId 模板 ID
     * @param projectId  项目 ID
     * @param params     查询参数
     * @return 导出任务 ID
     */
    public Long exportExcel(Long templateId, String projectId, Map<String, Object> params) {
        ReportTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("报表模板不存在: " + templateId);
        }

        // 创建导出任务记录
        ReportExportTask task = new ReportExportTask();
        task.setTemplateId(templateId);
        task.setTemplateName(template.getTemplateName());
        task.setProjectId(projectId);
        task.setOperatorId(getParamStr(params, "operatorId"));
        task.setOperatorName(getParamStr(params, "operatorName"));
        task.setQueryParams(params != null ? JSONUtil.toJsonStr(params) : "{}");
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now());
        task.setExpiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRE_DAYS));
        taskMapper.insert(task);

        try {
            // 查询数据
            List<Map<String, Object>> data = queryExportData(templateId, params);

            // 生成 Excel
            String fileName = template.getTemplateKey() + "_" + System.currentTimeMillis() + ".xlsx";
            String filePath = generateExcel(template, data, task.getId(), fileName);

            // 更新任务记录
            task.setFileName(fileName);
            task.setFileUrl(filePath);
            task.setFileSize(Files.size(Paths.get(filePath)));
            task.setRowCount(data.size());
            task.setStatus("completed");
            task.setCompletedAt(LocalDateTime.now());
            task.setExpiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRE_DAYS));
            taskMapper.updateById(task);

            log.info("报表导出完成: taskId={}, template={}, rows={}, file={}",
                    task.getId(), template.getTemplateKey(), data.size(), filePath);

        } catch (Exception e) {
            task.setStatus("failed");
            task.setErrorMsg(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            log.error("报表导出失败: taskId={}, template={}", task.getId(), template.getTemplateKey(), e);
            throw new RuntimeException("报表导出失败: " + e.getMessage(), e);
        }

        return task.getId();
    }

    /**
     * 生成 Excel 文件
     *
     * @param template 报表模板
     * @param data     查询数据
     * @param taskId   任务 ID
     * @param fileName 文件名
     * @return 文件存储路径
     */
    private String generateExcel(ReportTemplate template, List<Map<String, Object>> data,
                                  Long taskId, String fileName) throws IOException {
        // 确保导出目录存在
        Path exportDir = Paths.get(EXPORT_DIR);
        Files.createDirectories(exportDir);

        JSONArray columns = JSONUtil.parseArray(template.getColumnsConfig());

        // 使用 SXSSFWorkbook（支持大数据量写入）
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet(template.getTemplateName());

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 0;

            // ===== 页眉 =====
            if (StrUtil.isNotBlank(template.getHeaderTemplate())) {
                Row headerRow = sheet.createRow(rowIdx++);
                Cell headerCell = headerRow.createCell(0);
                headerCell.setCellValue(resolveHeaderFooter(template.getHeaderTemplate(), data.size()));
                CellStyle headerTextStyle = workbook.createCellStyle();
                Font hf = workbook.createFont();
                hf.setFontHeightInPoints((short) 14);
                hf.setBold(true);
                headerTextStyle.setFont(hf);
                headerCell.setCellStyle(headerTextStyle);
                rowIdx++; // 空行
            }

            // ===== 列标题行 =====
            Row titleRow = sheet.createRow(rowIdx++);
            List<String> columnKeys = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                JSONObject col = columns.getJSONObject(i);
                String key = col.getStr("key");
                String title = col.getStr("title", key);
                Integer width = col.getInt("width", 20);
                columnKeys.add(key);

                Cell cell = titleRow.createCell(i);
                cell.setCellValue(title);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, width * 256);
            }

            // ===== 数据行 =====
            for (Map<String, Object> rowData : data) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < columnKeys.size(); i++) {
                    Object value = rowData.get(columnKeys.get(i));
                    Cell cell = dataRow.createCell(i);
                    cell.setCellStyle(dataStyle);

                    JSONObject colDef = columns.getJSONObject(i);
                    String colType = colDef.getStr("type", "text");
                    setCellValue(cell, value, colType);
                }
            }

            // ===== 页脚 =====
            if (StrUtil.isNotBlank(template.getFooterTemplate())) {
                rowIdx++; // 空行
                Row footerRow = sheet.createRow(rowIdx);
                Cell footerCell = footerRow.createCell(0);
                footerCell.setCellValue(resolveHeaderFooter(template.getFooterTemplate(), data.size()));
            }

            // 写入文件
            String filePath = EXPORT_DIR + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            // 清理临时文件
            workbook.dispose();

            return filePath;
        }
    }

    /**
     * 根据列类型设置单元格值
     */
    private void setCellValue(Cell cell, Object value, String colType) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        switch (colType) {
            case "number":
            case "decimal":
                try {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } catch (NumberFormatException e) {
                    cell.setCellValue(value.toString());
                }
                break;
            case "date":
                cell.setCellValue(value.toString());
                break;
            case "enum":
            case "text":
            default:
                cell.setCellValue(value.toString());
                break;
        }
    }

    /**
     * 解析页眉/页脚模板中的占位符
     *
     * <p>支持占位符：${date} ${time} ${totalRows} ${templateName}</p>
     */
    private String resolveHeaderFooter(String templateText, int totalRows) {
        String result = templateText
                .replace("${date}", LocalDateTime.now().toLocalDate().toString())
                .replace("${time}", LocalDateTime.now().toLocalTime().toString().substring(0, 8))
                .replace("${totalRows}", String.valueOf(totalRows));
        return result;
    }

    // ==================== 任务查询 ====================

    /**
     * 查询历史导出任务
     */
    public Page<ReportExportTask> getTaskHistory(String operatorId, String status, Map<String, Object> pageParams) {
        Page<ReportExportTask> page = PageHelper.of(pageParams);
        LambdaQueryWrapper<ReportExportTask> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(operatorId)) {
            wrapper.eq(ReportExportTask::getOperatorId, operatorId);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(ReportExportTask::getStatus, status);
        }
        wrapper.orderByDesc(ReportExportTask::getCreatedAt);
        return taskMapper.selectPage(page, wrapper);
    }

    /**
     * 获取任务详情
     */
    public ReportExportTask getTask(Long id) {
        return taskMapper.selectById(id);
    }

    // ==================== 清理 ====================

    /**
     * 删除过期任务记录及其文件
     */
    public void deleteExpiredTasks() {
        LambdaQueryWrapper<ReportExportTask> wrapper = new LambdaQueryWrapper<ReportExportTask>()
                .eq(ReportExportTask::getStatus, "completed")
                .lt(ReportExportTask::getExpiresAt, LocalDateTime.now());

        List<ReportExportTask> expiredTasks = taskMapper.selectList(wrapper);
        int deletedCount = 0;

        for (ReportExportTask task : expiredTasks) {
            // 删除本地文件
            if (StrUtil.isNotBlank(task.getFileUrl())) {
                try {
                    Path filePath = Paths.get(task.getFileUrl());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    log.warn("删除过期导出文件失败: taskId={}, file={}", task.getId(), task.getFileUrl(), e);
                }
            }
            // 删除任务记录
            taskMapper.deleteById(task.getId());
            deletedCount++;
        }

        if (deletedCount > 0) {
            log.info("清理过期报表导出任务: count={}", deletedCount);
        }
    }

    // ==================== 工具方法 ====================

    private String getParamStr(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) return null;
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }
}
