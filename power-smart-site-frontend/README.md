# 电力智慧工地 - 前端

Fork 自 **萤丰 YFConstruction** 开源前端，已适配电力智慧工地平台。

## 修改内容

| 文件 | 原值 | 修改后 |
|------|------|--------|
| `config/dev.env.js` | `BASE_API: "http://zhgd.sdyingfeng.cn/api/"` | `BASE_API: "http://localhost:8080/"` |
| `config/prod.env.js` | `BASE_API: "/qdoner/"` | `BASE_API: "http://YOUR_SERVER_IP:8080/"` |

所有 API 请求直接指向 Spring Cloud Gateway（端口 8080），由网关路由到对应的微服务。

## 启动

```bash
cd power-smart-site-frontend
npm install
npm run dev    # 开发模式，默认端口 8449
npm run build  # 生产构建
```

## 网关 API 映射

| 前端请求 | 网关 → 微服务 |
|----------|---------------|
| `/build/labourTeam/*` | → worker 服务 |
| `/build/labourSubcontractor/*` | → worker 服务 |
| `/build/safetyMaterial/*` | → hazard 服务 |
| `/build/videoMonitor/*` | → device 服务 |
| `/build/equipmentLocation/*` | → device 服务 |
| `/login, /logout` | → system 服务 |
| `/work/work/*` | → progress 服务 |
| `/api/v1/*` | 内部 REST（兼容） |
