<template>


  <div class="data-document-generation">


    <el-card>


      <template #header>


        <div class="card-header">


          <h2>数据文档生成管理</h2>


          <span class="subtitle">生成等价类表和正交表</span>


        </div>


      </template>





      <el-tabs v-model="activeTab" @tab-change="handleTabChange">


        <!-- 等价类表生成 -->


        <el-tab-pane label="等价类表生成" name="equivalence">


          <el-form :model="equivalenceForm" :rules="equivalenceFormRules" ref="equivalenceFormRef" label-width="120px">


            <el-form-item label="数据来源">


              <el-radio-group v-model="equivalenceForm.sourceType">


                <el-radio label="requirement">按需求检索内容方式/用例复用</el-radio>


                <el-radio label="cases">按用例检索内容方式/用例复用</el-radio>


                <el-radio label="manual">手动输入/用例复用</el-radio>


              </el-radio-group>


            </el-form-item>





            <el-form-item label="需求" prop="requirementId" v-if="equivalenceForm.sourceType === 'requirement'">


              <el-select


                v-model="equivalenceForm.requirementId"


                placeholder="请选择需求"


                filterable


                clearable


                style="width: 100%"


                :loading="requirementLoading"


              >


                <el-option


                  v-for="req in requirementList"


                  :key="req.id"


                  :label="`${req.requirementCode} - ${req.requirementName}`"


                  :value="req.id"


                />


              </el-select>


            </el-form-item>





            <el-form-item label="用例列表" prop="caseIds" v-if="equivalenceForm.sourceType === 'cases'">


              <el-select


                v-model="equivalenceForm.caseIds"


                placeholder="请选择用例（可多选）"


                multiple


                filterable


                clearable


                style="width: 100%"


                :loading="caseLoading"


              >


                <el-option


                  v-for="tc in testCaseList"


                  :key="tc.id"


                  :label="`${tc.caseCode} - ${tc.caseName}`"


                  :value="tc.id"


                />


              </el-select>


            </el-form-item>





            <el-form-item label="参数列表" prop="parameters" v-if="equivalenceForm.sourceType === 'manual'">


              <div class="parameter-list">


                <div


                  v-for="(param, index) in equivalenceForm.parameters"


                  :key="index"


                  class="parameter-item"


                >


                  <el-input


                    v-model="param.parameterName"


                    placeholder="参数名称"


                    style="width: 200px; margin-right: 10px"


                  />


                  <el-input


                    v-model="param.parameterType"


                    placeholder="参数类型"


                    style="width: 150px; margin-right: 10px"


                  />


                  <el-input


                    v-model="param.validClasses"


                    placeholder="有效等价类（逗号分隔）"


                    style="flex: 1; margin-right: 10px"


                  />


                  <el-input


                    v-model="param.invalidClasses"


                    placeholder="无效等价类（逗号分隔，可选）"


                    style="flex: 1; margin-right: 10px"


                  />


                  <el-button type="danger" size="small" @click="removeParameter(index)">删除</el-button>


                </div>


                <el-button type="primary" size="small" @click="addParameter">添加参数</el-button>


              </div>


            </el-form-item>





            <el-form-item label="表格标题">


              <el-input v-model="equivalenceForm.title" placeholder="请输入表格标题（可选）" />


            </el-form-item>





            <el-form-item>


              <el-button type="primary" @click="handleGenerateEquivalence" :loading="equivalenceLoading">


                生成等价类表


              </el-button>


              <el-button @click="resetEquivalenceForm">重置</el-button>


            </el-form-item>


          </el-form>





          <!-- 等价类表结果展示 -->


          <div v-if="equivalenceResult" class="result-section">


            <el-divider>生成结果</el-divider>


            <div class="result-header">


              <h3>{{ equivalenceResult.title }}</h3>


              <div class="result-actions">


                <el-button size="small" @click="exportEquivalenceExcel">导出Excel</el-button>


                <el-button size="small" @click="exportEquivalenceWord">导出Word</el-button>


              </div>


            </div>


            <el-table :data="equivalenceResult.testCases" border stripe style="width: 100%; margin-top: 20px">


              <el-table-column prop="caseNumber" label="用例编号" width="120" />


              <el-table-column


                v-for="param in equivalenceResult.parameters"


                :key="param.parameterName"


                :prop="`parameterValues.${param.parameterName}`"


                :label="param.parameterName"


              />


              <el-table-column prop="isValid" label="有效性" width="100">


                <template #default="{ row }">


                  <el-tag :type="row.isValid ? 'success' : 'danger'">


                    {{ row.isValid ? '有效' : '无效' }}


                  </el-tag>


                </template>


              </el-table-column>


            </el-table>


            <div class="result-summary" style="margin-top: 20px">


              <el-statistic-group>


                <el-statistic title="总用例数" :value="equivalenceResult.totalCases" />


                <el-statistic title="有效用例数" :value="equivalenceResult.validCases" />


                <el-statistic title="无效用例数" :value="equivalenceResult.invalidCases" />


              </el-statistic-group>


            </div>


          </div>


        </el-tab-pane>





        <!-- 正交表生成 -->


        <el-tab-pane label="正交表生成" name="orthogonal">


          <el-form :model="orthogonalForm" :rules="orthogonalFormRules" ref="orthogonalFormRef" label-width="120px">


            <el-form-item label="因素列表" prop="factors">


              <div class="factor-list">


                <div


                  v-for="(factor, index) in orthogonalForm.factors"


                  :key="index"


                  class="factor-item"


                >


                  <el-input


                    v-model="factor.factorName"


                    placeholder="因素名称"


                    style="width: 200px; margin-right: 10px"


                  />


                  <el-input


                    v-model="factor.levels"


                    placeholder="水平值（逗号分隔，如：A,B,C）"


                    style="flex: 1; margin-right: 10px"


                  />


                  <el-button type="danger" size="small" @click="removeFactor(index)">删除</el-button>


                </div>


                <el-button type="primary" size="small" @click="addFactor">添加因素</el-button>


              </div>


            </el-form-item>





            <el-form-item label="表格标题">


              <el-input v-model="orthogonalForm.title" placeholder="请输入表格标题（可选）" />


            </el-form-item>





            <el-form-item>


              <el-button type="primary" @click="handleGenerateOrthogonal" :loading="orthogonalLoading">


                生成正交表


              </el-button>


              <el-button @click="resetOrthogonalForm">重置</el-button>


            </el-form-item>


          </el-form>





          <!-- 正交表结果展示 -->


          <div v-if="orthogonalResult" class="result-section">


            <el-divider>生成结果</el-divider>


            <div class="result-header">


              <h3>{{ orthogonalResult.title }}</h3>


              <div class="result-actions">


                <el-button size="small" @click="exportOrthogonalExcel">导出Excel</el-button>


                <el-button size="small" @click="exportOrthogonalWord">导出Word</el-button>


              </div>


            </div>


            <el-alert


              :title="`正交表类型: ${orthogonalResult.orthogonalType} | 用例缩减率: ${orthogonalResult.reductionRate}% | 理论最大用例数: ${orthogonalResult.theoreticalMaxCases}`"


              type="info"


              :closable="false"


              style="margin: 20px 0"


            />


            <el-table :data="orthogonalResult.testCases" border stripe style="width: 100%; margin-top: 20px">


              <el-table-column prop="caseNumber" label="用例编号" width="120" />


              <el-table-column


                v-for="factor in orthogonalResult.factors"


                :key="factor.factorName"


                :prop="`factorValues.${factor.factorName}`"


                :label="factor.factorName"


              />


            </el-table>


            <div class="result-summary" style="margin-top: 20px">


              <el-statistic title="总用例数" :value="orthogonalResult.totalCases" />


            </div>


          </div>


        </el-tab-pane>


      </el-tabs>


    </el-card>


  </div>


</template>





<script setup lang="ts">


import { ref, reactive, onMounted } from 'vue'


import { ElMessage } from 'element-plus'


import { dataDocumentApi, type EquivalenceTableRequest, type OrthogonalTableRequest, type EquivalenceParameter, type OrthogonalFactor } from '@/api/dataDocument'


import { requirementApi, type TestRequirement } from '@/api/requirement'


import { testCaseApi, type TestCase } from '@/api/testCase'





onMounted(() => {


  loadRequirements()


  loadTestCases()


})





// 标签页页需求ID列�?


const requirementList = ref<TestRequirement[]>([])


const requirementLoading = ref(false)





// 用例列表


const testCaseList = ref<TestCase[]>([])


const caseLoading = ref(false)





// 等价类表表单


const equivalenceFormRef = ref()


const equivalenceForm = reactive<EquivalenceTableRequest & { sourceType: string; parameters: EquivalenceParameter[] }>({


  sourceType: 'requirement',


  requirementId: undefined,


  caseIds: undefined,


  parameters: [],


  title: ''


})





const equivalenceFormRules = {


  requirementId: [


    { required: true, message: '请选择需求', trigger: 'change',


      validator: (_: any, value: any, callback: any) => {


        if (equivalenceForm.sourceType === 'requirement' && !value) {


          callback(new Error('请选择需求'))


        } else {


          callback()


        }


      }


    }


  ],


  caseIds: [


    { required: true, message: '请选择用例', trigger: 'change',


      validator: (_: any, value: any, callback: any) => {


        if (equivalenceForm.sourceType === 'cases' && (!value || value.length === 0)) {


          callback(new Error('请至少选择一个用�'))


        } else {


          callback()


        }


      }


    }


  ],


  parameters: [


    { required: true, message: '请至少添加一个参数', trigger: 'change',


      validator: (_: any, value: any, callback: any) => {


        if (equivalenceForm.sourceType === 'manual' && (!value || value.length === 0)) {


          callback(new Error('请至少添加一个参数'))


        } else {


          callback()


        }


      }


    }


  ]


}





const equivalenceLoading = ref(false)


const equivalenceResult = ref<any>(null)





const orthogonalFormRules = {
  factors: [
    { required: true, message: '请至少添加一个因子', trigger: 'change',
      validator: (_: any, value: any, callback: any) => {
        if (!value || value.length === 0) {


          callback(new Error('请至少添加一个因子'))


        } else {


          callback()


        }


      }


    }


  ]


}





const orthogonalLoading = ref(false)


const orthogonalResult = ref<any>(null)





// 加载需求ID列表�?


const loadRequirements = async () => {


  requirementLoading.value = true


  try {


    const res = await requirementApi.getRequirementList({ page: 0, size: 1000 })


    requirementList.value = res.data.content || []


  } catch (error) {


    console.error('加载需求ID列表失败', error)


  } finally {


    requirementLoading.value = false


  }


}





// 加载用例列表


const loadTestCases = async () => {


  caseLoading.value = true


  try {


    const res = await testCaseApi.getTestCaseList({ page: 0, size: 1000 })


    testCaseList.value = res.data.content || []


  } catch (error) {


    console.error('加载用例列表失败:', error)


  } finally {


    caseLoading.value = false


  }


}





// 添加参数


const addParameter = () => {


  equivalenceForm.parameters.push({


    parameterName: '',


    parameterType: '',


    validClasses: '',


    invalidClasses: ''


  })


}





// 删除参数


const removeParameter = (index: number) => {


  equivalenceForm.parameters.splice(index, 1)


}





// 添加因素


const addFactor = () => {


  orthogonalForm.factors.push({


    factorName: '',


    levels: ''


  })


}





// 删除因素


const removeFactor = (index: number) => {


  orthogonalForm.factors.splice(index, 1)


}





// 生成等价类表


const handleGenerateEquivalence = async () => {


  if (!equivalenceFormRef.value) return


  await equivalenceFormRef.value.validate(async (valid: boolean) => {


    if (!valid) return





    equivalenceLoading.value = true


    try {


      const request: EquivalenceTableRequest = {


        title: equivalenceForm.title || undefined


      }





      if (equivalenceForm.sourceType === 'requirement') {


        request.requirementId = equivalenceForm.requirementId


      } else if (equivalenceForm.sourceType === 'cases') {


        request.caseIds = equivalenceForm.caseIds


      } else {


        // 手动输入：转换参数格式

        request.parameters = equivalenceForm.parameters.map(p => ({

          parameterName: p.parameterName,

          parameterType: p.parameterType,

          validClasses: p.validClasses.split(',').map(s => s.trim()).filter(s => s),

          invalidClasses: p.invalidClasses ? p.invalidClasses.split(',').map(s => s.trim()).filter(s => s) : undefined

        }))

      }



      const res = await dataDocumentApi.generateEquivalenceTable(request)

      equivalenceResult.value = res.data



      ElMessage.success('等价类表生成成功')

    } catch (error: any) {


      ElMessage.error(error.message || '生成等价类表失败')


    } finally {


      equivalenceLoading.value = false


    }


  })


}





// 生成正交表


const handleGenerateOrthogonal = async () => {


  if (!orthogonalFormRef.value) return


  await orthogonalFormRef.value.validate(async (valid: boolean) => {


    if (!valid) return




    orthogonalLoading.value = true


    try {


      const request: OrthogonalTableRequest = {


        title: orthogonalForm.title || undefined,


        factors: orthogonalForm.factors.map(f => ({


          factorName: f.factorName,


          levels: f.levels.split(',').map(s => s.trim()).filter(s => s)


        }))


      }




      const res = await dataDocumentApi.generateOrthogonalTable(request)


      orthogonalResult.value = res.data




      ElMessage.success('正交表生成成功')


    } catch (error: any) {


      ElMessage.error(error.message || '生成正交表失败')


    } finally {


      orthogonalLoading.value = false


    }


  })


}





// 导出等价类表Excel


const exportEquivalenceExcel = async () => {


  if (!equivalenceResult.value) return


  try {


    const blob = await dataDocumentApi.exportEquivalenceTableToExcel(equivalenceResult.value)


    downloadFile(blob, `${equivalenceResult.value.title || '等价类表'}.xlsx`)


    ElMessage.success('导出成功')


  } catch (error: any) {


    ElMessage.error(error.message || '导出失败')


  }


}





// 导出等价类表Word


const exportEquivalenceWord = async () => {


  if (!equivalenceResult.value) return


  try {


    const blob = await dataDocumentApi.exportEquivalenceTableToWord(equivalenceResult.value)


    downloadFile(blob, `${equivalenceResult.value.title || '等价类表'}.docx`)


    ElMessage.success('导出成功')


  } catch (error: any) {


    ElMessage.error(error.message || '导出失败')


  }


}





// 导出正交表Excel


const exportOrthogonalExcel = async () => {


  if (!orthogonalResult.value) return


  try {


    const blob = await dataDocumentApi.exportOrthogonalTableToExcel(orthogonalResult.value)


    downloadFile(blob, `${orthogonalResult.value.title || '正交表'}.xlsx`)


    ElMessage.success('导出成功')


  } catch (error: any) {


    ElMessage.error(error.message || '导出失败')


  }


}





// 导出正交表Word


const exportOrthogonalWord = async () => {


  if (!orthogonalResult.value) return


  try {


    const blob = await dataDocumentApi.exportOrthogonalTableToWord(orthogonalResult.value)


    downloadFile(blob, `${orthogonalResult.value.title || '正交表'}.docx`)


    ElMessage.success('导出成功')


  } catch (error: any) {


    ElMessage.error(error.message || '导出失败')


  }


}





// 下载文件


const downloadFile = (blob: Blob, fileName: string) => {


  const url = window.URL.createObjectURL(blob)


  const link = document.createElement('a')


  link.href = url


  link.download = fileName


  document.body.appendChild(link)


  link.click()


  document.body.removeChild(link)


  window.URL.revokeObjectURL(url)


}





// 重置表单


const resetEquivalenceForm = () => {


  equivalenceFormRef.value?.resetFields()


  equivalenceForm.parameters = []


  equivalenceResult.value = null


}





const resetOrthogonalForm = () => {


  orthogonalFormRef.value?.resetFields()


  orthogonalForm.factors = []


  orthogonalResult.value = null


}





const handleTabChange = () => {
  // 切换标签页时的处理

}

</script>



<style scoped lang="scss">


.data-document-generation {


  .card-header {


    display: flex;


    justify-content: space-between;


    align-items: center;





    h2 {


      margin: 0;


      font-size: 20px;


      font-weight: 600;


    }




    .subtitle {


      color: #909399;


      font-size: 14px;


    }


  }





  .parameter-list,


  .factor-list {

    .parameter-item,

    .factor-item {

      display: flex;

      align-items: center;

      margin-bottom: 10px;

    }

  }



  .result-section {


    margin-top: 30px;




    .result-header {


      display: flex;


      justify-content: space-between;


      align-items: center;


      margin-bottom: 20px;




      h3 {


        margin: 0;


        font-size: 18px;


      }




      .result-actions {


        display: flex;


        gap: 10px;


      }


    }




    .result-summary {


      display: flex;


      gap: 40px;


    }


  }


}


</style>