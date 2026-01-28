import request from './request'

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

// 知识库类型
export interface KnowledgeBase {
  id?: number
  kbCode: string
  kbName: string
  kbDescription?: string
  kbType?: string
  documentCount?: number
  chunkCount?: number
  lastSyncTime?: string
  createTime?: string
  updateTime?: string
  [key: string]: any
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

  // 上传文档到知识库
  uploadDocument(kbId: number, file: File, creatorId?: number) {
    const formData = new FormData()
    formData.append('kb_id', kbId.toString())
    formData.append('file_name', file.name)
    
    return new Promise<{ success: boolean; docId?: number; docCode?: string; chunks?: number; message?: string }>((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = async () => {
        try {
          const base64Content = (reader.result as string).split(',')[1] || reader.result as string
          formData.append('file_content', base64Content)
          if (creatorId) {
            formData.append('creator_id', creatorId.toString())
          }
          
          const response = await request.post('/v1/knowledge/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          })
          resolve(response.data || response)
        } catch (error) {
          reject(error)
        }
      }
      reader.onerror = reject
      reader.readAsDataURL(file)
    })
  },

  // 语义检索知识库文档
  searchDocuments(params: SearchDocumentsParams) {
    return request.post<KnowledgeDocument[]>('/v1/knowledge/documents/search', null, { params })
  },

  // 关键词检索知识库文档
  searchDocumentsByKeyword(keyword: string, params?: { docType?: string; topK?: number }) {
    return request.get<KnowledgeDocument[]>(`/v1/knowledge/documents/keyword/${keyword}`, { params })
  },

  // 获取知识库统计信息
  getStatistics(kbId: number) {
    return request.post<{ documentCount: number; chunkCount: number; lastSyncTime?: string }>('/v1/knowledge/statistics', null, {
      params: { kb_id: kbId }
    })
  },

  // 同步知识库
  syncKnowledgeBase(params: KBSyncParams) {
    return request.post('/v1/knowledge/sync', null, { params })
  },

  // 获取同步日志
  getSyncLogs(kbId: number) {
    return request.get(`/v1/knowledge/knowledge-base/${kbId}/sync-logs`)
  },

  // 授予权限
  grantPermission(params: KBPermissionParams) {
    return request.post('/v1/knowledge/permission/grant', null, { params })
  },

  // 撤销权限
  revokePermission(kbId: number, userId: number, permissionType: string) {
    return request.delete('/v1/knowledge/permission/revoke', {
      params: { kb_id: kbId, user_id: userId, permission_type: permissionType }
    })
  },

  // 检查权限
  checkPermission(kbId: number, userId: number, permissionType: string) {
    return request.get<{ hasPermission: boolean }>('/v1/knowledge/permission/check', {
      params: { kb_id: kbId, user_id: userId, permission_type: permissionType }
    })
  },

  // 获取用户的知识库列表
  getUserKnowledgeBases(userId: number, permissionType?: string) {
    return request.get<KnowledgeBase[]>(`/v1/knowledge/user/${userId}/knowledge-bases`, {
      params: permissionType ? { permission_type: permissionType } : {}
    })
  },

  // 获取知识库的权限列表
  getKBPermissions(kbId: number) {
    return request.get(`/v1/knowledge/knowledge-base/${kbId}/permissions`)
  }
}

