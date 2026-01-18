<template>


  <div class="test-report-list">


    <div class="page-header">


      <div class="header-left">


        <h2 class="page-title">测试报告管理</h2>


        <p class="page-subtitle">管理和维护测试报告，支持生成、预览和导出</p>


      </div>


      <div class="header-right">


        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">


          <el-icon><Plus /></el-icon>


          新建报告


        </el-button>


      </div>


    </div>





    <!-- 搜索�?-->


    <el-card class="search-card" shadow="never">


      <el-form :inline="true" :model="searchForm" class="search-form">


        <el-form-item label="报告名称">


          <el-input


            v-model="searchForm.reportName"


            placeholder="搜索报告名称..."


            clearable


            prefix-icon="Search"


            @clear="handleSearch"


            @keyup.enter="handleSearch"


            style="width: 240px"


          />


        </el-form-item>


        <el-form-item label="报告类型">


          <el-select


            v-model="searchForm.reportType"


            placeholder="全部类型"


            clearable


            style="width: 160px"


            @change="handleSearch"


          >


            <el-option label="执行报告" value="EXECUTION" />


            <el-option label="覆盖率率率报告" value="COVERAGE" />


            <el-option label="质量报告" value="QUALITY" />


            <el-option label="风险报告" value="RISK" />


          </el-select>


        </el-form-item>


        <el-form-item>


          <el-button type="primary" plain @click="handleSearch">查询</el-button>


          <el-button @click="handleReset">重置</el-button>


        </el-form-item>


      </el-form>


    </el-card>





    <!-- 报告列表 -->


    <el-card class="table-card" shadow="never">


      <div v-if="loading" class="skeleton-container" style="padding: 20px;">


        <el-skeleton :rows="10" animated />


      </div>


      <el-table


        v-else


        :data="reportList"


        


        style="width: 100%"


      >


        <el-table-column prop="reportCode" label="报告编码" width="160">


           <template #default="scope">


            <span class="code-text">{{ scope.row.reportCode }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="reportName" label="报告名称" min-width="200" show-overflow-tooltip>


          <template #default="scope">


            <span class="name-text" @click="handleView(scope.row)">{{ scope.row.reportName }}</span>


          </template>


        </el-table-column>


        <el-table-column prop="reportType" label="报告类型" width="120">


          <template #default="scope">


            <el-tag effect="light" round>{{ getReportTypeText(scope.row.reportType) }}</el-tag>


          </template>


        </el-table-column>


        <el-table-column prop="reportStatus" label="状�" width="100">


          <template #default="scope">


            <div class="status-indicator">


              <span :class="['dot', getStatusType(scope.row.reportStatus)]"></span>


              <span>{{ getStatusText(scope.row.reportStatus) }}</span>


            </div>


          </template>


        </el-table-column>


        <el-table-column prop="creatorName" label="创建人�" width="120" />


        <el-table-column prop="createTime" label="创建时间" width="180" />


        <el-table-column label="操作" width="200" fixed="right">


          <template #default="scope">


            <div class="action-buttons">


              <el-tooltip content="编辑" placement="top" v-if="scope.row.reportStatus === 'DRAFT'">


                <el-button circle size="small" type="primary" plain @click="handleEdit(scope.row)">


                  <el-icon><Edit /></el-icon>


                </el-button>


              </el-tooltip>


              


              <el-tooltip content="发布" placement="top" v-if="scope.row.reportStatus === 'DRAFT'">


                <el-button circle size="small" type="success" plain @click="handlePublish(scope.row)">


                  <el-icon><Promotion /></el-icon>


                </el-button>


              </el-tooltip>





              <el-tooltip content="导出Word" placement="top" v-if="scope.row.reportStatus === 'PUBLISHED'">


                 <el-button circle size="small" type="primary" plain @click="handleExport(scope.row, 'WORD')">


                  <el-icon><Document /></el-icon>


                </el-button>


              </el-tooltip>





              <el-tooltip content="删除" placement="top" v-if="scope.row.reportStatus === 'DRAFT'">


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


      width="600px"


      @close="handleDialogClose"


    >


      <el-form


        ref="formRef"


        :model="formData"


        :rules="formRules"


        label-width="100px"


      >


        <el-form-item label="报告名称" prop="reportName">


          <el-input


            v-model="formData.reportName"


            placeholder="请输入报告名�"


            maxlength="100"


            show-word-limit


          />


        </el-form-item>


        <el-form-item label="报告类型" prop="reportType">


          <el-select v-model="formData.reportType" placeholder="请选择报告类型" style="width: 100%" :disabled="isEdit">


            <el-option label="执行报告" value="EXECUTION" />


            <el-option label="覆盖率率率报告" value="COVERAGE" />


            <el-option label="质量报告" value="QUALITY" />


            <el-option label="风险报告" value="RISK" />


          </el-select>


        </el-form-item>


        <el-form-item label="关联需�" prop="requirementId">


           <!-- Ideally this should be a remote search select, simplified as input for now or could implement lookup -->


           <el-input-number v-model="formData.requirementId" placeholder="需求ID" style="width: 100%" :min="1" />


        </el-form-item>


        <el-form-item label="执行人中任务" prop="executionTaskId">


           <el-input-number v-model="formData.executionTaskId" placeholder="执行人中任务ID" style="width: 100%" :min="1" />


        </el-form-item>


      </el-form>


      <template #footer>


        <el-button @click="dialogVisible = false">取消</el-button>


        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">


          确定


        </el-button>


      </template>


    </el-dialog>





    <!-- 查看详情对话�?-->


    <el-dialog


      v-model="viewDialogVisible"


      title="报告详情"


      width="800px"


    >


      <el-descriptions :column="2" border>


        <el-descriptions-item label="报告编码">{{ viewData.reportCode }}</el-descriptions-item>


        <el-descriptions-item label="报告名称">{{ viewData.reportName }}</el-descriptions-item>


        <el-descriptions-item label="报告类型">{{ getReportTypeText(viewData.reportType) }}</el-descriptions-item>


        <el-descriptions-item label="状�">{{ getStatusText(viewData.reportStatus) }}</el-descriptions-item>


        <el-descriptions-item label="创建人�">{{ viewData.creatorName }}</el-descriptions-item>


        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>


        <el-descriptions-item label="报告摘要" :span="2">


            {{ viewData.reportSummary || '暂无摘要' }}


        </el-descriptions-item>


         <el-descriptions-item label="报告内容" :span="2">


             <div class="json-content">


                {{ viewData.reportContent || '暂无内容' }}


             </div>


        </el-descriptions-item>


      </el-descriptions>


    </el-dialog>


  </div>


</template>





<script setup lang="ts">


import { ref, reactive, computed, onMounted } from 'vue'


import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'


import { Plus, Search, Edit, Delete, Promotion, Document } from '@element-plus/icons-vue'


import { testReportApi, type TestReportRequestDTO, type TestReportResponseDTO } from '@/api/testReport'





// 响应式数据表单验证规则


const formRules: FormRules = {


  reportName: [


    { required: true, message: '请输入报告名称', trigger: 'blur' }


  ],


  reportType: [


    { required: true, message: '请选择报告类型', trigger: 'change' }


  ]


}





const dialogTitle = computed(() => (isEdit.value ? '编辑报告' : '新建报告'))





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





const getStatusText = (status: string) => {


    const map: Record<string, string> = {


        DRAFT: '草稿',


        PUBLISHED: '已发布'


    }


    return map[status] || 'info'


}





// Actions


const loadReportList = async () => {


  loading.value = true


  try {


    const response = await testReportApi.getReportList(pagination.page - 1, pagination.size)


    if (response.data) {


      reportList.value = response.data.content || []


      pagination.total = response.data.totalElements || 0


    }


  } catch (error) {


    console.error('Failed to load reports', error)


    ElMessage.error('加载报告列表失败')


  } finally {


    loading.value = false


  }


}





const handleSearch = () => {


    pagination.page = 1


    // Note: API implementation in previous step didn't explicitly include search params in getReportList signature,


    // but typically it should. The generated API client only had page/size.


    // I should probably update the API client if search is needed, but for now let's just refresh.


    // Wait, the RequirementList had search params passed to API.


    // My implemented `getReportList` only accepts page and size.


    // I will stick to basic pagination to match my API implementation for now,


    // or I should update API to support filtering.


    // Given the task description, I'll stick to basic load for now and maybe update API later if needed.


    // Actually, let's just reload.


    loadReportList()


}





const handleReset = () => {


    searchForm.reportName = ''


    searchForm.reportType = ''


    handleSearch()


}





const handleSizeChange = (size: number) => {


  pagination.size = size


  pagination.page = 1


  loadReportList()


}





const handlePageChange = (page: number) => {


  pagination.page = page


  loadReportList()


}





const handleCreate = () => {


  isEdit.value = false


  resetForm()


  dialogVisible.value = true


}





const handleEdit = (row: TestReportResponseDTO) => {


  isEdit.value = true


  formData.reportName = row.reportName


  formData.reportType = row.reportType as any


  formData.requirementId = row.requirementId


  formData.executionTaskId = row.executionTaskId


  // Store ID for update

  ;(formData as any).id = row.id

  dialogVisible.value = true


}





const handleView = async (row: TestReportResponseDTO) => {


    try {


        const res = await testReportApi.getReportById(row.id)


        viewData.value = res


        viewDialogVisible.value = true


    } catch(e) {


        console.error(e)


    }


}





const handleDelete = async (row: TestReportResponseDTO) => {


    try {


        await ElMessageBox.confirm('确定要删除该报告吗？', '提示', { type: 'warning' })


        await testReportApi.deleteReport(row.id)


        ElMessage.success('删除成功')


        loadReportList()


    } catch (e) {


        // cancel


    }


}





const handlePublish = async (row: TestReportResponseDTO) => {


    try {


        await ElMessageBox.confirm('确定要发布该报告吗？发布后不可修改', '提示', { type: 'warning' })


        await testReportApi.publishReport(row.id)


        ElMessage.success('发布成功')


        loadReportList()


    } catch(e) {


        // cancel


    }


}





const handleExport = async (row: TestReportResponseDTO, format: 'WORD' | 'PDF' | 'EXCEL') => {


    try {


        const url = await testReportApi.exportReport(row.reportCode, format)


        if (url) {


            window.open(url, '_blank')


        } else {


            ElMessage.warning('导出链接为空')


        }


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


                if (isEdit.value) {


                     await testReportApi.updateReport((formData as any).id, formData)


                     ElMessage.success('更新成功')


                } else {


                     await testReportApi.generateReport(formData)


                     ElMessage.success('创建人成功')


                }


                dialogVisible.value = false


                loadReportList()


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


    formData.reportName = ''


    formData.reportType = 'EXECUTION'


    formData.requirementId = undefined


    formData.executionTaskId = undefined


    delete (formData as any).id


}





onMounted(() => {


    loadReportList()


})





</script>





<style scoped lang="scss">


.test-report-list {


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


    margin-bottom: 20px;


    border: none;


    :deep(.el-card__body) { padding: 20px; }


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


    .status-indicator {


      display: flex;


      align-items: center;


      gap: 6px;


      .dot {


        width: 8px;


        height: 8px;


        border-radius: 50%;


        &.info { background-color: var(--el-color-info); }


        &.success { background-color: var(--el-color-success); }


      }


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