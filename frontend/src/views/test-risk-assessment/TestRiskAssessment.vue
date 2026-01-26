<template>


  <div class="test-risk-assessment">


    <div class="page-header">


      <div class="header-left">


        <h2 class="page-title">风险评估管理</h2>


        <p class="page-subtitle">评估需求ID和执行任务的风险等级及上线可行�</p>


      </div>


      <div class="header-right">


        <el-button type="primary" size="large" @click="handleCreate" class="create-btn">


          <el-icon><Plus /></el-icon>


          新建评估


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


        :data="assessmentList"


        


        style="width: 100%"


      >


        <el-table-column prop="assessmentCode" label="评估编码" width="160">


           <template #default="scope">


            <span class="code-text">{{ scope.row.assessmentCode }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="assessmentName" label="评估名称" min-width="200" show-overflow-tooltip>


          <template #default="scope">


            <span class="name-text" @click="handleView(scope.row)">{{ scope.row.assessmentName }}</span>


          </template>


        </el-table-column>


        <el-table-column prop="riskLevel" label="风险等级" width="120">


          <template #default="scope">


            <el-tag :type="getRiskLevelType(scope.row.riskLevel)" effect="dark">


                {{ getRiskLevelText(scope.row.riskLevel) }}


            </el-tag>


          </template>


        </el-table-column>


        <el-table-column prop="riskScore" label="风险评分" width="150">


           <template #default="scope">


              <span :class="['score-text', getRiskLevelType(scope.row.riskLevel)]">{{ scope.row.riskScore }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="feasibilityScore" label="可行性评�" width="150">


            <template #default="scope">


              <span class="score-text success">{{ scope.row.feasibilityScore }}</span>


           </template>


        </el-table-column>


        <el-table-column prop="assessmentTime" label="评估时间" width="180" />


        <el-table-column label="操作" width="150" fixed="right">


          <template #default="scope">


            <div class="action-buttons">


              <el-tooltip content="查看详情" placement="top">


                <el-button circle size="small" type="primary" plain @click="handleView(scope.row)">


                  <el-icon><View /></el-icon>


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





    <!-- 新建评估对话�?-->


    <el-dialog


      v-model="dialogVisible"


      title="新建风险评估"


      width="600px"


      @close="handleDialogClose"


    >


      <el-form


        ref="formRef"


        :model="formData"


        :rules="formRules"


        label-width="100px"


      >


        <el-form-item label="评估名称" prop="assessmentName">


          <el-input v-model="formData.assessmentName" placeholder="请输入评估名称" maxlength="100" show-word-limit />


        </el-form-item>


        <el-form-item label="关联对象" prop="targetType">


           <el-radio-group v-model="targetType">


              <el-radio label="REQUIREMENT">需求</el-radio>


              <el-radio label="EXECUTION">执行任务</el-radio>


           </el-radio-group>


        </el-form-item>


        <el-form-item v-if="targetType === 'REQUIREMENT'" label="需求ID" prop="requirementId">


           <el-input-number v-model="formData.requirementId" placeholder="请输入需求ID" style="width: 100%" :min="1" />


        </el-form-item>


        <el-form-item v-if="targetType === 'EXECUTION'" label="执行任务ID" prop="executionTaskId">


           <el-input-number v-model="formData.executionTaskId" placeholder="请输入执行任务ID" style="width: 100%" :min="1" />


        </el-form-item>


      </el-form>


      <template #footer>


        <el-button @click="dialogVisible = false">取消</el-button>


        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">


          开始评估


        </el-button>


      </template>


    </el-dialog>





    <!-- 查看详情对话框-->


    <el-dialog


      v-model="viewDialogVisible"


      title="评估详情"


      width="900px"


    >


      <el-descriptions :column="2" border>


        <el-descriptions-item label="评估编码">{{ viewData.assessmentCode }}</el-descriptions-item>


        <el-descriptions-item label="评估名称">{{ viewData.assessmentName }}</el-descriptions-item>


        <el-descriptions-item label="风险等级">


             <el-tag :type="getRiskLevelType(viewData.riskLevel)" effect="dark">


                {{ getRiskLevelText(viewData.riskLevel) }}


            </el-tag>


        </el-descriptions-item>


        <el-descriptions-item label="风险评分">{{ viewData.riskScore }}</el-descriptions-item>


        <el-descriptions-item label="可行性评估">{{ viewData.feasibilityScore }}</el-descriptions-item>


         <el-descriptions-item label="评估时间">{{ viewData.assessmentTime }}</el-descriptions-item>


        <el-descriptions-item label="上线建议" :span="2">


            {{ viewData.feasibilityRecommendation || '-'}}


        </el-descriptions-item>


        <el-descriptions-item label="风险项" :span="2">


             <div class="json-content">


                {{ viewData.riskItems || '-'}}


             </div>


        </el-descriptions-item>


        <el-descriptions-item label="评估详情" :span="2">


             <div class="json-content">


                {{ viewData.assessmentDetails || '-'}}


             </div>


        </el-descriptions-item>


      </el-descriptions>


    </el-dialog>


  </div>


</template>





<script setup lang="ts">


import { ref, reactive, onMounted, computed } from 'vue'


import { ElMessage, type FormInstance, type FormRules } from 'element-plus'


import { Plus, View } from '@element-plus/icons-vue'


import { testRiskAssessmentApi, type TestRiskAssessmentRequestDTO, type TestRiskAssessmentResponseDTO } from '@/api/testRiskAssessment'




// 响应式数据定义
const loading = ref(false)
const assessmentList = ref<TestRiskAssessmentResponseDTO[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const formRef = ref<FormInstance | null>(null)
const formData = reactive<TestRiskAssessmentRequestDTO>({
  assessmentName: '',
  requirementId: undefined,
  executionTaskId: undefined
})
const targetType = ref<'REQUIREMENT' | 'EXECUTION'>('REQUIREMENT')
const dialogVisible = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const viewData = ref<TestRiskAssessmentResponseDTO>({} as TestRiskAssessmentResponseDTO)




// 响应式数据表单验证规则


const formRules = computed<FormRules>(() => {


    const rules: FormRules = {


      assessmentName: [


        { required: true, message: '请输入评估名称', trigger: 'blur' }


      ]


    }


    if (targetType.value === 'REQUIREMENT') {


        rules.requirementId = [{ required: true, message: '请输入需求ID', trigger: 'blur' }]


    } else {


        rules.executionTaskId = [{ required: true, message: '请输入执行任务ID', trigger: 'blur' }]


    }


    return rules


})





// Helpers


const getRiskLevelText = (level: string) => {


    const map: Record<string, string> = {


        HIGH: '高风险',


        MEDIUM: '中风险',


        LOW: '低风险'


    }


    return map[level] || level


}





const getRiskLevelType = (level: string) => {


    const map: Record<string, string> = {


        HIGH: 'danger',


        MEDIUM: 'warning',


        LOW: 'success'


    }


    return map[level] || 'info'


}





// Actions


const loadAssessmentList = async () => {


  loading.value = true


  try {


    const response = await testRiskAssessmentApi.getAssessmentList(pagination.page - 1, pagination.size)


    if (response.data) {


      assessmentList.value = response.data.content || []


      pagination.total = response.data.totalElements || 0


    }


  } catch (error) {


    console.error('Failed to load assessment list', error)


    ElMessage.error('加载评估列表失败')


  } finally {


    loading.value = false


  }


}





const handleSizeChange = (size: number) => {


  pagination.size = size


  pagination.page = 1


  loadAssessmentList()


}





const handlePageChange = (page: number) => {


  pagination.page = page


  loadAssessmentList()


}





const handleCreate = () => {


  resetForm()


  dialogVisible.value = true


}





const handleView = async (row: TestRiskAssessmentResponseDTO) => {


    try {


        const res = await testRiskAssessmentApi.getAssessmentById(row.id)


        viewData.value = res


        viewDialogVisible.value = true


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


                // Clear irrelevant ID based on target type


                if (targetType.value === 'REQUIREMENT') {


                    formData.executionTaskId = undefined


                     await testRiskAssessmentApi.assessRequirementRisk(formData.requirementId!)


                } else {


                    formData.requirementId = undefined


                     await testRiskAssessmentApi.assessExecutionTaskRisk(formData.executionTaskId!)


                }





                // Note: The specific API methods (assessRequirementRisk, assessExecutionTaskRisk) take ID only, but we might want to pass name too.


                // However, the current API client definitions for those methods only take ID. 


                // The general 'assessRisk' takes the DTO.


                // Let's use assessRisk for full control if we want to set name.


                // Wait, the backend controller specific methods create new assessment entities. 


                // Let's stick to using `assessRisk` (general) which takes the DTO including name.


                


                await testRiskAssessmentApi.assessRisk(formData)


                


                ElMessage.success('评估已完成')


                dialogVisible.value = false


                loadAssessmentList()


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


    formData.assessmentName = ''


    formData.requirementId = undefined


    formData.executionTaskId = undefined


    targetType.value = 'REQUIREMENT'


}





onMounted(() => {


    loadAssessmentList()


})





</script>





<style scoped lang="scss">


.test-risk-assessment {


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


    .score-text {


        font-weight: bold;


        &.danger { color: var(--el-color-danger); }


        &.warning { color: var(--el-color-warning); }


        &.success { color: var(--el-color-success); }


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