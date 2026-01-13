# 前端状态管理 Store 使用说明

## 概述

本项目使用 Pinia 进行全局状态管理，包含以下 Store 模块：

- **user** - 用户信息管理
- **config** - 系统配置管理
- **cache** - 缓存数据管理
- **app** - 应用状态管理

## Store 模块说明

### 1. User Store (`user.ts`)

管理用户登录状态、用户信息等。

**使用示例：**
```typescript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 登录
userStore.login({ id: 1, username: 'admin' }, 'token-string')

// 获取用户信息
const userInfo = userStore.userInfo
const isLoggedIn = userStore.isLoggedIn

// 登出
userStore.logout()
```

**状态持久化：**
- 用户信息和token自动保存到 `localStorage`
- 页面刷新后自动恢复

### 2. Config Store (`config.ts`)

管理系统配置、API配置等。

**使用示例：**
```typescript
import { useConfigStore } from '@/store/config'

const configStore = useConfigStore()

// 获取配置
const apiBaseUrl = configStore.apiBaseUrl
const pageSize = configStore.pageSize

// 更新配置
configStore.setTheme('dark')
configStore.setLanguage('en-US')
configStore.setPageSize(20)
```

**状态持久化：**
- 配置信息自动保存到 `localStorage`
- 页面刷新后自动恢复

### 3. Cache Store (`cache.ts`)

管理需求列表、测试分层、测试方法、模板列表、模型配置等缓存数据。

**特性：**
- 自动缓存机制（默认5分钟过期）
- 支持强制刷新
- 自动过滤启用的数据

**使用示例：**
```typescript
import { useCacheStore } from '@/store/cache'

const cacheStore = useCacheStore()

// 加载数据（使用缓存）
await cacheStore.loadRequirementList()
await cacheStore.loadLayerList()
await cacheStore.loadMethodList()
await cacheStore.loadTemplateList()
await cacheStore.loadModelList()

// 强制刷新（忽略缓存）
await cacheStore.loadRequirementList(true)

// 获取数据
const requirementList = cacheStore.requirementList
const activeLayers = cacheStore.activeLayers // 只包含启用的测试分层
const activeMethods = cacheStore.activeMethods // 只包含启用的测试方法
const activeModels = cacheStore.activeModels // 只包含启用的模型配置

// 清除缓存
cacheStore.clearCache('requirementList') // 清除指定缓存
cacheStore.clearAllCache() // 清除所有缓存
```

**在组件中使用：**
```vue
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useCacheStore } from '@/store/cache'

const cacheStore = useCacheStore()

// 使用计算属性获取数据
const requirementList = computed(() => cacheStore.requirementList)
const layerList = computed(() => cacheStore.activeLayers)

// 加载数据
onMounted(async () => {
  await cacheStore.loadRequirementList()
  await cacheStore.loadLayerList()
})
</script>
```

### 4. App Store (`app.ts`)

管理全局加载状态、错误状态、侧边栏状态等。

**使用示例：**
```typescript
import { useAppStore } from '@/store/app'

const appStore = useAppStore()

// 显示/隐藏全局加载
appStore.showLoading('正在加载...')
appStore.hideLoading()

// 错误处理
appStore.setError('操作失败', 'ERROR_CODE')
appStore.clearError()

// 侧边栏控制
appStore.toggleSidebar()
appStore.setSidebarCollapsed(true)
```

**状态持久化：**
- 侧边栏状态自动保存到 `localStorage`

## 最佳实践

### 1. 在组件中使用 Store

```vue
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useCacheStore } from '@/store/cache'
import { useAppStore } from '@/store/app'

const cacheStore = useCacheStore()
const appStore = useAppStore()

// 使用计算属性获取响应式数据
const requirementList = computed(() => cacheStore.requirementList)
const loading = computed(() => cacheStore.loading.requirementList)

// 在组件挂载时加载数据
onMounted(async () => {
  try {
    appStore.showLoading('加载数据中...')
    await cacheStore.loadRequirementList()
  } catch (error) {
    appStore.setError('加载失败')
  } finally {
    appStore.hideLoading()
  }
})
</script>
```

### 2. 缓存数据的使用

- 对于不经常变化的数据（如测试分层、测试方法、模型配置），使用 `cacheStore` 进行缓存
- 缓存默认5分钟过期，可以通过 `force` 参数强制刷新
- 使用计算属性 `activeLayers`、`activeMethods`、`activeModels` 获取已过滤的启用数据

### 3. 状态持久化

- 用户信息、配置信息、侧边栏状态等会自动持久化到 `localStorage`
- 页面刷新后会自动恢复这些状态
- 不需要手动管理持久化逻辑

### 4. 错误处理

- 使用 `appStore.setError()` 设置全局错误信息
- 在组件中可以通过 `appStore.errorMessage` 获取错误信息
- 操作完成后记得调用 `appStore.clearError()` 清除错误

## 注意事项

1. **不要在 Store 外部直接修改状态**：所有状态修改都应该通过 Store 提供的方法
2. **合理使用缓存**：对于频繁变化的数据，不要使用缓存，或者设置较短的过期时间
3. **避免循环依赖**：Store 之间不要相互依赖
4. **类型安全**：使用 TypeScript 确保类型安全

