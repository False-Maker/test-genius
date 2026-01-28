<template>
  <div class="knowledge-base-management">
    <div class="header">
      <h2>知识库管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建知识库
        </el-button>
      </div>
    </div>

    <!-- 知识库列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="knowledgeBaseList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="kbCode" label="知识库编码" width="180" />
        <el-table-column prop="kbName" label="知识库名称" min-width="200" />
        <el-table-column prop="kbDescription" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="documentCount" label="文档数量" width="120" />
        <el-table-column prop="chunkCount" label="分块数量" width="120" />
        <el-table-column prop="lastSyncTime" label="最后同步时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.lastSyncTime">
              {{ formatDateTime(scope.row.lastSyncTime) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleUpload(scope.row)">
              上传文档
            </el-button>
            <el-button size="small" link type="primary" @click="handleSync(scope.row)">
              同步
            </el-button>
            <el-button size="small" link type="primary" @click="handleViewDocuments(scope.row)">
              文档列表
            </el-button>
            <el-button size="small" link type="primary" @click="handleManagePermissions(scope.row)">
              权限管理
            </el-button>
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑知识库对话框 -->
    <el-dialog
      v-model="kbDialogVisible"
      :title="isEdit ? '编辑知识库' : '创建知识库'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="kbFormRef"
        :model="kbFormData"
        :rules="kbFormRules"
        label-width="120px"
      >
        <el-form-item label="知识库编码" prop="kbCode">
          <el-input
            v-model="kbFormData.kbCode"
            placeholder="请输入知识库编码"
            maxlength="100"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="知识库名称" prop="kbName">
          <el-input
            v-model="kbFormData.kbName"
            placeholder="请输入知识库名称"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="描述" prop="kbDescription">
          <el-input
            v-model="kbFormData.kbDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入知识库描述（可选）"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="知识库类型" prop="kbType">
          <el-select v-model="kbFormData.kbType" placeholder="请选择知识库类型">
            <el-option label="测试规范" value="测试规范" />
            <el-option label="业务知识" value="业务知识" />
            <el-option label="用例模板" value="用例模板" />
            <el-option label="通用知识" value="通用知识" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="kbDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitKB">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 文档上传对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文档"
      width="600px"
      @close="handleUploadDialogClose"
    >
      <el-form
        ref="uploadFormRef"
        :model="uploadFormData"
        label-width="120px"
      >
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持的文件格式：txt, md, pdf, doc, docx, ppt, pptx, html, csv
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUploadSubmit">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 同步对话框 -->
    <el-dialog
      v-model="syncDialogVisible"
      title="同步知识库"
      width="600px"
      @close="handleSyncDialogClose"
    >
      <el-form
        ref="syncFormRef"
        :model="syncFormData"
        label-width="120px"
      >
        <el-form-item label="同步类型">
          <el-radio-group v-model="syncFormData.syncType">
            <el-radio label="incremental">增量同步</el-radio>
            <el-radio label="full">全量同步</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="源文件路径">
          <el-input
            v-model="syncFormData.sourcePath"
            placeholder="请输入源文件路径"
          />
        </el-form-item>
        <el-form-item label="分块策略">
          <el-select v-model="syncFormData.chunkingStrategy">
            <el-option label="段落" value="paragraph" />
            <el-option label="句子" value="sentence" />
            <el-option label="固定大小" value="fixed" />
          </el-select>
        </el-form-item>
        <el-form-item label="分块大小">
          <el-input-number
            v-model="syncFormData.chunkSize"
            :min="100"
            :max="5000"
            :step="100"
          />
        </el-form-item>
        <el-form-item label="分块重叠">
          <el-input-number
            v-model="syncFormData.chunkOverlap"
            :min="0"
            :max="500"
            :step="50"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="syncDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="syncLoading" @click="handleSyncSubmit">
          开始同步
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import { knowledgeBaseApi, type KnowledgeBase, type KBSyncParams } from '@/api/knowledgeBase'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const uploadLoading = ref(false)
const syncLoading = ref(false)
const kbDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const syncDialogVisible = ref(false)
const isEdit = ref(false)
const kbFormRef = ref<FormInstance>()
const uploadFormRef = ref<FormInstance>()
const syncFormRef = ref<FormInstance>()
const uploadRef = ref()
const fileList = ref<any[]>([])

const knowledgeBaseList = ref<KnowledgeBase[]>([])
const currentKB = ref<KnowledgeBase | null>(null)

const kbFormData = reactive<Partial<KnowledgeBase>>({
  kbCode: '',
  kbName: '',
  kbDescription: '',
  kbType: ''
})

const uploadFormData = reactive({
  file: null as File | null
})

const syncFormData = reactive<KBSyncParams>({
  kbId: 0,
  syncType: 'incremental',
  sourcePath: '',
  chunkingStrategy: 'paragraph',
  chunkSize: 1000,
  chunkOverlap: 200
})

// 表单验证规则
const kbFormRules: FormRules = {
  kbCode: [
    { required: true, message: '请输入知识库编码', trigger: 'blur' }
  ],
  kbName: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' }
  ]
}

// 加载知识库列表
const loadKnowledgeBases = async () => {
  loading.value = true
  try {
    // 获取当前用户ID
    const userId = userStore.userInfo?.id || 1 // 如果未登录使用默认ID 1
    const response = await knowledgeBaseApi.getUserKnowledgeBases(userId)
    if (response.data) {
      knowledgeBaseList.value = Array.isArray(response.data) ? response.data : response.data.knowledge_bases || []
      
      // 加载每个知识库的统计信息
      for (const kb of knowledgeBaseList.value) {
        if (kb.id) {
          try {
            const statsResponse = await knowledgeBaseApi.getStatistics(kb.id)
            if (statsResponse.data) {
              kb.documentCount = statsResponse.data.documentCount
              kb.chunkCount = statsResponse.data.chunkCount
              kb.lastSyncTime = statsResponse.data.lastSyncTime
            }
          } catch (error) {
            console.error(`加载知识库 ${kb.id} 统计信息失败:`, error)
          }
        }
      }
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
    ElMessage.error('加载知识库列表失败')
  } finally {
    loading.value = false
  }
}

// 创建知识库
const handleCreate = () => {
  isEdit.value = false
  resetKBForm()
  kbDialogVisible.value = true
}

// 上传文档
const handleUpload = (kb: KnowledgeBase) => {
  currentKB.value = kb
  resetUploadForm()
  uploadDialogVisible.value = true
}

// 同步知识库
const handleSync = (kb: KnowledgeBase) => {
  currentKB.value = kb
  syncFormData.kbId = kb.id || 0
  syncFormData.sourcePath = ''
  syncDialogVisible.value = true
}

// 查看文档列表
const handleViewDocuments = (kb: KnowledgeBase) => {
  router.push({
    name: 'KnowledgeBaseDocuments',
    params: { kbId: kb.id }
  })
}

// 权限管理
const handleManagePermissions = (kb: KnowledgeBase) => {
  router.push({
    name: 'KnowledgeBasePermissions',
    params: { kbId: kb.id }
  })
}

// 删除知识库
const handleDelete = async (kb: KnowledgeBase) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除知识库 "${kb.kbName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除API
    if (kb.id) {
      await knowledgeBaseApi.deleteKnowledgeBase(kb.id)
      ElMessage.success('删除成功')
      loadKnowledgeBases()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除知识库失败:', error)
      ElMessage.error('删除知识库失败')
    }
  }
}

// 文件选择
const handleFileChange = (file: any) => {
  uploadFormData.file = file.raw
}

// 提交知识库表单
const handleSubmitKB = async () => {
  if (!kbFormRef.value) return
  
  await kbFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value && currentKB.value?.id) {
          // 更新知识库
          await knowledgeBaseApi.updateKnowledgeBase(currentKB.value.id, kbFormData)
        } else {
          // 创建知识库
          const createData = {
            ...kbFormData,
            creatorId: userStore.userInfo?.id || 1,
            isActive: '1'
          }
          await knowledgeBaseApi.createKnowledgeBase(createData)
        }
        
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        kbDialogVisible.value = false
        loadKnowledgeBases()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 提交上传
const handleUploadSubmit = async () => {
  if (!uploadFormData.file) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  if (!currentKB.value?.id) {
    ElMessage.error('知识库ID不存在')
    return
  }
  
  uploadLoading.value = true
  try {
    const response = await knowledgeBaseApi.uploadDocument(
      currentKB.value.id,
      uploadFormData.file
    )
    
    if (response.success) {
      ElMessage.success('文档上传成功')
      uploadDialogVisible.value = false
      loadKnowledgeBases()
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

// 提交同步
const handleSyncSubmit = async () => {
  if (!syncFormRef.value) return
  
  if (!syncFormData.sourcePath) {
    ElMessage.warning('请输入源文件路径')
    return
  }
  
  syncLoading.value = true
  try {
    const response = await knowledgeBaseApi.syncKnowledgeBase(syncFormData)
    
    if (response.data?.success) {
      ElMessage.success('同步任务已启动')
      syncDialogVisible.value = false
      loadKnowledgeBases()
    } else {
      ElMessage.error(response.data?.error || '同步失败')
    }
  } catch (error: any) {
    console.error('同步失败:', error)
    ElMessage.error(error.message || '同步失败')
  } finally {
    syncLoading.value = false
  }
}

// 重置表单
const resetKBForm = () => {
  Object.assign(kbFormData, {
    kbCode: '',
    kbName: '',
    kbDescription: '',
    kbType: ''
  })
  kbFormRef.value?.clearValidate()
}

const resetUploadForm = () => {
  uploadFormData.file = null
  fileList.value = []
  uploadRef.value?.clearFiles()
}

const resetSyncForm = () => {
  syncFormData.sourcePath = ''
  syncFormData.syncType = 'incremental'
  syncFormData.chunkingStrategy = 'paragraph'
  syncFormData.chunkSize = 1000
  syncFormData.chunkOverlap = 200
}

// 对话框关闭
const handleDialogClose = () => {
  resetKBForm()
}

const handleUploadDialogClose = () => {
  resetUploadForm()
}

const handleSyncDialogClose = () => {
  resetSyncForm()
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style scoped>
.knowledge-base-management {
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

.table-card {
  margin-bottom: 20px;
}
</style>
