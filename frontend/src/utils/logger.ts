import { ElMessage } from 'element-plus'

export const logger = {
  error(message: string, error?: unknown, showToUser = true) {
    console.error('[Error]', message, error)
    
    // Optional: show to user
    if (showToUser) {
      ElMessage.error(typeof error === 'string' ? error : message)
    }
  },
  
  warn(message: string) {
    console.warn('[Warn]', message)
  },
  
  info(message: string) {
    console.info('[Info]', message)
  }
}