<template>
  <div class="agent-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Agent 管理</span>
          <el-button type="primary" @click="handleCreate">创建 Agent</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="Agent名称">
          <el-input v-model="searchForm.agentName" placeholder="请输入Agent名称" clearable />
        </el-form-item>
        <el-form-item label="Agent类型">
          <el-select v-model="searchForm.agentType" placeholder="请选择类型" clearable>
            <el-option label="测试设计助手" value="TEST_DESIGN_ASSISTANT" />
            <el-option label="用例优化" value="CASE_OPTIMIZATION" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isActive" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Agent列表 -->
      <el-table :data="agentList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="agentCode" label="编码" width="180" />
        <el-table-column prop="agentName" label="名称" width="200" />
        <el-table-column prop="agentType" label="类型" width="180">
          <template #default="{ row }">
            <el-tag v-if="row.agentType === 'TEST_DESIGN_ASSISTANT'" type="primary">测试设计助手</el-tag>
            <el-tag v-else-if="row.agentType === 'CASE_OPTIMIZATION'" type="success">用例优化</el-tag>
            <el-tag v-else type="info">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="agentDescription" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              active-value="1"
              inactive-value="0"
              @change="handleToggleActive(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleChat(row)">对话</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="Agent编码" prop="agentCode" v-if="!isEdit">
          <el-input v-model="form.agentCode" placeholder="请输入Agent编码（自动生成）" disabled />
        </el-form-item>
        <el-form-item label="Agent名称" prop="agentName">
          <el-input v-model="form.agentName" placeholder="请输入Agent名称" />
        </el-form-item>
        <el-form-item label="Agent类型" prop="agentType">
          <el-select v-model="form.agentType" placeholder="请选择Agent类型" style="width: 100%">
            <el-option label="测试设计助手" value="TEST_DESIGN_ASSISTANT" />
            <el-option label="用例优化" value="CASE_OPTIMIZATION" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="agentDescription">
          <el-input
            v-model="form.agentDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入Agent描述"
          />
        </el-form-item>
        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="5"
            placeholder="请输入系统提示词"
          />
        </el-form-item>
        <el-form-item label="最大迭代次数" prop="maxIterations">
          <el-input-number v-model="form.maxIterations" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="最大Token数" prop="maxTokens">
          <el-input-number v-model="form.maxTokens" :min="100" :max="10000" :step="100" />
        </el-form-item>
        <el-form-item label="温度参数" prop="temperature">
          <el-slider v-model="form.temperature" :min="0" :max="1" :step="0.1" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { agentApi, type Agent } from '@/api/agent'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  agentName: '',
  agentType: '',
  isActive: ''
})

// 列表数据
const agentList = ref<Agent[]>([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('创建 Agent')
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<Agent>({
  agentCode: '',
  agentName: '',
  agentType: 'TEST_DESIGN_ASSISTANT',
  agentDescription: '',
  systemPrompt: '',
  maxIterations: 10,
  maxTokens: 4000,
  temperature: 0.7,
  isActive: '1'
})

// 表单验证规则
const rules: FormRules = {
  agentName: [
    { required: true, message: '请输入Agent名称', trigger: 'blur' }
  ],
  agentType: [
    { required: true, message: '请选择Agent类型', trigger: 'change' }
  ],
  systemPrompt: [
    { required: true, message: '请输入系统提示词', trigger: 'blur' }
  ]
}

// 获取Agent列表
const loadAgentList = async () => {
  loading.value = true
  try {
    const { data } = await agentApi.getAllActiveAgents()
    agentList.value = data || []
  } catch (error) {
    ElMessage.error('获取Agent列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  loadAgentList()
}

// 重置
const handleReset = () => {
  searchForm.agentName = ''
  searchForm.agentType = ''
  searchForm.isActive = ''
  loadAgentList()
}

// 创建
const handleCreate = () => {
  dialogTitle.value = '创建 Agent'
  isEdit.value = false
  form.agentCode = ''
  form.agentName = ''
  form.agentType = 'TEST_DESIGN_ASSISTANT'
  form.agentDescription = ''
  form.systemPrompt = ''
  form.maxIterations = 10
  form.maxTokens = 4000
  form.temperature = 0.7
  form.isActive = '1'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Agent) => {
  dialogTitle.value = '编辑 Agent'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 对话
const handleChat = (row: Agent) => {
  router.push({
    name: 'AgentChat',
    params: { agentId: row.id }
  })
}

// 删除
const handleDelete = (row: Agent) => {
  ElMessageBox.confirm(`确定要删除Agent "${row.agentName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await agentApi.deleteAgent(row.id!)
      ElMessage.success('删除成功')
      loadAgentList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 切换状态
const handleToggleActive = async (row: Agent) => {
  try {
    await agentApi.toggleAgentActive(row.id!, row.isActive === '1')
    ElMessage.success(row.isActive === '1' ? '启用成功' : '禁用成功')
  } catch (error) {
    ElMessage.error('操作失败')
    row.isActive = row.isActive === '1' ? '0' : '1'
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await agentApi.updateAgent(form.id!, form)
        ElMessage.success('更新成功')
      } else {
        await agentApi.createAgent(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadAgentList()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 初始化
onMounted(() => {
  loadAgentList()
})
</script>

<style scoped lang="scss">
.agent-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>

