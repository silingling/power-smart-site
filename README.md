# 电力智慧工地平台 (Power Smart Site)

> **混合架构**: 自研 Spring Cloud 微服务后端 + 兼容 tongye TYConstruction 规范的前端

## 🏗️ 技术栈

| 层 | 技术 |
|------|------|
| **后端** | Spring Cloud Alibaba 2021 + MyBatis-Plus 3.5 + Spring Boot 2.7 |
| **注册中心** | Nacos |
| **网关** | Spring Cloud Gateway |
| **数据库** | MySQL 8.0 + InfluxDB 2.x (时序) + Redis |
| **IoT** | MQTT (Eclipse Paho) + Netty |
| **文档** | Knife4j (Swagger) |
| **前端** | 自有前端，API 兼容 tongye TYConstruction 规范 |

## 📁 项目结构

```
power-smart-site/
├── common/          # 公共模块 (Result/异常/配置/工具)
├── gateway/         # 网关 (路由适配 + 限流)
├── worker/          # 人员管理 (工人/班组/考勤/分包商/工种/进出场)
├── device/          # 设备管理 (台账/传感器时序数据/告警引擎/视频监控/位置树)
├── hazard/          # 隐患闭环 (上报→派单→整改→验收/AI违规)
├── progress/        # 进度管理 (工序树/偏差分析)
├── system/          # 系统管理 (用户/项目/权限)
├── dashboard/       # 数据看板 (六维聚合)
└── api/             # Feign 跨服务调用客户端
```

## 🚀 快速启动

```bash
# 1. 启动基础设施
docker-compose up -d

# 2. 导入数据库
mysql -h 127.0.0.1 -u root -p < init-database.sql

# 3. 按以下顺序启动微服务
power-smart-gateway    # 端口 8080
power-smart-system     # 端口 8085
power-smart-worker     # 端口 8081
power-smart-device     # 端口 8082
power-smart-progress   # 端口 8083
power-smart-hazard     # 端口 8084
power-smart-dashboard  # 端口 8086
```

## 🔄 开发路线

- [x] Phase 1: 后端骨架 + 前端 API 适配 (113文件/3742行)
- [ ] Phase 2: 网关路由 + 登录鉴权
- [ ] Phase 3: 前端页面定制（基于 tongye 规范）
- [ ] Phase 4: IoT 设备接入 + 告警引擎完善
- [ ] Phase 5: 电力行业专项 (变电站/输电/配电)

## 🔗 参考

- tongye TYConstruction（参考）：https://gitee.com/qingyun-software/TYConstruction
