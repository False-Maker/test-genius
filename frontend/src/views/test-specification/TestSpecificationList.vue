<template>


  <div class="test-specification-list">


    <div class="page-header">


      <div class="header-left">


        <h2 class="page-title">测试规约管理</h2>


        <p class="page-subtitle">管理测试规约、字段测试要点和逻辑测试要点</p>


      </div>


      <div class="header-right">


        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">


          <el-icon><Plus /></el-icon>


          新建规约


        </el-button>


      </div>


    </div>





    <!-- 搜索�?-->


    <el-card class="search-card" shadow="never">


      <el-form :inline="true" :model="searchForm" class="search-form">


        <el-form-item label="规约名称">


          <el-input v-model="searchForm.specName" placeholder="请输入规约名�" clearable @keyup.enter="handleSearch" />


        </el-form-item>


        <el-form-item label="规约类型">


          <el-select v-model="searchForm.specType" placeholder="请选择类型" clearable style="width: 150px">


            <el-option label="应用�" value="APPLICATION" />


            <el-option label="公共" value="PUBLIC" />


          </el-select>


        </el-form-item>


         <el-form-item label="状�">


          <el-select v-model="searchForm.isActive" placeholder="请选择状态�" clearable style="width: 150px">


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


        :data="specList"


        


        style="width: 100%"


      >


        <el-table-column prop="specCode" label="规约编码" width="160">


           <template #default="scope">


            <span class="code-text">{{ scope.row.specCode }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="specName" label="规约名称" min-width="200" show-overflow-tooltip />


        <el-table-column prop="specType" label="类型" width="120">


          <template #default="scope">


            <el-tag :type="scope.row.specType === 'PUBLIC' ? 'success' : 'primary'">


                {{ scope.row.specType === 'PUBLIC' ? '公共' : '应用'}}


            </el-tag>


          </template>


        </el-table-column>


        <el-table-column prop="specCategory" label="分类" width="120" show-overflow-tooltip />


        <el-table-column prop="currentVersion" label="当前版本" width="100">


             <template #default="scope">


                <el-tag effect="plain" type="info">{{ scope.row.currentVersion || 'V1.0' }}</el-tag>


             </template>


        </el-table-column>


        <el-table-column prop="isActive" label="状�" width="100">


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


        <el-table-column label="操作" width="200" fixed="right">


          <template #default="scope">


            <div class="action-buttons">


              <el-tooltip content="编辑" placement="top">


                <el-button circle size="small" type="primary" plain @click="handleEdit(scope.row)">


                  <el-icon><Edit /></el-icon>


                </el-button>


              </el-tooltip>


               <el-tooltip content="详情" placement="top">


                <el-button circle size="small" type="info" plain @click="handleDetail(scope.row)">


                  <el-icon><View /></el-icon>


                </el-button>


              </el-tooltip>


              <el-popconfirm


                title="确定删除此规约吗�"


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


      :title="dialogType === 'create' ? '新建测试规约' : '编辑测试规约'"


      width="800px"


      @close="handleDialogClose"


      top="5vh"


    >


      <el-form


        ref="formRef"


        :model="formData"


        :rules="formRules"


        label-width="120px"


      >


        <el-form-item label="规约名称" prop="specName">


          <el-input v-model="formData.specName" placeholder="请输入规约名�" />


        </el-form-item>


        


        <el-row :gutter="20">


             <el-col :span="12">


                <el-form-item label="规约类型" prop="specType">


                  <el-select v-model="formData.specType" placeholder="请选择" style="width: 100%">


                    <el-option label="应用�" value="APPLICATION" />


                    <el-option label="公共" value="PUBLIC" />


                  </el-select>


                </el-form-item>


             </el-col>


             <el-col :span="12">


                <el-form-item label="规约分类" prop="specCategory">


                  <el-input v-model="formData.specCategory" placeholder="请输入分�" />


                </el-form-item>


             </el-col>


        </el-row>





        <el-form-item label="适用模块" prop="applicableModules">


          <el-input v-model="formData.applicableModules" placeholder="多个模块用逗号分隔" />


        </el-form-item>


        


        <el-form-item label="适用测试分层" prop="applicableLayers">


           <el-input v-model="formData.applicableLayers" placeholder="多个分层用逗号分隔" />


        </el-form-item>


        


        <el-form-item label="适用测试方法" prop="applicableMethods">


           <el-input v-model="formData.applicableMethods" placeholder="多个方法用逗号分隔" />


        </el-form-item>





        <el-form-item label="规约内容" prop="specContent">


           <el-input 


             v-model="formData.specContent" 


             type="textarea" 


             :rows="6" 


             placeholder="请输入规约内容(JSON格式)" 


            />


        </el-form-item>





        <el-form-item label="规约描述" prop="specDescription">


           <el-input v-model="formData.specDescription" type="textarea" :rows="3" placeholder="请输入描述" />


        </el-form-item>


        


        <el-row :gutter="20">


             <el-col :span="12">


                 <el-form-item label="生效日期" prop="effectiveDate">


                    <el-date-picker


                        v-model="formData.effectiveDate"


                        type="date"


                        placeholder="选择日期"


                        value-format="YYYY-MM-DD"


                        style="width: 100%"


                    />


                </el-form-item>


             </el-col>


             <el-col :span="12">


                <el-form-item label="失效日期" prop="expireDate">


                    <el-date-picker


                        v-model="formData.expireDate"


                        type="date"


                        placeholder="选择日期"


                        value-format="YYYY-MM-DD"


                         style="width: 100%"


                    />


                </el-form-item>


             </el-col>


        </el-row>





      </el-form>


      <template #footer>


        <el-button @click="dialogVisible = false">取消</el-button>


        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">


          确定


        </el-button>


      </template>


    </el-dialog>


    


    <!-- Detail Dialog (Simplified for now) -->


    <el-dialog


        v-model="detailVisible"


        title="规约详情"


        width="800px"


    >


        <el-descriptions :column="2" border>


            <el-descriptions-item label="规约编码">{{ currentDetail?.specCode }}</el-descriptions-item>


            <el-descriptions-item label="规约名称">{{ currentDetail?.specName }}</el-descriptions-item>


            <el-descriptions-item label="类型">{{ currentDetail?.specType }}</el-descriptions-item>


            <el-descriptions-item label="分类">{{ currentDetail?.specCategory }}</el-descriptions-item>


             <el-descriptions-item label="适用模块" :span="2">{{ currentDetail?.applicableModules }}</el-descriptions-item>


             <el-descriptions-item label="适用分层" :span="2">{{ currentDetail?.applicableLayers }}</el-descriptions-item>


        </el-descriptions>


        <div style="margin-top: 20px;">


            <h4>规约内容</h4>


            <pre style="background: rgba(255, 255, 255, 0.05); padding: 10px; border-radius: 4px; overflow: auto; max-height: 300px;">{{ currentDetail?.specContent }}</pre>


        </div>


         <div style="margin-top: 20px;">


            <h4>描述</h4>


            <p>{{ currentDetail?.specDescription }}</p>


        </div>


        <template #footer>


             <el-button @click="detailVisible = false">关闭</el-button>


        </template>


    </el-dialog>





  </div>


</template>





<script setup lang="ts">


import { ref, reactive, onMounted } from 'vue'


import { ElMessage, type FormInstance, type FormRules } from 'element-plus'


import { Plus, Search, Refresh, Edit, Delete, View } from '@element-plus/icons-vue'


import { testSpecificationApi, type TestSpecificationRequestDTO, type TestSpecificationResponseDTO } from '@/api/testSpecification'





// 响应式数据
const loading = ref(false)
const specList = ref<TestSpecificationResponseDTO[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const searchForm = reactive({
  specName: '',
  specType: '',
  isActive: ''
})
const formData = reactive<Partial<TestSpecificationRequestDTO>>({
  specName: '',
  specType: 'APPLICATION',
  specCategory: '',
  specDescription: '',
  specContent: '',
  applicableModules: '',
  applicableLayers: '',
  applicableMethods: '',
  effectiveDate: '',
  expireDate: ''
})
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const currentId = ref<number | undefined>(undefined)
const currentDetail = ref<TestSpecificationResponseDTO | null>(null)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)





// 响应式数据表单验证规则


const formRules: FormRules = {


  specName: [


    { required: true, message: '请输入规约名称', trigger: 'blur' }


  ],


  specType: [


      { required: true, message: '请选择规约类型', trigger: 'change' }


  ]


}





// Actions


const loadSpecList = async () => {


  loading.value = true


  try {


    const response = await testSpecificationApi.getSpecificationList(


      pagination.page - 1, 


      pagination.size,


      searchForm.specName || undefined,


      searchForm.specType || undefined,


      searchForm.isActive || undefined


    )


    if (response.data) {


      specList.value = response.data.content || []


      pagination.total = response.data.totalElements || 0


    }


  } catch (error) {


    console.error('Failed to load specification list', error)


    ElMessage.error('加载规约列表失败')


  } finally {


    loading.value = false


  }


}





const handleSearch = () => {


    pagination.page = 1


    loadSpecList()


}





const resetSearch = () => {


    searchForm.specName = ''


    searchForm.specType = ''


    searchForm.isActive = ''


    handleSearch()


}





const handleSizeChange = (size: number) => {


  pagination.size = size


  pagination.page = 1


  loadSpecList()


}





const handlePageChange = (page: number) => {


  pagination.page = page


  loadSpecList()


}





const handleCreate = () => {


  dialogType.value = 'create'


  resetForm()


  dialogVisible.value = true


}





const handleEdit = (row: TestSpecificationResponseDTO) => {


  dialogType.value = 'edit'


  currentId.value = row.id


  Object.assign(formData, {


      specName: row.specName,


      specType: row.specType,


      specCategory: row.specCategory,


      specDescription: row.specDescription,


      specContent: row.specContent,


      applicableModules: row.applicableModules,


      applicableLayers: row.applicableLayers,


      applicableMethods: row.applicableMethods,


      effectiveDate: row.effectiveDate,


      expireDate: row.expireDate


  })


  dialogVisible.value = true


}





const handleDetail = (row: TestSpecificationResponseDTO) => {


    currentDetail.value = row


    detailVisible.value = true


}





const handleDelete = async (row: TestSpecificationResponseDTO) => {


    try {


        await testSpecificationApi.deleteSpecification(row.id)


        ElMessage.success('删除成功')


        if (specList.value.length === 1 && pagination.page > 1) {


            pagination.page--


        }


        loadSpecList()


    } catch(e) {


        console.error(e)


    }


}





const handleStatusChange = async (row: TestSpecificationResponseDTO, val: string) => {


    try {


        await testSpecificationApi.updateSpecificationStatus(row.id, val)


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


                    await testSpecificationApi.createSpecification(formData)


                    ElMessage.success('创建成功')


                } else if (currentId.value) {


                    await testSpecificationApi.updateSpecification(currentId.value, formData)


                    ElMessage.success('更新成功')


                }


                dialogVisible.value = false


                loadSpecList()


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


    formData.specName = ''


    formData.specType = 'APPLICATION'


    formData.specCategory = ''


    formData.specDescription = ''


    formData.specContent = ''


    formData.applicableModules = ''


    formData.applicableLayers = ''


    formData.applicableMethods = ''


    formData.effectiveDate = ''


    formData.expireDate = ''


    currentId.value = undefined


}





onMounted(() => {


    loadSpecList()


})





</script>





<style scoped lang="scss">


.test-specification-list {


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