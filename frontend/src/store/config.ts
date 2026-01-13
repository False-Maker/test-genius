import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 系统配置接口
 */
export interface SystemConfig {
  apiBaseUrl: string
  timeout: number
  pageSize: number
  pageSizes: number[]
  theme: 'light' | 'dark'
  language: 'zh-CN' | 'en-US'
}

/**
 * 配置信息Store
 * 管理系统配置、API配置等
 */
export const useConfigStore = defineStore('config', () => {
  // 默认配置
  const defaultConfig: SystemConfig = {
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    timeout: 30000,
    pageSize: 10,
    pageSizes: [10, 20, 50, 100],
    theme: 'light',
    language: 'zh-CN'
  }

  // 状态
  const config = ref<SystemConfig>({ ...defaultConfig })

  // 从localStorage恢复配置
  const initFromStorage = () => {
    const storedConfig = localStorage.getItem('systemConfig')
    if (storedConfig) {
      try {
        const parsed = JSON.parse(storedConfig)
        config.value = { ...defaultConfig, ...parsed }
      } catch (error) {
        console.error('解析系统配置失败:', error)
        localStorage.removeItem('systemConfig')
      }
    }
  }

  // 计算属性
  const apiBaseUrl = computed(() => config.value.apiBaseUrl)
  const timeout = computed(() => config.value.timeout)
  const pageSize = computed(() => config.value.pageSize)
  const pageSizes = computed(() => config.value.pageSizes)
  const theme = computed(() => config.value.theme)
  const language = computed(() => config.value.language)

  // 更新配置
  const updateConfig = (newConfig: Partial<SystemConfig>) => {
    config.value = { ...config.value, ...newConfig }
    localStorage.setItem('systemConfig', JSON.stringify(config.value))
  }

  // 重置配置
  const resetConfig = () => {
    config.value = { ...defaultConfig }
    localStorage.setItem('systemConfig', JSON.stringify(config.value))
  }

  // 设置主题
  const setTheme = (newTheme: 'light' | 'dark') => {
    updateConfig({ theme: newTheme })
  }

  // 设置语言
  const setLanguage = (newLanguage: 'zh-CN' | 'en-US') => {
    updateConfig({ language: newLanguage })
  }

  // 设置分页大小
  const setPageSize = (size: number) => {
    updateConfig({ pageSize: size })
  }

  // 初始化
  initFromStorage()

  return {
    // 状态
    config,
    // 计算属性
    apiBaseUrl,
    timeout,
    pageSize,
    pageSizes,
    theme,
    language,
    // 方法
    updateConfig,
    resetConfig,
    setTheme,
    setLanguage,
    setPageSize,
    initFromStorage
  }
})

