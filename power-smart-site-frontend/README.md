# 山西同业电力 - 智慧工地前端

基于tongye TYConstruction 前端规范定制。

## 修改内容

| 文件 | 修改事项 |
|------|----------|
| `config/dev.env.js` | `BASE_API` 指向本地网关 |
| `config/prod.env.js` | `BASE_API` 指向生产网关 |

所有 API 请求指向 Spring Cloud Gateway（端口 8080）。

## 启动

```bash
npm install
npm run dev    # 端口 8449
npm run build  # 生产构建
```
