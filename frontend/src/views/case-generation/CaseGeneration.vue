<template>
  <div class="case-generation">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>智能用例生成</h2>
        </div>
      </template>

      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="需求" prop="requirementId">
          <el-select
            v-model="form.requirementId"
            placeholder="请选择需求"
            filterable
            clearable
            style="width: 100%"
            :loading="requirementLoading"
          >
            <el-option
              v-for="req in requirementList"
              :key="req.id"
              :label="`${req.requirementCode} - ${req.requirementName}`"
              :value="req.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="测试分层" prop="layerCode">
          <el-select
            v-model="form.layerCode"
            placeholder="请选择测试分层"
            clearable
            style="width: 100%"
            :loading="layerLoading"
          >
            <el-option
              v-for="layer in layerList"
              :key="layer.layerCode"
              :label="layer.layerName"
              :value="layer.layerCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="测试方法" prop="methodCode">
          <el-select
            v-model="form.methodCode"
            placeholder="请选择测试方法"
            clearable
            style="width: 100%"
            :loading="methodLoading"
          >
            <el-option
              v-for="method in methodList"
              :key="method.methodCode"
              :label="method.methodName"
              :value="method.methodCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="提示词模板">
          <el-select
            v-model="form.templateId"
            placeholder="请选择提示词模板（可选）"
            filterable
            clearable
            style="width: 100%"
            :loading="templateLoading"
          >
            <el-option
              v-for="template in templateList"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="模型配置">
          <el-select
            v-model="form.modelCode"
            placeholder="请选择模型配置（可选，默认使用优先级最高的模型）"
            filterable
            clearable
            style="width: 100%"
            :loading="modelLoading"
          >
            <el-option
              v-for="model in modelList"
              :key="model.modelCode"
              :label="model.modelName"
              :value="model.modelCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleGenerate" :loading="generateLoading">
            生成用例
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 生成结果 -->
      <el-card v-if="generationResult" class="result-card" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>生成结果</span>
            <el-tag :type="getStatusType(generationResult.status)">
              {{ getStatusText(generationResult.status) }}
            </el-tag>
          </div>
        </template>

        <div v-if="generationResult.status === 'PROCESSING'">
          <el-progress :percentage="generationResult.progress || 0" />
          <p style="margin-top: 10px; color: #909399">
            {{ generationResult.message || '正在生成用例，请稍候...' }}
          </p>
        </div>

        <div v-else-if="generationResult.status === 'SUCCESS'">
          <el-alert
            type="success"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          >
            {{ generationResult.message || '用例生成成功' }}
            <template v-if="generationResult.successCases !== undefined">
              <br />成功生成 {{ generationResult.successCases }} 个用例
              <template v-if="generationResult.failCases && generationResult.failCases > 0">
                ，失败 {{ generationResult.failCases }} 个
              </template>
            </template>
          </el-alert>
          <el-button type="primary" @click="handleViewResults">查看生成的用例</el-button>
        </div>

        <div v-else-if="generationResult.status === 'FAILED'">
          <el-alert
            type="error"
            :closable="false"
            show-icon
          >
            {{ generationResult.message || '用例生成失败' }}
          </el-alert>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { requirementApi, type TestRequirement } from '@/api/requirement'
import { commonApi, type TestLayer, type TestDesignMethod, type ModelConfig } from '@/api/common'
import { promptTemplateApi, type PromptTemplate } from '@/api/promptTemplate'
import { caseGenerationApi, type CaseGenerationRequest, type GenerationTask } from '@/api/caseGeneration'

// 响应式数据
const formRef = ref<FormInstance>()
const requirementLoading = ref(false)
const layerLoading = ref(false)
const methodLoading = ref(false)
const templateLoading = ref(false)
const modelLoading = ref(false)
const generateLoading = ref(false)

const requirementList = ref<TestRequirement[]>([])
const layerList = ref<TestLayer[]>([])
const methodList = ref<TestDesignMethod[]>([])
const templateList = ref<PromptTemplate[]>([])
const modelList = ref<ModelConfig[]>([])

const form = reactive<CaseGenerationRequest & { templateId?: number }>({
  requirementId: undefined,
  layerCode: '',
  methodCode: '',
  templateId: undefined,
  modelCode: undefined
})

const router = useRouter()
const generationResult = ref<GenerationTask | null>(null)
let pollTimer: number | null = null

// 表单验证规则
const formRules: FormRules = {
  requirementId: [
    { required: true, message: '请选择需求', trigger: 'change' }
  ]
}

// 获取状态文本
const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    PENDING: '等待中',
    PROCESSING: '处理中',
    SUCCESS: '成功',
    FAILED: '失败'
  }
  return statusMap[status || ''] || status || '-'
}

// 获取状态类型
const getStatusType = (status?: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'info',
    PROCESSING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return typeMap[status || ''] || ''
}

// 加载需求列表
const loadRequirementList = async () => {
  requirementLoading.value = true
  try {
    const response = await requirementApi.getRequirementList({ page: 0, size: 100 })
    if (response.data) {
      requirementList.value = response.data.content || []
    }
  } catch (error) {
    console.error('加载需求列表失败:', error)
  } finally {
    requirementLoading.value = false
  }
}

// 加载测试分层列表
const loadLayerList = async () => {
  layerLoading.value = true
  try {
    const response = await commonApi.getTestLayerList()
    if (response.data) {
      layerList.value = response.data
    }
  } catch (error) {
    console.error('加载测试分层列表失败:', error)
  } finally {
    layerLoading.value = false
  }
}

// 加载测试方法列表
const loadMethodList = async () => {
  methodLoading.value = true
  try {
    const response = await commonApi.getTestDesignMethodList()
    if (response.data) {
      methodList.value = response.data
    }
  } catch (error) {
    console.error('加载测试方法列表失败:', error)
  } finally {
    methodLoading.value = false
  }
}

// 加载提示词模板列表
const loadTemplateList = async () => {
  templateLoading.value = true
  try {
    const response = await promptTemplateApi.getTemplateList({ page: 0, size: 100 })
    if (response.data) {
      // 只显示启用的模板
      templateList.value = (response.data.content || []).filter(
        (t: PromptTemplate) => t.isActive === '1'
      )
    }
  } catch (error) {
    console.error('加载提示词模板列表失败:', error)
  } finally {
    templateLoading.value = false
  }
}

// 加载模型配置列表
const loadModelList = async () => {
  modelLoading.value = true
  try {
    const response = await commonApi.getModelConfigList()
    if (response.data) {
      modelList.value = response.data
    }
  } catch (error) {
    console.error('加载模型配置列表失败:', error)
  } finally {
    modelLoading.value = false
  }
}

// 轮询任务状态
const pollTaskStatus = async (taskId: number) => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }

  pollTimer = window.setInterval(async () => {
    try {
      const response = await caseGenerationApi.getGenerationTask(taskId)
      if (response.data) {
        generationResult.value = response.data

        // 如果任务完成或失败，停止轮询
        if (
          response.data.status === 'SUCCESS' ||
          response.data.status === 'FAILED'
        ) {
          if (pollTimer) {
            clearInterval(pollTimer)
            pollTimer = null
          }
        }
      }
    } catch (error) {
      console.error('查询任务状态失败:', error)
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }
  }, 2000) // 每2秒轮询一次
}

// 生成用例
const handleGenerate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      generateLoading.value = true
      try {
        const request: CaseGenerationRequest = {
          requirementId: form.requirementId!,
          layerCode: form.layerCode || undefined,
          methodCode: form.methodCode || undefined,
          templateId: form.templateId,
          modelCode: form.modelCode
        }

        const response = await caseGenerationApi.generateTestCases(request)
        if (response.data) {
          generationResult.value = {
            id: response.data.taskId,
            requirementId: form.requirementId!,
            status: response.data.status || 'PENDING',
            message: response.data.message
          }

          // 如果是异步任务，开始轮询状态
          if (response.data.taskId && response.data.status === 'PROCESSING') {
            pollTaskStatus(response.data.taskId)
          }

          ElMessage.success('用例生成任务已提交')
        }
      } catch (error) {
        console.error('生成用例失败:', error)
        ElMessage.error('生成用例失败，请稍后重试')
      } finally {
        generateLoading.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  generationResult.value = null
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 查看生成结果
const handleViewResults = () => {
  if (generationResult.value && generationResult.value.requirementId) {
    // 跳转到用例列表页面，并筛选该需求相关的用例
    const router = useRouter()
    router.push({
      path: '/test-cases',
      query: {
        requirementId: generationResult.value.requirementId.toString()
      }
    })
  } else {
    ElMessage.warning('无法获取需求ID')
  }
}

// 初始化
onMounted(() => {
  loadRequirementList()
  loadLayerList()
  loadMethodList()
  loadTemplateList()
  loadModelList()
})

// 组件卸载时清理定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<style scoped lang="scss">
.case-generation {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
    }
  }

  .result-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
