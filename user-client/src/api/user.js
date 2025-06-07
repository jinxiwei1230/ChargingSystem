import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000 // 请求超时时间
})

// 用户登录
export function login(username, password) {
  return service({
    url: '/user/login',
    method: 'post',
    params: {
      username,
      password
    }
  })
}

// 用户注册
export function register(data) {
  return service({
    url: '/user/register',
    method: 'post',
    params: data
  })
} 