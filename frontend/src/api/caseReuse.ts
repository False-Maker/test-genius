import request from './request'
import type { ApiResult } from './types'

// 相似用例类型
export interface SimilarCase {
  caseId: number
  caseCode: string
  caseName: string
  similarity?: number
  [key: string]: any
}

export interface SearchSimilarCasesParams {
  caseText: string
  layerId?: number
  methodId?: number
  topK?: number
  similarityThreshold?: number
}

// 用例复用API
export const caseReuseApi = {
  // 初始化用例向量表
  initCaseVectorTable() {
    return request.post<boolean>('/v1/case-reuse/init')
  },

  // 更新用例的向量表示
  updateCaseEmbedding(caseId: number) {
    return request.post<boolean>(`/v1/case-reuse/cases/${caseId}/embedding`)
  },

  // 搜索相似用例（基于语义相似度）
  searchSimilarCases(params: SearchSimilarCasesParams) {
    return request.post<SimilarCase[]>('/v1/case-reuse/cases/search/similar', null, { params })
  },

  // 关键词检索用例
  searchCasesByKeyword(keyword: string, params?: { layerId?: number; methodId?: number; topK?: number }) {
    return request.get<SimilarCase[]>(`/v1/case-reuse/cases/search/keyword/${keyword}`, { params })
  },

  // 推荐相似用例（基于现有用例）
  recommendSimilarCases(caseId: number, topK?: number) {
    return request.get<SimilarCase[]>(`/v1/case-reuse/cases/${caseId}/recommend`, {
      params: { topK }
    })
  },

  // 创建用例组合（测试套件）
  createCaseSuite(suiteName: string, caseIds: number[], creatorId?: number) {
    return request.post<number>('/v1/case-reuse/suites', null, {
      params: {
        suiteName,
        caseIds: caseIds.join(','),
        creatorId
      }
    })
  }
}

