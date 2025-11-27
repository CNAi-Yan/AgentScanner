import axios from 'axios'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 基础URL，实际项目中应该从环境变量获取
  timeout: 5000 // 请求超时时间
})

// 登录请求
export const login = (data) => {
  return service({
    url: '/login',
    method: 'post',
    data
  })
}