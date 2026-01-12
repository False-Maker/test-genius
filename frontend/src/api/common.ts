import request from './request'
import type { ApiResult } from './types'

// 测试分层类型
export interface TestLayer {
  id: number
  layerCode: string
  layerName: string
  layerDescription?: string
  layerOrder?: number
  isActive?: string
}

// 测试设计方法类型
export interface TestDesignMethod {
  id: number
  methodCode: string
  methodName: string
  methodDescription?: string
  applicableLayers?: string
  example?: string
  isActive?: string
}

// 通用API
export const commonApi = {
  // 获取测试分层列表
  getTestLayerList() {
    return request.get<TestLayer[]>('/v1/common/test-layers')
  },

  // 获取测试设计方法列表
  getTestDesignMethodList() {
    return request.get<TestDesignMethod[]>('/v1/common/test-design-methods')
  }
}

