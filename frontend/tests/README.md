# 前端测试文档

## 测试框架

本项目使用 [Vitest](https://vitest.dev/) 作为测试框架，配合 [@vue/test-utils](https://vue-test-utils.vuejs.org/) 进行 Vue 组件测试。

## 测试配置

- **测试环境**: happy-dom（快速、轻量的 DOM 实现）
- **覆盖率工具**: @vitest/coverage-v8
- **覆盖率阈值**: 70%（lines, functions, branches, statements）

## 运行测试

```bash
# 运行测试（watch模式）
npm run test

# 运行测试（UI模式）
npm run test:ui

# 运行测试（一次性）
npm run test:run

# 运行测试并生成覆盖率报告
npm run test:coverage
```

## 测试文件结构

```
frontend/
├── tests/
│   ├── setup.ts              # 测试全局配置
│   ├── mocks/                # Mock文件
│   │   ├── axios.ts         # Axios Mock
│   │   └── store.ts         # Store Mock
│   ├── api/                  # API测试
│   │   └── common.test.ts   # 通用API测试
│   ├── store/                # Store测试
│   │   ├── user.test.ts     # 用户Store测试
│   │   └── cache.test.ts    # 缓存Store测试
│   └── README.md            # 测试文档
└── vitest.config.ts         # Vitest配置
```

## 测试覆盖范围

### 已完成的测试

1. **API测试** (`tests/api/`)
   - ✅ `common.test.ts` - 通用API测试（测试分层、测试方法、模型配置）

2. **Store测试** (`tests/store/`)
   - ✅ `user.test.ts` - 用户Store测试（登录、登出、用户信息管理）
   - ✅ `cache.test.ts` - 缓存Store测试（数据加载、缓存机制、计算属性）

### 待完成的测试（可选）

1. **组件测试** (`tests/components/`)
   - 页面组件测试（RequirementList、TestCaseList、CaseGeneration等）

2. **更多API测试**
   - requirement.test.ts - 需求管理API
   - testCase.test.ts - 用例管理API
   - promptTemplate.test.ts - 提示词模板API
   - caseGeneration.test.ts - 用例生成API

## 编写测试指南

### 测试API函数

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { commonApi } from '@/api/common'
import request from '@/api/request'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('commonApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该成功获取数据', async () => {
    const mockData = [{ id: 1, name: 'test' }]
    vi.mocked(request.get).mockResolvedValue({
      code: 200,
      message: '操作成功',
      data: mockData
    })

    const result = await commonApi.getTestLayerList()
    expect(result.data).toEqual(mockData)
  })
})
```

### 测试Store

```typescript
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/store/user'

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('应该正确设置用户信息', () => {
    const store = useUserStore()
    store.setUserInfo({ id: 1, username: 'test' })
    expect(store.userInfo?.username).toBe('test')
  })
})
```

### 测试组件（示例）

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MyComponent from '@/components/MyComponent.vue'

describe('MyComponent', () => {
  it('应该正确渲染', () => {
    const wrapper = mount(MyComponent, {
      props: { title: 'Test' }
    })
    expect(wrapper.text()).toContain('Test')
  })
})
```

## 测试最佳实践

1. **测试隔离**: 每个测试应该独立，使用 `beforeEach` 清理状态
2. **Mock外部依赖**: 使用 `vi.mock()` Mock API调用和外部依赖
3. **测试命名**: 使用描述性的测试名称，说明测试的目的
4. **覆盖率目标**: 关键业务逻辑覆盖率≥70%
5. **异步测试**: 使用 `async/await` 处理异步操作

## 持续集成

测试已集成到CI/CD流程中，每次提交代码时会自动运行测试并检查覆盖率。

## 参考文档

- [Vitest文档](https://vitest.dev/)
- [Vue Test Utils文档](https://vue-test-utils.vuejs.org/)
- [Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)

