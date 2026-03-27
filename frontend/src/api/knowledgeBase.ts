import request from './request'
import type { ApiResult } from './types'

// 知识库文档类型
export interface KnowledgeDocument {
  id?: number
  docCode: string
  docName: string
  docType: string
  docContent: string
  docCategory?: string
  docUrl?: string
  similarity?: number
  [key: string]: unknown
}

type KnowledgeDocumentResponse = {
  id?: number
  doc_code?: string
  docCode?: string
  doc_name?: string
  docName?: string
  doc_type?: string
  docType?: string
  doc_content?: string
  docContent?: string
  doc_category?: string
  docCategory?: string
  doc_url?: string | null
  docUrl?: string | null
  similarity?: number
  [key: string]: unknown
}

// 知识库类型
export interface KnowledgeBase {
  id?: number
  kbCode: string
  kbName: string
  kbDescription?: string
  kbType?: string
  creatorId?: number
  isActive?: string
  documentCount?: number
  chunkCount?: number
  lastSyncTime?: string
  createTime?: string
updateTime?: string
  [key: string]: unknown
}

export interface SearchDocumentsParams {
  queryText: string
  docType?: string
  topK?: number
  similarityThreshold?: number
}

export interface KBSyncParams {
  kbId: number
  syncType: 'incremental' | 'full'
  sourcePath: string
  chunkingStrategy?: string
  chunkSize?: number
  chunkOverlap?: number
}

export interface KBPermissionParams {
  kbId: number
  userId: number
  permissionType: 'read' | 'write' | 'admin'
}

const normalizeKnowledgeDocument = (item: KnowledgeDocumentResponse): KnowledgeDocument => ({
  ...item,
  docCode: item.docCode ?? item.doc_code ?? '',
  docName: item.docName ?? item.doc_name ?? '',
  docType: item.docType ?? item.doc_type ?? '',
  docContent: item.docContent ?? item.doc_content ?? '',
  docCategory: item.docCategory ?? item.doc_category,
  docUrl: item.docUrl ?? item.doc_url ?? undefined,
  similarity: item.similarity
})

const normalizeKnowledgeDocumentList = (
  response: ApiResult<KnowledgeDocumentResponse[]>
): ApiResult<KnowledgeDocument[]> => ({
  ...response,
  data: (response.data || []).map(normalizeKnowledgeDocument)
})

// 知识库API（统一走 Java 后端 /api/v1/knowledge-base）
export const knowledgeBaseApi = {
  // 获取知识库列表
  listKnowledgeBases() {
    return request.get<any, ApiResult<KnowledgeBase[]>>('/v1/knowledge-base')
  },

  // 获取知识库详情
  getKnowledgeBaseById(id: number) {
    return request.get<any, ApiResult<KnowledgeBase>>(`/v1/knowledge-base/${id}`)
  },

  // 生成知识库编码
  generateKbCode() {
    return request.get<any, ApiResult<string>>('/v1/knowledge-base/generate-code')
  },

  // 初始化知识库（代理到 Python）
  initKnowledgeBase() {
    return request.post<any, ApiResult<boolean>>('/v1/knowledge-base/init')
  },

  // 添加知识库文档（代理到 Python）
  addDocument(params: {
    kbId?: number
    docCode: string
    docName: string
    docType: string
    docContent: string
    docCategory?: string
    docUrl?: string
    creatorId?: number
  }) {
    return request.post<any, ApiResult<number>>('/v1/knowledge-base/documents', params)
  },

  // 上传文档到知识库（Java 再代理到 Python）
  uploadDocument(kbId: number, file: File, creatorId?: number) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('creatorId', String(creatorId ?? 1))
    return request.post<any, ApiResult<string>>(`/v1/knowledge-base/${kbId}/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).then((res: ApiResult<string>) => ({
      success: res.code === 200 || res.code === 0,
      docCode: res.data,
      message: res.message
    }))
  },

  // 语义检索知识库文档（代理到 Python）
  async searchDocuments(params: SearchDocumentsParams) {
    const response = await request.post<any, ApiResult<KnowledgeDocumentResponse[]>>('/v1/knowledge-base/documents/search', {
      queryText: params.queryText,
      docType: params.docType,
      topK: params.topK ?? 10,
      similarityThreshold: params.similarityThreshold ?? 0.7
    })
    return normalizeKnowledgeDocumentList(response)
  },

  // 关键词检索知识库文档（代理到 Python）
  async searchDocumentsByKeyword(keyword: string, params?: { docType?: string; topK?: number }) {
    const response = await request.get<any, ApiResult<KnowledgeDocumentResponse[]>>(
      `/v1/knowledge-base/documents/keyword/${encodeURIComponent(keyword)}`,
      { params: { docType: params?.docType, topK: params?.topK ?? 10 } }
    )
    return normalizeKnowledgeDocumentList(response)
  },

  async listDocuments(kbId: number, limit = 20) {
    const response = await request.get<any, ApiResult<KnowledgeDocumentResponse[]>>(`/v1/knowledge-base/${kbId}/documents`, {
      params: { limit }
    })
    return normalizeKnowledgeDocumentList(response)
  },

// 获取知识库统计信息（通过详情接口，已包含 documentCount/chunkCount/lastSyncTime）
  getStatistics(kbId: number) {
    return request.get<ApiResult<KnowledgeBase>>(`/v1/knowledge-base/${kbId}`).then((res) => {
      const dto = res.data as unknown as KnowledgeBase
      return {
        ...res,
        data: dto ? {
          documentCount: dto.documentCount ?? 0,
          chunkCount: dto.chunkCount ?? 0,
          lastSyncTime: dto.lastSyncTime != null ? String(dto.lastSyncTime) : undefined
        } : undefined
      }
    })
  },

  // 同步知识库
  syncKnowledgeBase(params: KBSyncParams): Promise<ApiResult<Record<string, unknown>>> {
    return request.post<Record<string, unknown>>(
      `/v1/knowledge-base/${params.kbId}/sync`,
      null,
      { params: { syncType: params.syncType, sourcePath: params.sourcePath } }
    ) as unknown as Promise<ApiResult<Record<string, unknown>>>
  },

  // 获取同步日志
  getSyncLogs(kbId: number) {
    return request.get<any, ApiResult<any>>(`/v1/knowledge-base/${kbId}/sync-logs`)
  },

  // 授予权限
  grantPermission(params: KBPermissionParams) {
    return request.post<any, ApiResult<boolean>>('/v1/knowledge-base/permission/grant', params)
  },

  // 撤销权限
  revokePermission(kbId: number, userId: number, permissionType: string) {
    return request.delete<any, ApiResult<boolean>>('/v1/knowledge-base/permission/revoke', {
      params: { kbId, userId, permissionType }
    })
  },

  // 检查权限
  checkPermission(kbId: number, userId: number, permissionType: string) {
    return request.get<any, ApiResult<boolean>>('/v1/knowledge-base/permission/check', {
      params: { kbId, userId, permissionType }
    })
  },

  // 获取用户的知识库列表
  getUserKnowledgeBases(userId: number, permissionType?: string) {
    return request.get<any, ApiResult<KnowledgeBase[]>>(`/v1/knowledge-base/user/${userId}`, {
      params: permissionType ? { permissionType } : {}
    })
  },

  // 获取知识库的权限列表
  getKBPermissions(kbId: number) {
    return request.get<any, ApiResult<any>>(`/v1/knowledge-base/${kbId}/permissions`)
  },

  // 创建知识库
  createKnowledgeBase(data: KnowledgeBase & { creatorId?: number; isActive?: string }) {
    return request.post<any, ApiResult<number>>('/v1/knowledge-base', data)
  },

  // 更新知识库
  updateKnowledgeBase(id: number, data: KnowledgeBase) {
    return request.put<any, ApiResult<KnowledgeBase>>(`/v1/knowledge-base/${id}`, data)
  },

  // 删除知识库
  deleteKnowledgeBase(id: number) {
    return request.delete<any, ApiResult<boolean>>(`/v1/knowledge-base/${id}`)
  }
}
