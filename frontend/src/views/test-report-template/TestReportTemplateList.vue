<template>


  <div class="test-report-template-list">


    <div class="page-header">


      <div class="header-left">


        <h2 class="page-title">报告模板管理</h2>


        <p class="page-subtitle">管理测试报告的生成模�</p>


      </div>


      <div class="header-right">


        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">


          <el-icon><Plus /></el-icon>


          新建模板


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


        :data="templateList"


        


        style="width: 100%"


      >


        <el-table-column prop="templateCode" label="模板编码" width="160">


           <template #default="scope">


            <span class="code-text">{{ scope.row.templateCode }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="templateName" label="模板名称" min-width="200" show-overflow-tooltip>


          <template #default="scope">


            <span class="name-text" @click="handleView(scope.row)">{{ scope.row.templateName }}</span>


            <el-tag v-if="scope.row.isDefault === '1'" size="small" type="success" effect="plain" style="margin-left: 8px">默认</el-tag>


          </template>


        </el-table-column>


        <el-table-column prop="templateType" label="类型" width="120">


          <template #default="scope">


            <el-tag effect="light" round>{{ getReportTypeText(scope.row.templateType) }}</el-tag>


          </template>


        </el-table-column>


        <el-table-column prop="isActive" label="状�" width="100">


          <template #default="scope">


            <el-switch


                v-model="scope.row.isActive"


                active-value="1"


                inactive-value="0"


                @change="handleStatusChange(scope.row)"


            />


          </template>


        </el-table-column>


        <el-table-column prop="updateTime" label="更新时间" width="180" />


        <el-table-column label="操作" width="200" fixed="right">


          <template #default="scope">


            <div class="action-buttons">


              <el-tooltip content="编辑" placement="top">


                <el-button circle size="small" type="primary" plain @click="handleEdit(scope.row)">


                  <el-icon><Edit /></el-icon>


                </el-button>


              </el-tooltip>


              


              <el-tooltip content="设为默认" placement="top" v-if="scope.row.isDefault === '0' && scope.row.isActive === '1'">


                <el-button circle size="small" type="warning" plain @click="handleSetDefault(scope.row)">


                  <el-icon><Star /></el-icon>


                </el-button>


              </el-tooltip>





              <el-tooltip content="删除" placement="top">


                <el-button circle size="small" type="danger" plain @click="handleDelete(scope.row)">


                  <el-icon><Delete /></el-icon>


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





    <!-- 创建人/编辑对话�?-->


    <el-dialog


      v-model="dialogVisible"


      :title="dialogTitle"


      width="800px"


      @close="handleDialogClose"


    >


      <el-form


        ref="formRef"


        :model="formData"


        :rules="formRules"


        label-width="100px"


      >


        <el-form-item label="模板名称" prop="templateName">


          <el-input v-model="formData.templateName" placeholder="请输入模板名称" maxlength="100" show-word-limit />


        </el-form-item>


        <el-form-item label="模板类型" prop="templateType">


          <el-select v-model="formData.templateType" placeholder="请选择模板类型" style="width: 100%">


            <el-option label="执行报告" value="EXECUTION" />


            <el-option label="覆盖率报告" value="COVERAGE" />


            <el-option label="质量报告" value="QUALITY" />


            <el-option label="风险报告" value="RISK" />


          </el-select>


        </el-form-item>


        <el-form-item label="文件格式" prop="fileFormat">


             <el-select v-model="formData.fileFormat" placeholder="请选择文件格式" style="width: 100%">


                <el-option label="Word" value="WORD" />


                <el-option label="PDF" value="PDF" />


                <el-option label="Excel" value="EXCEL" />


             </el-select>


        </el-form-item>


        <el-form-item label="模板描述" prop="templateDescription">


          <el-input v-model="formData.templateDescription" type="textarea" :rows="2" placeholder="请输入模板描述" />


        </el-form-item>


        <el-form-item label="模板内容" prop="templateContent">


           <el-input 


               v-model="formData.templateContent" 


               type="textarea" 


               :rows="10" 


               placeholder="请输入模板内容（JSON格式）"


               style="font-family: monospace;"


           />


        </el-form-item>


        <el-form-item label="默认模板">


            <el-switch v-model="formData.isDefault" active-value="1" inactive-value="0" />


        </el-form-item>


         <el-form-item label="启用状态">


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





    <!-- 查看详情对话框-->


    <el-dialog


      v-model="viewDialogVisible"


      title="模板详情"


      width="800px"


    >


      <el-descriptions :column="2" border>


        <el-descriptions-item label="模板编码">{{ viewData.templateCode }}</el-descriptions-item>


        <el-descriptions-item label="模板名称">{{ viewData.templateName }}</el-descriptions-item>


         <el-descriptions-item label="模板类型">{{ getReportTypeText(viewData.templateType) }}</el-descriptions-item>


         <el-descriptions-item label="文件格式">{{ viewData.fileFormat }}</el-descriptions-item>


        <el-descriptions-item label="状态">{{ viewData.isActive === '1' ? '启用' : '禁用' }}</el-descriptions-item>


        <el-descriptions-item label="默认模板">{{ viewData.isDefault === '1' ? '是' : '否' }}</el-descriptions-item>


         <el-descriptions-item label="模板描述" :span="2">{{ viewData.templateDescription || '-' }}</el-descriptions-item>


         <el-descriptions-item label="模板内容" :span="2">


             <div class="json-content">


                {{ viewData.templateContent }}


             </div>


        </el-descriptions-item>


      </el-descriptions>


    </el-dialog>


  </div>


</template>





<script setup lang="ts">


import { ref, reactive, computed, onMounted } from 'vue'


import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'


import { Plus, Edit, Delete, Star } from '@element-plus/icons-vue'


import { testReportTemplateApi, type TestReportTemplateRequestDTO, type TestReportTemplateResponseDTO } from '@/api/testReportTemplate'





// 响应式数据


const loading = ref(false)


const templateList = ref<TestReportTemplateResponseDTO[]>([])


const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})


const dialogVisible = ref(false)


const viewDialogVisible = ref(false)


const isEdit = ref(false)


const formRef = ref<FormInstance>()


const formData = reactive<Partial<TestReportTemplateRequestDTO>>({
  templateName: '',
  templateType: 'EXECUTION',
  templateContent: '{}',
  fileFormat: 'WORD',
  templateDescription: '',
  isActive: '1',
  isDefault: '0'
})


const submitLoading = ref(false)


const viewData = ref<TestReportTemplateResponseDTO>({} as TestReportTemplateResponseDTO)





// 响应式数据表单验证规则


const formRules: FormRules = {


  templateName: [


    { required: true, message: '请输入模板名称', trigger: 'blur' }


  ],


  templateType: [


    { required: true, message: '请选择模板类型', trigger: 'change' }


  ],


   templateContent: [


    { required: true, message: '请输入模板内容', trigger: 'blur' }


  ]


}





const dialogTitle = computed(() => (isEdit.value ? '编辑模板' : '新建模板'))





// Helpers


const getReportTypeText = (type: string) => {


    const map: Record<string, string> = {


        EXECUTION: '执行报告',


        COVERAGE: '覆盖率报告',


        QUALITY: '质量报告',


        RISK: '风险报告'


    }


    return map[type] || type


}





// Actions


const loadTemplateList = async () => {


  loading.value = true


  try {


    const response = await testReportTemplateApi.getTemplateList(pagination.page - 1, pagination.size)


    if (response.data) {


      templateList.value = response.data.content || []


      pagination.total = response.data.totalElements || 0


    }


  } catch (error) {


    console.error('Failed to load templates', error)


    ElMessage.error('加载模板列表失败')


  } finally {


    loading.value = false


  }


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


  isEdit.value = false


  resetForm()


  dialogVisible.value = true


}





const handleEdit = (row: TestReportTemplateResponseDTO) => {


  isEdit.value = true


  Object.assign(formData, row)


  // Store ID for update

  ;(formData as any).id = row.id

  dialogVisible.value = true


}





const handleView = async (row: TestReportTemplateResponseDTO) => {


    try {


        const res = await testReportTemplateApi.getTemplateById(row.id)


        viewData.value = res


        viewDialogVisible.value = true


    } catch(e) {


        console.error(e)


    }


}





const handleDelete = async (row: TestReportTemplateResponseDTO) => {


    try {


        await ElMessageBox.confirm('确定要删除该模板吗？', '提示', { type: 'warning' })


        await testReportTemplateApi.deleteTemplate(row.id)


        ElMessage.success('删除成功')


        loadTemplateList()


    } catch (e) {


        // cancel


    }


}





const handleStatusChange = async (row: TestReportTemplateResponseDTO) => {


    try {


         await testReportTemplateApi.toggleTemplateStatus(row.id, row.isActive)


         ElMessage.success('状态已更新')


    } catch (e) {


        // revert


        row.isActive = row.isActive === '1' ? '0' : '1'


        console.error(e)


    }


}





const handleSetDefault = async (row: TestReportTemplateResponseDTO) => {


    try {


         await testReportTemplateApi.setDefaultTemplate(row.id, row.templateType)


         ElMessage.success('已设为默认')


         loadTemplateList()


    } catch (e) {


        console.error(e)


    }


}





const handleSubmit = async () => {


    if (!formRef.value) return


    await formRef.value.validate(async (valid) => {


        if (valid) {


            submitLoading.value = true


            try {


                if (isEdit.value) {


                     await testReportTemplateApi.updateTemplate((formData as any).id, formData)


                     ElMessage.success('更新成功')


                } else {


                     await testReportTemplateApi.createTemplate(formData)


                     ElMessage.success('创建成功')


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


    Object.assign(formData, {


        templateName: '',


        templateType: 'EXECUTION',


        templateContent: '{}',


        fileFormat: 'WORD',


        templateDescription: '',


        isActive: '1',


        isDefault: '0'


    })


    delete (formData as any).id


}





onMounted(() => {


    loadTemplateList()


})





</script>





<style scoped lang="scss">


.test-report-template-list {


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