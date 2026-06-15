/*
 * Forked from YFConstruction - 适配电力智慧工地平台
 * 修改：BASE_API 指向生产网关地址（部署时替换为实际地址）
 */
'use strict'

module.exports = {
  NODE_ENV: '"production"',
  BASE_API: '"http://YOUR_SERVER_IP:8080/"',
  INDEX_URL: '"build/index"',
  VIDEO_URL: '"http://YOUR_SERVER_IP:8080"',
  DEV_TOOL_FORBID: false
}
