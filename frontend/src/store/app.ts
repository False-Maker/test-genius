import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用状态Store
 * 管理全局加载状态、错误状态、消息提示等
 */
export const useAppStore = defineStore('app', () => {
  // 全局加载状态
  const globalLoading = ref(false)
  const loadingText = ref('加载中...')

  // 错误信息
  const errorMessage = ref<string | null>(null)
  const errorCode = ref<string | null>(null)

  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)

  // 显示全局加载
  const showLoading = (text = '加载中...') => {
    globalLoading.value = true
    loadingText.value = text
  }

  // 隐藏全局加载
  const hideLoading = () => {
    globalLoading.value = false
    loadingText.value = '加载中...'
  }

  // 设置错误信息
  const setError = (message: string, code?: string) => {
    errorMessage.value = message
    errorCode.value = code || null
  }

  // 清除错误信息
  const clearError = () => {
    errorMessage.value = null
    errorCode.value = null
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
    // 保存到localStorage
    localStorage.setItem('sidebarCollapsed', String(sidebarCollapsed.value))
  }

  // 设置侧边栏状态
  const setSidebarCollapsed = (collapsed: boolean) => {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('sidebarCollapsed', String(collapsed))
  }

  // 从localStorage恢复侧边栏状态
  const initSidebarFromStorage = () => {
    const stored = localStorage.getItem('sidebarCollapsed')
    if (stored !== null) {
      sidebarCollapsed.value = stored === 'true'
    }
  }

  // 初始化
  initSidebarFromStorage()

  return {
    // 状态
    globalLoading,
    loadingText,
    errorMessage,
    errorCode,
    sidebarCollapsed,
    // 方法
    showLoading,
    hideLoading,
    setError,
    clearError,
    toggleSidebar,
    setSidebarCollapsed,
    initSidebarFromStorage
  }
})

