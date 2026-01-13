import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { TestLayer, TestDesignMethod, ModelConfig } from '@/api/common'
import type { TestRequirement } from '@/api/requirement'
import type { PromptTemplate } from '@/api/promptTemplate'
import { requirementApi } from '@/api/requirement'
import { commonApi } from '@/api/common'
import { promptTemplateApi } from '@/api/promptTemplate'

/**
 * 缓存数据Store
 * 管理需求列表、测试分层、测试方法、模板列表、模型配置等缓存数据
 */
export const useCacheStore = defineStore('cache', () => {
  // 缓存过期时间（毫秒）- 默认5分钟
  const CACHE_EXPIRE_TIME = 5 * 60 * 1000

  // 状态
  const requirementList = ref<TestRequirement[]>([])
  const layerList = ref<TestLayer[]>([])
  const methodList = ref<TestDesignMethod[]>([])
  const templateList = ref<PromptTemplate[]>([])
  const modelList = ref<ModelConfig[]>([])

  // 缓存时间戳
  const requirementListTime = ref<number>(0)
  const layerListTime = ref<number>(0)
  const methodListTime = ref<number>(0)
  const templateListTime = ref<number>(0)
  const modelListTime = ref<number>(0)

  // 加载状态
  const loading = ref({
    requirementList: false,
    layerList: false,
    methodList: false,
    templateList: false,
    modelList: false
  })

  // 检查缓存是否过期
  const isCacheExpired = (timestamp: number): boolean => {
    return Date.now() - timestamp > CACHE_EXPIRE_TIME
  }

  // 加载需求列表
  const loadRequirementList = async (force = false) => {
    // 如果缓存未过期且不强制刷新，直接返回
    if (!force && !isCacheExpired(requirementListTime.value) && requirementList.value.length > 0) {
      return requirementList.value
    }

    loading.value.requirementList = true
    try {
      const response = await requirementApi.getRequirementList({ page: 0, size: 1000 })
      if (response.data) {
        requirementList.value = response.data.content || []
        requirementListTime.value = Date.now()
      }
      return requirementList.value
    } catch (error) {
      console.error('加载需求列表失败:', error)
      throw error
    } finally {
      loading.value.requirementList = false
    }
  }

  // 加载测试分层列表
  const loadLayerList = async (force = false) => {
    if (!force && !isCacheExpired(layerListTime.value) && layerList.value.length > 0) {
      return layerList.value
    }

    loading.value.layerList = true
    try {
      const response = await commonApi.getTestLayerList()
      if (response.data) {
        layerList.value = response.data
        layerListTime.value = Date.now()
      }
      return layerList.value
    } catch (error) {
      console.error('加载测试分层列表失败:', error)
      throw error
    } finally {
      loading.value.layerList = false
    }
  }

  // 加载测试方法列表
  const loadMethodList = async (force = false) => {
    if (!force && !isCacheExpired(methodListTime.value) && methodList.value.length > 0) {
      return methodList.value
    }

    loading.value.methodList = true
    try {
      const response = await commonApi.getTestDesignMethodList()
      if (response.data) {
        methodList.value = response.data
        methodListTime.value = Date.now()
      }
      return methodList.value
    } catch (error) {
      console.error('加载测试方法列表失败:', error)
      throw error
    } finally {
      loading.value.methodList = false
    }
  }

  // 加载提示词模板列表
  const loadTemplateList = async (force = false) => {
    if (!force && !isCacheExpired(templateListTime.value) && templateList.value.length > 0) {
      return templateList.value
    }

    loading.value.templateList = true
    try {
      const response = await promptTemplateApi.getTemplateList({ page: 0, size: 1000 })
      if (response.data) {
        // 只缓存启用的模板
        templateList.value = (response.data.content || []).filter(
          (t: PromptTemplate) => t.isActive === '1'
        )
        templateListTime.value = Date.now()
      }
      return templateList.value
    } catch (error) {
      console.error('加载提示词模板列表失败:', error)
      throw error
    } finally {
      loading.value.templateList = false
    }
  }

  // 加载模型配置列表
  const loadModelList = async (force = false) => {
    if (!force && !isCacheExpired(modelListTime.value) && modelList.value.length > 0) {
      return modelList.value
    }

    loading.value.modelList = true
    try {
      const response = await commonApi.getModelConfigList()
      if (response.data) {
        modelList.value = response.data
        modelListTime.value = Date.now()
      }
      return modelList.value
    } catch (error) {
      console.error('加载模型配置列表失败:', error)
      throw error
    } finally {
      loading.value.modelList = false
    }
  }

  // 清除所有缓存
  const clearAllCache = () => {
    requirementList.value = []
    layerList.value = []
    methodList.value = []
    templateList.value = []
    modelList.value = []
    requirementListTime.value = 0
    layerListTime.value = 0
    methodListTime.value = 0
    templateListTime.value = 0
    modelListTime.value = 0
  }

  // 清除指定缓存
  const clearCache = (type: 'requirementList' | 'layerList' | 'methodList' | 'templateList' | 'modelList') => {
    switch (type) {
      case 'requirementList':
        requirementList.value = []
        requirementListTime.value = 0
        break
      case 'layerList':
        layerList.value = []
        layerListTime.value = 0
        break
      case 'methodList':
        methodList.value = []
        methodListTime.value = 0
        break
      case 'templateList':
        templateList.value = []
        templateListTime.value = 0
        break
      case 'modelList':
        modelList.value = []
        modelListTime.value = 0
        break
    }
  }

  // 计算属性 - 获取启用的测试分层
  const activeLayers = computed(() => {
    return layerList.value.filter(layer => layer.isActive === '1')
  })

  // 计算属性 - 获取启用的测试方法
  const activeMethods = computed(() => {
    return methodList.value.filter(method => method.isActive === '1')
  })

  // 计算属性 - 获取启用的模型配置
  const activeModels = computed(() => {
    return modelList.value.filter(model => model.isActive === '1')
  })

  return {
    // 状态
    requirementList,
    layerList,
    methodList,
    templateList,
    modelList,
    loading,
    // 计算属性
    activeLayers,
    activeMethods,
    activeModels,
    // 方法
    loadRequirementList,
    loadLayerList,
    loadMethodList,
    loadTemplateList,
    loadModelList,
    clearAllCache,
    clearCache
  }
})

