'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_API: '"http://localhost:8080/"',
  INDEX_URL: '"build/index"',
  VIDEO_URL: '"http://localhost:8080"'
})
