import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户信息接口
 */
export interface UserInfo {
  id?: number
  username?: string
  nickname?: string
  email?: string
  avatar?: string
  roles?: string[]
  permissions?: string[]
}

/**
 * 用户信息Store
 * 管理用户登录状态、用户信息等
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string>('')
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  // 从localStorage恢复状态
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    const storedUserInfo = localStorage.getItem('userInfo')
    
    if (storedToken) {
      token.value = storedToken
    }
    
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('userInfo')
      }
    }
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 登录
  const login = (info: UserInfo, newToken: string) => {
    setUserInfo(info)
    setToken(newToken)
  }

  // 登出
  const logout = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 更新用户信息
  const updateUserInfo = (info: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    if (!userInfo.value?.permissions) return false
    return userInfo.value.permissions.includes(permission)
  }

  // 检查多个权限（满足任意一个即可）
  const hasAnyPermission = (permissions: string[]): boolean => {
    if (!userInfo.value?.permissions) return false
    return permissions.some(p => userInfo.value?.permissions?.includes(p))
  }

  // 检查多个权限（必须全部满足）
  const hasAllPermissions = (permissions: string[]): boolean => {
    if (!userInfo.value?.permissions) return false
    return permissions.every(p => userInfo.value?.permissions?.includes(p))
  }

  // 初始化
  initFromStorage()

  return {
    // 状态
    userInfo,
    token,
    isLoggedIn,
    // 方法
    setUserInfo,
    setToken,
    login,
    logout,
    updateUserInfo,
    initFromStorage,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions
  }
})

