<template>
  <div class="knowledge-base-container">
    <div class="kb-layout">
      <!-- Top Action Bar & Search -->
      <el-card class="bento-card header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="page-title">
              <el-icon><Reading /></el-icon>
              <h2>知识库</h2>
            </div>
            <div class="search-bar">
               <el-input
                v-model="searchForm.queryText"
                placeholder="Search knowledge base..."
                prefix-icon="Search"
                clearable
                class="search-input"
                @keyup.enter="handleSearch"
              >
                <template #prepend>
                   <el-select v-model="searchType" style="width: 110px">
                    <el-option label="Semantic" value="semantic" />
                    <el-option label="Keyword" value="keyword" />
                   </el-select>
                </template>
              </el-input>
              <el-select
                v-model="searchForm.docType"
                placeholder="Type"
                clearable
                style="width: 120px"
              >
                <el-option label="测试规范" value="测试规范" />
                <el-option label="业务知识" value="业务知识" />
                <el-option label="用例模板" value="用例模板" />
              </el-select>
              <el-button type="primary" @click="handleSearch" :loading="loading">
                <el-icon><Search /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div class="header-actions">
             <el-button @click="handleInit" text>Init DB</el-button>
             <el-divider direction="vertical" />
             <el-button @click="handleUpload">
              <el-icon class="mr-1"><Upload /></el-icon> Upload
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon class="mr-1"><Plus /></el-icon> Add New
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- Document Grid -->
      <div class="content-area" v-loading="loading">
         <div v-if="documentList.length === 0 && !loading" class="empty-state">
            <el-empty description="Knowledge base is empty" />
         </div>
         
         <div v-else class="document-grid">
            <el-card 
              v-for="doc in documentList" 
              :key="doc.id" 
              class="doc-card"
              shadow="hover"
            >
              <div class="doc-card-header">
                 <div class="doc-icon">
                    <el-icon v-if="doc.docType === '测试规范'"><Notebook /></el-icon>
                    <el-icon v-else-if="doc.docType === '用例模板'"><DocumentCopy /></el-icon>
                    <el-icon v-else><Document /></el-icon>
                 </div>
                 <div class="doc-meta">
                    <div class="doc-code mono-text">{{ doc.docCode }}</div>
                    <div class="doc-date">
                        <el-tag size="small" type="info" effect="plain">{{ doc.docType }}</el-tag>
                    </div>
                 </div>
              </div>
              
              <div class="doc-body">
                 <h3 class="doc-title" :title="doc.docName">{{ doc.docName }}</h3>
                 <p class="doc-preview">{{ doc.docContent ? doc.docContent.substring(0, 80) + '...' : 'No preview available' }}</p>
                 
                 <div class="doc-tags" v-if="doc.docCategory">
                    <el-tag size="small" type="primary" class="cat-tag">{{ doc.docCategory }}</el-tag>
                 </div>
                 
                 <div class="doc-similarity" v-if="searchType === 'semantic' && doc.similarity !== undefined">
                    <span class="label">Match:</span>
                    <span class="value">{{ (doc.similarity * 100).toFixed(1) }}%</span>
                 </div>
              </div>
              
              <div class="doc-footer">
                 <el-button text bg size="small" @click="handleView(doc)">View Details</el-button>
                 <el-link v-if="doc.docUrl" :href="doc.docUrl" target="_blank" :underline="false">
                    <el-icon><Link /></el-icon>
                 </el-link>
              </div>
            </el-card>
         </div>
      </div>
    </div>

    <!-- 添加文档对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="Add Document"
      width="800px"
      @close="handleDialogClose"
      class="bento-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
      >
        <el-row :gutter="20">
           <el-col :span="12">
            <el-form-item label="Code" prop="docCode">
              <el-input v-model="formData.docCode" placeholder="DOC-001" maxlength="100" />
            </el-form-item>
           </el-col>
           <el-col :span="12">
            <el-form-item label="Type" prop="docType">
              <el-select v-model="formData.docType" placeholder="Select type" style="width: 100%">
                <el-option label="测试规范" value="测试规范" />
                <el-option label="业务知识" value="业务知识" />
                <el-option label="用例模板" value="用例模板" />
              </el-select>
            </el-form-item>
           </el-col>
        </el-row>
        
        <el-row :gutter="20">
           <el-col :span="12">
             <el-form-item label="Name" prop="docName">
              <el-input v-model="formData.docName" placeholder="Document Name" maxlength="500" />
            </el-form-item>
           </el-col>
           <el-col :span="12">
             <el-form-item label="Category" prop="docCategory">
              <el-input v-model="formData.docCategory" placeholder="Category (Optional)" maxlength="100" />
            </el-form-item>
           </el-col>
        </el-row>

        <el-form-item label="Content" prop="docContent">
          <el-input
            v-model="formData.docContent"
            type="textarea"
            :rows="8"
            placeholder="Document content..."
          />
        </el-form-item>
        <el-form-item label="URL" prop="docUrl">
          <el-input v-model="formData.docUrl" placeholder="External URL (Optional)" maxlength="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          Confirm
        </el-button>
      </template>
    </el-dialog>

    <!-- 上传文档对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="Upload Document"
      width="600px"
      @close="resetUploadForm"
      class="bento-dialog"
    >
      <el-form label-position="top">
        <el-form-item label="File Selection">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            drag
            class="bento-upload"
          >
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">
              Drop file here or <em>click to upload</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                Supported: txt, md, pdf, doc, docx, ppt, pptx, html, csv
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUploadSubmit">
          Upload
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看文档对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="Document Details"
      width="800px"
      class="bento-dialog"
    >
      <el-descriptions :column="2" border class="bento-descriptions">
        <el-descriptions-item label="ID (Code)"><span class="mono-text">{{ viewData.docCode || '-' }}</span></el-descriptions-item>
        <el-descriptions-item label="Type">{{ viewData.docType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Name" :span="2">{{ viewData.docName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Category">{{ viewData.docCategory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Similarity" v-if="viewData.similarity !== undefined">
           <span class="text-success">{{ (viewData.similarity * 100).toFixed(1) }}%</span>
        </el-descriptions-item>
        <el-descriptions-item label="Content" :span="2">
          <div class="doc-content-view">
            {{ viewData.docContent || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="URL" :span="2">
          <el-link v-if="viewData.docUrl" :href="viewData.docUrl" target="_blank" type="primary">
            {{ viewData.docUrl }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Upload, Search, Reading, Document, DocumentCopy, Notebook, Link } from '@element-plus/icons-vue'
import { knowledgeBaseApi, type KnowledgeDocument } from '@/api/knowledgeBase'

// 响应式数据据�?
const loading = ref(false)
const submitLoading = ref(false)
const uploadLoading = ref(false)
const addDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const fileList = ref<any[]>([])
const currentKBId = ref<number>(1) // 默认知识库ID，实际应从配置或选择获取

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

// 上传文档
const handleUpload = () => {
  resetUploadForm()
  uploadDialogVisible.value = true
}

// 文件选择
const handleFileChange = (file: any) => {
  fileList.value = [file]
}

// 提交上传
const handleUploadSubmit = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  const file = fileList.value[0].raw
  if (!file) {
    ElMessage.warning('文件不存在')
    return
  }
  
  uploadLoading.value = true
  try {
    const response = await knowledgeBaseApi.uploadDocument(currentKBId.value, file)
    
    if (response.success) {
      ElMessage.success('文档上传成功')
      uploadDialogVisible.value = false
      handleReset()
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 重置上传表单
const resetUploadForm = () => {
  fileList.value = []
  uploadRef.value?.clearFiles()
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

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.knowledge-base-container {
  height: 100%;
  overflow: hidden;
  padding: 24px;
}

.kb-layout {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

.bento-card {
  &.header-card {
    flex-shrink: 0;
    
    :deep(.el-card__body) {
        padding: 16px 24px;
    }
  }
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    
    .header-left {
        display: flex;
        align-items: center;
        gap: 24px;
        flex: 1;
        
        .page-title {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .el-icon {
                font-size: 24px;
                color: $tech-white;
            }
            
            h2 {
                margin: 0;
                font-size: 18px;
                font-weight: 600;
                color: $text-main;
            }
        }
        
        .search-bar {
            flex: 1;
            max-width: 600px;
             display: flex;
             gap: 8px;
             
             .search-input {
                 width: 100%;
             }
        }
    }
    
    .header-actions {
        display: flex;
        align-items: center;
        gap: 12px;
    }
}

.content-area {
    flex: 1;
    overflow-y: auto;
    min-height: 0;
    
    .empty-state {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
}

.document-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    padding-bottom: 24px;
}

.doc-card {
    height: 100%;
    transition: all 0.2s ease;
    border: 1px solid transparent;
    cursor: pointer;
    
    &:hover {
        transform: translateY(-2px);
        border-color: $border-focus;
        box-shadow: 0 4px 20px rgba(0,0,0,0.3);
    }
    
    :deep(.el-card__body) {
        padding: 20px;
        height: 100%;
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
    }
    
    .doc-card-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 16px;
        
        .doc-icon {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            background: rgba($tech-white, 0.05);
            display: flex;
            align-items: center;
            justify-content: center;
            color: $tech-white;
            font-size: 20px;
        }
        
        .doc-meta {
            text-align: right;
            
            .doc-code {
                font-size: 11px;
                color: $text-secondary;
                margin-bottom: 4px;
            }
        }
    }
    
    .doc-body {
        flex: 1;
        
        .doc-title {
            margin: 0 0 8px;
            font-size: 16px;
            font-weight: 600;
            color: $text-main;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.4;
        }
        
        .doc-preview {
            margin: 0 0 16px;
            font-size: 13px;
            color: $text-secondary;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.5;
        }
        
        .doc-tags {
            margin-bottom: 12px;
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
        }
        
        .doc-similarity {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            padding: 4px 8px;
            background: rgba($acid-green, 0.1);
            border-radius: 4px;
            color: $acid-green;
            width: fit-content;
            
            .value {
                font-weight: bold;
                font-family: $font-mono;
            }
        }
    }
    
    .doc-footer {
        margin-top: 16px;
        padding-top: 12px;
        border-top: 1px solid $border-light;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
}

.mr-1 {
    margin-right: 4px;
}

.doc-content-view {
    font-family: $font-family;
    line-height: 1.6;
    color: $text-regular;
    white-space: pre-wrap;
    max-height: 50vh;
    overflow-y: auto;
    padding: 12px;
    background: rgba(0,0,0,0.2);
    border-radius: 4px;
}
</style>
