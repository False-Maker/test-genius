<template>
  <div class="version-history">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>版本历史</span>
          <div>
            <el-button size="small" type="warning" @click="handleShowAbTest">A/B测试</el-button>
            <el-button size="small" type="primary" @click="handleCreateVersion">创建新版本</el-button>
          </div>
        </div>
      </template>

      <!-- 版本列表 -->
      <el-table :data="versions" stripe style="width: 100%">
        <el-table-column prop="versionNumber" label="版本号" width="100" />
        <el-table-column prop="versionName" label="版本名称" width="200" />
        <el-table-column prop="versionDescription" label="版本描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="changeLog" label="变更日志" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isCurrent" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isCurrent === '1' ? 'success' : 'info'">
              {{ scope.row.isCurrent === '1' ? '当前版本' : '历史版本' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleViewVersion(scope.row)">
              查看
            </el-button>
            <el-button size="small" link type="warning" @click="handleCompareVersion(scope.row)">
              对比
            </el-button>
            <el-button
              v-if="scope.row.isCurrent !== '1'"
              size="small"
              link
              type="success"
              @click="handleRollback(scope.row)"
            >
              回滚
            </el-button>
            <el-button
              v-if="scope.row.isCurrent !== '1'"
              size="small"
              link
              type="danger"
              @click="handleDeleteVersion(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 版本详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="版本详情" width="1000px">
      <el-descriptions :column="2" border v-if="currentVersion">
        <el-descriptions-item label="版本号">{{ currentVersion.versionNumber }}</el-descriptions-item>
        <el-descriptions-item label="版本名称">{{ currentVersion.versionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本描述" :span="2">
          {{ currentVersion.versionDescription || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="变更日志" :span="2">
          {{ currentVersion.changeLog || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentVersion.createdByName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentVersion.createTime }}</el-descriptions-item>
        <el-descriptions-item label="模板内容" :span="2">
          <pre class="version-content">{{ currentVersion.templateContent }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="模板变量" :span="2">
          <pre class="version-content">{{ currentVersion.templateVariables || '-' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 版本对比对话框 -->
    <el-dialog v-model="compareDialogVisible" title="版本对比" width="1200px">
      <div v-if="compareResult" class="version-compare">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>版本 {{ compareResult.version1.versionNumber }}</span>
              </template>
              <div class="version-content-panel">
                <div class="version-info">
                  <p><strong>版本名称:</strong> {{ compareResult.version1.versionName || '-' }}</p>
                  <p><strong>创建时间:</strong> {{ compareResult.version1.createTime }}</p>
                </div>
                <el-divider />
                <div class="content-section">
                  <h4>模板内容:</h4>
                  <pre class="version-content">{{ compareResult.version1.templateContent }}</pre>
                </div>
                <div class="content-section">
                  <h4>模板变量:</h4>
                  <pre class="version-content">{{ compareResult.version1.templateVariables || '-' }}</pre>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>版本 {{ compareResult.version2.versionNumber }}</span>
              </template>
              <div class="version-content-panel">
                <div class="version-info">
                  <p><strong>版本名称:</strong> {{ compareResult.version2.versionName || '-' }}</p>
                  <p><strong>创建时间:</strong> {{ compareResult.version2.createTime }}</p>
                </div>
                <el-divider />
                <div class="content-section">
                  <h4>模板内容:</h4>
                  <pre class="version-content">{{ compareResult.version2.templateContent }}</pre>
                </div>
                <div class="content-section">
                  <h4>模板变量:</h4>
                  <pre class="version-content">{{ compareResult.version2.templateVariables || '-' }}</pre>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-alert
          v-if="compareResult.diff"
          :title="`差异: ${compareResult.diff.contentChanged ? '内容已变更' : '内容未变更'}, ${compareResult.diff.variablesChanged ? '变量已变更' : '变量未变更'}`"
          type="info"
          style="margin-top: 20px"
        />
      </div>
    </el-dialog>

    <!-- 创建版本对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建新版本" width="800px">
      <el-form ref="createFormRef" :model="createForm" :rules="createFormRules" label-width="120px">
        <el-form-item label="版本名称">
          <el-input v-model="createForm.versionName" placeholder="请输入版本名称" />
        </el-form-item>
        <el-form-item label="版本描述">
          <el-input
            v-model="createForm.versionDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入版本描述"
          />
        </el-form-item>
        <el-form-item label="变更日志">
          <el-input
            v-model="createForm.changeLog"
            type="textarea"
            :rows="3"
            placeholder="请输入变更日志"
          />
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input
            v-model="createForm.templateContent"
            type="textarea"
            :rows="8"
            placeholder="请输入模板内容"
          />
        </el-form-item>
        <el-form-item label="模板变量">
          <el-input
            v-model="createForm.templateVariables"
            type="textarea"
            :rows="4"
            placeholder='请输入JSON格式的变量定义，例如：{"变量名": "变量说明"}'
          />
        </el-form-item>
        <el-form-item label="设为当前版本">
          <el-radio-group v-model="createForm.isCurrent">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleSubmitCreate">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- A/B测试管理对话框 -->
    <el-dialog
      v-model="abTestDialogVisible"
      title="A/B测试管理"
      width="1200px"
    >
      <AbTestManagement :template-id="templateId" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  promptTemplateVersionApi,
  type PromptTemplateVersion,
  type PromptTemplateVersionRequest,
  type VersionCompareResult
} from '@/api/promptTemplate'

import AbTestManagement from './AbTestManagement.vue'

interface Props {
  templateId: number
}

const props = defineProps<Props>()

// 响应式数据
const loading = ref(false)
const versions = ref<PromptTemplateVersion[]>([])

const viewDialogVisible = ref(false)
const compareDialogVisible = ref(false)
const createDialogVisible = ref(false)
const currentVersion = ref<PromptTemplateVersion | null>(null)
const compareResult = ref<VersionCompareResult | null>(null)
const compareVersion1 = ref<PromptTemplateVersion | null>(null)

const createFormRef = ref<FormInstance>()
const createLoading = ref(false)
const createForm = reactive<PromptTemplateVersionRequest>({
  versionName: '',
  versionDescription: '',
  changeLog: '',
  templateContent: '',
  templateVariables: '',
  isCurrent: '1'
})

const createFormRules: FormRules = {
  templateContent: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

// 方法
const loadVersions = async () => {
  loading.value = true
  try {
    const response = await promptTemplateVersionApi.getVersions(props.templateId)
    if (response.data) {
      versions.value = response.data
    }
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    loading.value = false
  }
}

const handleViewVersion = async (version: PromptTemplateVersion) => {
  try {
    const response = await promptTemplateVersionApi.getVersionById(props.templateId, version.id!)
    if (response.data) {
      currentVersion.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取版本详情失败:', error)
    ElMessage.error('获取版本详情失败')
  }
}

const handleCompareVersion = async (version: PromptTemplateVersion) => {
  try {
    // 获取当前版本
    const currentResponse = await promptTemplateVersionApi.getCurrentVersion(props.templateId)
    if (currentResponse.data) {
      compareVersion1.value = currentResponse.data
      
      // 对比两个版本
      const compareResponse = await promptTemplateVersionApi.compareVersions(
        props.templateId,
        compareVersion1.value.versionNumber,
        version.versionNumber
      )
      if (compareResponse.data) {
        compareResult.value = compareResponse.data
        compareDialogVisible.value = true
      }
    }
  } catch (error) {
    console.error('版本对比失败:', error)
    ElMessage.error('版本对比失败')
  }
}

const handleRollback = async (version: PromptTemplateVersion) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到版本 ${version.versionNumber} 吗？`,
      '确认回滚',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await promptTemplateVersionApi.rollbackToVersion(
      props.templateId,
      version.versionNumber
    )
    if (response.data) {
      ElMessage.success('回滚成功')
      loadVersions()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('回滚失败:', error)
      ElMessage.error(error.response?.data?.message || '回滚失败')
    }
  }
}

const handleShowAbTest = () => {
  abTestDialogVisible.value = true
}

const handleDeleteVersion = async (version: PromptTemplateVersion) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除版本 ${version.versionNumber} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await promptTemplateVersionApi.deleteVersion(props.templateId, version.id!)
    ElMessage.success('删除成功')
    loadVersions()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleCreateVersion = () => {
  // 重置表单
  createForm.versionName = ''
  createForm.versionDescription = ''
  createForm.changeLog = ''
  createForm.templateContent = ''
  createForm.templateVariables = ''
  createForm.isCurrent = '1'
  createDialogVisible.value = true
}

const handleSubmitCreate = async () => {
  if (!createFormRef.value) return

  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      createLoading.value = true
      try {
        await promptTemplateVersionApi.createVersion(props.templateId, createForm)
        ElMessage.success('创建版本成功')
        createDialogVisible.value = false
        loadVersions()
      } catch (error: any) {
        console.error('创建版本失败:', error)
        ElMessage.error(error.response?.data?.message || '创建版本失败')
      } finally {
        createLoading.value = false
      }
    }
  })
}

// 初始化
onMounted(() => {
  loadVersions()
})
</script>

<style scoped lang="scss">
.version-history {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .version-content {
    white-space: pre-wrap;
    word-wrap: break-word;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    max-height: 400px;
    overflow-y: auto;
    background: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
  }

  .version-compare {
    .version-content-panel {
      .version-info {
        margin-bottom: 16px;
        p {
          margin: 8px 0;
        }
      }

      .content-section {
        margin-top: 16px;

        h4 {
          margin: 0 0 8px 0;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
