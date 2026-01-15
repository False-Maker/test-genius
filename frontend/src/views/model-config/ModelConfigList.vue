<template>
  <div class="model-config-list">
    <div class="header">
      <h2>模型配置管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建模型配置
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模型名称">
          <el-input
            v-model="searchForm.modelName"
            placeholder="请输入模型名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="模型类型">
          <el-select
            v-model="searchForm.modelType"
            placeholder="请选择类型"
            clearable
            @change="handleSearch"
          >
            <el-option label="DeepSeek" value="DeepSeek" />
            <el-option label="豆包" value="豆包" />
            <el-option label="Kimi" value="Kimi" />
            <el-option label="千问" value="千问" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-select
            v-model="searchForm.isActive"
            placeholder="请选择状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 模型配置列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="modelConfigList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="modelCode" label="模型编码" width="180" />
        <el-table-column prop="modelName" label="模型名称" min-width="150" />
        <el-table-column prop="modelType" label="模型类型" width="120" />
        <el-table-column prop="modelVersion" label="版本" width="100" />
        <el-table-column prop="priority" label="优先级" width="100" sortable>
          <template #default="scope">
            {{ scope.row.priority || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isActive === '1' ? 'success' : 'danger'">
              {{ scope.row.isActive === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dailyLimit" label="每日限制" width="120">
          <template #default="scope">
            {{ scope.row.dailyLimit || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              size="small"
              link
              :type="scope.row.isActive === '1' ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.isActive === '1' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="模型名称" prop="modelName">
          <el-input
            v-model="formData.modelName"
            placeholder="请输入模型名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="formData.modelType" placeholder="请选择模型类型">
            <el-option label="DeepSeek" value="DeepSeek" />
            <el-option label="豆包" value="豆包" />
            <el-option label="Kimi" value="Kimi" />
            <el-option label="千问" value="千问" />
          </el-select>
        </el-form-item>
        <el-form-item label="API端点" prop="apiEndpoint">
          <el-input
            v-model="formData.apiEndpoint"
            placeholder="请输入API端点地址"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            placeholder="请输入API密钥"
            maxlength="500"
            show-password
          />
        </el-form-item>
        <el-form-item label="模型版本" prop="modelVersion">
          <el-input
            v-model="formData.modelVersion"
            placeholder="请输入模型版本"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="最大Token数" prop="maxTokens">
          <el-input-number
            v-model="formData.maxTokens"
            :min="1"
            :max="100000"
            placeholder="请输入最大Token数"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="温度参数" prop="temperature">
          <el-input-number
            v-model="formData.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            :precision="2"
            placeholder="请输入温度参数（0-2）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number
            v-model="formData.priority"
            :min="1"
            :max="100"
            placeholder="请输入优先级（数字越小优先级越高）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日限制" prop="dailyLimit">
          <el-input-number
            v-model="formData.dailyLimit"
            :min="0"
            placeholder="请输入每日调用限制（0表示无限制）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="isActive">
          <el-radio-group v-model="formData.isActive">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="模型配置详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模型编码">{{ viewData.modelCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模型名称">{{ viewData.modelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模型类型">{{ viewData.modelType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模型版本">{{ viewData.modelVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="API端点" :span="2">
          {{ viewData.apiEndpoint || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="最大Token数">{{ viewData.maxTokens || '-' }}</el-descriptions-item>
        <el-descriptions-item label="温度参数">{{ viewData.temperature || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ viewData.priority || '-' }}</el-descriptions-item>
        <el-descriptions-item label="每日限制">{{ viewData.dailyLimit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="启用状态">
          <el-tag :type="viewData.isActive === '1' ? 'success' : 'danger'">
            {{ viewData.isActive === '1' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { modelConfigApi, type ModelConfig } from '@/api/modelConfig'
import type { PageResult } from '@/api/types'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const modelConfigList = ref<ModelConfig[]>([])
const formData = reactive<ModelConfig>({
  modelName: '',
  modelType: '',
  apiEndpoint: '',
  apiKey: '',
  modelVersion: '',
  maxTokens: undefined,
  temperature: undefined,
  priority: undefined,
  dailyLimit: undefined,
  isActive: '1'
})
const viewData = ref<ModelConfig>({})

const searchForm = reactive({
  modelName: '',
  modelType: '',
  isActive: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单验证规则
const formRules: FormRules = {
  modelName: [
    { required: true, message: '请输入模型名称', trigger: 'blur' },
    { max: 200, message: '模型名称长度不能超过200个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => (isEdit.value ? '编辑模型配置' : '新建模型配置'))

// 加载模型配置列表
const loadModelConfigList = async () => {
  loading.value = true
  try {
    const response = await modelConfigApi.getModelConfigList({
      page: pagination.page - 1,
      size: pagination.size,
      modelName: searchForm.modelName || undefined,
      modelType: searchForm.modelType || undefined,
      isActive: searchForm.isActive || undefined
    })
    
    if (response.data) {
      modelConfigList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载模型配置列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadModelConfigList()
}

// 重置搜索
const handleReset = () => {
  searchForm.modelName = ''
  searchForm.modelType = ''
  searchForm.isActive = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadModelConfigList()
}

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page
  loadModelConfigList()
}

// 新建模型配置
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑模型配置
const handleEdit = async (row: ModelConfig) => {
  isEdit.value = true
  try {
    const response = await modelConfigApi.getModelConfigById(row.id!)
    if (response.data) {
      Object.assign(formData, response.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取模型配置详情失败:', error)
  }
}

// 查看模型配置
const handleView = async (row: ModelConfig) => {
  try {
    const response = await modelConfigApi.getModelConfigById(row.id!)
    if (response.data) {
      viewData.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取模型配置详情失败:', error)
  }
}

// 删除模型配置
const handleDelete = async (row: ModelConfig) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模型配置"${row.modelName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await modelConfigApi.deleteModelConfig(row.id!)
    ElMessage.success('删除成功')
    loadModelConfigList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除模型配置失败:', error)
    }
  }
}

// 启用/禁用模型配置
const handleToggleStatus = async (row: ModelConfig) => {
  try {
    const newStatus = row.isActive === '1' ? '0' : '1'
    await modelConfigApi.toggleModelConfigStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadModelConfigList()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await modelConfigApi.updateModelConfig(formData.id!, formData)
          ElMessage.success('更新成功')
        } else {
          await modelConfigApi.createModelConfig(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadModelConfigList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    modelCode: undefined,
    modelName: '',
    modelType: '',
    apiEndpoint: '',
    apiKey: '',
    modelVersion: '',
    maxTokens: undefined,
    temperature: undefined,
    priority: undefined,
    dailyLimit: undefined,
    isActive: '1'
  })
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 初始化
onMounted(() => {
  loadModelConfigList()
})
</script>

<style scoped>
.model-config-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

