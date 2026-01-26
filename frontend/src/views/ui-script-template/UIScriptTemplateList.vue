<template>


  <div class="ui-script-template-list">


    <div class="page-header">


      <div class="header-left">


        <h2 class="page-title">UI脚本模板管理</h2>


        <p class="page-subtitle">管理UI自动化测试脚本的生成模板，支持多种框架和语言</p>


      </div>


      <div class="header-right">


        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">


          <el-icon><Plus /></el-icon>


          新建模板


        </el-button>


      </div>


    </div>





    <!-- 搜索�?-->


    <el-card class="search-card" shadow="never">


      <el-form :inline="true" :model="searchForm" class="search-form">


        <el-form-item label="模板名称称">


          <el-input v-model="searchForm.templateName" placeholder="请输入模板名称" clearable @keyup.enter="handleSearch" />


        </el-form-item>


        <el-form-item label="模板类型">


          <el-select v-model="searchForm.templateType" placeholder="请选择类型" clearable style="width: 150px">


            <el-option label="Selenium" value="SELENIUM" />


            <el-option label="Playwright" value="PLAYWRIGHT" />


            <el-option label="Puppeteer" value="PUPPETEER" />


          </el-select>


        </el-form-item>


        <el-form-item label="脚本语言">


          <el-select v-model="searchForm.scriptLanguage" placeholder="请选择语言" clearable style="width: 150px">


            <el-option label="Java" value="JAVA" />


            <el-option label="Python" value="PYTHON" />


            <el-option label="JavaScript" value="JAVASCRIPT" />


          </el-select>


        </el-form-item>


        <el-form-item label="状态�">


          <el-select v-model="searchForm.isActive" placeholder="请选择状态态�" clearable style="width: 150px">


            <el-option label="启用" value="1" />


            <el-option label="禁用" value="0" />


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


        :data="templateList"


        


        style="width: 100%"


      >


        <el-table-column prop="templateCode" label="模板编码" width="160">


           <template #default="scope">


            <span class="code-text">{{ scope.row.templateCode }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="templateName" label="模板名称称" min-width="200" show-overflow-tooltip />


        <el-table-column prop="templateType" label="类型" width="120">


          <template #default="scope">


            <el-tag effect="light">{{ scope.row.templateType }}</el-tag>


          </template>


        </el-table-column>


         <el-table-column prop="scriptLanguage" label="语言" width="120">


          <template #default="scope">


            <el-tag type="info" effect="plain">{{ scope.row.scriptLanguage }}</el-tag>


          </template>


        </el-table-column>


        <el-table-column prop="isActive" label="状态�" width="100">


          <template #default="scope">


            <el-switch


              v-model="scope.row.isActive"


              active-value="1"


              inactive-value="0"


              @change="(val) => handleStatusChange(scope.row, val as string)"


            />


          </template>


        </el-table-column>


        <el-table-column prop="updateTime" label="更新时间" width="180" />


        <el-table-column label="操作" width="150" fixed="right">


          <template #default="scope">


            <div class="action-buttons">


              <el-tooltip content="编辑" placement="top">


                <el-button circle size="small" type="primary" plain @click="handleEdit(scope.row)">


                  <el-icon><Edit /></el-icon>


                </el-button>


              </el-tooltip>


              <el-popconfirm


                title="确定删除此模板吗�"


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


      :title="dialogType === 'create' ? '新建脚本模板' : '编辑脚本模板'"


      width="800px"


      @close="handleDialogClose"


      top="5vh"


    >


      <el-form


        ref="formRef"


        :model="formData"


        :rules="formRules"


        label-width="100px"


      >


        <el-row :gutter="20">


            <el-col :span="24">


                <el-form-item label="模板名称" prop="templateName">


                  <el-input v-model="formData.templateName" placeholder="请输入模板名称" />


                </el-form-item>


            </el-col>


        </el-row>


        <el-row :gutter="20">


            <el-col :span="12">


                <el-form-item label="模板类型" prop="templateType">


                  <el-select v-model="formData.templateType" placeholder="请选择类型" style="width: 100%">


                    <el-option label="Selenium" value="SELENIUM" />


                    <el-option label="Playwright" value="PLAYWRIGHT" />


                    <el-option label="Puppeteer" value="PUPPETEER" />


                  </el-select>


                </el-form-item>


            </el-col>


            <el-col :span="12">


                 <el-form-item label="脚本语言" prop="scriptLanguage">


                  <el-select v-model="formData.scriptLanguage" placeholder="请选择语言" style="width: 100%">


                    <el-option label="Java" value="JAVA" />


                    <el-option label="Python" value="PYTHON" />


                    <el-option label="JavaScript" value="JAVASCRIPT" />


                  </el-select>


                </el-form-item>


            </el-col>


        </el-row>


        


        <el-form-item label="适用场景" prop="applicableScenarios">


           <el-input v-model="formData.applicableScenarios" type="textarea" :rows="2" placeholder="请输入适用场景描述" />


        </el-form-item>





        <el-form-item label="模板内容" prop="templateContent">


           <el-input 


             v-model="formData.templateContent" 


             type="textarea" 


             :rows="10" 


             placeholder="请输入模板内容"


             class="code-input"


            />


        </el-form-item>





        <el-form-item label="变量定义" prop="templateVariables">


           <el-input 


             v-model="formData.templateVariables" 


             type="textarea" 


             :rows="4" 


             placeholder="请输入变量定义(JSON格式)" 


            />


        </el-form-item>


        


         <el-form-item label="模板描述" prop="templateDescription">


           <el-input v-model="formData.templateDescription" type="textarea" :rows="2" placeholder="请输入模板描述" />


        </el-form-item>


        


        <el-form-item label="启用状态" prop="isActive">


            <el-switch v-model="formData.isActive" active-value="1" inactive-value="0" />


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


import { uiScriptTemplateApi, type UIScriptTemplateRequestDTO, type UIScriptTemplateResponseDTO } from '@/api/uiScriptTemplate'





// 响应式数据
const loading = ref(false)
const templateList = ref<UIScriptTemplateResponseDTO[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const searchForm = reactive({
  templateName: '',
  templateType: '',
  scriptLanguage: '',
  isActive: ''
})
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const formData = reactive<UIScriptTemplateRequestDTO>({
  templateName: '',
  templateType: 'SELENIUM',
  scriptLanguage: 'JAVA',
  templateContent: '',
  templateVariables: '',
  applicableScenarios: '',
  templateDescription: '',
  isActive: '1'
})
const submitLoading = ref(false)
const currentId = ref<number | undefined>(undefined)

// 响应式数据表单验证规则


const formRules: FormRules = {


  templateName: [


    { required: true, message: '请输入模板名称', trigger: 'blur' }


  ],


  templateType: [


      { required: true, message: '请选择模板类型', trigger: 'change' }


  ],


  scriptLanguage: [


      { required: true, message: '请选择脚本语言', trigger: 'change' }


  ],


  templateContent: [


      { required: true, message: '请输入模板内容', trigger: 'blur' }


  ]


}





// Actions


const loadTemplateList = async () => {


  loading.value = true


  try {


    const response = await uiScriptTemplateApi.getTemplateList(


      pagination.page - 1, 


      pagination.size,


      searchForm.templateName || undefined,


      searchForm.templateType || undefined,


      searchForm.scriptLanguage || undefined,


      searchForm.isActive || undefined


    )


    if (response.data) {


      templateList.value = response.data.content || []


      pagination.total = response.data.totalElements || 0


    }


  } catch (error) {


    console.error('Failed to load template list', error)


    ElMessage.error('加载模板列表失败')


  } finally {


    loading.value = false


  }


}





const handleSearch = () => {


    pagination.page = 1


    loadTemplateList()


}





const resetSearch = () => {


    searchForm.templateName = ''


    searchForm.templateType = ''


    searchForm.scriptLanguage = ''


    searchForm.isActive = ''


    handleSearch()


}





const handleSizeChange = (size: number) => {


  pagination.size = size


  pagination.page = 1


  loadTemplateList()


}





const handlePageChange = (page: number) => {


  pagination.page = page


  loadTemplateList()


}





const handleCreate = () => {


  dialogType.value = 'create'


  resetForm()


  dialogVisible.value = true


}





const handleEdit = (row: UIScriptTemplateResponseDTO) => {


  dialogType.value = 'edit'


  currentId.value = row.id


  Object.assign(formData, {


      templateName: row.templateName,


      templateType: row.templateType,


      scriptLanguage: row.scriptLanguage,


      templateContent: row.templateContent,


      templateVariables: row.templateVariables,


      applicableScenarios: row.applicableScenarios,


      templateDescription: row.templateDescription,


      isActive: row.isActive


  })


  dialogVisible.value = true


}





const handleDelete = async (row: UIScriptTemplateResponseDTO) => {


    try {


        await uiScriptTemplateApi.deleteTemplate(row.id)


        ElMessage.success('删除成功')


        if (templateList.value.length === 1 && pagination.page > 1) {


            pagination.page--


        }


        loadTemplateList()


    } catch(e) {


        console.error(e)


    }


}





const handleStatusChange = async (row: UIScriptTemplateResponseDTO, val: string) => {


    try {


        await uiScriptTemplateApi.updateTemplateStatus(row.id, val)


        ElMessage.success('状态更新成功')


    } catch(e) {


        console.error(e)


        row.isActive = val === '1' ? '0' : '1' // Revert on error


    }


}





const handleSubmit = async () => {


    if (!formRef.value) return


    await formRef.value.validate(async (valid) => {


        if (valid) {


            submitLoading.value = true


            try {


                if (dialogType.value === 'create') {


                    await uiScriptTemplateApi.createTemplate(formData)


                    ElMessage.success('创建成功')


                } else if (currentId.value) {


                    await uiScriptTemplateApi.updateTemplate(currentId.value, formData)


                    ElMessage.success('更新成功')


                }


                dialogVisible.value = false


                loadTemplateList()


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


    formData.templateName = ''


    formData.templateType = 'SELENIUM'


    formData.scriptLanguage = 'JAVA'


    formData.templateContent = ''


    formData.templateVariables = ''


    formData.applicableScenarios = ''


    formData.templateDescription = ''


    formData.isActive = '1'


    currentId.value = undefined


}





onMounted(() => {


    loadTemplateList()


})





</script>





<style scoped lang="scss">


.ui-script-template-list {


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