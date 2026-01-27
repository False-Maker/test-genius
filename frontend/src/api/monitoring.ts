import request from './request'

// 性能统计响应类型
export interface PerformanceStats {
  totalCount: number
  successCount: number
  failedCount: number
  successRate: number
  failureRate: number
  avgResponseTime: number
  p50ResponseTime: number
  p95ResponseTime: number
  p99ResponseTime: number
}

// 响应时间统计类型
export interface ResponseTimeStats {
  min: number
  max: number
  avg: number
  p50: number
  p95: number
  p99: number
}

// 成功率统计类型
export interface SuccessRateStats {
  totalCount: number
  successCount: number
  failedCount: number
  successRate: number
  failureRate: number
}

// Token使用量统计类型
export interface TokenUsageStats {
  totalTokens: number
  avgTokens: number
  requestCount: number
}

// 成本统计类型
export interface CostStats {
  totalCost: number
  avgCost: number
  requestCount: number
}

// 模型使用情况统计类型
export interface ModelUsageStats {
  modelUsage: Record<string, number>
  totalRequests: number
}

// 应用使用情况统计类型
export interface AppUsageStats {
  appUsage: Record<string, number>
  totalRequests: number
}

// 时间序列数据点类型
export interface TimeSeriesDataPoint {
  time: string
  value: number
}

// 时间序列数据类型
export interface TimeSeriesData {
  data: TimeSeriesDataPoint[]
  metric: string
  interval: string
}

// 监控API
export const monitoringApi = {
  // 获取性能统计
  getPerformanceStats(
    startTime: string,
    endTime: string,
    modelCode?: string,
    appType?: string,
    userId?: number
  ) {
    const params: any = { startTime, endTime }
    if (modelCode) params.modelCode = modelCode
    if (appType) params.appType = appType
    if (userId) params.userId = userId
    return request.get<PerformanceStats>('/v1/monitoring/performance', { params })
  },

  // 获取响应时间统计
  getResponseTimeStats(
    startTime: string,
    endTime: string,
    modelCode?: string,
    appType?: string
  ) {
    const params: any = { startTime, endTime }
    if (modelCode) params.modelCode = modelCode
    if (appType) params.appType = appType
    return request.get<ResponseTimeStats>('/v1/monitoring/response-time', { params })
  },

  // 获取成功率统计
  getSuccessRateStats(
    startTime: string,
    endTime: string,
    modelCode?: string,
    appType?: string
  ) {
    const params: any = { startTime, endTime }
    if (modelCode) params.modelCode = modelCode
    if (appType) params.appType = appType
    return request.get<SuccessRateStats>('/v1/monitoring/success-rate', { params })
  },

  // 获取Token使用量统计
  getTokenUsageStats(
    startTime: string,
    endTime: string,
    modelCode?: string,
    appType?: string
  ) {
    const params: any = { startTime, endTime }
    if (modelCode) params.modelCode = modelCode
    if (appType) params.appType = appType
    return request.get<TokenUsageStats>('/v1/monitoring/token-usage', { params })
  },

  // 获取成本统计
  getCostStats(
    startTime: string,
    endTime: string,
    modelCode?: string,
    appType?: string,
    userId?: number
  ) {
    const params: any = { startTime, endTime }
    if (modelCode) params.modelCode = modelCode
    if (appType) params.appType = appType
    if (userId) params.userId = userId
    return request.get<CostStats>('/v1/monitoring/cost', { params })
  },

  // 获取模型使用情况统计
  getModelUsageStats(startTime: string, endTime: string) {
    return request.get<ModelUsageStats>('/v1/monitoring/model-usage', {
      params: { startTime, endTime }
    })
  },

  // 获取应用使用情况统计
  getAppUsageStats(startTime: string, endTime: string) {
    return request.get<AppUsageStats>('/v1/monitoring/app-usage', {
      params: { startTime, endTime }
    })
  },

  // 获取时间序列数据
  getTimeSeriesData(
    startTime: string,
    endTime: string,
    interval: 'HOUR' | 'DAY' | 'WEEK' | 'MONTH',
    metric: 'RESPONSE_TIME' | 'SUCCESS_RATE' | 'TOKEN_USAGE' | 'COST',
    modelCode?: string,
    appType?: string
  ) {
    const params: any = { startTime, endTime, interval, metric }
    if (modelCode) params.modelCode = modelCode
    if (appType) params.appType = appType
    return request.get<TimeSeriesData>('/v1/monitoring/time-series', { params })
  }
}
