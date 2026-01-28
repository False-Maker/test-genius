<template>
  <el-dialog
    v-model="visible"
    title="选择工作流"
    width="800px"
    @close="handleClose"
  >
    <div class="workflow-selection">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索工作流名称或编码"
          prefix-icon="Search"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 工作流列表 -->
      <el-table
        v-loading="loading"
        :data="filteredWorkflows"
        stripe
        highlight-current-row
        @current-change="handleCurrentChange"
        height="400px"
      >
        <el-table-column prop="workflowCode" label="编码" width="180" />
        <el-table-column prop="workflowName" label="名称" min-width="200" />
        <el-table-column prop="workflowType" label="类型" width="120" />
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedWorkflow" @click="handleConfirm">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { workflowApi, type WorkflowDefinition } from '@/api/workflow'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'select', workflow: WorkflowDefinition): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const workflows = ref<WorkflowDefinition[]>([])
const searchKeyword = ref('')
const selectedWorkflow = ref<WorkflowDefinition | null>(null)

// 过滤后的工作流列表
const filteredWorkflows = computed(() => {
  if (!searchKeyword.value) return workflows.value
  const keyword = searchKeyword.value.toLowerCase()
  return workflows.value.filter(
    (w) =>
      w.workflowName.toLowerCase().includes(keyword) ||
      w.workflowCode.toLowerCase().includes(keyword)
  )
})

// 加载工作流列表
const loadWorkflows = async () => {
  loading.value = true
  try {
    const { data } = await workflowApi.getAllWorkflows()
    workflows.value = data || []
  } catch (error) {
    console.error('加载工作流列表失败:', error)
    ElMessage.error('加载工作流列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 实际上通过computed属性已经过滤了，这里只是为了触发视图更新或预留后端搜索
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
}

// 选择行
const handleCurrentChange = (val: WorkflowDefinition | null) => {
  selectedWorkflow.value = val
}

// 确认选择
const handleConfirm = () => {
  if (selectedWorkflow.value) {
    emit('select', selectedWorkflow.value)
    visible.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  selectedWorkflow.value = null
  searchKeyword.value = ''
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  return time ? new Date(time).toLocaleString() : '-'
}

// 监听对话框显示
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      loadWorkflows()
    }
  }
)
</script>

<style scoped>
.workflow-selection {
  padding: 10px 0;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
