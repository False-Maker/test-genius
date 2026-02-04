<template>
  <div class="case-generation-container">
    <div class="bento-grid">
      <!-- Config Section (Left/Main) -->
      <div class="bento-item config-item">
        <el-card class="bento-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><MagicStick /></el-icon>
                <h2>智能用例生成</h2>
              </div>
              <span class="header-subtitle">AI-Powered Case Generation</span>
            </div>
          </template>

          <el-form :model="form" :rules="formRules" ref="formRef" label-position="top" class="bento-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="需求ID (Requirement)" prop="requirementId">
                  <el-select
                    v-model="form.requirementId"
                    placeholder="选择需求"
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
              </el-col>
              <el-col :span="12">
                <el-form-item label="模型配置 (Model)" prop="modelCode">
                  <el-select
                    v-model="form.modelCode"
                    placeholder="默认模型"
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
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="测试分层 (Layer)" prop="layerCode">
                  <el-select
                    v-model="form.layerCode"
                    placeholder="全部"
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
              </el-col>
              <el-col :span="8">
                <el-form-item label="测试方法 (Method)" prop="methodCode">
                  <el-select
                    v-model="form.methodCode"
                    placeholder="全部"
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
              </el-col>
              <el-col :span="8">
                 <el-form-item label="提示词模板 (Prompt)">
                  <el-select
                    v-model="form.templateId"
                    placeholder="默认模板"
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
              </el-col>
            </el-row>

            <el-form-item label="工作流 (Workflow)">
              <el-select
                v-model="form.workflowId"
                placeholder="默认流程"
                filterable
                clearable
                style="width: 100%"
                :loading="workflowLoading"
              >
                <el-option
                  v-for="workflow in workflowList"
                  :key="workflow.id"
                  :label="`${workflow.workflowName} (${workflow.workflowCode})`"
                  :value="workflow.id"
                />
              </el-select>
              <div class="form-helper">
                <el-link type="primary" :underline="false" @click="handleOpenWorkflowEditor">
                  <el-icon class="mr-1"><Edit /></el-icon> 创建新工作流
                </el-link>
                <el-divider direction="vertical" />
                <el-link type="info" :underline="false" @click="loadWorkflows">
                  <el-icon class="mr-1"><Refresh /></el-icon> 刷新
                </el-link>
              </div>
            </el-form-item>

            <div class="form-actions">
              <el-button class="action-btn" type="primary" @click="handleGenerate" :loading="generateLoading" size="large">
                <el-icon class="mr-2"><VideoPlay /></el-icon> 开始执行 (Generate)
              </el-button>
              <el-button class="action-btn" @click="handleReset" size="large">重置</el-button>
            </div>
          </el-form>
        </el-card>
      </div>

      <!-- Result Section (Right/Status) -->
      <div class="bento-item status-item">
        <el-card class="bento-card status-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Odometer /></el-icon>
                <h2>当前状态</h2>
              </div>
            </div>
          </template>
          
          <div class="status-content">
            <div v-if="!generationResult" class="empty-status">
              <el-empty description="等待任务开始..." :image-size="80" />
            </div>

            <div v-else class="active-status">
              <div class="status-indicator">
                <el-progress 
                  type="dashboard" 
                  :percentage="generationResult.progress || (generationResult.status === 'SUCCESS' ? 100 : 50)" 
                  :status="generationResult.status === 'SUCCESS' ? 'success' : (generationResult.status === 'FAILED' ? 'exception' : '')"
                  :width="120"
                />
                <div class="status-text">{{ getStatusText(generationResult.status) }}</div>
              </div>
              
              <div class="status-details">
                <p class="status-message">{{ generationResult.message }}</p>
                
                <div v-if="generationResult.status === 'SUCCESS'" class="result-stats">
                   <div class="stat-item success">
                    <span class="label">Success</span>
                    <span class="value">{{ generationResult.successCases || 0 }}</span>
                   </div>
                   <div class="stat-item fail" v-if="generationResult.failCases">
                    <span class="label">Failed</span>
                    <span class="value">{{ generationResult.failCases }}</span>
                   </div>
                </div>

                <div class="status-actions">
                   <el-button 
                    v-if="generationResult.status === 'SUCCESS'" 
                    type="success" 
                    plain 
                    block 
                    @click="handleViewResults"
                  >
                    查看结果详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- History List Section (Bottom/Full) -->
      <div class="bento-item list-item">
        <el-card class="bento-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><List /></el-icon>
                <h2>任务历史</h2>
              </div>
              <el-button link type="primary" @click="handleRefreshTaskList">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="taskListLoading"
            :data="taskList"
            style="width: 100%"
            class="bento-table"
          >
            <el-table-column prop="taskCode" label="TASK ID" width="160">
                <template #default="scope">
                    <span class="mono-text">{{ scope.row.taskCode }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="requirementName" label="REQUIREMENT" min-width="200" show-overflow-tooltip />
            <el-table-column label="CONFIG" min-width="200">
              <template #default="scope">
                <div class="config-tags">
                    <el-tag size="small" type="info" v-if="scope.row.layerName">{{ scope.row.layerName }}</el-tag>
                    <el-tag size="small" type="info" v-if="scope.row.methodName">{{ scope.row.methodName }}</el-tag>
                    <el-tag size="small" type="info" v-if="scope.row.modelCode">{{ scope.row.modelCode }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="STATUS" width="100">
              <template #default="scope">
                <el-tag :type="getTaskStatusType(scope.row.taskStatus)" effect="plain">
                  {{ getTaskStatusText(scope.row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="CASES" width="120">
              <template #default="scope">
                 <span :class="{'text-success': true}">{{ scope.row.successCases }}</span> / {{ scope.row.totalCases }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="TIME" width="180">
                <template #default="scope">
                    <span class="mono-text text-sm">{{ scope.row.createTime }}</span>
                </template>
            </el-table-column>
            <el-table-column label="ACTIONS" width="180" fixed="right">
              <template #default="scope">
                <el-button size="small" link type="primary" @click="viewTaskDetail(scope.row)">
                  Details
                </el-button>
                <el-button
                  size="small"
                  link
                  type="success"
                  :disabled="scope.row.taskStatus !== 'SUCCESS'"
                  @click="exportTaskToExcel(scope.row)"
                >
                  Export
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="taskPagination.page"
              v-model:page-size="taskPagination.size"
              :total="taskPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              small
            />
          </div>
        </el-card>
      </div>
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      title="TASK DETAILS"
      width="1000px"
      @close="currentTaskDetail = null"
      class="bento-dialog"
    >
      <el-descriptions v-loading="taskDetailLoading" :column="2" border class="bento-descriptions">
        <el-descriptions-item label="任务编号 (Task Code)">
             <span class="mono-text">{{ currentTaskDetail?.taskCode }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="任务状态 (Status)">
          <el-tag :type="getTaskStatusType(currentTaskDetail?.taskStatus)">
            {{ getTaskStatusText(currentTaskDetail?.taskStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="需求 (Requirement)">
            {{ currentTaskDetail?.requirementCode }} - {{ currentTaskDetail?.requirementName }}
        </el-descriptions-item>
        <el-descriptions-item label="配置 (Config)">
            {{ currentTaskDetail?.layerName }} / {{ currentTaskDetail?.methodName }} / {{ currentTaskDetail?.modelCode }}
        </el-descriptions-item>
        <el-descriptions-item label="用例统计 (Stats)">
          Total: {{ currentTaskDetail?.totalCases }} | Success: {{ currentTaskDetail?.successCases }} | Failed: {{ currentTaskDetail?.failCases }}
        </el-descriptions-item>
        <el-descriptions-item label="时间 (Time)">
            {{ currentTaskDetail?.createTime }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">CASES GENERATED</el-divider>

      <el-table
        :data="currentTaskDetail?.cases || []"
        style="width: 100%"
        max-height="400"
        class="bento-table"
      >
        <el-table-column prop="caseCode" label="ID" width="150" >
             <template #default="scope"><span class="mono-text">{{ scope.row.caseCode }}</span></template>
        </el-table-column>
        <el-table-column prop="caseName" label="Name" min-width="200" show-overflow-tooltip />
        <el-table-column prop="casePriority" label="Pri" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.casePriority" :type="getPriorityType(scope.row.casePriority)" size="small">
              {{ scope.row.casePriority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="testStep" label="Steps" min-width="200" show-overflow-tooltip />
        <el-table-column prop="expectedResult" label="Expected" min-width="150" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="taskDetailDialogVisible = false">Close</el-button>
        <el-button
          type="primary"
          :disabled="currentTaskDetail?.taskStatus !== 'SUCCESS'"
          @click="currentTaskDetail && exportTaskToExcel(currentTaskDetail)"
        >
          Export Excel
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>



<script setup lang="ts">
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { MagicStick, VideoPlay, Refresh, Odometer, List, Edit } from '@element-plus/icons-vue'
import { caseGenerationApi, type CaseGenerationRequest, type GenerationTask, type TaskListItem, type TaskDetail, type TaskListQuery } from '@/api/caseGeneration'
import { workflowApi, type WorkflowDefinition } from '@/api/workflow'
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



const form = reactive<CaseGenerationRequest & { templateId?: number; workflowId?: number }>({

  requirementId: undefined,

  layerCode: '',

  methodCode: '',

  templateId: undefined,

  modelCode: undefined,

  workflowId: undefined

})

// 工作流相关
const workflowList = ref<WorkflowDefinition[]>([])
const workflowLoading = ref(false)



const router = useRouter()

const generationResult = ref<GenerationTask | null>(null)

// 任务列表相关
const taskList = ref<TaskListItem[]>([])
const taskListLoading = ref(false)
const taskPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 任务详情对话框
const taskDetailDialogVisible = ref(false)
const taskDetailLoading = ref(false)
const currentTaskDetail = ref<TaskDetail | null>(null)


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

// 获取任务状态文本
const getTaskStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    SUCCESS: '成功',
    FAILED: '失败'
  }
  return statusMap[status || ''] || status || '-'
}

// 获取任务状态类型
const getTaskStatusType = (status?: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'info',
    PROCESSING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return typeMap[status || ''] || ''
}

// 获取优先级类型
const getPriorityType = (priority?: string) => {
  const typeMap: Record<string, string> = {
    高: 'danger',
    中: 'warning',
    低: 'info'
  }
  return typeMap[priority || ''] || ''
}

// 加载任务列表
const loadTaskList = async () => {
  taskListLoading.value = true
  try {
    const query: TaskListQuery = {
      page: taskPagination.page,
      size: taskPagination.size
    }

    const response = await caseGenerationApi.getTaskList(query)
    if (response.data) {
      taskList.value = response.data.list || []
      taskPagination.total = response.data.total
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    taskListLoading.value = false
  }
}

// 查看任务详情
const viewTaskDetail = async (task: TaskListItem) => {
  taskDetailDialogVisible.value = true
  taskDetailLoading.value = true
  try {
    const response = await caseGenerationApi.getTaskDetail(task.id)
    if (response.data) {
      currentTaskDetail.value = response.data
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
    ElMessage.error('加载任务详情失败')
  } finally {
    taskDetailLoading.value = false
  }
}

// 导出任务用例到Excel
const exportTaskToExcel = async (task: TaskListItem) => {
  try {
    const response = await caseGenerationApi.exportTaskToExcel(task.id)

    // 创建Blob对象并下载
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `用例生成任务_${task.taskCode}.xlsx`
    link.click()

    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  taskPagination.page = page
  loadTaskList()
}

const handleSizeChange = (size: number) => {
  taskPagination.size = size
  taskPagination.page = 1
  loadTaskList()
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

    // 加载工作流列表

    await loadWorkflows()

  } catch (error) {

    console.error('加载数据失败:', error)

    ElMessage.error('加载数据失败，请稍后重试')

  }

}

// 加载工作流列表

const loadWorkflows = async () => {

  workflowLoading.value = true

  try {

    const response = await workflowApi.getWorkflowsByType('CASE_GENERATION')

    if (response.data) {

      workflowList.value = response.data.filter(w => w.isActive)

    }

  } catch (error) {

    console.error('加载工作流列表失败:', error)

    ElMessage.error('加载工作流列表失败')

  } finally {

    workflowLoading.value = false

  }

}

// 打开工作流编辑器

const handleOpenWorkflowEditor = () => {

  router.push('/workflow')

}

// 使用工作流生成用例

const handleGenerateWithWorkflow = async () => {

  try {

    // 获取工作流配置

    const workflowResponse = await workflowApi.getWorkflow(form.workflowId!)

    if (!workflowResponse.data) {

      ElMessage.error('工作流不存在')

      generateLoading.value = false

      return

    }

    const workflow = workflowResponse.data

    // 准备输入数据

    const inputData: Record<string, any> = {

      requirement_id: form.requirementId,

      requirement_text: requirementList.value.find(r => r.id === form.requirementId)?.requirementText || '',

      layer_code: form.layerCode || undefined,

      method_code: form.methodCode || undefined,

      template_id: form.templateId,

      model_code: form.modelCode

    }

    // 执行工作流

    const result = await workflowApi.executeWorkflow(

      workflow.workflowConfig,

      inputData,

      workflow.id,

      workflow.workflowCode,

      workflow.version

    )

    if (result.data.status === 'success') {

      generationResult.value = {

        id: parseInt(result.data.execution_id || '0'),

        requirementId: form.requirementId!,

        status: 'SUCCESS',

        message: '用例生成成功（通过工作流）',

        result: result.data.output

      }

      ElMessage.success('用例生成成功（通过工作流）')

    } else {

      generationResult.value = {

        id: parseInt(result.data.execution_id || '0'),

        requirementId: form.requirementId!,

        status: 'FAILED',

        message: result.data.error || '用例生成失败'

      }

      ElMessage.error('用例生成失败: ' + (result.data.error || '未知错误'))

    }

  } catch (error: any) {

    console.error('使用工作流生成用例失败:', error)

    ElMessage.error('使用工作流生成用例失败: ' + (error.message || '未知错误'))

    generationResult.value = {

      id: 0,

      requirementId: form.requirementId!,

      status: 'FAILED',

      message: error.message || '使用工作流生成用例失败'

    }

  } finally {

    generateLoading.value = false

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

        // 如果选择了工作流，使用工作流执行

        if (form.workflowId) {

          await handleGenerateWithWorkflow()

          return

        }

        // 否则使用默认流程

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
@use '@/styles/variables.scss' as *;

.case-generation-container {
  height: 100%;
  overflow-y: auto;
  // Use padding to create space around the bento grid
  padding: 24px;
}

.bento-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: auto auto;
  gap: 24px;
  max-width: 1600px;
  margin: 0 auto;
  
  // Responsive: Stack on smaller screens
  @media (max-width: 1200px) {
    grid-template-columns: 1fr;
  }
}

.bento-item {
  &.config-item {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
  }
  
  &.status-item {
    grid-column: 2 / 3;
    grid-row: 1 / 2;
    
    @media (max-width: 1200px) {
      grid-column: 1 / 2;
      grid-row: 2 / 3;
    }
  }
  
  &.list-item {
    grid-column: 1 / 3;
    grid-row: 2 / 3;
    
     @media (max-width: 1200px) {
      grid-column: 1 / 2;
      grid-row: 3 / 4;
    }
  }
}

.bento-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  &.status-card {
     :deep(.el-card__body) {
        justify-content: center;
        align-items: center;
     }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-title {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .el-icon {
      font-size: 18px;
      color: $tech-white;
    }
    
    h2 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
  }
  
  .header-subtitle {
    font-family: $font-mono;
    font-size: 12px;
    color: $text-secondary;
    text-transform: uppercase;
    opacity: 0.7;
  }
}

.bento-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .form-helper {
    margin-top: 8px;
    display: flex;
    align-items: center;
    font-size: 12px;
    
    .mr-1 {
      margin-right: 4px;
    }
  }
  
  .form-actions {
    margin-top: 32px;
    display: flex;
    gap: 16px;
    
    .action-btn {
      flex: 1;
    }
  }
}

.status-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 300px;
  
  .empty-status {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0.5;
  }
  
  .active-status {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 24px;
    text-align: center;
    
    .status-indicator {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      
      .status-text {
        font-size: 24px;
        font-weight: bold;
        color: $tech-white;
      }
    }
    
    .status-details {
      width: 100%;
      
      .status-message {
        color: $text-secondary;
        margin-bottom: 24px;
      }
      
      .result-stats {
        display: flex;
        justify-content: center;
        gap: 24px;
        margin-bottom: 24px;
        
        .stat-item {
            display: flex;
            flex-direction: column;
            
            .label {
                font-family: $font-mono;
                font-size: 10px;
                text-transform: uppercase;
                color: $text-secondary;
            }
            
            .value {
                font-family: $font-mono;
                font-size: 24px;
                font-weight: bold;
            }
            
            &.success .value { color: $acid-green; }
            &.fail .value { color: $alert-red; }
        }
      }
    }
  }
}

.config-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

// Utility
.mono-text {
    font-family: $font-mono;
}

.text-success {
    color: $acid-green;
}

.mr-2 {
  margin-right: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>