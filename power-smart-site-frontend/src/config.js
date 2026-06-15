/**
 * 获取当前页面的完整URL
 * @returns {string} 完整的域名地址
 */
const getLocationOrigin = () => {
  return window.location.protocol + '//' + window.location.hostname + (window.location.port ? ':' + window.location.port : '')
}

// 系统标题配置
const companyName = '智慧工地'
const version = 'V11.0.0'
const baiduKey = '8SPafncsFsyVXFGGfUEmmDNwcQo6UrI9' // 百度地图API密钥

// WebSocket配置模块，由于socket会使vue代理失效，这里使用独立的基础路径
const startSocket = false // 是否启用WebSocket
let socketBaseUrl = 'http://zhgd.sdyingfeng.cn?target=PCM' // 开发基础路径

// 生产环境使用环境变量中的API地址
if (process.env.NODE_ENV === 'production') {
  socketBaseUrl = process.env.BASE_API
}

// socket配置项
const socketOpts = {
  path: '', // 生产环境path
  transports: ['websocket', 'polling'], // 连接方式
  timeout: 5000, // 超时时间
  reconnectionAttempts: 5, // 重连次数
  reconnectionDelay: 10000 // 重连间隔
}

// 消息类型
const messageTypes = [
  'SYS_XTGG',
  'BUS_EXA_SUBMIT',
  'BUS_EXA_REF',
  'BUS_EXA_TG',
  'SAFETY_EMERGENCY',
  'SECURITY_RISKS',
  'SECURITY_RISKS_DETECTION',
  'ALARM_EQU_ENVRMT',
  'EQUI_OFFLINE_TIMEOUT',
  'MONITOR_EQUI_ONLINE',
  'MONITOR_EQUI_OFFLINE',
  'QUAL_RISKS_DETECTION',
  'ENTERPRISE_SUBMISSION'
]

export default {
  version,
  companyName,
  getLocationOrigin,
  baiduKey,
  startSocket,
  socketBaseUrl,
  socketOpts,
  messageTypes,
}
