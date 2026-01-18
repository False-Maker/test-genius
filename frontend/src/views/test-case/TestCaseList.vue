<template>
  <div class="test-case-list">
    <div class="header">
      <h2>用例管理</h2>
      <div class="header-actions">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出用例
        </el-button>
        <el-button @click="handleExportTemplate">
          <el-icon><Document /></el-icon>
          下载模板
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入用例
        </el-button>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建用例
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用例名称">
          <el-input
            v-model="searchForm.caseName"
            placeholder="请输入用例名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用例状态">
          <el-select
            v-model="searchForm.caseStatus"
            placeholder="请选择状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已审核" value="REVIEWED" />
            <el-option label="已废弃" value="OBSOLETE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用例列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="testCaseList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="caseCode" label="用例编码" width="180" />
        <el-table-column prop="caseName" label="用例名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="caseType" label="用例类型" width="100">
          <template #default="scope">
            {{ scope.row.caseType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="casePriority" label="优先级" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.casePriority" :type="getPriorityType(scope.row.casePriority)">
              {{ scope.row.casePriority }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="caseStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.caseStatus)">
              {{ getStatusText(scope.row.caseStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="creatorName" label="创建人" width="120">
          <template #default="scope">
            {{ scope.row.creatorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" link type="success" @click="handleQualityAssess(scope.row)">
              质量评估
            </el-button>
            <el-dropdown v-if="getAvailableStatuses(scope.row.caseStatus).length > 0" @command="(cmd) => handleStatusChange(scope.row, cmd)">
              <el-button size="small" link type="primary">
                状态流转 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="status in getAvailableStatuses(scope.row.caseStatus)"
                    :key="status.value"
                    :command="status.value"
                  >
                    {{ status.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              v-if="scope.row.caseStatus === 'PENDING_REVIEW'"
              size="small"
              link
              type="success"
              @click="handleReview(scope.row)"
            >
              审核
            </el-button>
            <el-button
              size="small"
              link
              type="danger"
              :disabled="!canDelete(scope.row.caseStatus)"
              @click="handleDelete(scope.row)"
            >
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
      width="900px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用例名称" prop="caseName">
              <el-input
                v-model="formData.caseName"
                placeholder="请输入用例名称"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用例类型" prop="caseType">
              <el-select v-model="formData.caseType" placeholder="请选择用例类型">
                <el-option label="正常" value="正常" />
                <el-option label="异常" value="异常" />
                <el-option label="边界" value="边界" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用例优先级" prop="casePriority">
              <el-select v-model="formData.casePriority" placeholder="请选择优先级">
                <el-option label="高" value="高" />
                <el-option label="中" value="中" />
                <el-option label="低" value="低" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求ID" prop="requirementId">
              <el-input-number
                v-model="formData.requirementId"
                :min="1"
                placeholder="请输入需求ID"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="前置条件" prop="preCondition">
          <el-input
            v-model="formData.preCondition"
            type="textarea"
            :rows="3"
            placeholder="请输入前置条件"
          />
        </el-form-item>
        <el-form-item label="测试步骤" prop="testStep">
          <el-input
            v-model="formData.testStep"
            type="textarea"
            :rows="4"
            placeholder="请输入测试步骤"
          />
        </el-form-item>
        <el-form-item label="预期结果" prop="expectedResult">
          <el-input
            v-model="formData.expectedResult"
            type="textarea"
            :rows="4"
            placeholder="请输入预期结果"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="用例状态" prop="caseStatus">
          <el-select v-model="formData.caseStatus" placeholder="请选择状态">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已审核" value="REVIEWED" />
            <el-option label="已废弃" value="OBSOLETE" />
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
      title="用例详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用例编码">
          {{ viewData.caseCode }}
        </el-descriptions-item>
        <el-descriptions-item label="用例名称">
          {{ viewData.caseName }}
        </el-descriptions-item>
        <el-descriptions-item label="用例类型">
          {{ viewData.caseType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="用例优先级">
          <el-tag v-if="viewData.casePriority" :type="getPriorityType(viewData.casePriority)">
            {{ viewData.casePriority }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="用例状态">
          <el-tag :type="getStatusType(viewData.caseStatus)">
            {{ getStatusText(viewData.caseStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本号">
          {{ viewData.version }}
        </el-descriptions-item>
        <el-descriptions-item label="需求ID">
          {{ viewData.requirementId || '-' }}
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
        <el-descriptions-item label="前置条件" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.preCondition || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="测试步骤" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.testStep || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="预期结果" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.expectedResult || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="用例审核"
      width="600px"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewFormRules"
        label-width="120px"
      >
        <el-form-item label="审核结果" prop="reviewResult">
          <el-radio-group v-model="reviewForm.reviewResult">
            <el-radio label="PASS">通过</el-radio>
            <el-radio label="REJECT">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="handleReviewSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入用例对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入用例"
      width="600px"
    >
      <el-upload
        ref="importUploadRef"
        :http-request="handleImportUpload"
        :before-upload="beforeImportUpload"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :file-list="importFileList"
        :limit="1"
        accept=".xlsx,.xls"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将Excel文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持上传Excel文件（.xlsx, .xls），请先下载模板文件
          </div>
        </template>
      </el-upload>

      <!-- 导入结果 -->
      <div v-if="importResult" class="import-result" style="margin-top: 20px">
        <el-alert
          :type="importResult.failCount > 0 ? warning : success"
          :closable="false"
          show-icon
        >
          <template #title>
            <div>
              <p>导入完成！成功：{{ importResult.successCount }} 个，失败：{{ importResult.failCount }} 个</p>
              <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top: 10px">
                <p><strong>错误详情：</strong></p>
                <ul style="margin: 5px 0; padding-left: 20px">
                  <li v-for="(error, index) in importResult.errors" :key="index">
                    第{{ error.row }}行：{{ error.message }}
                  </li>
                </ul>
              </div>
            </div>
          </template>
        </el-alert>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 质量评估对话框 -->
    <el-dialog
      v-model="qualityDialogVisible"
      title="用例质量评估"
      width="800px"
    >
      <div v-if="qualityLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" style="font-size: 32px"><Loading /></el-icon>
        <p style="margin-top: 10px">正在评估用例质量...</p>
      </div>
      <div v-else-if="qualityResult">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="综合评分" :span="2">
            <el-tag :type="getQualityLevelType(qualityResult.qualityLevel)" size="large">
              {{ qualityResult.totalScore }} 分 - {{ qualityResult.qualityLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完整性评分">
            {{ qualityResult.completenessScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="规范性评分">
            {{ qualityResult.standardizationScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="可执行性评分">
            {{ qualityResult.executabilityScore }} 分
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>详细评分</el-divider>

        <el-collapse v-model="activeCollapse">
          <el-collapse-item title="完整性评分详情" name="completeness">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="前置条件">
                {{ qualityResult.details.completeness.preConditionScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="测试步骤">
                {{ qualityResult.details.completeness.testStepScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="预期结果">
                {{ qualityResult.details.completeness.expectedResultScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="基本信息">
                {{ qualityResult.details.completeness.basicInfoScore }} 分
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
          <el-collapse-item title="规范性评分详情" name="standardization">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="命名规范性">
                {{ qualityResult.details.standardization.namingScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="格式规范性">
                {{ qualityResult.details.standardization.formatScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="内容规范性">
                {{ qualityResult.details.standardization.contentScore }} 分
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
          <el-collapse-item title="可执行性评分详情" name="executability">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="步骤清晰度">
                {{ qualityResult.details.executability.stepClarityScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="数据准备难度">
                {{ qualityResult.details.executability.dataPreparationScore }} 分
              </el-descriptions-item>
              <el-descriptions-item label="环境依赖">
                {{ qualityResult.details.executability.environmentDependencyScore }} 分
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
          <el-collapse-item title="优化建议" name="suggestions">
            <ul v-if="qualityResult.suggestions && qualityResult.suggestions.length > 0" style="margin: 0; padding-left: 20px">
              <li v-for="(suggestion, index) in qualityResult.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
            <p v-else style="color: #909399">暂无优化建议</p>
          </el-collapse-item>
        </el-collapse>
      </div>
      <template #footer>
        <el-button @click="qualityDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, ArrowDown, Download, Upload, Document, UploadFilled, Loading } from '@element-plus/icons-vue'
import { testCaseApi, type TestCase } from '@/api/testCase'
  import type { PageResult } from '@/api/types'
  import { testCaseQualityApi, type QualityScore } from '@/api/testCaseQuality'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const reviewLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const importDialogVisible = ref(false)
const importLoading = ref(false)
const importUploadRef = ref()
const importFileList = ref<any[]>([])
const importResult = ref<{
  successCount: number
  failCount: number
  errors: Array<{ row: number; message: 'string '}>
} | null>(null)
const qualityDialogVisible = ref(false)
const qualityLoading = ref(false)
const qualityResult = ref<QualityScore | null>(null)
const activeCollapse = ref<string[]>([])
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const reviewFormRef = ref<FormInstance>()

const testCaseList = ref<TestCase[]>([])
const formData = reactive<TestCase>({
  caseName: '',
  caseType: '',
  casePriority: '',
  requirementId: undefined,
  preCondition: '',
  testStep: '',
  expectedResult: '',
  caseStatus: 'DRAFT'
})
const viewData = ref<TestCase>({})
const currentReviewCase = ref<TestCase | null>(null)

const searchForm = reactive({
  caseName: '',
  caseStatus: ''
})

const reviewForm = reactive({
  reviewResult: '',
  reviewComment: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单验证规则
const formRules: FormRules = {
  caseName: [
    { required: true, message: '请输入用例名称', trigger: 'blur' },
    { max: 500, message: '用例名称长度不能超过500个字符', trigger: 'blur' }
  ],
  caseType: [
    { required: true, message: '请选择用例类型', trigger: 'change' }
  ],
  casePriority: [
    { required: true, message: '请选择用例优先级', trigger: 'change' }
  ]
}

const reviewFormRules: FormRules = {
  reviewResult: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => (isEdit.value ? '编辑用例' : '新建用例'))

// 获取状态文本
const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    PENDING_REVIEW: '待审核',
    REVIEWED: '已审核',
    OBSOLETE: '已废弃'
  }
  return statusMap[status || ''] || status || '-'
}

// 获取状态类型
const getStatusType = (status?: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    PENDING_REVIEW: 'warning',
    REVIEWED: 'success',
    OBSOLETE: 'danger'
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

// 获取可用的状态流转选项
const getAvailableStatuses = (currentStatus?: string) => {
  const statusMap: Record<string, Array<{ label: string; value: string }>> = {
    DRAFT: [
      { label: '提交审核', value: 'PENDING_REVIEW' },
      { label: '废弃', value: 'OBSOLETE' }
    ],
    PENDING_REVIEW: [
      { label: '退回草稿', value: 'DRAFT' },
      { label: '废弃', value: 'OBSOLETE' }
    ],
    REVIEWED: [
      { label: '废弃', value: 'OBSOLETE' },
      { label: '重新编辑', value: 'DRAFT' }
    ],
    OBSOLETE: [
      { label: '重新编辑', value: 'DRAFT' }
    ]
  }
  return statusMap[currentStatus || ''] || []
}

// 判断是否可以删除
const canDelete = (status?: string) => {
  return status !== 'REVIEWED'
}

// 加载用例列表
const loadTestCaseList = async () => {
  loading.value = true
  try {
    const response = await testCaseApi.getTestCaseList({
      page: pagination.page - 1,
      size: pagination.size,
      caseName: searchForm.caseName || undefined,
      caseStatus: searchForm.caseStatus || undefined
    })
    
    if (response.data) {
      testCaseList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载用例列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadTestCaseList()
}

// 重置搜索
const handleReset = () => {
  searchForm.caseName = ''
  searchForm.caseStatus = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadTestCaseList()
}

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page
  loadTestCaseList()
}

// 新建用例
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑用例
const handleEdit = async (row: TestCase) => {
  isEdit.value = true
  try {
    const response = await testCaseApi.getTestCaseById(row.id!)
    if (response.data) {
      Object.assign(formData, response.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用例详情失败:', error)
  }
}

// 查看详情
const handleView = async (row: TestCase) => {
  try {
    const response = await testCaseApi.getTestCaseById(row.id!)
    if (response.data) {
      viewData.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用例详情失败:', error)
  }
}

// 删除用例
const handleDelete = async (row: TestCase) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用例"${row.caseName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await testCaseApi.deleteTestCase(row.id!)
    ElMessage.success('删除成功')
    loadTestCaseList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除用例失败:', error)
    }
  }
}

// 状态流转
const handleStatusChange = async (row: TestCase, status: string) => {
  try {
    await testCaseApi.updateCaseStatus(row.id!, status)
    ElMessage.success('状态更新成功')
    loadTestCaseList()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 审核用例
const handleReview = (row: TestCase) => {
  currentReviewCase.value = row
  reviewForm.reviewResult = 
  reviewForm.reviewComment = 
  reviewDialogVisible.value = true
}

// 提交审核
const handleReviewSubmit = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (valid && currentReviewCase.value) {
      reviewLoading.value = true
      try {
        await testCaseApi.reviewTestCase(
          currentReviewCase.value.id!,
          reviewForm.reviewResult,
          reviewForm.reviewComment
        )
        ElMessage.success('审核成功')
        reviewDialogVisible.value = false
        loadTestCaseList()
      } catch (error) {
        console.error('审核失败:', error)
      } finally {
        reviewLoading.value = false
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await testCaseApi.updateTestCase(formData.id!, formData)
          ElMessage.success('更新成功')
        } else {
          await testCaseApi.createTestCase(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadTestCaseList()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 导出用例
const handleExport = async () => {
  try {
    const response = await testCaseApi.exportTestCases({
      caseName: searchForm.caseName || undefined,
      caseStatus: searchForm.caseStatus || undefined
    })
    
    // 创建下载链接
    const blob = new Blob([response.data as Blob], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `测试用例_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导出模板
const handleExportTemplate = async () => {
  try {
    const response = await testCaseApi.exportTemplate()
    
    // 创建下载链接
    const blob = new Blob([response.data as Blob], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '测试用例导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

// 导入用例
const handleImport = () => {
  importDialogVisible.value = true
  importFileList.value = []
  importResult.value = null
}

// 导入前验证
const beforeImportUpload = (file: File) => {
  const validTypes = ['.xlsx', '.xls']
  const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
  
  if (!validTypes.includes(fileExtension)) {
    ElMessage.error('只能上传Excel文件（.xlsx, .xls）')
    return false
  }

  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }

  return true
}

// 自定义上传
const handleImportUpload = async (options: any) => {
  const file = options.file
  importLoading.value = true
  importResult.value = null
  
  try {
    const response = await testCaseApi.importTestCases(file)
    if (response.data) {
      importResult.value = response.data
      if (response.data.failCount === 0) {
        ElMessage.success(`导入成功，共导入 ${response.data.successCount} 个用例`)
        loadTestCaseList()
      } else {
        ElMessage.warning(`导入完成，成功 ${response.data.successCount} 个，失败 ${response.data.failCount} 个`)
      }
    }
  } catch (error: any) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    importLoading.value = false
  }
}

// 导入成功
const handleImportSuccess = () => {
  // 已在handleImportUpload中处理
}

// 导入失败
const handleImportError = (error: Error) => {
  console.error('导入失败:', error)
  ElMessage.error('导入失败：' + error.message)
  importLoading.value = false
}

// 提交导入
const submitImport = () => {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  importUploadRef.value?.submit()
}

// 质量评估
const handleQualityAssess = async (row: TestCase) => {
  qualityDialogVisible.value = true
  qualityLoading.value = true
  qualityResult.value = null
  activeCollapse.value = []
  
  try {
    const response = await testCaseQualityApi.assessQuality(row.id!)
    if (response.data) {
      qualityResult.value = response.data
      activeCollapse.value = ['completeness', 'standardization', 'executability', 'suggestions']
    }
  } catch (error) {
    console.error('质量评估失败:', error)
    ElMessage.error('质量评估失败')
  } finally {
    qualityLoading.value = false
  }
}

// 获取质量等级类型
const getQualityLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    优秀: 'success',
    良好: 'success',
    一般: 'warning',
    需改进: 'danger'
  }
  return typeMap[level] || 'info'
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    caseCode: undefined,
    caseName: '',
    caseType: '',
    casePriority: '',
    requirementId: undefined,
    preCondition: '',
    testStep: '',
    expectedResult: '',
    caseStatus: 'DRAFT'
  })
  formRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 初始化
onMounted(() => {
  loadTestCaseList()
})
</script>

<style scoped lang="scss">
.test-case-list {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }
  }

  .search-card {
    margin-bottom: 20px;

    .search-form {
      margin: 0;
    }
  }

  .table-card {
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
