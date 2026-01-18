import request from './request'

// 等价类参数定义
export interface EquivalenceParameter {
  parameterName: string
  parameterType: string
  validClasses: string[]
  invalidClasses?: string[]
}

// 等价类表相关类型定义
export interface EquivalenceTableRequest {
  requirementId?: number
  caseIds?: number[]
  parameters?: EquivalenceParameter[]
  title?: string
}

export interface EquivalenceTableResponse {
  tableCode: string
  title: string
  parameters: EquivalenceParameter[]
  testCases: Array<{
    caseNumber: string
    parameterValues: Record<string, string>
    isValid: boolean
  }>
  totalCases: number
  validCases: number
  invalidCases: number
}

// 正交表因素定义
export interface OrthogonalFactor {
  factorName: string
  levels: string[]
}

// 正交表相关类型定义
export interface OrthogonalTableRequest {
  factors: OrthogonalFactor[]
  title?: string
}

export interface OrthogonalTableResponse {
  tableCode: string
  title: string
  factors: OrthogonalFactor[]
  orthogonalType: string
  testCases: Array<{
    caseNumber: string
    factorValues: Record<string, string>
  }>
  totalCases: number
  theoreticalMaxCases: number
  reductionRate: number
}

// 数据文档生成API
export const dataDocumentApi = {
  // 生成等价类表
  generateEquivalenceTable(data: EquivalenceTableRequest) {
    return request.post<EquivalenceTableResponse>('/api/v1/data-documents/equivalence-tables', data)
  },

  // 生成正交表
  generateOrthogonalTable(data: OrthogonalTableRequest) {
    return request.post<OrthogonalTableResponse>('/api/v1/data-documents/orthogonal-tables', data)
  },

  // 导出等价类表到Excel
  exportEquivalenceTableToExcel(data: EquivalenceTableResponse) {
    return request.post('/api/v1/data-documents/equivalence-tables/export/excel', data, {
      responseType: 'blob'
    })
  },

  // 导出等价类表到Word
  exportEquivalenceTableToWord(data: EquivalenceTableResponse) {
    return request.post('/api/v1/data-documents/equivalence-tables/export/word', data, {
      responseType: 'blob'
    })
  },

  // 导出正交表到Excel
  exportOrthogonalTableToExcel(data: OrthogonalTableResponse) {
    return request.post('/api/v1/data-documents/orthogonal-tables/export/excel', data, {
      responseType: 'blob'
    })
  },

  // 导出正交表到Word
  exportOrthogonalTableToWord(data: OrthogonalTableResponse) {
    return request.post('/api/v1/data-documents/orthogonal-tables/export/word', data, {
      responseType: 'blob'
    })
  }
}

