/**
 * 缓存配置
 * 定义缓存策略和过期时间
 */
export const cacheConfig = {
  /** 缓存过期时间（毫秒），默认 5 分钟 */
  expireTime: 5 * 60 * 1000,

  /** 是否启用缓存 */
  enableCache: true,

  /** 最大缓存条目数 */
  maxSize: 100,

  /** 缓存键前缀 */
  keyPrefix: 'app_cache_'
}
