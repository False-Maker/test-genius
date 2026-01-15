<template>
  <div class="requirement-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">需求管理</h2>
        <p class="page-subtitle">管理和维护所有的测试需求文档</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">
          <el-icon><Plus /></el-icon>
          新建需求
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="需求名称">
          <el-input
            v-model="searchForm.requirementName"
            placeholder="搜索需求名称..."
            clearable
            prefix-icon="Search"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="需求状态">
          <el-select
            v-model="searchForm.requirementStatus"
            placeholder="全部状态"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="审核中" value="REVIEWING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 需求列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="requirementList"
        :header-cell-style="{ background: '#f9fafb' }"
        style="width: 100%"
      >
        <el-table-column prop="requirementCode" label="需求编码" width="160">
           <template #default="scope">
            <span class="code-text">{{ scope.row.requirementCode }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="requirementName" label="需求名称" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span class="name-text">{{ scope.row.requirementName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requirementType" label="需求类型" width="120">
          <template #default="scope">
            <el-tag effect="light" round>{{ scope.row.requirementType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessModule" label="业务模块" width="120">
          <template #default="scope">
            {{ scope.row.businessModule || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="requirementStatus" label="状态" width="100">
          <template #default="scope">
            <div class="status-indicator">
              <span :class="['dot', getStatusType(scope.row.requirementStatus)]"></span>
              <span>{{ getStatusText(scope.row.requirementStatus) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-tooltip content="查看详情" placement="top">
                <el-button circle size="small" type="info" plain @click="handleView(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑需求" placement="top">
                <el-button circle size="small" type="primary" plain @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="智能分析" placement="top">
                <el-button circle size="small" type="success" plain @click="handleAnalyze(scope.row)">
                  <el-icon><MagicStick /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-dropdown trigger="click" @command="(cmd) => handleStatusChange(scope.row, cmd)">
                <el-button circle size="small" type="warning" plain class="more-btn">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="status in getAvailableStatuses(scope.row.requirementStatus)"
                      :key="status.value"
                      :command="status.value"
                    >
                      {{ status.label }}
                    </el-dropdown-item>
                    <el-dropdown-item divided 
                      :disabled="!canDelete(scope.row.requirementStatus)"
                      @click="handleDelete(scope.row)"
                      style="color: var(--el-color-danger)"
                    >
                      删除需求
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
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
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- Keeping Dialogs unchanged but they will inherit global styles -->
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
        <el-form-item label="需求名称" prop="requirementName">
          <el-input
            v-model="formData.requirementName"
            placeholder="请输入需求名称"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="需求类型" prop="requirementType">
          <el-select v-model="formData.requirementType" placeholder="请选择需求类型" style="width: 100%">
            <el-option label="新功能" value="新功能" />
            <el-option label="优化" value="优化" />
            <el-option label="缺陷修复" value="缺陷修复" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务模块" prop="businessModule">
          <el-input
            v-model="formData.businessModule"
            placeholder="请输入业务模块，如：投保/核保/理赔/保全"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="需求描述" prop="requirementDescription">
          <el-input
            v-model="formData.requirementDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入需求描述"
          />
        </el-form-item>
        <el-form-item label="需求文档" prop="requirementDocUrl">
          <FileUpload
            v-model="formData.requirementDocUrl"
            :limit="1"
            :disabled="false"
            :auto-upload="true"
            tip-text="支持上传Word文档（.doc, .docx）和PDF文档（.pdf），单个文件不超过100MB"
            @success="handleFileUploadSuccess"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="需求状态" prop="requirementStatus">
          <el-select v-model="formData.requirementStatus" placeholder="请选择状态" style="width: 100%">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="审核中" value="REVIEWING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
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
      title="需求详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="需求编码">
          {{ viewData.requirementCode }}
        </el-descriptions-item>
        <el-descriptions-item label="需求名称">
          {{ viewData.requirementName }}
        </el-descriptions-item>
        <el-descriptions-item label="需求类型">
          {{ viewData.requirementType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="业务模块">
          {{ viewData.businessModule || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="需求状态">
          <el-tag :type="getStatusType(viewData.requirementStatus)">
            {{ getStatusText(viewData.requirementStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本号">
          {{ viewData.version }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">
          {{ viewData.creatorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ viewData.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ viewData.updateTime }}
        </el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.requirementDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="需求文档" :span="2">
          <el-link v-if="viewData.requirementDocUrl" :href="viewData.requirementDocUrl" target="_blank" type="primary">
            {{ viewData.requirementDocUrl }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 需求分析对话框 -->
    <el-dialog
      v-model="analysisDialogVisible"
      title="需求分析"
      width="900px"
    >
      <div v-if="analysisLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" style="font-size: 32px; color: var(--el-color-primary)"><Loading /></el-icon>
        <p style="margin-top: 10px; color: #909399">正在智能分析需求...</p>
      </div>
      <div v-else-if="analysisResult">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求名称">
            {{ analysisResult.requirementName }}
          </el-descriptions-item>
          <el-descriptions-item label="分析时间">
            {{ analysisResult.analysisTime }}
          </el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">
            <el-tag
              v-for="(keyword, index) in analysisResult.keywords"
              :key="index"
              effect="plain"
              style="margin-right: 8px; margin-bottom: 8px"
            >
              {{ keyword }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="内容长度">
            {{ analysisResult.contentLength }} 字符
          </el-descriptions-item>
          <el-descriptions-item label="句子数量">
            {{ analysisResult.sentenceCount }} 句
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>测试要点</el-divider>
        <div v-if="analysisResult.testPoints && analysisResult.testPoints.length > 0">
          <el-table :data="analysisResult.testPoints" border stripe>
            <el-table-column prop="point" label="测试要点" min-width="200" />
            <el-table-column prop="description" label="描述" min-width="300" />
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.priority" :type="getPriorityType(scope.row.priority)" effect="dark">
                  {{ scope.row.priority }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无测试要点" />

        <el-divider>业务规则</el-divider>
        <div v-if="analysisResult.businessRules && analysisResult.businessRules.length > 0">
          <el-table :data="analysisResult.businessRules" border stripe>
            <el-table-column prop="rule" label="业务规则" min-width="200" />
            <el-table-column prop="description" label="描述" min-width="300" />
            <el-table-column prop="type" label="类型" width="120">
               <template #default="scope">
                <el-tag type="info" effect="plain">{{ scope.row.type }}</el-tag>
               </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无业务规则" />
      </div>
      <template #footer>
        <el-button @click="analysisDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, ArrowDown, Loading, View, Edit, MagicStick, More, Search } from '@element-plus/icons-vue'
import { requirementApi, type TestRequirement } from '@/api/requirement'
import type { PageResult } from '@/api/types'
import FileUpload from '@/components/FileUpload.vue'
import type { FileUploadResponse } from '@/api/fileUpload'
import { requirementAnalysisApi, type RequirementAnalysisResult } from '@/api/requirementAnalysis'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const analysisDialogVisible = ref(false)
const analysisLoading = ref(false)
const analysisResult = ref<RequirementAnalysisResult | null>(null)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const requirementList = ref<TestRequirement[]>([])
const formData = reactive<TestRequirement>({
  requirementName: '',
  requirementType: '',
  businessModule: '',
  requirementDescription: '',
  requirementDocUrl: '',
  requirementStatus: 'DRAFT'
})
const viewData = ref<TestRequirement>({})

const searchForm = reactive({
  requirementName: '',
  requirementStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单验证规则
const formRules: FormRules = {
  requirementName: [
    { required: true, message: '请输入需求名称', trigger: 'blur' },
    { max: 500, message: '需求名称长度不能超过500个字符', trigger: 'blur' }
  ],
  requirementType: [
    { required: true, message: '请选择需求类型', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => (isEdit.value ? '编辑需求' : '新建需求'))

// 获取状态文本
const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    REVIEWING: '审核中',
    APPROVED: '已通过',
    CLOSED: '已关闭'
  }
  return statusMap[status || ''] || status || '-'
}

// 获取状态类型 (Used for dot color)
const getStatusType = (status?: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    REVIEWING: 'warning',
    APPROVED: 'success',
    CLOSED: 'danger'
  }
  return typeMap[status || ''] || ''
}

// 获取可用的状态流转选项
const getAvailableStatuses = (currentStatus?: string) => {
  const statusMap: Record<string, Array<{ label: string; value: string }>> = {
    DRAFT: [
      { label: '提交审核', value: 'REVIEWING' },
      { label: '关闭', value: 'CLOSED' }
    ],
    REVIEWING: [
      { label: '通过', value: 'APPROVED' },
      { label: '关闭', value: 'CLOSED' },
      { label: '退回草稿', value: 'DRAFT' }
    ],
    APPROVED: [
      { label: '关闭', value: 'CLOSED' }
    ],
    CLOSED: []
  }
  return statusMap[currentStatus || ''] || []
}

// 判断是否可以删除
const canDelete = (status?: string) => {
  return status !== 'APPROVED' && status !== 'CLOSED'
}

// 加载需求列表
const loadRequirementList = async () => {
  loading.value = true
  try {
    const response = await requirementApi.getRequirementList({
      page: pagination.page - 1,
      size: pagination.size,
      requirementName: searchForm.requirementName || undefined,
      requirementStatus: searchForm.requirementStatus || undefined
    })
    
    if (response.data) {
      requirementList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载需求列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRequirementList()
}

// 重置搜索
const handleReset = () => {
  searchForm.requirementName = ''
  searchForm.requirementStatus = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadRequirementList()
}

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page
  loadRequirementList()
}

// 新建需求
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑需求
const handleEdit = async (row: TestRequirement) => {
  isEdit.value = true
  try {
    const response = await requirementApi.getRequirementById(row.id!)
    if (response.data) {
      Object.assign(formData, response.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取需求详情失败:', error)
  }
}

// 查看详情
const handleView = async (row: TestRequirement) => {
  try {
    const response = await requirementApi.getRequirementById(row.id!)
    if (response.data) {
      viewData.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取需求详情失败:', error)
  }
}

// 删除需求
const handleDelete = async (row: TestRequirement) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除需求"${row.requirementName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await requirementApi.deleteRequirement(row.id!)
    ElMessage.success('删除成功')
    loadRequirementList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除需求失败:', error)
    }
  }
}

// 状态流转
const handleStatusChange = async (row: TestRequirement, status: string) => {
  try {
    await requirementApi.updateRequirementStatus(row.id!, status)
    ElMessage.success('状态更新成功')
    loadRequirementList()
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
          await requirementApi.updateRequirement(formData.id!, formData)
          ElMessage.success('更新成功')
        } else {
          await requirementApi.createRequirement(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadRequirementList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 文件上传成功
const handleFileUploadSuccess = (response: FileUploadResponse) => {
  formData.requirementDocUrl = response.fileUrl
  ElMessage.success('文件上传成功')
}

// 需求分析
const handleAnalyze = async (row: TestRequirement) => {
  analysisDialogVisible.value = true
  analysisLoading.value = true
  analysisResult.value = null
  
  try {
    const response = await requirementAnalysisApi.analyzeRequirement(row.id!)
    if (response.data) {
      analysisResult.value = response.data
    }
  } catch (error) {
    console.error('需求分析失败:', error)
    ElMessage.error('需求分析失败')
  } finally {
    analysisLoading.value = false
  }
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return typeMap[priority] || 'info'
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    requirementCode: undefined,
    requirementName: '',
    requirementType: '',
    businessModule: '',
    requirementDescription: '',
    requirementDocUrl: '',
    requirementStatus: 'DRAFT'
  })
  formRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 初始化
onMounted(() => {
  loadRequirementList()
})
</script>

<style scoped lang="scss">
.requirement-list {
  // Container padding is now handled by App.vue (if verified), but App.view set overflow on el-main.
  // Actually, App.vue el-main has padding: 24px. So we don't need padding here.
  // But we want a little gap between elements.
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      .page-title {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
      
      .page-subtitle {
        margin: 4px 0 0;
        color: var(--el-text-color-secondary);
        font-size: 14px;
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
    border: none;
    
    :deep(.el-card__body) {
      padding: 20px;
    }

    .search-form {
      .el-form-item {
        margin-bottom: 0;
        margin-right: 24px;
        
        &:last-child {
          margin-right: 0;
        }
      }
    }
  }

  .table-card {
    border: none;
    position: relative;
    
    .code-text {
      font-family: monospace;
      color: var(--el-text-color-regular);
      background: #f3f4f6;
      padding: 2px 6px;
      border-radius: 4px;
    }
    
    .name-text {
      font-weight: 500;
      color: var(--el-color-primary);
      cursor: pointer;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    .status-indicator {
      display: flex;
      align-items: center;
      gap: 6px;
      
      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        
        &.info { background-color: var(--el-color-info); }
        &.warning { background-color: var(--el-color-warning); }
        &.success { background-color: var(--el-color-success); }
        &.danger { background-color: var(--el-color-danger); }
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
      
      .more-btn {
        margin-left: 0;
      }
    }
    
    .pagination {
      margin-top: 24px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>

