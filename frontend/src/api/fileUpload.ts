import request from './request'

// 文件上传响应类型
export interface FileUploadResponse {
  filePath: string
  fileUrl: string
  fileName: string
  fileSize: string
}

// 文件上传API
export const fileUploadApi = {
  // 上传文件
  uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<FileUploadResponse>('/v1/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除文件
  deleteFile(filePath: string) {
    // 需要对路径进行编码
    const encodedPath = encodeURIComponent(filePath)
    return request.delete(`/v1/files/${encodedPath}`)
  }
}

