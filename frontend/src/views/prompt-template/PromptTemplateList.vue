<template>
  <div class="prompt-template-list">
    <div class="header">
      <h2>提示词模板管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建模板
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模板名称">
          <el-input
            v-model="searchForm.templateName"
            placeholder="请输入模板名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="模板分类">
          <el-input
            v-model="searchForm.templateCategory"
            placeholder="请输入模板分类"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 模板列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="templateList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="templateCode" label="模板编码" width="180" />
        <el-table-column prop="templateName" label="模板名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="templateCategory" label="模板分类" width="150">
          <template #default="scope">
            {{ scope.row.templateCategory || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="templateType" label="模板类型" width="120">
          <template #default="scope">
            {{ scope.row.templateType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isActive === '1' ? 'success' : 'info'">
              {{ scope.row.isActive === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
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
            <el-button
              size="small"
              link
              type="info"
              @click="handleTestGenerate(scope.row)"
            >
              测试生成
            </el-button>
            <el-button
              size="small"
              link
              type="danger"
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
      width="1000px"
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
            <el-form-item label="模板名称" prop="templateName">
              <el-input
                v-model="formData.templateName"
                placeholder="请输入模板名称"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板分类" prop="templateCategory">
              <el-input
                v-model="formData.templateCategory"
                placeholder="请输入模板分类"
                maxlength="100"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板类型" prop="templateType">
              <el-input
                v-model="formData.templateType"
                placeholder="请输入模板类型"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态" prop="isActive">
              <el-radio-group v-model="formData.isActive">
                <el-radio label="1">启用</el-radio>
                <el-radio label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="模板内容" prop="templateContent">
          <el-input
            v-model="formData.templateContent"
            type="textarea"
            :rows="8"
            placeholder="请输入模板内容，使用 {变量名} 格式定义变量，例如：{requirement_name}"
          />
          <div class="form-tip">
            提示：使用 {变量名} 格式定义变量，例如：{requirement_name}、{business_module}
          </div>
        </el-form-item>
        <el-form-item label="模板变量定义" prop="templateVariables">
          <el-input
            v-model="formData.templateVariables"
            type="textarea"
            :rows="4"
            placeholder='请输入JSON格式的变量定义，例如：{"requirement_name": "需求名称", "business_module": "业务模块"}'
          />
          <div class="form-tip">
            提示：JSON格式定义变量说明，例如：{"变量名": "变量说明"}
          </div>
        </el-form-item>
        <el-form-item label="适用测试分层" prop="applicableLayers">
          <el-input
            v-model="formData.applicableLayers"
            placeholder="请输入适用的测试分层，多个用逗号分隔"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="适用测试方法" prop="applicableMethods">
          <el-input
            v-model="formData.applicableMethods"
            placeholder="请输入适用的测试方法，多个用逗号分隔"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="适用业务模块" prop="applicableModules">
          <el-input
            v-model="formData.applicableModules"
            placeholder="请输入适用的业务模块，多个用逗号分隔"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="模板描述" prop="templateDescription">
          <el-input
            v-model="formData.templateDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
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
      title="模板详情"
      width="1000px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模板编码">
          {{ viewData.templateCode }}
        </el-descriptions-item>
        <el-descriptions-item label="模板名称">
          {{ viewData.templateName }}
        </el-descriptions-item>
        <el-descriptions-item label="模板分类">
          {{ viewData.templateCategory || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="模板类型">
          {{ viewData.templateType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="启用状态">
          <el-tag :type="viewData.isActive === '1' ? 'success' : 'info'">
            {{ viewData.isActive === '1' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本号">
          {{ viewData.version }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ viewData.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ viewData.updateTime }}
        </el-descriptions-item>
        <el-descriptions-item label="模板内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 300px; overflow-y: auto">{{ viewData.templateContent }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="模板变量定义" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.templateVariables || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="适用测试分层" :span="2">
          {{ viewData.applicableLayers || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="适用测试方法" :span="2">
          {{ viewData.applicableMethods || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="适用业务模块" :span="2">
          {{ viewData.applicableModules || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="模板描述" :span="2">
          <div style="white-space: pre-wrap">{{ viewData.templateDescription || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 测试生成对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      title="测试生成提示词"
      width="800px"
    >
      <el-form
        ref="testFormRef"
        :model="testForm"
        label-width="120px"
      >
        <el-form-item label="变量值">
          <div class="variable-inputs">
            <div
              v-for="(value, key) in testForm.variables"
              :key="key"
              class="variable-item"
            >
              <el-input
                v-model="testForm.variables[key]"
                :placeholder="`请输入 ${key} 的值`"
                style="width: 200px"
              />
              <el-button
                size="small"
                type="danger"
                link
                @click="removeVariable(key)"
              >
                删除
              </el-button>
            </div>
            <el-button
              size="small"
              type="primary"
              @click="addVariable"
            >
              添加变量
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="生成的提示词">
          <el-input
            v-model="generatedPrompt"
            type="textarea"
            :rows="10"
            readonly
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="generateLoading" @click="handleGenerate">
          生成
        </el-button>
        <el-button type="success" @click="handleCopyPrompt">复制</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { promptTemplateApi, type PromptTemplate } from '@/api/promptTemplate'
import type { PageResult } from '@/api/types'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const generateLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const testDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const testFormRef = ref<FormInstance>()

const templateList = ref<PromptTemplate[]>([])
const formData = reactive<PromptTemplate>({
  templateName: '',
  templateCategory: '',
  templateType: '',
  templateContent: '',
  templateVariables: '',
  applicableLayers: '',
  applicableMethods: '',
  applicableModules: '',
  templateDescription: '',
  isActive: '1'
})
const viewData = ref<PromptTemplate>({})
const currentTestTemplate = ref<PromptTemplate | null>(null)
const generatedPrompt = ref('')

const searchForm = reactive({
  templateName: '',
  templateCategory: ''
})

const testForm = reactive({
  variables: {} as Record<string, string>
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单验证规则
const formRules: FormRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { max: 500, message: '模板名称长度不能超过500个字符', trigger: 'blur' }
  ],
  templateContent: [
    { required: true, message: '请输入模板内容', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => (isEdit.value ? '编辑模板' : '新建模板'))

// 加载模板列表
const loadTemplateList = async () => {
  loading.value = true
  try {
    const response = await promptTemplateApi.getTemplateList({
      page: pagination.page - 1,
      size: pagination.size
    })
    
    if (response.data) {
      templateList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载模板列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadTemplateList()
}

// 重置搜索
const handleReset = () => {
  searchForm.templateName = ''
  searchForm.templateCategory = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadTemplateList()
}

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page
  loadTemplateList()
}

// 新建模板
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = async (row: PromptTemplate) => {
  isEdit.value = true
  try {
    const response = await promptTemplateApi.getTemplateById(row.id!)
    if (response.data) {
      Object.assign(formData, response.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取模板详情失败:', error)
  }
}

// 查看详情
const handleView = async (row: PromptTemplate) => {
  try {
    const response = await promptTemplateApi.getTemplateById(row.id!)
    if (response.data) {
      viewData.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取模板详情失败:', error)
  }
}

// 删除模板
const handleDelete = async (row: PromptTemplate) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.templateName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await promptTemplateApi.deleteTemplate(row.id!)
    ElMessage.success('删除成功')
    loadTemplateList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error)
    }
  }
}

// 切换启用状态
const handleToggleStatus = async (row: PromptTemplate) => {
  try {
    const newStatus = row.isActive === '1' ? '0' : '1'
    await promptTemplateApi.toggleTemplateStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadTemplateList()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 测试生成
const handleTestGenerate = async (row: PromptTemplate) => {
  currentTestTemplate.value = row
  testForm.variables = {}
  generatedPrompt.value = ''
  
  // 解析模板变量
  if (row.templateVariables) {
    try {
      const variables = JSON.parse(row.templateVariables)
      Object.keys(variables).forEach(key => {
        testForm.variables[key] = ''
      })
    } catch (e) {
      // 如果解析失败，尝试从模板内容中提取变量
      const variablePattern = /\{([^}]+)\}/g
      let match
      while ((match = variablePattern.exec(row.templateContent)) !== null) {
        const varName = match[1].trim()
        if (!testForm.variables[varName]) {
          testForm.variables[varName] = ''
        }
      }
    }
  } else {
    // 从模板内容中提取变量
    const variablePattern = /\{([^}]+)\}/g
    let match
    while ((match = variablePattern.exec(row.templateContent)) !== null) {
      const varName = match[1].trim()
      if (!testForm.variables[varName]) {
        testForm.variables[varName] = ''
      }
    }
  }
  
  testDialogVisible.value = true
}

// 添加变量
const addVariable = () => {
  const varName = prompt('请输入变量名:')
  if (varName && varName.trim()) {
    testForm.variables[varName.trim()] = ''
  }
}

// 删除变量
const removeVariable = (key: string) => {
  delete testForm.variables[key]
}

// 生成提示词
const handleGenerate = async () => {
  if (!currentTestTemplate.value) return
  
  generateLoading.value = true
  try {
    const response = await promptTemplateApi.generatePrompt(
      currentTestTemplate.value.id!,
      testForm.variables
    )
    if (response.data) {
      generatedPrompt.value = response.data
      ElMessage.success('生成成功')
    }
  } catch (error) {
    console.error('生成提示词失败:', error)
  } finally {
    generateLoading.value = false
  }
}

// 复制提示词
const handleCopyPrompt = async () => {
  try {
    await navigator.clipboard.writeText(generatedPrompt.value)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
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
          await promptTemplateApi.updateTemplate(formData.id!, formData)
          ElMessage.success('更新成功')
        } else {
          await promptTemplateApi.createTemplate(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadTemplateList()
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
    templateCode: undefined,
    templateName: '',
    templateCategory: '',
    templateType: '',
    templateContent: '',
    templateVariables: '',
    applicableLayers: '',
    applicableMethods: '',
    applicableModules: '',
    templateDescription: '',
    isActive: '1'
  })
  formRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 初始化
onMounted(() => {
  loadTemplateList()
})
</script>

<style scoped lang="scss">
.prompt-template-list {
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

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .variable-inputs {
    .variable-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
    }
  }
}
</style>
