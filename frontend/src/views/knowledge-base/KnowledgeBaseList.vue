<template>
  <div class="knowledge-base-list">
    <div class="header">
      <h2>知识库管�?管理</h2>
      <div class="header-actions">
        <el-button @click="handleInit">初始化知识库</el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加文档
        </el-button>
      </div>
    </div>

    <!-- 搜索�?-->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="检索方式">
          <el-radio-group v-model="searchType">
            <el-radio label="semantic">语义检索</el-radio>
            <el-radio label="keyword">关键词检索</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="检索内容">
          <el-input
            v-model="searchForm.queryText"
            placeholder="请输入检索内容�"
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select
            v-model="searchForm.docType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="测试规范" value="测试规范" />
            <el-option label="业务知识" value="业务知识" />
            <el-option label="用例模板" value="用例模板" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">检索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文档列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="documentList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="docCode" label="文档编码" width="180" />
        <el-table-column prop="docName" label="文档名称" min-width="200" />
        <el-table-column prop="docType" label="文档类型" width="120" />
        <el-table-column prop="docCategory" label="分类" width="120" />
        <el-table-column prop="similarity" label="相似�" width="100" v-if="searchType === 'semantic'">
          <template #default="scope">
            <span v-if="scope.row.similarity !== undefined">
              {{ (scope.row.similarity * 100).toFixed(1) }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="docContent" label="文档内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleView(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加文档对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加文档"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="文档编码" prop="docCode">
          <el-input
            v-model="formData.docCode"
            placeholder="请输入文档编码�"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="文档名称" prop="docName">
          <el-input
            v-model="formData.docName"
            placeholder="请输入文档名称"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="formData.docType" placeholder="请选择文档类型">
            <el-option label="测试规范" value="测试规范" />
            <el-option label="业务知识" value="业务知识" />
            <el-option label="用例模板" value="用例模板" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档分类" prop="docCategory">
          <el-input
            v-model="formData.docCategory"
            placeholder="请输入文档分类（可选）"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="文档内容" prop="docContent">
          <el-input
            v-model="formData.docContent"
            type="textarea"
            :rows="8"
            placeholder="请输入文档内�"
          />
        </el-form-item>
        <el-form-item label="文档URL" prop="docUrl">
          <el-input
            v-model="formData.docUrl"
            placeholder="请输入文档URL（可选）"
            maxlength="1000"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看文档对话�?-->
    <el-dialog
      v-model="viewDialogVisible"
      title="文档详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文档编码">{{ viewData.docCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文档名称">{{ viewData.docName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文档类型">{{ viewData.docType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文档分类">{{ viewData.docCategory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文档内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 400px; overflow-y: auto">
            {{ viewData.docContent || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="文档URL" :span="2">
          <el-link v-if="viewData.docUrl" :href="viewData.docUrl" target="_blank">
            {{ viewData.docUrl }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="相似�" v-if="viewData.similarity !== undefined">
          {{ (viewData.similarity * 100).toFixed(1) }}%
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { knowledgeBaseApi, type KnowledgeDocument } from '@/api/knowledgeBase'

// 响应式数据据�?
const loading = ref(false)
const submitLoading = ref(false)
const addDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref<FormInstance>()

const documentList = ref<KnowledgeDocument[]>([])
const searchType = ref<'semantic' | 'keyword'>('semantic')
const searchForm = reactive({
  queryText: '',
  docType: ''
})

const formData = reactive<Partial<KnowledgeDocument>>({
  docCode: '',
  docName: '',
  docType: '',
  docCategory: '',
  docContent: '',
  docUrl: ''
})
const viewData = ref<KnowledgeDocument>({})

// 表单验证规则
const formRules: FormRules = {
  docCode: [
    { required: true, message: '请输入文档编码码', trigger: 'blur' }
  ],
  docName: [
    { required: true, message: '请输入文档名称', trigger: 'blur' }
  ],
  docType: [
    { required: true, message: '请选择文档类型', trigger: 'change' }
  ],
  docContent: [
    { required: true, message: '请输入文档内容', trigger: 'blur' }
  ]
}

// 初始化知识库
const handleInit = async () => {
  try {
    await ElMessageBox.confirm('确定要初始化知识库吗？这将创建人必要的表结构。', '初始化确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await knowledgeBaseApi.initKnowledgeBase()
    if (response.data) {
      ElMessage.success('知识库初始化成功')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('初始化知识库失败', error)
      ElMessage.error('初始化知识库失败')
    }
  }
}

// 添加文档
const handleAdd = () => {
  resetForm()
  addDialogVisible.value = true
}

// 查看文档
const handleView = (row: KnowledgeDocument) => {
  viewData.value = row
  viewDialogVisible.value = true
}

// 检索文档内容方式文档�?
const handleSearch = async () => {
  if (!searchForm.queryText.trim()) {
    ElMessage.warning('请输入检索内容')
    return
  }

  loading.value = true
  try {
    let response
    if (searchType.value === 'semantic') {
      response = await knowledgeBaseApi.searchDocuments({
        queryText: searchForm.queryText,
        docType: searchForm.docType || undefined,
        topK: 10,
        similarityThreshold: 0.7
      })
    } else {
      response = await knowledgeBaseApi.searchDocumentsByKeyword(searchForm.queryText, {
        docType: searchForm.docType || undefined,
        topK: 10
      })
    }
    
    if (response.data) {
      documentList.value = response.data
      if (documentList.value.length === 0) {
        ElMessage.info('未找到相关文档')
      }
    }
  } catch (error) {
      console.error('检索失败', error)
      ElMessage.error('检索失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.queryText = ''
  searchForm.docType = ''
  documentList.value = []
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await knowledgeBaseApi.addDocument({
          docCode: formData.docCode!,
          docName: formData.docName!,
          docType: formData.docType!,
          docContent: formData.docContent!,
          docCategory: formData.docCategory,
          docUrl: formData.docUrl
        })
        ElMessage.success('添加成功')
        addDialogVisible.value = false
        handleReset()
      } catch (error) {
        console.error('添加失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    docCode: '',
    docName: '',
    docType: '',
    docCategory: '',
    docContent: '',
    docUrl: ''
  })
  formRef.value?.clearValidate()
}

// 对话框关闭闭�?
const handleDialogClose = () => {
  resetForm()
}
</script>

<style scoped>
.knowledge-base-list {
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

.header-actions {
  display: flex;
  gap: 10px;
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
</style>
