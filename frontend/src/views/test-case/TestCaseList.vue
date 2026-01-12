<template>
  <div class="test-case-list">
    <div class="header">
      <h2>用例管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建用例
      </el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { testCaseApi, type TestCase } from '@/api/testCase'
import type { PageResult } from '@/api/types'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const reviewLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
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
  reviewForm.reviewResult = ''
  reviewForm.reviewComment = ''
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
