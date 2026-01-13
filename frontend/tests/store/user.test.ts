import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore, type UserInfo } from '@/store/user'

describe('useUserStore', () => {
  beforeEach(() => {
    // 创建新的Pinia实例
    setActivePinia(createPinia())
    // 清除localStorage
    localStorage.clear()
  })

  describe('初始化', () => {
    it('应该正确初始化空状态', () => {
      const store = useUserStore()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('应该从localStorage恢复token和用户信息', () => {
      const mockUserInfo: UserInfo = {
        id: 1,
        username: 'test-user',
        nickname: '测试用户'
      }

      localStorage.setItem('token', 'stored-token')
      localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))

      const store = useUserStore()
      store.initFromStorage()

      expect(store.token).toBe('stored-token')
      expect(store.userInfo).toEqual(mockUserInfo)
      expect(store.isLoggedIn).toBe(true)
    })

    it('应该处理无效的localStorage用户信息', () => {
      localStorage.setItem('userInfo', 'invalid-json')

      const store = useUserStore()
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      store.initFromStorage()

      expect(store.userInfo).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
      
      consoleErrorSpy.mockRestore()
    })
  })

  describe('设置用户信息', () => {
    it('应该正确设置用户信息', () => {
      const store = useUserStore()
      const userInfo: UserInfo = {
        id: 1,
        username: 'test-user',
        nickname: '测试用户'
      }

      store.setUserInfo(userInfo)

      expect(store.userInfo).toEqual(userInfo)
      expect(localStorage.getItem('userInfo')).toBe(JSON.stringify(userInfo))
    })

    it('应该正确设置token', () => {
      const store = useUserStore()
      const token = 'new-token-123'

      store.setToken(token)

      expect(store.token).toBe(token)
      expect(localStorage.getItem('token')).toBe(token)
    })

    it('应该正确登录', () => {
      const store = useUserStore()
      const userInfo: UserInfo = {
        id: 1,
        username: 'test-user'
      }
      const token = 'login-token'

      store.login(userInfo, token)

      expect(store.userInfo).toEqual(userInfo)
      expect(store.token).toBe(token)
      expect(store.isLoggedIn).toBe(true)
    })
  })

  describe('更新用户信息', () => {
    it('应该正确更新用户信息', () => {
      const store = useUserStore()
      const initialUserInfo: UserInfo = {
        id: 1,
        username: 'test-user',
        nickname: '旧昵称'
      }

      store.setUserInfo(initialUserInfo)
      store.updateUserInfo({ nickname: '新昵称', email: 'test@example.com' })

      expect(store.userInfo?.nickname).toBe('新昵称')
      expect(store.userInfo?.email).toBe('test@example.com')
      expect(store.userInfo?.id).toBe(1)
      expect(store.userInfo?.username).toBe('test-user')
    })

    it('当用户信息为空时更新应该不报错', () => {
      const store = useUserStore()

      expect(() => {
        store.updateUserInfo({ nickname: '新昵称' })
      }).not.toThrow()

      expect(store.userInfo).toBeNull()
    })
  })

  describe('登出', () => {
    it('应该正确清除用户信息和token', () => {
      const store = useUserStore()
      store.login({ id: 1, username: 'test-user' }, 'token-123')

      store.logout()

      expect(store.userInfo).toBeNull()
      expect(store.token).toBe('')
      expect(store.isLoggedIn).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
    })
  })

  describe('isLoggedIn计算属性', () => {
    it('应该在没有token时返回false', () => {
      const store = useUserStore()
      expect(store.isLoggedIn).toBe(false)
    })

    it('应该在没有用户信息时返回false', () => {
      const store = useUserStore()
      store.setToken('token-only')

      expect(store.isLoggedIn).toBe(false)
    })

    it('应该在token和用户信息都存在时返回true', () => {
      const store = useUserStore()
      store.login({ id: 1, username: 'test-user' }, 'token-123')

      expect(store.isLoggedIn).toBe(true)
    })
  })
})

