import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'

// 加载状态管理
let loadingInstance: LoadingInstance | null = null
let loadingCount = 0

// 显示加载
const showLoading = () => {
  loadingCount++
  if (loadingCount === 1 && !loadingInstance) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
}

// 隐藏加载
const hideLoading = () => {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 可以在这里添加token等认证信息
    // const token = localStorage.getItem('token')
    // if (token && config.headers) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    
    // 显示加载状态（如果配置了showLoading）
    if (config.showLoading !== false) {
      showLoading()
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    hideLoading()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 隐藏加载状态
    if (response.config.showLoading !== false) {
      hideLoading()
    }
    
    const res = response.data
    
    // 如果返回的状态码不是200，则视为错误
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  (error) => {
    // 隐藏加载状态
    hideLoading()
    
    console.error('响应错误:', error)
    let message = '请求失败'
    
    if (error.response) {
      const res = error.response.data
      // 优先使用后端返回的错误信息
      if (res && res.message) {
        message = res.message
      } else {
        switch (error.response.status) {
          case 400:
            message = '请求参数错误'
            break
          case 401:
            message = '未授权，请重新登录'
            // 可以在这里跳转到登录页
            break
          case 403:
            message = '拒绝访问'
            break
          case 404:
            message = '请求地址不存在'
            break
          case 500:
            message = '服务器内部错误'
            break
          default:
            message = `请求失败: ${error.response.status}`
        }
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 扩展AxiosRequestConfig类型
declare module 'axios' {
  export interface AxiosRequestConfig {
    showLoading?: boolean
  }
}

export default service

