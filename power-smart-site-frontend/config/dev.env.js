/*
 * Forked from YFConstruction - 适配电力智慧工地平台
 * 修改：BASE_API 指向本地网关 http://localhost:8080
 */
'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_API: '"http://localhost:8080/"',
  INDEX_URL: '"build/index"',
  VIDEO_URL: '"http://localhost:8080"'
})
