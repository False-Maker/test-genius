import request from './request'

// 场景图相关类型定义
export interface SceneDiagramRequest {
  requirementId?: number
  caseIds?: number[]
  title?: string
  direction?: 'LR' | 'TB' | 'RL' | 'BT'
  includeCaseDetails?: boolean
}

export interface SceneDiagramResponse {
  diagramCode: string
  title: string
  mermaidCode: string
  nodeCount: number
  edgeCount: number
}

// 路径图相关类型定义
export interface PathDiagramRequest {
  caseId?: number
  caseIds?: number[]
  requirementId?: number
  title?: string
  direction?: 'LR' | 'TB' | 'RL' | 'BT'
}

export interface PathDiagramResponse {
  diagramCode: string
  title: string
  mermaidCode: string
  pathCount: number
  nodeCount: number
  edgeCount: number
}

// 流程文档生成API
export const flowDocumentApi = {
  // 生成场景图
  generateSceneDiagram(data: SceneDiagramRequest) {
    return request.post<SceneDiagramResponse>('/api/v1/flow-documents/scene-diagrams', data)
  },

  // 生成路径图
  generatePathDiagram(data: PathDiagramRequest) {
    return request.post<PathDiagramResponse>('/api/v1/flow-documents/path-diagrams', data)
  },

  // 导出场景图文件
  exportSceneDiagramFile(mermaidCode: string, format: 'PNG' | 'SVG' | 'PDF', fileName?: string) {
    return request.post<string>('/api/v1/flow-documents/scene-diagrams/export', null, {
      params: {
        mermaidCode,
        format,
        fileName
      }
    })
  },

  // 导出路径图文件
  exportPathDiagramFile(mermaidCode: string, format: 'PNG' | 'SVG' | 'PDF', fileName?: string) {
    return request.post<string>('/api/v1/flow-documents/path-diagrams/export', null, {
      params: {
        mermaidCode,
        format,
        fileName
      }
    })
  }
}

