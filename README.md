# 电力智慧工地平台 (Power Smart Site)

> 由 **同业电力 (tongye)** 打造的电力行业智慧工地综合管理平台。
> 基于 Spring Cloud Alibaba 微服务架构，覆盖人员、设备、安全、进度、物资五大核心领域，集成 IoT（MQTT）、AI 视觉识别、时序数据库（InfluxDB）等能力。

---

## 📋 目录

- [项目概览](#项目概览)
- [技术栈](#技术栈)
- [模块架构](#模块架构)
- [微服务列表](#微服务列表)
- [数据库模型](#数据库模型)
- [功能详解](#功能详解)
  - [人员管理 (worker)](#人员管理-worker)
  - [设备管理 (device)](#设备管理-device)
  - [安全管理 (hazard)](#安全管理-hazard)
  - [进度管理 (progress)](#进度管理-progress)
  - [系统管理 (system)](#系统管理-system)
  - [数据看板 (dashboard)](#数据看板-dashboard)
- [API 一览](#api-一览)
- [快速启动](#快速启动)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [参考](#参考)

---

## 项目概览

**电力智慧工地**是专为电力行业（变电站、输电线路、配电工程等）建设项目设计的工地数字化管理平台。系统提供从人员入场、设备台账、隐患上报/整改/验收、AI 安全监控、特种作业票审批到物资进销存的完整闭环管理能力。

**核心价值：**
- 🏗️ **统一管理** — 10 个微服务模块，83 张业务表，覆盖工地全场景
- 🔗 **前端兼容** — API 兼容 tongye TYConstruction 前端规范，可平滑对接现有前端
- 🤖 **AI 赋能** — 摄像头 AI 违规识别、智能巡检、自动告警
- 🔌 **IoT 集成** — MQTT 实时设备数据采集，时序数据库存储分析
- 📊 **数据驱动** — 六维数据看板，多维度聚合分析

**项目规模：** ~23,800 行代码（371 个 Java 文件 + SQL + XML）
**代码仓库：** https://github.com/silingling/power-smart-site

## 技术栈

### 后端核心

| 技术 | 用途 | 版本 |
|------|------|------|
| Spring Cloud Alibaba | 微服务架构 | 2021.x |
| Spring Boot | 应用框架 | 2.7.x |
| Spring Cloud Gateway | API 网关 + 限流 | - |
| Nacos | 注册中心 + 配置中心 | - |
| MyBatis-Plus | ORM（代码生成 + 分页 + 自动填充） | 3.5.x |
| MySQL 8.0 | 关系型数据库（主业务） | 8.0 |
| Redis | 缓存 + 分布式锁 + 限流 | - |

### 扩展能力

| 技术 | 用途 | 说明 |
|------|------|------|
| InfluxDB 2.x | 时序数据库 | 设备传感器/告警历史数据存储 |
| MQTT (Eclipse Paho) | IoT 物联网通信 | 设备实时数据上报 |
| Netty | 高性能网络框架 | 视频流/告警推送 |
| RabbitMQ | 消息队列 | 异步通知分发（邮件/SMS/飞书/站内信） |

### 基础设施

| 技术 | 用途 |
|------|------|
| Knife4j (Swagger) | API 文档自动生成 |
| Lombok | 代码简化 |
| Hutool | 工具库 |
| Jackson (FastJSON2) | JSON 处理 |
| JWT | 认证授权 |
| SLF4J + Logback | 日志 |
| OSS (MinIO) | 文件存储 |

### 前端

| 技术 | 用途 |
|------|------|
| Vue 2 | 前端框架 |
| 前端规范 | 兼容 tongye TYConstruction 规范 |
| API 通信 | 通过 Spring Cloud Gateway（端口 8080） |

## 模块架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Spring Cloud Gateway                     │
│              端口 8080 · 路由 · 限流 · CORS · JWT 鉴权        │
└──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬────┘
       │      │      │      │      │      │      │      │
  ┌────┴┐ ┌──┴──┐ ┌┴───┐ ┌┴───┐ ┌┴────┐ ┌┴────┐ ┌┴────┐ ┌┴───┐
  │worker│ │device│hazard│progress│system│dashboard│ │ api │
  │8081  │ │8082  │8084  │8083    │8085  │8086     │ │     │
  └─────┘ └─────┘ └────┘ └──────┘ └─────┘ └───────┘ └─────┘
                          │
                    ┌─────┴─────┐
                    │  common   │
                    │ (公共模块) │
                    └───────────┘
```

### 模块职责

| 模块 | 端口 | 文件 | 行数 | 核心功能 |
|------|------|------|------|----------|
| **worker** | 8081 | 35 | 1,368 | 人员档案/班组/考勤/分包商/工种/进出场 |
| **device** | 8082 | 81 | 5,126 | 设备台账/传感器/告警引擎/MQTT/视频监控/围栏/物资 |
| **progress** | 8083 | 27 | 1,192 | 施工日志/安全日志/电子签名 |
| **hazard** | 8084 | 112 | 6,647 | 隐患闭环/AI视觉/巡检/特种作业票/安全资料/应急 |
| **system** | 8085 | 82 | 5,198 | 用户/项目/权限/通知/报表/系统配置 |
| **dashboard** | 8086 | 2 | 308 | 六维数据看板 |
| **gateway** | 8080 | 3 | 204 | 路由/限流/JWT鉴权/CORS |
| **common** | — | 28 | 1,574 | 公共工具/实体/异常/注解/配置 |
| **api** | — | 1 | 19 | Feign 跨服务调用客户端 |

## 微服务列表

### 人员管理 (worker) — 端口 8081

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `WorkerController` | `/build/worker` | 工人档案 CRUD（姓名/身份证/手机/人脸/工种/培训/状态） |
| `WorkerTeamController` | `/build/workerTeam` | 班组管理（含班组长/成员/工种） |
| `LabourSubcontractorController` | `/build/labourSubcontractor` | 分包商管理 |
| `LabourConstructionUnitController` | `/build/labourConstructionUnit` | 建设单位管理 |
| `LabourWorktypeController` | `/build/labourWorktype` | 工种字典 |
| `LabourAdvanceRetreatController` | `/build/labourAdvanceRetreat` | 进出场记录（入场/退场/人脸比对/安全教育） |
| `BLabourAttendanceRecordController` | `/build/attendanceRecord` | 考勤记录 |
| `LabourInfocollectionController` | `/build/labourInfocollection` | 人员信息采集 |

### 设备管理 (device) — 端口 8082

**设备台账类：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `DeviceController` | `/build/device` | 通用设备 CRUD |
| `EquipmentAssetsController` | `/build/equipmentAssets` | 设备资产管理 |
| `EquipmentLocationController` | `/build/equipmentLocation` | 设备位置台账树（按项目/区域/楼层组织） |
| `SubstationEquipmentController` | `/build/substationEquipment` | 变电站设备台账（含油浸式变压器参数/SF6/有载调压等电力专项字段） |
| `SubstationInspectionController` | `/build/substationInspection` | 变电站巡检管理 |
| `TransmissionController` | `/build/transmissionTower` | 输电铁塔台账 |
| `TransmissionSpanController` | `/build/transmissionSpan` | 输电线路档距管理 |
| `SupplyPointController` | `/build/supplyPoint` | 水电供应点 |
| `SingleBuildingInfoController` | `/build/singleBuildingInfo` | 单体楼栋信息 |

**传感器 & IoT：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `EqMonitorDataAtController` | - | 传感器时序数据查询（InfluxDB） |
| `AlertRuleController` | `/build/alertRule` | 告警规则 CRUD |
| `MonitorPointAlertController` | `/build/monitorPointAlert` | 监测点告警管理 |
| `FenceAlertEventController` | `/build/fenceAlertEvent` | 围栏告警事件（电子围栏越界告警） |

**视频 & 围栏：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `VideoMonitorController` | `/build/videoMonitor` + `/build/ysy` | 视频监控管理 |
| `SafetyFenceController` | `/build/safetyFence` | 安全围栏管理（位置/状态/人员） |

**物资管理：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `MaterialCategoryController` | `/build/materialCategory` | 物资分类 |
| `MaterialInfoController` | `/build/materialInfo` | 物资信息（名称/规格/单位/库存上限下限） |
| `MaterialStockRecordController` | `/build/materialStockRecord` | 物资出入库流水 |
| `MaterialCheckController` | `/build/materialCheck` | 物资盘点（含盘点明细） |

**通知设备：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `NotificationController` | `/build/notification` + `/api/v1/sse` | 设备端通知（SSE 推送 + 在线状态 + 未读数） |

### 安全管理 (hazard) — 端口 8084（最大模块）

**隐患闭环：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `HazardController` | `/hazard/workOrder` | 工单增删改查 + 状态流转 |
| `HazardBuildController` | `/build/hazardReport` | 隐患上报/列表/详情/统计/更新/删除 + 审批 |
| `HazardWorkOrderController` | `/build/hazardWorkOrder` | 整改工单创建/列表/整改提交/验收 |

**AI 视觉识别：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `AiViolationController` | `/build/aiViolation` | AI 违规识别记录（未戴安全帽/未穿反光衣/禁区闯入等） |
| `AiCameraController` | `/build/aiCamera` | AI 摄像头管理 |
| `AiSnapshotController` | `/build/aiSnapshot` | AI 抓拍快照管理 |
| `AiDetectionCallbackController` | `/build/aiDetectionCallback` | AI 检测回调接口 |

**施工区域 & 围栏：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `ConstructionAreaController` | `/build/constructionArea` | 施工区域管理（含风险等级/责任人） |

**巡检管理：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `InspectionPlanController` | `/build/inspectionPlan` | 巡检计划（周期/区域/路线/责任人） |
| `InspectionTemplateController` | `/build/inspectionTemplate` | 巡检模板（检查项/标准/阈值） |
| `InspectionTaskController` | `/build/inspectionTask` | 巡检任务生成/分配/执行 |
| `InspectionRecordController` | `/build/inspectionRecord` | 巡检记录（结果/图片/异常） |
| `InspectionIssueController` | `/build/inspectionIssue` | 巡检发现的问题闭环 |

**特种作业票：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `SpecialWorkPermitController` | `/build/specialWorkPermit` | 动火/登高/受限空间/临时用电/吊装/挖掘/占道 七类特种作业票全生命周期管理（申请→提交→审批→开工→完工→验收→关闭→取消→延期） |

**安全资料 & 质量资料：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `SafetyMaterialController` | `/build/safetyMaterial` | 安全资料管理 |
| `SafetyMaterialCatalogController` | `/build/safetyMaterialCatalog` | 安全资料目录 |
| `SafetyMaterialChangelogController` | `/build/safetyMaterialChangelog` | 安全资料变更日志 |
| `QualMaterialController` | `/build/qualMaterial` | 质量资料管理 |
| `QualMaterialCatalogController` | `/build/qualMaterialCatalog` | 质量资料目录 |
| `QualMaterialChangelogController` | `/build/qualMaterialChangelog` | 质量资料变更日志 |

**应急管理：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `EmergencyPlanController` | `/build/emergencyPlan` | 应急预案管理 |
| `EmergencyDrillController` | `/build/emergencyDrill` | 应急演练管理 |
| `EmergencySupplyController` | `/build/emergencySupply` | 应急物资管理 |
| `EmergencySupplyRecordController` | `/build/emergencySupplyRecord` | 应急物资出入库记录 |
| `EmergencyIncidentController` | `/build/emergencyIncident` | 应急事件管理 |
| `EmergencyContactController` | `/build/emergencyContact` | 应急联系人管理 |

**其他：**

| Controller | 功能描述 |
|-----------|----------|
| `FileController` | 文件上传（支持分片/断点续传） |

### 进度管理 (progress) — 端口 8083

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `ProgressController` | `/build/progress` | 施工任务进度管理 |
| `ProgressPlanManageDetailsController` | `/progress/details` | 计划进度明细 |
| `ConstructionLogController` | `/build/constructionLog` | 施工日志（天气/人员/机械/工作量/质量/问题） |
| `SafetyLogController` | `/build/safetyLog` | 安全日志（巡查/教育/交底/隐患/整改） |
| `LogTemplateController` | `/build/logTemplate` | 日志模板管理 |
| `ElectronicSignatureController` | `/build/electronicSignature` | 电子签名管理 |

### 系统管理 (system) — 端口 8085

**基础管理：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `AdminController` | 多路径（`/login` / `/adminUser/*` / `/adminDept/*` / `/adminCommon/*`） | 登录/登出/用户管理/部门管理/系统状态 |
| `AdminConfigController` | `/adminConfig` | 系统配置管理 |
| `AdminRoleController` | `/adminRole` | 角色/权限管理 |
| `AdminMenuController` | `/adminMenu` | 菜单管理 |
| `SysUserController` | `/build/sysUser` | 系统用户管理 |
| `ProjectController` | `/build/project` | 项目/工程管理 |
| `ProjectInfoController` | `/build/projectInfo` | 项目信息（含 PmsId 映射） |
| `ProjectUserController` | `/build/projectUser` | 项目人员分配 |
| `EvalLevelController` | `/build/evalLevel` | 评价等级管理 |

**通知中心：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `NotificationTemplateController` | `/build/notificationTemplate` | 通知模板（占位符/多语言） |
| `NotificationSubscriptionController` | `/build/notificationSubscription` | 通知订阅（用户按类型/渠道订阅） |
| `NotificationChannelConfigController` | `/build/notificationChannelConfig` | 通知渠道配置（邮件/SMS/飞书Webhook/站内信） |
| `NotificationDispatchController` | `/build/notificationDispatch` | 通知发送/重试/状态查看 |

**报表 & 导出：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `ReportTemplateController` | `/build/reportTemplate` | 报表模板管理 |
| `ReportExportController` | `/build/reportExport` | 报表导出（异步任务/Excel/PDF） |
| `ExcelExportController` | `/build/excelExport` | Excel 通用导出 |

**其他：**

| Controller | 路径前缀 | 功能描述 |
|-----------|---------|----------|
| `ApiFileController` | `/build/apiFile` | 文件管理 |
| `PmsSyncController` | `/build/pmsSync` | PMS 数据同步 |
| `OperateLogController` | `/build/operateLog` | 操作审计日志查询 |

## 数据库模型

共 **83 张表**，覆盖以下领域：

### 人员管理（9 张）
`worker` · `worker_certificate` · `worker_team` · `attendance_record` · `labour_subcontractor` · `labour_construction_unit` · `labour_worktype` · `labour_advance_retreat` · `labour_infocollection`

### 设备管理（12+ 张）
`device` · `device_alarm` · `equipment_assets` · `equipment_location` · `video_monitor` · `safety_fence` · `monitor_point_alert` · `alert_rule` · `supply_point` · `single_building_info` · `substation_equipment` · `transmission_tower` · `transmission_span`

### 物资管理（6 张）
`material_category` · `material_info` · `material_stock_record` · `material_check` · `material_check_item`

### 安全管理（20+ 张）
`hazard_report` · `hazard_work_order` · `approval_record` · `construction_area` · `ai_violation` · `ai_camera` · `ai_snapshot` · `ai_detection_callback` · `special_work_permit` · `safety_material` · `safety_material_catalog` · `safety_material_changelog` · `qual_material` · `qual_material_catalog` · `qual_material_changelog` · `emergency_plan` · `emergency_drill` · `emergency_supply` · `emergency_supply_record` · `emergency_incident` · `emergency_contact`

### 巡检管理（5 张）
`inspection_plan` · `inspection_template` · `inspection_task` · `inspection_record` · `inspection_issue`

### 进度管理（5 张）
`progress_task` · `progress_report` · `construction_log` · `safety_log` · `log_template` · `electronic_signature`

### 系统管理（10+ 张）
`sys_user` · `sys_dept` · `sys_role` · `sys_menu` · `project` · `project_info` · `sys_config` · `operate_log` · `pms_id_mapping` · `dashboard_config`

### 通知中心（5 张）
`notification_template` · `notification_subscription` · `notification_channel_config` · `notification_delivery` · `notification_delivery_item`

### 报表导出（2 张）
`report_template` · `report_export_task`

## API 一览

所有前端 API 统一通过 Spring Cloud Gateway（端口 8080）路由到各微服务。

### 通用设计原则

- **请求方式**：95% 以上接口使用 `POST` 方法（兼容前端规范）
- **请求/响应体**：`application/json`
- **统一响应格式**：`Result<T>` 封装（`code` + `message` + `data`）
- **分页**：`PageResult<T>`（`list` + `total` + `pageNum` + `pageSize`）
- **认证**：JWT Token（`Authorization: Bearer <token>`）
- **审计日志**：`@OperateLog` 注解自动记录

### 典型 API 路径模式

```
POST /build/<模块>/<操作>           # CRUD 操作
POST /build/<模块>/query<实体>List   # 分页查询
POST /build/<模块>/get<实体>/{id}    # 按 ID 查询
POST /build/<模块>/add<实体>         # 新增
POST /build/<模块>/set<实体>         # 更新
POST /build/<模块>/del<实体>/{id}    # 删除
```

## 快速启动

### 前置条件

- JDK 1.8+
- MySQL 8.0+
- Redis 5.x+
- Nacos 2.x
- Maven 3.6+
- Node.js 14+ (前端)
- Docker + Docker Compose (可选)

### 1. 启动基础设施

```bash
# 使用 Docker Compose 一键启动 MySQL + Redis + Nacos
docker-compose up -d
```

### 2. 初始化数据库

```bash
# 执行 DDL 脚本（83 张表 + 初始数据）
mysql -h 127.0.0.1 -u root -p < init-database.sql
```

### 3. 启动微服务（按顺序）

```bash
# 1) 服务注册中心（Nacos 默认端口 8848）

# 2) 启动网关（端口 8080）
cd power-smart-site-gateway
mvn spring-boot:run

# 3) 启动业务微服务（顺序无关，推荐以下顺序）
cd power-smart-site-system    && mvn spring-boot:run  # 端口 8085
cd power-smart-site-worker    && mvn spring-boot:run  # 端口 8081
cd power-smart-site-device    && mvn spring-boot:run  # 端口 8082
cd power-smart-site-progress  && mvn spring-boot:run  # 端口 8083
cd power-smart-site-hazard    && mvn spring-boot:run  # 端口 8084
cd power-smart-site-dashboard && mvn spring-boot:run  # 端口 8086
```

### 4. 启动前端

```bash
cd power-smart-site-frontend
npm install
npm run dev    # 开发环境（端口 8449）
npm run build  # 生产构建
```

### 5. 访问

| 服务 | 地址 |
|------|------|
| API 网关 | http://localhost:8080 |
| Knife4j API 文档 | http://localhost:8080/doc.html |
| 前端 | http://localhost:8449 |

## 项目结构

```
power-smart-site/
├── power-smart-site-common/              # 公共模块
│   └── src/main/java/com/powersmart/common/
│       ├── annotation/                   # 自定义注解（@OperateLog）
│       ├── auth/                         # JWT + 鉴权 + SecurityContext
│       ├── config/                       # 全局配置（MVC/异常/InfluxDB/MQTT/MyBatisPlus/Redis/Swagger）
│       ├── constant/                     # 常量定义
│       ├── entity/                       # 公共实体（Result/PageResult）
│       ├── exception/                    # 业务异常
│       ├── mapper/                       # 公共 Mapper
│       ├── push/                         # SSE 推送
│       ├── service/                      # 公共 Service
│       └── util/                         # 工具类（PageHelper/RedisUtil）
│
├── power-smart-site-gateway/             # 网关模块
│   └── src/main/java/com/powersmart/gateway/
│       ├── config/                       # CORS + 限流
│       └── filter/                       # JWT 鉴权过滤器
│
├── power-smart-site-worker/              # 人员管理
│   └── src/main/java/com/powersmart/worker/
│       ├── controller/                   # 8 个 REST Controller
│       ├── entity/                       # 9 个实体
│       ├── mapper/                       # 9 个 Mapper
│       ├── service/                      # 9 个 Service
│       └── service/impl/                 # 实现类
│
├── power-smart-site-device/              # 设备管理
│   └── src/main/java/com/powersmart/device/
│       ├── controller/                   # 21 个 REST Controller
│       ├── entity/                       # ~15 个实体
│       ├── mapper/                       # Mapper 接口
│       ├── service/                      # Service 接口
│       ├── service/impl/                 # 实现类
│       └── service/engine/              # 告警引擎
│
├── power-smart-site-hazard/              # 安全管理（最大模块）
│   └── src/main/java/com/powersmart/hazard/
│       ├── controller/                   # 29 个 REST Controller
│       ├── entity/                       # ~30 个实体
│       ├── mapper/                       # Mapper 接口
│       ├── service/                      # Service 接口
│       └── service/impl/                 # 实现类
│
├── power-smart-site-progress/            # 进度管理
│   └── src/main/java/com/powersmart/progress/
│       ├── controller/                   # 6 个 REST Controller
│       ├── entity/                       # 实体
│       ├── mapper/                       # Mapper
│       ├── service/                      # Service
│       └── service/impl/                 # 实现类
│
├── power-smart-site-system/              # 系统管理
│   └── src/main/java/com/powersmart/system/
│       ├── controller/                   # 16 个 REST Controller
│       ├── entity/                       # 实体
│       ├── mapper/                       # Mapper
│       ├── service/                      # Service（含通知渠道适配器）
│       └── service/channel/              # 通知渠道（Email/SMS/Feishu/InApp）
│
├── power-smart-site-dashboard/           # 数据看板
│   └── src/main/java/com/powersmart/dashboard/
│       └── controller/                   # 六维聚合看板
│
├── power-smart-site-api/                 # Feign 客户端
│
├── power-smart-site-frontend/            # 前端源码
│
├── init-database.sql                     # 数据库 DDL（83 张表）
├── pom.xml                               # 父 POM
└── docker-compose.yml                    # 基础设施编排
```

## 功能详解

### 🔧 人员管理 (worker)

完整的劳务实名制管理，符合建筑行业监管要求。

**核心流程：**
```
入场登记 → 人脸采集 → 安全教育考试 → 分配班组 → 日常考勤 → 退场
```

**关键能力：**
- 工人档案：姓名/身份证/手机号/人脸照片/工种/培训状态/合同
- 班组管理：班组长/成员/工种划分/出勤统计
- 进出场管理：人脸比对、安全教育培训记录、退场审批
- 考勤：按项目/班组/个人多维统计
- 分包商/建设单位：资质管理、合同有效期追踪

### 🔧 设备管理 (device)

覆盖电力行业特有设备 + 通用工地设备。

**专用设备类型：**
- **变电站设备**：变压器（油浸式/SF6/有载调压/冷却方式/额定容量）、断路器（额定电流/开断电流/操作机构/机械次数）、隔离开关、CT/PT、避雷器、母线等
- **输电设备**：铁塔（呼高/转角度数/电压等级/基础类型）、档距（导线型号/地线型号/弧垂）
- **监测设备**：温度传感器、湿度传感器、振动传感器、气体检测仪
- **视频监控**：摄像头（球机/枪机/云台）、NVR、AI 分析摄像头

**IoT 能力：**
- MQTT 实时数据接入（设备上报 → 时序存储 → 阈值判断 → 告警推送）
- InfluxDB 时序数据存储（最近 1 年全量数据）
- 告警引擎（多规则/多级告警/告警升级/告警确认关闭）

**电子围栏：**
- 围栏区域配置（经纬度/半径）
- 围栏告警（越界/滞留/超时）
- 人员-围栏关联

### 🔧 安全管理 (hazard) — 核心模块

**隐患闭环流程：**
```
上报 → 审批 → 派单/整改 → 整改完成 → 验收 → 关闭
                        ↕
             超时未整改 → 升级告警
```

**隐患类型：**
- 安全防护类（临边/洞口/脚手架/高处作业）
- 用电安全类（配电箱/电缆/接地）
- 消防类（灭火器/消防通道/易燃物）
- 机械类（起重/挖掘/桩工）
- 环境类（噪声/粉尘/有害气体）
- AI 检测类（未戴安全帽/未穿反光衣/禁区闯入/抽烟/打电话）

**AI 视觉识别：**
- 支持对接第三方 AI 检测服务（HTTP 回调接收检测结果）
- 实时摄像头抓拍 + AI 分析 + 告警推送
- 违规记录追溯（图片/时间/位置/违规类型）

**巡检能力：**
- 巡检计划（日/周/月/自定义周期）
- 巡检模板（预定义检查项 + 评分标准 + 阈值）
- 巡检任务（自动生成/手动创建/分配责任人）
- 巡检记录（正常/异常/图片上传/位置打卡）
- 巡检问题（自动关联隐患闭环）

**特种作业票 — 全生命周期管理：**

| 作业类型 | 状态流转 |
|---------|---------|
| 🔥 动火作业 | 草稿→提交→安全复核→审批通过→开工→完工→关闭 |
| 🪜 登高作业 | 同上（+ 超期自动提醒） |
| 🚧 受限空间 | 同上（+ 气体检测填报） |
| ⚡ 临时用电 | 同上（+ 设备检查清单） |
| 🏗️ 吊装作业 | 同上（+ 吊装方案附件） |
| 🚜 挖掘作业 | 同上（+ 管线确认） |
| 🚫 占道作业 | 同上（+ 交通方案） |

**通知中心：**
- **渠道**：邮件（SMTP）、短信（阿里云/腾讯云）、飞书 Webhook、站内信（SSE 实时推送）
- **模板**：占位符引擎、多语言支持
- **订阅**：用户按类型/渠道自行订阅，避免骚扰
- **审计**：每条通知的发送/重试/状态全记录

### 🔧 进度管理 (progress)

**施工日志：**
- 天气/温度记录
- 人员出勤统计
- 机械使用记录
- 当日工作量/完成进度
- 质量问题记录
- 安全情况记录

**安全日志：**
- 安全巡查记录
- 安全教育/交底记录
- 隐患整改跟踪
- 安全事故记录

**电子签名：**
- 支持手写签名采集
- 签名与日志/报告关联
- 签名验证/审计

### 🔧 数据看板 (dashboard)

六维聚合仪表盘：

| 维度 | 指标 |
|------|------|
| 👷 人员 | 在场人数/出勤率/工种分布/退场人数 |
| 🛠️ 设备 | 运行率/故障率/告警数/在线传感器数 |
| ⚠️ 隐患 | 未处理/整改中/已关闭/超期未整改/分类统计 |
| 📈 进度 | 总体完成率/各区域进度/里程碑达成/偏差分析 |
| 🚨 告警 | 实时告警/工单超期/紧急告警推送 |
| 📋 综合 | 项目概览/风险热力图/趋势分析 |

## 环境要求

### 开发环境

| 工具 | 版本 |
|------|------|
| JDK | 1.8+（推荐 11） |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 5.x+ |
| Nacos | 2.x |
| Node.js | 14+ |
| Docker | 20+（可选） |

### 生产环境（推荐）

| 组件 | 部署方式 |
|------|---------|
| 微服务 | Docker/K8s 容器化部署 |
| MySQL | 主从复制 + 读写分离 |
| Redis | 集群模式 |
| Nacos | 集群模式 |
| RabbitMQ | 集群模式（通知消息） |
| MinIO | 分布式文件存储 |
| Nginx | 反向代理 + SSL 终端 |

---

## 参考

- tongye TYConstruction（参考）：https://gitee.com/qingyun-software/TYConstruction
- Spring Cloud Alibaba：https://spring.io/projects/spring-cloud-alibaba
- MyBatis-Plus：https://baomidou.com
- InfluxDB：https://www.influxdata.com
- Knife4j：https://doc.xiaominfo.com
