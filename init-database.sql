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
