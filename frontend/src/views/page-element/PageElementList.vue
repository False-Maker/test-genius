<template>
  <div class="page-element-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">页面元素管理</h2>
        <p class="page-subtitle">管理测试对象的页面元素信息，包括定位方式、属性等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">
          <el-icon><Plus /></el-icon>
          新建元素
        </el-button>
      </div>
    </div>

    <!-- 搜索�?-->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="页面URL">
          <el-input v-model="searchForm.pageUrl" placeholder="请输入页面URL" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="元素类型">
          <el-select v-model="searchForm.elementType" placeholder="请选择元素类型" clearable style="width: 180px">
            <el-option label="按钮 (Button)" value="BUTTON" />
            <el-option label="输入框(Input)" value="INPUT" />
            <el-option label="链接 (Link)" value="LINK" />
            <el-option label="文本 (Text)" value="TEXT" />
            <el-option label="下拉框(Select)" value="SELECT" />
            <el-option label="复选框 (Checkbox)" value="CHECKBOX" />
            <el-option label="单选框 (Radio)" value="RADIO" />
            <el-option label="其他 (Other)" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <div v-if="loading" class="skeleton-container" style="padding: 20px;">
        <el-skeleton :rows="10" animated />
      </div>
      <el-table
        v-else
        :data="elementList"
        
        style="width: 100%"
      >
        <el-table-column prop="elementCode" label="元素编码" width="160">
           <template #default="scope">
            <span class="code-text">{{ scope.row.elementCode }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="pageUrl" label="页面URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="elementType" label="元素类型" width="120">
          <template #default="scope">
            <el-tag>{{ scope.row.elementType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="elementText" label="元素文本" min-width="150" show-overflow-tooltip />
        <el-table-column prop="elementLocatorType" label="定位方式" width="120" />
        <el-table-column prop="elementLocatorValue" label="定位�" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" type="primary" plain @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                title="确定删除此元素吗�"
                confirm-button-text="删除"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="handleDelete(scope.row)"
              >
                <template #reference>
                  <div class="delete-btn-wrapper">
                    <el-tooltip content="删除" placement="top">
                      <el-button circle size="small" type="danger" plain>
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </template>
              </el-popconfirm>
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

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新建页面元素' : '编辑页面元素'"
      width="650px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="页面URL" prop="pageUrl">
          <el-input v-model="formData.pageUrl" placeholder="请输入页面URL" />
        </el-form-item>
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="元素类型" prop="elementType">
                  <el-select v-model="formData.elementType" placeholder="请选择" style="width: 100%">
                    <el-option label="按钮 (Button)" value="BUTTON" />
                    <el-option label="输入框(Input)" value="INPUT" />
                    <el-option label="链接 (Link)" value="LINK" />
                    <el-option label="文本 (Text)" value="TEXT" />
                    <el-option label="下拉框(Select)" value="SELECT" />
                    <el-option label="复选框 (Checkbox)" value="CHECKBOX" />
                    <el-option label="单选框 (Radio)" value="RADIO" />
                    <el-option label="其他 (Other)" value="OTHER" />
                  </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                 <el-form-item label="元素文本" prop="elementText">
                  <el-input v-model="formData.elementText" placeholder="请输入元素文本" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row :gutter="20">
             <el-col :span="12">
                <el-form-item label="定位方式" prop="elementLocatorType">
                  <el-select v-model="formData.elementLocatorType" placeholder="请选择" style="width: 100%">
                    <el-option label="ID" value="ID" />
                    <el-option label="Name" value="NAME" />
                    <el-option label="Class Name" value="CLASS_NAME" />
                    <el-option label="Tag Name" value="TAG_NAME" />
                    <el-option label="Link Text" value="LINK_TEXT" />
                    <el-option label="Partial Link Text" value="PARTIAL_LINK_TEXT" />
                    <el-option label="CSS Selector" value="CSS_SELECTOR" />
                    <el-option label="XPath" value="XPATH" />
                  </el-select>
                </el-form-item>
             </el-col>
             <el-col :span="12">
                  <el-form-item label="定位值" prop="elementLocatorValue">
                    <el-input v-model="formData.elementLocatorValue" placeholder="请输入定位值" />
                  </el-form-item>
             </el-col>
        </el-row>
        
        <el-form-item label="元素属性" prop="elementAttributes">
           <el-input 
             v-model="formData.elementAttributes" 
             type="textarea" 
             :rows="4" 
             placeholder="请输入元素属性(JSON格式)" 
            />
        </el-form-item>
         <el-form-item label="页面结构" prop="pageStructure">
           <el-input 
             v-model="formData.pageStructure" 
             type="textarea" 
             :rows="4" 
             placeholder="请输入页面结构(JSON格式)" 
            />
        </el-form-item>
        <el-form-item label="截图URL" prop="screenshotUrl">
           <el-input v-model="formData.screenshotUrl" placeholder="请输入截图URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { pageElementApi, type PageElementInfoRequestDTO, type PageElementInfoResponseDTO } from '@/api/pageElement'
import type { PageResult } from '@/api/types'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const elementList = ref<PageElementInfoResponseDTO[]>([])
const currentId = ref<number | undefined>(undefined)

const formData = reactive<PageElementInfoRequestDTO>({
  pageUrl: '',
  elementType: '',
  elementLocatorType: 'XPATH',
  elementLocatorValue: '',
  elementText: '',
  elementAttributes: '',
  pageStructure: '',
  screenshotUrl: ''
})

const searchForm = reactive({
  pageUrl: '',
  elementType: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单验证规则
const formRules: FormRules = {
  pageUrl: [
    { required: true, message: '请输入页面URL', trigger: 'blur' }
  ],
  elementLocatorType: [
      { required: true, message: '请选择定位方式', trigger: 'change' }
  ],
  elementLocatorValue: [
      { required: true, message: '请输入定位值', trigger: 'blur' }
  ]
}

// Actions
const loadElementList = async () => {
  loading.value = true
  try {
    const response = await pageElementApi.getPageElementList(
      pagination.page - 1, 
      pagination.size,
      searchForm.pageUrl || undefined,
      searchForm.elementType || undefined
    )
    if (response.data) {
      elementList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load element list', error)
    ElMessage.error('加载元素列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
    pagination.page = 1
    loadElementList()
}

const resetSearch = () => {
    searchForm.pageUrl = ''
    searchForm.elementType = ''
    handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadElementList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadElementList()
}

const handleCreate = () => {
  dialogType.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: PageElementInfoResponseDTO) => {
  dialogType.value = 'edit'
  currentId.value = row.id
  Object.assign(formData, {
      pageUrl: row.pageUrl,
      elementType: row.elementType,
      elementLocatorType: row.elementLocatorType,
      elementLocatorValue: row.elementLocatorValue,
      elementText: row.elementText,
      elementAttributes: row.elementAttributes,
      pageStructure: row.pageStructure,
      screenshotUrl: row.screenshotUrl
  })
  dialogVisible.value = true
}

const handleDelete = async (row: PageElementInfoResponseDTO) => {
    try {
        await pageElementApi.deletePageElement(row.id)
        ElMessage.success('删除成功')
        if (elementList.value.length === 1 && pagination.page > 1) {
            pagination.page--
        }
        loadElementList()
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
                if (dialogType.value === 'create') {
                    await pageElementApi.createPageElement(formData)
                    ElMessage.success('创建人成功')
                } else if (currentId.value) {
                    await pageElementApi.updatePageElement(currentId.value, formData)
                    ElMessage.success('更新成功')
                }
                dialogVisible.value = false
                loadElementList()
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
  formData.pageUrl = ''
  formData.elementType = ''
  formData.elementLocatorType = 'XPATH'
  formData.elementLocatorValue = ''
  formData.elementText = ''
  formData.elementAttributes = ''
  formData.pageStructure = ''
  formData.screenshotUrl = ''
  currentId.value = undefined
}

onMounted(() => {
    loadElementList()
})

</script>

<style scoped lang="scss">
.page-element-list {
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
    margin-bottom: 24px;
    border: none;
    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      .el-form-item {
        margin-bottom: 0;
        margin-right: 16px;
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
    .action-buttons {
      display: flex;
      gap: 8px;
       .delete-btn-wrapper {
        display: inline-block;
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