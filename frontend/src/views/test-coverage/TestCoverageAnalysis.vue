<template>
  <div class="test-coverage-analysis">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">测试覆盖率分析管理</h2>
        <p class="page-subtitle">分析测试用例对需求ID、功能、场景和代码的覆盖率情况</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">
          <el-icon><Plus /></el-icon>
          新建分析
        </el-button>
      </div>
    </div>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <div v-if="loading" class="skeleton-container" style="padding: 20px;">
        <el-skeleton :rows="10" animated />
      </div>
      <el-table
        v-else
        :data="analysisList"
        
        style="width: 100%"
      >
        <el-table-column prop="analysisCode" label="分析编码" width="160">
           <template #default="scope">
            <span class="code-text">{{ scope.row.analysisCode }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="analysisName" label="分析名称" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span class="name-text" @click="handleView(scope.row)">{{ scope.row.analysisName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="coverageType" label="覆盖率类型" width="120">
          <template #default="scope">
            <el-tag effect="light" round>{{ getCoverageTypeText(scope.row.coverageType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coverageRate" label="覆盖率" width="200">
           <template #default="scope">
               <el-progress 
                 :percentage="Math.round((scope.row.coverageRate || 0) * 100)" 
                 :status="getProgressStatus(scope.row.coverageRate)"
               />
           </template>
        </el-table-column>
        <el-table-column prop="analysisTime" label="分析时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-tooltip content="查看详情" placement="top">
                <el-button circle size="small" type="primary" plain @click="handleView(scope.row)">
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
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

    <!-- 新建分析对话框-->
    <el-dialog
      v-model="dialogVisible"
      title="新建覆盖率分析"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分析名称" prop="analysisName">
          <el-input v-model="formData.analysisName" placeholder="请输入分析名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="覆盖率类型" prop="coverageType">
          <el-select v-model="formData.coverageType" placeholder="请选择覆盖率类型" style="width: 100%">
            <el-option label="需求ID覆盖率" value="REQUIREMENT" />
            <el-option label="功能覆盖率" value="FUNCTION" />
            <el-option label="场景覆盖率" value="SCENARIO" />
            <el-option label="代码覆盖率" value="CODE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联需求" prop="requirementId">
           <el-input-number v-model="formData.requirementId" placeholder="需求ID" style="width: 100%" :min="1" />
        </el-form-item>
        <el-form-item v-if="formData.coverageType === 'CODE'" label="覆盖率数据" prop="coverageData">
            <el-input 
               v-model="coverageDataInput" 
               type="textarea" 
               :rows="5" 
               placeholder="请输入代码覆盖率数据（JSON格式）"
            />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          开始分析
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框-->
    <el-dialog
      v-model="viewDialogVisible"
      title="分析详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="分析编码">{{ viewData.analysisCode }}</el-descriptions-item>
        <el-descriptions-item label="分析名称">{{ viewData.analysisName }}</el-descriptions-item>
        <el-descriptions-item label="覆盖率类型">{{ getCoverageTypeText(viewData.coverageType) }}</el-descriptions-item>
        <el-descriptions-item label="覆盖率">{{ (viewData.coverageRate ? (viewData.coverageRate * 100).toFixed(2) : 0) }}%</el-descriptions-item>
        <el-descriptions-item label="总项数">{{ viewData.totalItems }}</el-descriptions-item>
        <el-descriptions-item label="已覆盖项">{{ viewData.coveredItems }}</el-descriptions-item>
        <el-descriptions-item label="分析时间">{{ viewData.analysisTime }}</el-descriptions-item>
        <el-descriptions-item label="未覆盖项" :span="2">
             <div class="json-content">
                {{ viewData.uncoveredItems || '-' }}
             </div>
        </el-descriptions-item>
        <el-descriptions-item label="详细数据" :span="2">
             <div class="json-content">
                {{ viewData.coverageDetails || '-'}}
             </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, View } from '@element-plus/icons-vue'
import { testCoverageApi, type TestCoverageAnalysisRequestDTO, type TestCoverageAnalysisResponseDTO } from '@/api/testCoverage'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref<FormInstance>()

const analysisList = ref<TestCoverageAnalysisResponseDTO[]>([])
const formData = reactive<TestCoverageAnalysisRequestDTO>({
  analysisName: '',
  coverageType: 'REQUIREMENT',
  requirementId: undefined
})
const coverageDataInput = ref('')
const viewData = ref<TestCoverageAnalysisResponseDTO>({} as TestCoverageAnalysisResponseDTO)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单验证规则
const formRules: FormRules = {
  analysisName: [
    { required: true, message: '请输入分析名称', trigger: 'blur' }
  ],
  coverageType: [
    { required: true, message: '请选择覆盖率类型', trigger: 'change' }
  ]
}

// Helpers
const getCoverageTypeText = (type: string) => {
    const map: Record<string, string> = {
        REQUIREMENT: '需求ID覆盖率',
        FUNCTION: '功能覆盖率',
        SCENARIO: '场景覆盖率',
        CODE: '代码覆盖率'
    }
    return map[type] || type
}

const getProgressStatus = (rate: number = 0) => {
    if (rate >= 0.8) return 'success'
    if (rate >= 0.5) return 'warning'
    return 'exception'
}

// Actions
const loadAnalysisList = async () => {
  loading.value = true
  try {
    const response = await testCoverageApi.getAnalysisList(pagination.page - 1, pagination.size)
    if (response.data) {
      analysisList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load analysis list', error)
    ElMessage.error('加载分析列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadAnalysisList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadAnalysisList()
}

const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const handleView = async (row: TestCoverageAnalysisResponseDTO) => {
    try {
        const res = await testCoverageApi.getAnalysisById(row.id)
        viewData.value = res
        viewDialogVisible.value = true
    } catch(e) {
        console.error(e)
    }
}

const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitLoading.value = true
            try {
                if (formData.coverageType === 'CODE') {
                     await testCoverageApi.analyzeCodeCoverage(formData.requirementId, coverageDataInput.value)
                } else if (formData.coverageType === 'REQUIREMENT' && formData.requirementId) {
                     await testCoverageApi.analyzeRequirementCoverage(formData.requirementId)
                } else if (formData.coverageType === 'FUNCTION' && formData.requirementId) {
                     await testCoverageApi.analyzeFunctionCoverage(formData.requirementId)
                } else if (formData.coverageType === 'SCENARIO' && formData.requirementId) {
                     await testCoverageApi.analyzeScenarioCoverage(formData.requirementId)
                } else {
                     // Default generic analysis
                     await testCoverageApi.analyzeCoverage(formData)
                }
                
                ElMessage.success('分析已启动')
                dialogVisible.value = false
                loadAnalysisList()
            } catch (e) {
                console.error(e)
            } finally {
                submitLoading.value = false
            }
        }
    })
}

const handleDialogClose = () => {
    resetForm()
}

const resetForm = () => {
    formData.analysisName = ''
    formData.coverageType = 'REQUIREMENT'
    formData.requirementId = undefined
    coverageDataInput.value = ''
}

onMounted(() => {
    loadAnalysisList()
})

</script>

<style scoped lang="scss">
.test-coverage-analysis {
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

  .table-card {
    border: none;
    .code-text {
      font-family: monospace;
      color: var(--el-text-color-regular);
      background: rgba(0, 212, 255, 0.1);
      padding: 2px 6px;
      border-radius: 4px;
    }
    .name-text {
        font-weight: 500;
        color: var(--el-color-primary);
        cursor: pointer;
        &:hover { text-decoration: underline; }
    }
    .pagination {
      margin-top: 24px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .json-content {
      background: rgba(255, 255, 255, 0.05);
      padding: 10px;
      border-radius: 4px;
      max-height: 400px;
      overflow: auto;
      white-space: pre-wrap;
      font-family: monospace;
      font-size: 12px;
  }
}
</style>