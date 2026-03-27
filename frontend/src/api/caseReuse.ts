import request from './request'
import type { ApiResult } from './types'

// 相似用例类型
export interface SimilarCase {
  caseId: number
  caseCode: string
  caseName: string
  caseType?: string
  casePriority?: string
  requirementId?: number
  layerId?: number
  methodId?: number
  preCondition?: string
  testStep?: string
  expectedResult?: string
  caseStatus?: string
  createTime?: string
  similarity?: number
  [key: string]: unknown
}

export interface SearchSimilarCasesParams {
  caseText: string
  layerId?: number
  methodId?: number
  topK?: number
  similarityThreshold?: number
}

type SimilarCaseResponse = {
  id?: number
  caseId?: number
  case_code?: string
  caseCode?: string
  case_name?: string
  caseName?: string
  case_type?: string
  caseType?: string
  case_priority?: string
  casePriority?: string
  requirement_id?: number
  requirementId?: number
  layer_id?: number
  layerId?: number
  method_id?: number
  methodId?: number
  pre_condition?: string
  preCondition?: string
  test_step?: string
  testStep?: string
  expected_result?: string
  expectedResult?: string
  case_status?: string
  caseStatus?: string
  create_time?: string
  createTime?: string
  similarity?: number
  [key: string]: unknown
}

const normalizeSimilarCase = (item: SimilarCaseResponse): SimilarCase => ({
  ...item,
  caseId: item.caseId ?? item.id ?? 0,
  caseCode: item.caseCode ?? item.case_code ?? '',
  caseName: item.caseName ?? item.case_name ?? '',
  caseType: item.caseType ?? item.case_type,
  casePriority: item.casePriority ?? item.case_priority,
  requirementId: item.requirementId ?? item.requirement_id,
  layerId: item.layerId ?? item.layer_id,
  methodId: item.methodId ?? item.method_id,
  preCondition: item.preCondition ?? item.pre_condition,
  testStep: item.testStep ?? item.test_step,
  expectedResult: item.expectedResult ?? item.expected_result,
  caseStatus: item.caseStatus ?? item.case_status,
  createTime: item.createTime ?? item.create_time,
  similarity: item.similarity
})

const normalizeSimilarCaseList = (
  response: ApiResult<SimilarCaseResponse[]>
): ApiResult<SimilarCase[]> => ({
  ...response,
  data: (response.data || []).map(normalizeSimilarCase)
})

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
  async searchSimilarCases(params: SearchSimilarCasesParams) {
    const response = await request.post<SimilarCaseResponse[]>('/v1/case-reuse/cases/search/similar', null, { params })
    return normalizeSimilarCaseList(response as unknown as ApiResult<SimilarCaseResponse[]>)
  },

  // 关键词检索用例
  async searchCasesByKeyword(keyword: string, params?: { layerId?: number; methodId?: number; topK?: number }) {
    const response = await request.get<SimilarCaseResponse[]>(`/v1/case-reuse/cases/search/keyword/${keyword}`, { params })
    return normalizeSimilarCaseList(response as unknown as ApiResult<SimilarCaseResponse[]>)
  },

  // 推荐相似用例（基于现有用例）
  async recommendSimilarCases(caseId: number, topK?: number) {
    const response = await request.get<SimilarCaseResponse[]>(`/v1/case-reuse/cases/${caseId}/recommend`, {
      params: { topK }
    })
    return normalizeSimilarCaseList(response as unknown as ApiResult<SimilarCaseResponse[]>)
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
