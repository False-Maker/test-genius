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



        <el-form-item label="工作流">

          <el-select

            v-model="form.workflowId"

            placeholder="请选择工作流（可选，使用工作流将替代默认流程）"

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

          <div style="margin-top: 5px; font-size: 12px; color: #909399">

            <el-link type="primary" :underline="false" @click="handleOpenWorkflowEditor">

              创建工作流

            </el-link>

            <span style="margin: 0 8px">|</span>

            <el-link type="primary" :underline="false" @click="loadWorkflows">

              刷新列表

            </el-link>

          </div>

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


    <!-- 用例生成任务列表 -->
    <el-card class="task-list-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>用例生成任务列表</span>
          <el-button link type="primary" @click="handleRefreshTaskList">
            刷新列表
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="taskListLoading"
        :data="taskList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="taskCode" label="任务编号" width="180" />
        <el-table-column prop="requirementName" label="需求名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="生成信息" min-width="200">
          <template #default="scope">
            {{ scope.row.layerName }} | {{ scope.row.methodName }} | {{ scope.row.modelCode }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getTaskStatusType(scope.row.taskStatus)">
              {{ getTaskStatusText(scope.row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用例数" width="120">
          <template #default="scope">
            {{ scope.row.successCases }}/{{ scope.row.totalCases }}
            <span v-if="scope.row.failCases > 0" style="color: #F56C6C; margin-left: 5px;">
              (失败{{ scope.row.failCases }})
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="viewTaskDetail(scope.row)">
              查看详情
            </el-button>
            <el-button
              size="small"
              link
              type="success"
              :disabled="scope.row.taskStatus !== 'SUCCESS'"
              @click="exportTaskToExcel(scope.row)"
            >
              导出Excel
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
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>


    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      title="任务详情"
      width="1000px"
      @close="currentTaskDetail = null"
    >
      <el-descriptions v-loading="taskDetailLoading" :column="2" border>
        <el-descriptions-item label="任务编号">{{ currentTaskDetail?.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getTaskStatusType(currentTaskDetail?.taskStatus)">
            {{ getTaskStatusText(currentTaskDetail?.taskStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="需求编号">{{ currentTaskDetail?.requirementCode }}</el-descriptions-item>
        <el-descriptions-item label="需求名称">{{ currentTaskDetail?.requirementName }}</el-descriptions-item>
        <el-descriptions-item label="测试分层">{{ currentTaskDetail?.layerName }}</el-descriptions-item>
        <el-descriptions-item label="测试方法">{{ currentTaskDetail?.methodName }}</el-descriptions-item>
        <el-descriptions-item label="模型配置">{{ currentTaskDetail?.modelCode }}</el-descriptions-item>
        <el-descriptions-item label="用例统计">
          {{ currentTaskDetail?.totalCases }}个
          (成功: {{ currentTaskDetail?.successCases }}, 失败: {{ currentTaskDetail?.failCases }})
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTaskDetail?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentTaskDetail?.completeTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">生成的用例列表</el-divider>

      <el-table
        :data="currentTaskDetail?.cases || []"
        stripe
        style="width: 100%"
        max-height="400"
      >
        <el-table-column prop="caseCode" label="用例编码" width="150" />
        <el-table-column prop="caseName" label="用例名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="caseType" label="用例类型" width="100" />
        <el-table-column prop="casePriority" label="优先级" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.casePriority" :type="getPriorityType(scope.row.casePriority)">
              {{ scope.row.casePriority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="preCondition" label="前置条件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="testStep" label="测试步骤" min-width="200" show-overflow-tooltip />
        <el-table-column prop="expectedResult" label="预期结果" min-width="150" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :disabled="currentTaskDetail?.taskStatus !== 'SUCCESS'"
          @click="currentTaskDetail && exportTaskToExcel(currentTaskDetail)"
        >
          导出Excel
        </el-button>
      </template>
    </el-dialog>

  </div>

</template>



<script setup lang="ts">

import { ref, reactive, onMounted, computed } from 'vue'

import { useRouter } from 'vue-router'

import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

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