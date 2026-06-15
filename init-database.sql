-- ========================================
-- 电力智慧工地平台 - 数据库初始化脚本
-- Power Smart Site Platform DDL
-- ========================================

CREATE DATABASE IF NOT EXISTS power_smart_site DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE power_smart_site;

-- 施工人员表
CREATE TABLE worker (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL COMMENT '所属项目ID',
  team_id bigint DEFAULT NULL COMMENT '班组ID',
  name varchar(50) NOT NULL COMMENT '姓名',
  id_card varchar(18) NOT NULL COMMENT '身份证号',
  phone varchar(20) DEFAULT NULL COMMENT '手机号',
  avatar_url varchar(255) DEFAULT NULL COMMENT '人脸照片URL',
  entry_date date DEFAULT NULL COMMENT '入场日期',
  exit_date date DEFAULT NULL COMMENT '退场日期',
  status tinyint DEFAULT '1' COMMENT '1-在岗 0-退场',
  worker_type varchar(50) DEFAULT NULL COMMENT '工种',
  training_passed tinyint DEFAULT '0' COMMENT '安全教育是否通过',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project (project_id),
  KEY idx_id_card (id_card)
) ENGINE=InnoDB COMMENT='施工人员';

-- 人员资质表
CREATE TABLE worker_certificate (
  id bigint NOT NULL AUTO_INCREMENT,
  worker_id bigint NOT NULL,
  cert_type varchar(50) NOT NULL COMMENT '证书类型',
  cert_number varchar(100) NOT NULL COMMENT '证书编号',
  issue_date date NOT NULL COMMENT '发证日期',
  expire_date date NOT NULL COMMENT '到期日期',
  issue_authority varchar(200) DEFAULT NULL COMMENT '发证机关',
  cert_image_url varchar(255) DEFAULT NULL COMMENT '证书图片',
  verified tinyint DEFAULT '0' COMMENT '是否核验通过',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_worker (worker_id),
  KEY idx_expire (expire_date)
) ENGINE=InnoDB COMMENT='人员资质证书';

-- 设备台账表
CREATE TABLE device (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  device_code varchar(50) NOT NULL COMMENT '设备编号（一机一码）',
  device_name varchar(100) NOT NULL COMMENT '设备名称',
  device_type varchar(50) NOT NULL COMMENT '设备类型',
  manufacturer varchar(100) DEFAULT NULL COMMENT '生产厂家',
  model varchar(100) DEFAULT NULL COMMENT '型号',
  serial_number varchar(100) DEFAULT NULL COMMENT '出厂编号',
  entry_date date DEFAULT NULL COMMENT '进场日期',
  next_inspection_date date DEFAULT NULL COMMENT '下次年检日期',
  next_maintenance_date date DEFAULT NULL COMMENT '下次维保日期',
  status tinyint DEFAULT '1' COMMENT '1-正常 2-运行 3-维修 4-退场',
  operator_id bigint DEFAULT NULL COMMENT '当前操作人员ID',
  location varchar(200) DEFAULT NULL COMMENT '当前位置',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (device_code),
  KEY idx_project_type (project_id, device_type),
  KEY idx_inspection (next_inspection_date),
  KEY idx_maintenance (next_maintenance_date)
) ENGINE=InnoDB COMMENT='设备台账';

-- 进度计划工序表
CREATE TABLE progress_task (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  parent_id bigint DEFAULT NULL COMMENT '父工序ID',
  task_name varchar(200) NOT NULL COMMENT '工序名称',
  task_level tinyint NOT NULL COMMENT '层级(1-单位工程 2-分部 3-分项 4-工序)',
  plan_start_date date NOT NULL COMMENT '计划开始日期',
  plan_end_date date NOT NULL COMMENT '计划结束日期',
  actual_start_date date DEFAULT NULL COMMENT '实际开始日期',
  actual_completion_rate decimal(5,2) DEFAULT '0.00' COMMENT '实际完成率(%)',
  responsible_team_id bigint DEFAULT NULL COMMENT '责任班组ID',
  responsible_person varchar(50) DEFAULT NULL COMMENT '责任人',
  predecessor_task_ids varchar(500) DEFAULT NULL COMMENT '前置工序ID列表',
  sort_order int DEFAULT '0' COMMENT '排序',
  status tinyint DEFAULT '0' COMMENT '0-未开始 1-进行中 2-已完成 3-滞后',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project_level (project_id, task_level),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB COMMENT='进度计划工序';

-- 进度上报记录表
CREATE TABLE progress_report (
  id bigint NOT NULL AUTO_INCREMENT,
  task_id bigint NOT NULL COMMENT '所属工序ID',
  reporter_id bigint NOT NULL COMMENT '上报人',
  report_date date NOT NULL COMMENT '上报日期',
  completion_rate decimal(5,2) NOT NULL COMMENT '完成率(%)',
  worker_count int DEFAULT NULL COMMENT '投入人数',
  image_urls varchar(2000) DEFAULT NULL COMMENT '现场照片(JSON数组)',
  note varchar(500) DEFAULT NULL COMMENT '备注',
  location varchar(200) DEFAULT NULL COMMENT '上报位置',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_task_date (task_id, report_date)
) ENGINE=InnoDB COMMENT='进度上报记录';

-- 隐患记录表
CREATE TABLE hazard_report (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  report_type tinyint NOT NULL COMMENT '1-AI 2-人工',
  hazard_type varchar(50) NOT NULL COMMENT '隐患类型',
  hazard_level tinyint NOT NULL COMMENT '1-一般 2-较大 3-重大',
  description varchar(1000) NOT NULL COMMENT '隐患描述',
  location varchar(200) DEFAULT NULL COMMENT '事发位置',
  area_id bigint DEFAULT NULL COMMENT '作业区域ID',
  image_url varchar(255) DEFAULT NULL COMMENT '隐患照片',
  video_url varchar(255) DEFAULT NULL COMMENT '隐患视频',
  reported_by bigint DEFAULT NULL COMMENT '上报人ID',
  status tinyint DEFAULT '1' COMMENT '1-待整改 2-整改中 3-已验收 4-已归档',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project_status (project_id, status),
  KEY idx_level (hazard_level),
  KEY idx_created (created_at)
) ENGINE=InnoDB COMMENT='隐患记录';

-- 隐患整改工单表
CREATE TABLE hazard_work_order (
  id bigint NOT NULL AUTO_INCREMENT,
  hazard_id bigint NOT NULL,
  assignee_id bigint NOT NULL COMMENT '整改责任人ID',
  assignee_team_id bigint DEFAULT NULL COMMENT '责任班组ID',
  deadline datetime NOT NULL COMMENT '整改截止时间',
  rectification_note varchar(1000) DEFAULT NULL COMMENT '整改说明',
  rectification_images varchar(2000) DEFAULT NULL COMMENT '整改后照片',
  rectification_time datetime DEFAULT NULL COMMENT '整改完成时间',
  verified_by bigint DEFAULT NULL COMMENT '验收人ID',
  verified_note varchar(500) DEFAULT NULL COMMENT '验收意见',
  verified_time datetime DEFAULT NULL COMMENT '验收时间',
  status tinyint DEFAULT '1' COMMENT '1-待整改 2-已整改待验收 3-验收通过 4-退回重改',
  escalated tinyint DEFAULT '0' COMMENT '是否已升级督办',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hazard (hazard_id),
  KEY idx_assignee (assignee_id),
  KEY idx_deadline (deadline)
) ENGINE=InnoDB COMMENT='隐患整改工单';

-- 系统用户表
CREATE TABLE sys_user (
  id bigint NOT NULL AUTO_INCREMENT,
  username varchar(50) NOT NULL,
  password varchar(200) NOT NULL,
  real_name varchar(50) DEFAULT NULL,
  phone varchar(20) DEFAULT NULL,
  role_ids varchar(200) DEFAULT NULL COMMENT '角色ID列表',
  project_id bigint DEFAULT NULL,
  status tinyint DEFAULT '1' COMMENT '1-启用 0-禁用',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户';

-- 工程项目表
CREATE TABLE project (
  id bigint NOT NULL AUTO_INCREMENT,
  project_name varchar(200) NOT NULL COMMENT '项目名称',
  project_code varchar(50) DEFAULT NULL COMMENT '项目编码',
  project_type varchar(50) DEFAULT NULL COMMENT '变电站/输电线路/配电/新能源',
  project_address varchar(500) DEFAULT NULL,
  contractor varchar(200) DEFAULT NULL COMMENT '施工单位',
  supervisor varchar(200) DEFAULT NULL COMMENT '监理单位',
  plan_start_date datetime DEFAULT NULL,
  plan_end_date datetime DEFAULT NULL,
  actual_start_date datetime DEFAULT NULL,
  actual_end_date datetime DEFAULT NULL,
  status tinyint DEFAULT '0' COMMENT '0-筹建 1-施工中 2-完工 3-已验收',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='工程项目';

-- 施工班组表
CREATE TABLE worker_team (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  team_name varchar(100) NOT NULL COMMENT '班组名称',
  leader_name varchar(50) DEFAULT NULL,
  leader_phone varchar(20) DEFAULT NULL,
  work_type varchar(50) DEFAULT NULL COMMENT '班组工种',
  member_count int DEFAULT '0',
  status tinyint DEFAULT '1' COMMENT '1-正常 0-解散',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project (project_id)
) ENGINE=InnoDB COMMENT='施工班组';

-- 人员考勤表
CREATE TABLE attendance_record (
  id bigint NOT NULL AUTO_INCREMENT,
  worker_id bigint NOT NULL,
  project_id bigint NOT NULL,
  attend_date date NOT NULL COMMENT '日期',
  check_in_time varchar(10) DEFAULT NULL COMMENT '签到时间 HH:mm',
  check_out_time varchar(10) DEFAULT NULL COMMENT '签退时间 HH:mm',
  attend_type varchar(20) DEFAULT '人脸' COMMENT '签到方式',
  status tinyint DEFAULT '1' COMMENT '1-正常 2-迟到 3-早退 4-缺勤',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_worker_date (worker_id, attend_date),
  KEY idx_project_date (project_id, attend_date)
) ENGINE=InnoDB COMMENT='人员考勤记录';

-- 设备告警表
CREATE TABLE device_alarm (
  id bigint NOT NULL AUTO_INCREMENT,
  device_id bigint NOT NULL,
  alarm_type varchar(50) NOT NULL COMMENT '告警类型',
  alarm_level varchar(20) NOT NULL DEFAULT 'warning' COMMENT 'warning/critical',
  alarm_value double DEFAULT NULL COMMENT '告警值',
  threshold_value double DEFAULT NULL COMMENT '阈值',
  description varchar(500) DEFAULT NULL,
  status tinyint DEFAULT '0' COMMENT '0-未处理 1-已处理 2-已忽略',
  handled_by bigint DEFAULT NULL,
  handled_time datetime DEFAULT NULL,
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_device (device_id),
  KEY idx_status (status)
) ENGINE=InnoDB COMMENT='设备告警';

-- 作业区域表（电子围栏）
CREATE TABLE construction_area (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  area_name varchar(100) NOT NULL COMMENT '区域名称',
  area_type varchar(50) DEFAULT NULL COMMENT '高压禁区/吊装区/临时带电区',
  risk_level varchar(20) DEFAULT 'medium' COMMENT 'high/medium/low',
  fence_points text COMMENT '围栏坐标JSON',
  responsible_person_id bigint DEFAULT NULL,
  responsible_team_id bigint DEFAULT NULL,
  status tinyint DEFAULT '1' COMMENT '1-启用 0-禁用',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project (project_id)
) ENGINE=InnoDB COMMENT='作业区域（电子围栏）';

-- AI违规识别记录
CREATE TABLE ai_violation (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  camera_id bigint DEFAULT NULL COMMENT '摄像头ID',
  violation_type varchar(50) NOT NULL COMMENT '违规类型',
  confidence double DEFAULT NULL COMMENT '识别置信度',
  snapshot_url varchar(500) DEFAULT NULL COMMENT '抓拍图片',
  video_url varchar(500) DEFAULT NULL COMMENT '违规视频',
  worker_id bigint DEFAULT NULL COMMENT '关联人员',
  status tinyint DEFAULT '0' COMMENT '0-未处理 1-已确认 2-误报',
  handled_by bigint DEFAULT NULL,
  handled_time datetime DEFAULT NULL,
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project_type (project_id, violation_type),
  KEY idx_status (status)
) ENGINE=InnoDB COMMENT='AI违规识别记录';

-- ========================================
-- Phase 1 新增：萤丰前端对接 — 分包商/工种/进出场/摄像头/位置台账
-- ========================================

CREATE TABLE labour_subcontractor (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint DEFAULT NULL,
  subcontractor_name varchar(200) NOT NULL COMMENT '分包商名称',
  subcontractor_type varchar(50) DEFAULT NULL COMMENT '劳务分包/专业分包/总包',
  legal_person varchar(100) DEFAULT NULL COMMENT '法人代表',
  contact_phone varchar(20) DEFAULT NULL,
  credit_code varchar(50) DEFAULT NULL COMMENT '统一社会信用代码',
  business_license varchar(500) DEFAULT NULL COMMENT '营业执照URL',
  qualification_level varchar(50) DEFAULT NULL COMMENT '资质等级',
  worker_count int DEFAULT '0' COMMENT '在册人数',
  contract_url varchar(500) DEFAULT NULL COMMENT '合同文件URL',
  status tinyint DEFAULT '1' COMMENT '1-合作中 0-已终止',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project (project_id)
) ENGINE=InnoDB COMMENT='分包商';

CREATE TABLE labour_construction_unit (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint DEFAULT NULL,
  unit_name varchar(200) NOT NULL COMMENT '单位名称',
  contact_person varchar(50) DEFAULT NULL,
  contact_phone varchar(20) DEFAULT NULL,
  credit_code varchar(50) DEFAULT NULL,
  status tinyint DEFAULT '1' COMMENT '1-启用 0-禁用',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='建设单位';

CREATE TABLE labour_worktype (
  id bigint NOT NULL AUTO_INCREMENT,
  worktype_name varchar(50) NOT NULL COMMENT '工种名称',
  worktype_code varchar(50) DEFAULT NULL COMMENT '工种编码',
  cert_required varchar(200) DEFAULT NULL COMMENT '所需证书',
  sort_order int DEFAULT '0',
  status tinyint DEFAULT '1' COMMENT '1-启用 0-禁用',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='工种字典';

CREATE TABLE labour_advance_retreat (
  id bigint NOT NULL AUTO_INCREMENT,
  worker_id bigint DEFAULT NULL,
  project_id bigint DEFAULT NULL,
  worker_name varchar(50) DEFAULT NULL,
  id_card varchar(18) DEFAULT NULL COMMENT '身份证号',
  phone varchar(20) DEFAULT NULL,
  team_id bigint DEFAULT NULL,
  subcontractor_id bigint DEFAULT NULL,
  worktype varchar(50) DEFAULT NULL,
  entry_date date DEFAULT NULL COMMENT '进场日期',
  exit_date date DEFAULT NULL COMMENT '退场日期',
  entry_type varchar(20) DEFAULT '首次' COMMENT '首次/返场',
  exit_type varchar(20) DEFAULT NULL COMMENT '退场方式',
  remark varchar(500) DEFAULT NULL,
  status tinyint DEFAULT '1' COMMENT '1-在场 0-已退场',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_worker (worker_id),
  KEY idx_project (project_id)
) ENGINE=InnoDB COMMENT='工人进出场记录';

CREATE TABLE equipment_location (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint DEFAULT NULL,
  parent_id bigint DEFAULT '0' COMMENT '父节点ID',
  location_name varchar(100) NOT NULL COMMENT '位置名称',
  location_type varchar(20) DEFAULT '区域' COMMENT '区域/点位',
  sort_order int DEFAULT '0',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_parent (parent_id),
  KEY idx_project (project_id)
) ENGINE=InnoDB COMMENT='设备位置台账树';

CREATE TABLE video_monitor (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint DEFAULT NULL,
  location_id bigint DEFAULT NULL,
  camera_name varchar(100) NOT NULL COMMENT '摄像头名称',
  camera_code varchar(100) DEFAULT NULL,
  camera_type varchar(20) DEFAULT NULL COMMENT '球机/枪机/全景',
  vendor varchar(50) DEFAULT NULL COMMENT '萤石/海康/大华',
  device_serial varchar(100) DEFAULT NULL COMMENT '设备序列号',
  validate_code varchar(50) DEFAULT NULL COMMENT '验证码',
  stream_url varchar(500) DEFAULT NULL COMMENT 'RTSP/HLS地址',
  install_position varchar(200) DEFAULT NULL COMMENT '安装位置',
  ai_functions varchar(200) DEFAULT NULL COMMENT 'AI功能',
  status tinyint DEFAULT '1' COMMENT '1-在线 0-离线 2-故障',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_location (location_id),
  KEY idx_project (project_id)
) ENGINE=InnoDB COMMENT='视频监控摄像头';

-- 初始化工种数据
INSERT INTO labour_worktype (worktype_name, worktype_code, cert_required, sort_order) VALUES
('电工', 'D003', '电工证', 1),
('焊工', 'D004', '焊工证', 2),
('塔吊司机', 'D005', '塔吊司机证', 3),
('起重指挥', 'D006', '起重指挥证', 4),
('架子工', 'D007', '架子工证', 5),
('钢筋工', 'D008', NULL, 6),
('混凝土工', 'D009', NULL, 7),
('普工', 'D010', NULL, 8),
('安全员', 'D011', '安全员证', 9),
('监理', 'D012', '监理工程师证', 10);

-- 安全资料表
CREATE TABLE safety_material (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint DEFAULT NULL,
  catalog_id bigint DEFAULT NULL COMMENT '目录ID',
  title varchar(200) NOT NULL,
  file_url varchar(500) DEFAULT NULL,
  file_type varchar(20) DEFAULT NULL,
  upload_by bigint DEFAULT NULL,
  is_collect tinyint DEFAULT '0' COMMENT '0-未收藏 1-已收藏',
  is_qual tinyint DEFAULT '0' COMMENT '0-安全资料 1-质量资料',
  status tinyint DEFAULT '1' COMMENT '1-正常 0-删除',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_project_catalog (project_id, catalog_id)
) ENGINE=InnoDB COMMENT='安全资料';

CREATE TABLE safety_material_catalog (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint DEFAULT NULL,
  parent_id bigint DEFAULT '0' COMMENT '上级目录ID',
  name varchar(100) NOT NULL,
  sort_order int DEFAULT '0',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='安全资料目录';

-- 部门表
CREATE TABLE sys_dept (
  id bigint NOT NULL AUTO_INCREMENT,
  parent_id bigint DEFAULT '0',
  dept_name varchar(100) NOT NULL,
  dept_type varchar(50) DEFAULT NULL COMMENT 'company/department/team',
  sort_order int DEFAULT '0',
  status tinyint DEFAULT '1' COMMENT '1-启用 0-禁用',
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='部门组织架构';

-- 初始化管理员账号（密码: admin / 123456）
INSERT INTO sys_user (username, password, real_name, phone, role_ids, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000000', '1,2', 1);

-- 初始化部门数据
INSERT INTO sys_dept (parent_id, dept_name, dept_type, sort_order) VALUES
(0, '山西同业电力', 'company', 1),
(1, '项目管理部', 'department', 2),
(1, '安全质量部', 'department', 3),
(1, '工程技术部', 'department', 4);

-- ============================================
-- 第二轮补齐: 38个缺失API所需的表
-- ============================================

-- 设备资产表（供 equipmentAssets/* 使用）
CREATE TABLE IF NOT EXISTS equipment_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_code VARCHAR(100) COMMENT '设备编号',
    device_type VARCHAR(100) COMMENT '设备类型(摄像头/扬尘/噪声/塔吊等)',
    project_id BIGINT DEFAULT 0 COMMENT '所属项目',
    location_id BIGINT DEFAULT 0 COMMENT '位置节点ID',
    monitor_point_type VARCHAR(50) COMMENT '监测点类型',
    status INT DEFAULT 0 COMMENT '0-离线 1-在线',
    video_monitor_id BIGINT DEFAULT 0 COMMENT '关联摄像头ID',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型号',
    install_date DATE COMMENT '安装日期',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    create_by VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备资产';

-- 单体楼栋表（供 singleBuildingInfo/* 使用）
CREATE TABLE IF NOT EXISTS single_building_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目',
    building_name VARCHAR(200) NOT NULL COMMENT '楼栋名称',
    building_code VARCHAR(100) COMMENT '楼栋编号',
    building_type VARCHAR(50) COMMENT '类型(住宅/商业/配套)',
    total_floors INT DEFAULT 0 COMMENT '总楼层',
    total_area DECIMAL(18,2) DEFAULT 0 COMMENT '总面积(m²)',
    start_date DATE COMMENT '开工日期',
    end_date DATE COMMENT '竣工日期',
    status INT DEFAULT 0 COMMENT '0-未开工 1-施工中 2-已竣工',
    remark VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_by VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单体楼栋信息';

-- 监测点告警表（供 monitorPointAlert/* 使用）
CREATE TABLE IF NOT EXISTS monitor_point_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT DEFAULT 0 COMMENT '所属项目',
    device_asset_id BIGINT DEFAULT 0 COMMENT '关联设备ID',
    point_type VARCHAR(50) NOT NULL COMMENT '监测类型(扬尘/噪声/水电/温湿度)',
    alert_content VARCHAR(500) COMMENT '告警内容',
    alert_level VARCHAR(20) DEFAULT 'normal' COMMENT '级别(normal/warning/critical)',
    alert_value DECIMAL(18,2) COMMENT '触发值',
    threshold_value DECIMAL(18,2) COMMENT '阈值',
    status INT DEFAULT 0 COMMENT '0-未处理 1-已处理 2-已忽略',
    handle_time DATETIME COMMENT '处理时间',
    handle_by VARCHAR(100) COMMENT '处理人',
    handle_remark VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测点告警';

-- 水电供应点表（供 adminSupplyPoint/* 使用）
CREATE TABLE IF NOT EXISTS supply_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT DEFAULT 0 COMMENT '所属项目',
    point_name VARCHAR(200) NOT NULL COMMENT '供应点名称',
    point_type VARCHAR(20) COMMENT 'water-供水 electric-供电',
    device_code VARCHAR(100) COMMENT '设备编号',
    location_desc VARCHAR(300) COMMENT '位置描述',
    unit_price DECIMAL(10,4) DEFAULT 0 COMMENT '单价',
    current_reading DECIMAL(18,2) DEFAULT 0 COMMENT '当前读数',
    status INT DEFAULT 0 COMMENT '0-停用 1-运行中',
    is_deleted INT DEFAULT 0,
    create_by VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水电供应点';

-- 质量资料变更日志表（供 qualMaterialChangelog/* 使用）
CREATE TABLE IF NOT EXISTS qual_material_changelog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT NOT NULL COMMENT '关联资料ID',
    project_id BIGINT DEFAULT 0,
    change_type VARCHAR(50) COMMENT '变更类型(add/update/delete/collect)',
    change_content TEXT COMMENT '变更内容',
    operator VARCHAR(100) COMMENT '操作人',
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量资料变更日志';

-- 安全资料变更日志表（供 safetyMaterialChangelog/* 使用）
CREATE TABLE IF NOT EXISTS safety_material_changelog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT NOT NULL COMMENT '关联资料ID',
    project_id BIGINT DEFAULT 0,
    change_type VARCHAR(50) COMMENT '变更类型',
    change_content TEXT COMMENT '变更内容',
    operator VARCHAR(100) COMMENT '操作人',
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全资料变更日志';

-- 评价等级表（供 evalLevel/* 使用）
CREATE TABLE IF NOT EXISTS eval_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT DEFAULT 0,
    level_name VARCHAR(100) NOT NULL COMMENT '等级名称(优/良/合格/不合格)',
    level_type VARCHAR(50) COMMENT '评价类型(safety/quality/progress)',
    score_min DECIMAL(5,2) COMMENT '最低分',
    score_max DECIMAL(5,2) COMMENT '最高分',
    color VARCHAR(20) COMMENT '显示颜色',
    remark VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价等级';

-- 系统配置表（供 adminConfig/* 使用）
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    config_desc VARCHAR(500) COMMENT '说明',
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- ============================================
-- Phase 2: RBAC 权限系统
-- ============================================

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色显示名',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识(admin/manager/safety_officer/...）',
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(255) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID（0=顶级）',
    name VARCHAR(100) NOT NULL COMMENT '菜单/权限名称',
    permission_key VARCHAR(100) COMMENT '权限标识符(build:safetyMaterial:list）',
    path VARCHAR(200) COMMENT '前端路由路径',
    icon VARCHAR(100) COMMENT '菜单图标',
    menu_type TINYINT DEFAULT 1 COMMENT '1-目录 2-菜单 3-按钮/权限点',
    visible TINYINT DEFAULT 1 COMMENT '1-显示 0-隐藏',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    is_deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单/权限';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- ========== 种子数据：角色 ==========
INSERT IGNORE INTO sys_role (role_name, role_key, status, sort_order, remark) VALUES
('超级管理员', 'admin', 1, 1, '系统超级管理员，拥有所有权限'),
('项目经理', 'project_manager', 1, 2, '项目管理，查看本项目所有数据'),
('安全员', 'safety_officer', 1, 3, '安全质量管理，编辑安全/质量资料'),
('资料员', 'data_clerk', 1, 4, '资料管理与归档'),
('普通用户', 'user', 1, 5, '基础查看权限');

-- ========== 种子数据：菜单树 ==========
INSERT IGNORE INTO sys_menu (parent_id, name, permission_key, path, icon, menu_type, sort_order) VALUES
-- 一级目录（首页）
(0, '首页', 'dashboard', '/dashboard', 'dashboard', 1, 1),
  (1, '项目看板', 'dashboard:view', '/dashboard', 'dashboard', 2, 1),
-- 一级目录（人员管理）
(0, '人员管理', 'worker', '/worker', 'team', 1, 2),
  (3, '人员花名册', 'worker:list', '/worker/list', 'list', 2, 1),
  (3, '班组管理', 'worker:team', '/worker/team', 'team', 2, 2),
  (3, '考勤管理', 'worker:attendance', '/worker/attendance', 'attendance', 2, 3),
-- 一级目录（设备管理）
(0, '设备管理', 'device', '/device', 'camera', 1, 3),
  (7, '设备资产', 'device:assets', '/device/assets', 'list', 2, 1),
  (7, '视频监控', 'device:camera', '/device/camera', 'camera', 2, 2),
  (7, '监测数据', 'device:monitor', '/device/monitor', 'data', 2, 3),
  (7, '变电站设备', 'device:substation', '/device/substation', 'cluster', 2, 4),
  (7, '输电线路', 'device:transmission', '/device/transmission', 'line-chart', 2, 5),
-- 一级目录（安全质量）
(0, '安全质量', 'safety', '/safety', 'shield', 1, 4),
  (11, '安全资料', 'safety:material', '/safety/material', 'file', 2, 1),
  (11, '质量资料', 'safety:quality', '/safety/quality', 'check-circle', 2, 2),
-- 一级目录（进度管理）
(0, '进度管理', 'progress', '/progress', 'progress', 1, 5),
  (14, '进度计划', 'progress:plan', '/progress/plan', 'plan', 2, 1),
  (14, '进度详情', 'progress:detail', '/progress/detail', 'detail', 2, 2),
-- 一级目录（系统管理）
(0, '系统管理', 'system', '/system', 'setting', 1, 99),
  (17, '用户管理', 'system:user', '/system/user', 'user', 2, 1),
  (17, '角色管理', 'system:role', '/system/role', 'role', 2, 2),
  (17, '菜单管理', 'system:menu', '/system/menu', 'menu', 2, 3),
  (17, '部门管理', 'system:dept', '/system/dept', 'dept', 2, 4),
  (17, '系统配置', 'system:config', '/system/config', 'config', 2, 5);

-- ========== 种子数据：角色-菜单（管理员 = 全部菜单） ==========
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE status = 1 AND is_deleted = 0;

-- ========== 种子数据：admin 用户关联角色 ==========
INSERT IGNORE INTO user_role (user_id, role_id) VALUES (1, 1);

-- ============================================
-- Phase 3: 审批流 + 文件存储
-- ============================================

-- 审批节点配置表
CREATE TABLE IF NOT EXISTS approval_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(50) NOT NULL COMMENT '业务类型(hazard/work_order/...)',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称(如"安全员初审"/"项目经理终审")',
    node_order INT DEFAULT 1 COMMENT '节点顺序(1开始)',
    role_key VARCHAR(50) NOT NULL COMMENT '负责角色(admin/safety_officer/...)',
    node_action VARCHAR(50) COMMENT '节点动作(pass/reject/escalate)',
    timeout_hours INT DEFAULT 0 COMMENT '超时小时数(0=不限制)',
    auto_action VARCHAR(20) DEFAULT 'escalate' COMMENT '超时自动处理(escalate/pass/reject)',
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_biz (biz_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点配置';

-- 审批记录表
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(50) NOT NULL COMMENT '业务类型(hazard/work_order)',
    biz_id BIGINT NOT NULL COMMENT '业务ID(隐患/工单ID)',
    node_id BIGINT COMMENT '审批节点ID',
    node_name VARCHAR(100) COMMENT '节点名称',
    action VARCHAR(20) NOT NULL COMMENT '操作(pass/reject/escalate/rework)',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    comment VARCHAR(500) COMMENT '审批意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_biz (biz_type, biz_id),
    KEY idx_operator (operator_id),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录';

-- 文件存储记录表
CREATE TABLE IF NOT EXISTS file_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(20) COMMENT '文件类型(pdf/doc/jpg/png/...)',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    storage_type VARCHAR(20) DEFAULT 'local' COMMENT '存储类型(local/oss/s3)',
    biz_type VARCHAR(50) COMMENT '关联业务类型(hazard_material/safety_material/...)',
    biz_id BIGINT COMMENT '关联业务ID',
    upload_by BIGINT COMMENT '上传人ID',
    upload_by_name VARCHAR(50) COMMENT '上传人姓名',
    is_deleted INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_biz (biz_type, biz_id),
    KEY idx_uploader (upload_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储记录';

-- 隐患与审批流程关联表
CREATE TABLE IF NOT EXISTS hazard_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hazard_id BIGINT NOT NULL COMMENT '隐患ID',
    current_node INT DEFAULT 1 COMMENT '当前审批节点序号',
    total_nodes INT DEFAULT 0 COMMENT '总节点数',
    approval_status VARCHAR(20) DEFAULT 'pending' COMMENT '审批状态(pending/approving/approved/rejected/escalated)',
    started_at DATETIME COMMENT '审批开始时间',
    finished_at DATETIME COMMENT '审批完成时间',
    escalated TINYINT DEFAULT 0 COMMENT '是否已升级',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hazard (hazard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐患审批流程';

-- ========== 种子数据：隐患审批节点 ==========
INSERT IGNORE INTO approval_node (biz_type, node_name, node_order, role_key, timeout_hours, auto_action) VALUES
('hazard', '安全员初审', 1, 'safety_officer', 12, 'escalate'),
('hazard', '安全负责人复核', 2, 'safety_officer', 24, 'escalate'),
('hazard', '项目经理终审', 3, 'project_manager', 48, 'escalate');

-- ============================================
-- Phase 4: 实时与推送
-- ============================================

-- 告警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    device_type VARCHAR(50) COMMENT '设备类型(塔吊/电焊机/扬尘/...)（空=通用）',
    sensor_type VARCHAR(50) NOT NULL COMMENT '传感器类型(load/tilt/temperature/pm25/noise/...)',
    operator VARCHAR(10) NOT NULL COMMENT '比较符(gt/lt/gte/lte/eq)',
    warning_threshold DECIMAL(18,4) COMMENT '告警阈值',
    critical_threshold DECIMAL(18,4) COMMENT '严重告警阈值(可选)',
    duration_seconds INT DEFAULT 0 COMMENT '持续超限秒数(防抖)',
    enabled TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    remark VARCHAR(255) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则';

-- 系统通知表
CREATE TABLE IF NOT EXISTS system_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content VARCHAR(1000) COMMENT '通知内容',
    biz_type VARCHAR(50) COMMENT '业务类型(hazard_approval/device_alarm/work_order/...)',
    biz_id BIGINT COMMENT '业务ID',
    level VARCHAR(20) DEFAULT 'info' COMMENT '级别(info/warning/critical)',
    is_read TINYINT DEFAULT 0 COMMENT '0-未读 1-已读',
    read_at DATETIME COMMENT '读取时间',
    is_deleted INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id, is_read),
    KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知';

-- ========== 种子数据：默认告警规则 ==========
INSERT IGNORE INTO alert_rule (rule_name, device_type, sensor_type, operator, warning_threshold, critical_threshold, duration_seconds) VALUES
('塔吊负载告警', '塔吊', 'load', 'gt', 80.0000, 90.0000, 5),
('塔吊倾斜告警', '塔吊', 'tilt', 'gt', 2.0000, 3.0000, 3),
('塔吊风速告警', '塔吊', 'wind_speed', 'gt', 10.7000, 13.8000, 10),
('电焊机温度告警', '电焊机', 'temperature', 'gt', 65.0000, 85.0000, 5),
('扬尘PM2.5告警', NULL, 'pm25', 'gt', 75.0000, 150.0000, 10),
('扬尘PM10告警', NULL, 'pm10', 'gt', 150.0000, 250.0000, 10),
('噪声告警', NULL, 'noise', 'gt', 70.0000, 85.0000, 10);

-- ========== Phase 5A：变电站设备台账 ==========

CREATE TABLE IF NOT EXISTS substation_equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型: GIS/transformer/breaker',
    device_code VARCHAR(100) NOT NULL COMMENT '设备编号（唯一标识）',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    bay_number VARCHAR(50) COMMENT '间隔编号（GIS专用）',
    voltage_level VARCHAR(20) COMMENT '电压等级: 110kV/220kV/500kV',

    -- 通用资产字段
    manufacturer VARCHAR(200) COMMENT '制造商',
    model VARCHAR(200) COMMENT '型号',
    serial_number VARCHAR(100) COMMENT '出厂编号',
    manufacture_date DATE COMMENT '出厂日期',
    install_date DATE COMMENT '安装日期',
    commission_date DATE COMMENT '投运日期',
    design_life_years INT COMMENT '设计寿命(年)',
    status VARCHAR(20) DEFAULT 'in_service' COMMENT '运行状态: in_service/maintenance/retired/fault',
    last_maintenance_date DATE COMMENT '最近检修日期',
    next_maintenance_date DATE COMMENT '下次检修日期',

    -- GIS 专用
    gas_type VARCHAR(50) COMMENT '绝缘气体类型(SF6/混合气体)',
    sf6_pressure_kpa DECIMAL(10,2) COMMENT 'SF6气压(kPa)',
    sf6_alarm_pressure_kpa DECIMAL(10,2) COMMENT 'SF6告警气压(kPa)',
    sealed_parts_count INT COMMENT '密封气室数量',

    -- 变压器(Transformer) 专用
    rated_capacity_mva DECIMAL(12,2) COMMENT '额定容量(MVA)',
    cooling_method VARCHAR(50) COMMENT '冷却方式(ONAN/OFAF/ODAF)',
    tap_changer_type VARCHAR(50) COMMENT '分接开关类型(OLTC/off-circuit)',
    tap_changer_positions INT COMMENT '分接档位数',
    oil_type VARCHAR(50) COMMENT '绝缘油类型(矿物油/天然酯)',
    oil_weight_kg DECIMAL(10,2) COMMENT '油重(kg)',
    winding_connection VARCHAR(10) COMMENT '绕组连接组别(YNd11/Yyn0)',

    -- 断路器(Breaker) 专用
    rated_current_ka DECIMAL(10,2) COMMENT '额定电流(kA)',
    rated_voltage_kv DECIMAL(10,2) COMMENT '额定电压(kV)',
    rated_breaking_current_ka DECIMAL(10,2) COMMENT '额定开断电流(kA)',
    operating_mechanism VARCHAR(50) COMMENT '操动机构类型(spring/hydraulic/pneumatic)',
    operating_voltage_v INT COMMENT '操作电压(V)',
    mechanical_operations INT DEFAULT 0 COMMENT '累计机械操作次数',
    breaking_count INT DEFAULT 0 COMMENT '累计开断次数',

    -- 位置/关联
    location_desc VARCHAR(255) COMMENT '物理位置描述',
    longitude DECIMAL(11,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    parent_id BIGINT COMMENT '所属上级设备ID(如变压器下套管)',
    video_monitor_id BIGINT COMMENT '关联视频监控ID',

    -- 台账附件
    attachment_json TEXT COMMENT '附件列表JSON(出厂报告/试验报告)',
    remark TEXT COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY idx_project_device_type (project_id, device_type),
    KEY idx_device_code (device_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='变电站设备台账';

-- 变电站巡检记录
CREATE TABLE IF NOT EXISTS substation_inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    inspector VARCHAR(50) NOT NULL COMMENT '巡检人',
    inspection_type VARCHAR(20) DEFAULT 'routine' COMMENT '类型: routine/patrol/special',
    inspection_date DATE NOT NULL COMMENT '巡检日期',
    content TEXT COMMENT '检测内容JSON',
    sf6_pressure DECIMAL(10,2) COMMENT 'SF6气压(kPa)',
    temperature DECIMAL(6,2) COMMENT '温度(°C)',
    noise_db DECIMAL(6,2) COMMENT '噪音(dB)',
    vibration_mm DECIMAL(8,4) COMMENT '振动幅值(mm)',
    result VARCHAR(10) DEFAULT 'normal' COMMENT '结果: normal/abnormal/urgent',
    description TEXT COMMENT '巡检描述/异常说明',
    image_json TEXT COMMENT '现场照片(JSON数组)',
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_equipment (equipment_id),
    KEY idx_date (inspection_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='变电站巡检记录';

-- ========== Phase 5B：输电线路（杆塔/档距） ==========

CREATE TABLE IF NOT EXISTS transmission_tower (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    tower_code VARCHAR(100) NOT NULL COMMENT '杆塔编号（唯一，如#001-#N）',
    tower_name VARCHAR(200) COMMENT '杆塔名称',
    tower_type VARCHAR(30) NOT NULL COMMENT '类型: angle/tension/suspension/terminal/transition',
    voltage_level VARCHAR(20) COMMENT '电压等级: 110kV/220kV/500kV',
    height_m DECIMAL(8,2) COMMENT '杆塔高度(m)',
    latitude DECIMAL(10,7) COMMENT '纬度',
    longitude DECIMAL(11,7) COMMENT '经度',
    altitude_m DECIMAL(8,2) COMMENT '海拔(m)',
    foundation_type VARCHAR(50) COMMENT '基础类型: cast_in_place/pile/rock',
    foundation_depth_m DECIMAL(6,2) COMMENT '基础埋深(m)',
    leg_count INT DEFAULT 4 COMMENT '塔腿数量',
    manufacturer VARCHAR(200) COMMENT '制造商',
    model VARCHAR(200) COMMENT '型号',
    serial_number VARCHAR(100) COMMENT '出厂编号',
    manufacture_date DATE COMMENT '出厂日期',
    install_date DATE COMMENT '安装日期',
    design_life_years INT COMMENT '设计寿命(年)',
    status VARCHAR(20) DEFAULT 'in_service' COMMENT '运行状态: in_service/maintenance/retired',
    last_inspection_date DATE COMMENT '最近巡检日期',
    next_inspection_date DATE COMMENT '下次巡检日期',
    image_json TEXT COMMENT '杆塔照片JSON',
    remark TEXT COMMENT '备注',
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tower_code (project_id, tower_code),
    KEY idx_project (project_id),
    KEY idx_type (tower_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输电线路杆塔台账';

-- 档距/弧垂表（一对二：from_tower ↔ to_tower 构成一个档距）
CREATE TABLE IF NOT EXISTS transmission_span (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    span_code VARCHAR(100) NOT NULL COMMENT '档距编号',
    from_tower_id BIGINT NOT NULL COMMENT '起始杆塔ID',
    to_tower_id BIGINT NOT NULL COMMENT '终止杆塔ID',
    span_length_m DECIMAL(10,2) NOT NULL COMMENT '档距长度(m)',
    conductor_type VARCHAR(50) COMMENT '导线类型(LGJ/LGJF/钢芯铝绞线)',
    conductor_spec VARCHAR(100) COMMENT '导线规格(如LGJ-300/40)',
    circuit_count INT DEFAULT 1 COMMENT '回路数',
    design_sag_m DECIMAL(8,2) COMMENT '设计弧垂(m)',
    current_sag_m DECIMAL(8,2) COMMENT '当前弧垂(m)',
    max_sag_allowed_m DECIMAL(8,2) COMMENT '最大允许弧垂(m)',
    sag_alarm_threshold_pct DECIMAL(5,2) DEFAULT 90 COMMENT '弧垂告警阈值(%)',
    max_wind_speed_ms DECIMAL(6,2) COMMENT '最大设计风速(m/s)',
    min_clearance_m DECIMAL(6,2) COMMENT '最小对地安全距离(m)',
    terrain_type VARCHAR(30) COMMENT '地形类型: plain/hill/mountain/crossing',
    crossing_desc VARCHAR(255) COMMENT '交叉跨越描述(跨越公路/铁路/河流)',
    last_inspection_date DATE COMMENT '最近巡检日期',
    next_inspection_date DATE COMMENT '下次巡检日期',
    status VARCHAR(20) DEFAULT 'normal' COMMENT '状态: normal/warning/critical',
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_span_code (project_id, span_code),
    KEY idx_from_tower (from_tower_id),
    KEY idx_to_tower (to_tower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输电线路档距/弧垂台账';

-- ========== Phase 5C：安全围栏/电子围栏 ==========

CREATE TABLE IF NOT EXISTS safety_fence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    fence_name VARCHAR(100) NOT NULL COMMENT '围栏名称',
    fence_type VARCHAR(20) NOT NULL COMMENT '类型: circle(圆形)/polygon(多边形)',
    color VARCHAR(10) DEFAULT '#FF0000' COMMENT '围栏显示颜色 #RRGGBB',
    description TEXT COMMENT '围栏描述',
    -- 圆形参数
    center_lat DECIMAL(10,7) COMMENT '中心点纬度',
    center_lng DECIMAL(11,7) COMMENT '中心点经度',
    radius_m DECIMAL(10,2) COMMENT '半径(米)',
    -- 多边形参数
    polygon_points TEXT COMMENT '多边形顶点坐标JSON: [[lng,lat],[lng,lat],...]',
    alert_level VARCHAR(10) DEFAULT 'medium' COMMENT '告警级别: high/medium/low',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 1-启用 0-禁用',
    create_by VARCHAR(50) COMMENT '创建人',
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_project (project_id),
    KEY idx_type (fence_type),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全围栏/电子围栏';

CREATE TABLE IF NOT EXISTS fence_alert_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fence_id BIGINT NOT NULL COMMENT '关联围栏ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    event_type VARCHAR(10) NOT NULL COMMENT '事件类型: enter(进入)/leave(离开)',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型: person/device/vehicle',
    target_id VARCHAR(100) NOT NULL COMMENT '目标ID(人员ID/设备编号)',
    target_name VARCHAR(100) COMMENT '目标名称(人员姓名/设备名称)',
    event_lat DECIMAL(10,7) COMMENT '事件发生纬度',
    event_lng DECIMAL(11,7) COMMENT '事件发生经度',
    description TEXT COMMENT '事件描述',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending(待处理)/processed(已处理)/ignored(已忽略)',
    processed_by VARCHAR(50) COMMENT '处理人',
    processed_at DATETIME COMMENT '处理时间',
    remark TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_fence (fence_id),
    KEY idx_project (project_id),
    KEY idx_status (status),
    KEY idx_event_type (event_type),
    KEY idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='围栏告警事件';

-- 默认菜单条目示例（可选）
INSERT IGNORE INTO sys_permission (permission_name, permission_code, module, parent_id, sort_order, is_deleted, created_at)
SELECT '安全围栏', 'safetyFence', 'device', id, 90, 0, NOW()
FROM sys_permission WHERE permission_code = 'device_root' AND NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'safetyFence'
);

INSERT IGNORE INTO sys_permission (permission_name, permission_code, module, parent_id, sort_order, is_deleted, created_at)
SELECT '围栏告警', 'fenceAlertEvent', 'device', id, 91, 0, NOW()
FROM sys_permission WHERE permission_code = 'device_root' AND NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'fenceAlertEvent'
);
