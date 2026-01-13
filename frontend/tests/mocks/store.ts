import { vi } from 'vitest'

/**
 * Mock User Store
 */
export const mockUserStore = {
  token: 'mock-token',
  userInfo: {
    id: 1,
    username: 'test-user'
  },
  setToken: vi.fn(),
  setUserInfo: vi.fn(),
  clearUserInfo: vi.fn()
}

/**
 * Mock Cache Store
 */
export const mockCacheStore = {
  requirementList: [],
  layerList: [],
  methodList: [],
  templateList: [],
  modelList: [],
  loading: {
    requirementList: false,
    layerList: false,
    methodList: false,
    templateList: false,
    modelList: false
  },
  loadRequirementList: vi.fn(),
  loadLayerList: vi.fn(),
  loadMethodList: vi.fn(),
  loadTemplateList: vi.fn(),
  loadModelList: vi.fn(),
  clearAllCache: vi.fn(),
  clearCache: vi.fn()
}

/**
 * 重置所有Store Mock
 */
export const resetStoreMocks = () => {
  vi.clearAllMocks()
  mockUserStore.setToken.mockClear()
  mockUserStore.setUserInfo.mockClear()
  mockUserStore.clearUserInfo.mockClear()
  mockCacheStore.loadRequirementList.mockClear()
  mockCacheStore.loadLayerList.mockClear()
  mockCacheStore.loadMethodList.mockClear()
  mockCacheStore.loadTemplateList.mockClear()
  mockCacheStore.loadModelList.mockClear()
  mockCacheStore.clearAllCache.mockClear()
  mockCacheStore.clearCache.mockClear()
}

