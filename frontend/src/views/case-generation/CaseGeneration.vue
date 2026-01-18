<template>

  <div class="case-generation">

    <el-card>

      <template #header>

        <div class="card-header">

          <h2>智能用例生成管理</h2>

        </div>

      </template>



      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">

        <el-form-item label="需求ID" prop="requirementId">

          <el-select

            v-model="form.requirementId"

            placeholder="请选择需求ID"

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

            {{ generationResult.message || '正在生成用例，请稍候..' }}

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

                ，失败{{ generationResult.failCases }} 个

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

import { ref, reactive, onMounted, computed } from 'vue'

import { useRouter } from 'vue-router'

import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { caseGenerationApi, type CaseGenerationRequest, type GenerationTask } from '@/api/caseGeneration'

import { useCacheStore } from '@/store/cache'



// 使用缓存store

const cacheStore = useCacheStore()



// 响应式数据据

const formRef = ref<FormInstance>()

const generateLoading = ref(false)



// 从store获取数据

const requirementList = computed(() => cacheStore.requirementList)

const layerList = computed(() => cacheStore.activeLayers)

const methodList = computed(() => cacheStore.activeMethods)

const templateList = computed(() => cacheStore.templateList)

const modelList = computed(() => cacheStore.activeModels)



// 加载状态

const requirementLoading = computed(() => cacheStore.loading.requirementList)

const layerLoading = computed(() => cacheStore.loading.layerList)

const methodLoading = computed(() => cacheStore.loading.methodList)

const templateLoading = computed(() => cacheStore.loading.templateList)

const modelLoading = computed(() => cacheStore.loading.modelList)



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

      { required: true, message: '请选择需求ID', trigger: 'change' }

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



// 加载所有数据（使用store的缓存机制）

const loadAllData = async () => {

  try {

    await Promise.all([

      cacheStore.loadRequirementList(),

      cacheStore.loadLayerList(),

      cacheStore.loadMethodList(),

      cacheStore.loadTemplateList(),

      cacheStore.loadModelList()

    ])

  } catch (error) {

    console.error('加载数据失败:', error)

    ElMessage.error('加载数据失败，请稍后重试')

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

      console.error('查询任务状态失败', error)

      if (pollTimer) {

        clearInterval(pollTimer)

        pollTimer = null

      }

    }

  }, 2000) // 2秒轮询一次

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

    // 跳转到用例列表页面，并筛选该需求ID相关的用例

    const router = useRouter()

    router.push({

      path: '/test-case',

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

  loadAllData()

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