import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAppStore } from '@/store/app'

describe('useAppStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  describe('初始化', () => {
    it('应该正确初始化空状态', () => {
      const store = useAppStore()

      expect(store.globalLoading).toBe(false)
      expect(store.loadingText).toBe('加载中...')
      expect(store.errorMessage).toBeNull()
      expect(store.errorCode).toBeNull()
      expect(store.sidebarCollapsed).toBe(false)
    })

    it('应该从localStorage恢复侧边栏状态', () => {
      localStorage.setItem('sidebarCollapsed', 'true')

      const store = useAppStore()
      store.initSidebarFromStorage()

      expect(store.sidebarCollapsed).toBe(true)
    })

    it('应该处理无效的localStorage侧边栏状态', () => {
      localStorage.setItem('sidebarCollapsed', 'invalid')

      const store = useAppStore()
      store.initSidebarFromStorage()

      expect(store.sidebarCollapsed).toBe(false)
    })
  })

  describe('加载状态管理', () => {
    it('应该正确显示加载状态', () => {
      const store = useAppStore()

      store.showLoading('正在加载数据...')

      expect(store.globalLoading).toBe(true)
      expect(store.loadingText).toBe('正在加载数据...')
    })

    it('应该使用默认加载文本', () => {
      const store = useAppStore()

      store.showLoading()

      expect(store.globalLoading).toBe(true)
      expect(store.loadingText).toBe('加载中...')
    })

    it('应该正确隐藏加载状态', () => {
      const store = useAppStore()
      store.showLoading('自定义文本')

      store.hideLoading()

      expect(store.globalLoading).toBe(false)
      expect(store.loadingText).toBe('加载中...')
    })

    it('应该支持多次显示和隐藏加载', () => {
      const store = useAppStore()

      store.showLoading('第一次')
      expect(store.globalLoading).toBe(true)

      store.hideLoading()
      expect(store.globalLoading).toBe(false)

      store.showLoading('第二次')
      expect(store.globalLoading).toBe(true)

      store.hideLoading()
      expect(store.globalLoading).toBe(false)
    })
  })

  describe('错误状态管理', () => {
    it('应该正确设置错误信息', () => {
      const store = useAppStore()

      store.setError('操作失败', 'ERR_001')

      expect(store.errorMessage).toBe('操作失败')
      expect(store.errorCode).toBe('ERR_001')
    })

    it('应该设置没有错误码的错误信息', () => {
      const store = useAppStore()

      store.setError('网络错误')

      expect(store.errorMessage).toBe('网络错误')
      expect(store.errorCode).toBeNull()
    })

    it('应该正确清除错误信息', () => {
      const store = useAppStore()
      store.setError('错误信息', 'ERR_001')

      store.clearError()

      expect(store.errorMessage).toBeNull()
      expect(store.errorCode).toBeNull()
    })

    it('应该支持多次设置错误', () => {
      const store = useAppStore()

      store.setError('错误1', 'ERR_001')
      expect(store.errorMessage).toBe('错误1')

      store.setError('错误2', 'ERR_002')
      expect(store.errorMessage).toBe('错误2')
      expect(store.errorCode).toBe('ERR_002')
    })
  })

  describe('侧边栏状态管理', () => {
    it('应该正确切换侧边栏状态', () => {
      const store = useAppStore()

      expect(store.sidebarCollapsed).toBe(false)

      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(true)
      expect(localStorage.getItem('sidebarCollapsed')).toBe('true')

      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(false)
      expect(localStorage.getItem('sidebarCollapsed')).toBe('false')
    })

    it('应该正确设置侧边栏折叠状态', () => {
      const store = useAppStore()

      store.setSidebarCollapsed(true)
      expect(store.sidebarCollapsed).toBe(true)
      expect(localStorage.getItem('sidebarCollapsed')).toBe('true')

      store.setSidebarCollapsed(false)
      expect(store.sidebarCollapsed).toBe(false)
      expect(localStorage.getItem('sidebarCollapsed')).toBe('false')
    })

    it('应该支持从localStorage恢复侧边栏状态', () => {
      localStorage.setItem('sidebarCollapsed', 'true')

      const store = useAppStore()
      store.initSidebarFromStorage()

      expect(store.sidebarCollapsed).toBe(true)
    })
  })

  describe('状态持久化', () => {
    it('侧边栏状态应该持久化到localStorage', () => {
      const store = useAppStore()

      store.toggleSidebar()

      expect(localStorage.getItem('sidebarCollapsed')).toBe('true')
    })

    it('设置侧边栏状态应该持久化到localStorage', () => {
      const store = useAppStore()

      store.setSidebarCollapsed(true)

      expect(localStorage.getItem('sidebarCollapsed')).toBe('true')
    })
  })
})
