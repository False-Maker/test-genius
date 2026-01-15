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
  [key: string]: any
}

export interface SearchDocumentsParams {
  queryText: string
  docType?: string
  topK?: number
  similarityThreshold?: number
}

// 知识库API
export const knowledgeBaseApi = {
  // 初始化知识库
  initKnowledgeBase() {
    return request.post<boolean>('/v1/knowledge/init')
  },

  // 添加知识库文档
  addDocument(params: {
    docCode: string
    docName: string
    docType: string
    docContent: string
    docCategory?: string
    docUrl?: string
    creatorId?: number
  }) {
    return request.post<number>('/v1/knowledge/documents', null, { params })
  },

  // 语义检索知识库文档
  searchDocuments(params: SearchDocumentsParams) {
    return request.post<KnowledgeDocument[]>('/v1/knowledge/documents/search', null, { params })
  },

  // 关键词检索知识库文档
  searchDocumentsByKeyword(keyword: string, params?: { docType?: string; topK?: number }) {
    return request.get<KnowledgeDocument[]>(`/v1/knowledge/documents/keyword/${keyword}`, { params })
  }
}

