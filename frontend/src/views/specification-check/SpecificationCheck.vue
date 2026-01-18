<template>

  <div class="specification-check">

    <div class="page-header">

      <div class="header-left">

        <h2 class="page-title">规约检查管理</h2>

        <p class="page-subtitle">对测试用例进行规约符合性检查、内容注入和报告生成</p>

      </div>

    </div>



    <el-card class="action-card" shadow="never">

      <el-form :inline="true" class="action-form">

        <el-form-item label="测试用例ID">

          <el-input-number v-model="caseId" :min="1" placeholder="请输入用例ID" />

        </el-form-item>

        <el-form-item>

          <el-button type="primary" @click="handleMatch" :loading="matchLoading">

            <el-icon><Connection /></el-icon> 匹配规约

          </el-button>

          <el-button type="success" @click="handleCheck" :loading="checkLoading">

            <el-icon><Finished /></el-icon> 检查符合性

          </el-button>

          <el-button type="warning" @click="handleInject" :loading="injectLoading">

            <el-icon><MagicStick /></el-icon> 注入规约

          </el-button>

          <el-button type="info" @click="handleReport" :loading="reportLoading">

            <el-icon><Document /></el-icon> 生成报告

          </el-button>

        </el-form-item>

      </el-form>

    </el-card>



    <div class="results-container">

        <!-- Matched Specs -->

        <el-card v-if="matchedSpecs.length > 0" class="result-card" shadow="never">

             <template #header>

                <div class="card-header">

                    <span>匹配的规约</span>

                </div>

            </template>

            <el-table :data="matchedSpecs" style="width: 100%">

                <el-table-column prop="specCode" label="编码" width="150" />

                <el-table-column prop="specName" label="名称" />

                <el-table-column prop="specType" label="类型" width="120" />

                <el-table-column prop="currentVersion" label="当前版本" width="100" />

            </el-table>

        </el-card>



        <!-- Compliance Check Result -->

        <el-card v-if="checkResult" class="result-card" shadow="never">

             <template #header>

                <div class="card-header">

                    <span>符合性检查结果</span>

                    <el-tag :type="checkResult.isCompliant ? 'success' : 'danger'">

                        {{ checkResult.isCompliant ? '符合' : '不符合'}}

                    </el-tag>

                </div>

            </template>

            <el-descriptions :column="4" border>

                <el-descriptions-item label="评分">{{ checkResult.complianceScore }}</el-descriptions-item>

                <el-descriptions-item label="总检查项">{{ checkResult.totalChecks }}</el-descriptions-item>

                <el-descriptions-item label="通过�">{{ checkResult.passedChecks }}</el-descriptions-item>

                <el-descriptions-item label="未通过�">{{ checkResult.failedChecks }}</el-descriptions-item>

            </el-descriptions>

            

            <div v-if="checkResult.issues && checkResult.issues.length > 0" style="margin-top: 15px;">

                <h4>存在问题</h4>

                <el-table :data="checkResult.issues" style="width: 100%">

                    <el-table-column prop="issueType" label="类型" width="120" />

                    <el-table-column prop="severity" label="严重程度" width="100">

                        <template #default="scope">

                            <el-tag :type="getSeverityType(scope.row.severity)">{{ scope.row.severity }}</el-tag>

                        </template>

                    </el-table-column>

                     <el-table-column prop="specName" label="相关规约" width="150" />

                    <el-table-column prop="issueDescription" label="描述" />

                    <el-table-column prop="suggestion" label="建议" />

                </el-table>

            </div>

        </el-card>



         <!-- Injection Result -->

         <el-card v-if="injectionResult" class="result-card" shadow="never">

             <template #header>

                <div class="card-header">

                    <span>注入结果</span>

                </div>

            </template>

            <el-tabs>

                <el-tab-pane label="增强后的用例">

                     <el-descriptions :column="1" border direction="vertical">

                        <el-descriptions-item label="前置条件">{{ injectionResult.enhancedTestCase.preCondition }}</el-descriptions-item>

                        <el-descriptions-item label="测试步骤">{{ injectionResult.enhancedTestCase.testStep }}</el-descriptions-item>

                         <el-descriptions-item label="预期结果">{{ injectionResult.enhancedTestCase.expectedResult }}</el-descriptions-item>

                    </el-descriptions>

                </el-tab-pane>

                <el-tab-pane label="应用规约">

                    <el-table :data="injectionResult.appliedSpecs" style="width: 100%">

                        <el-table-column prop="specCode" label="编码" />

                        <el-table-column prop="specName" label="名称" />

                    </el-table>

                </el-tab-pane>

            </el-tabs>

         </el-card>



          <!-- Report Result -->

         <el-card v-if="reportResult" class="result-card" shadow="never">

             <template #header>

                <div class="card-header">

                    <span>检查报�</span>

                </div>

            </template>

             <p>{{ reportResult.summary }}</p>

             <div style="margin-top: 10px;">

                 <strong>检查时�?</strong> {{ reportResult.checkTime }}

             </div>

         </el-card>



    </div>

  </div>

</template>



<script setup lang="ts">

import { ref } from 'vue'

import { ElMessage } from 'element-plus'

import { Connection, Finished, MagicStick, Document } from '@element-plus/icons-vue'

import { 

    specificationCheckApi, 

    type SpecificationSummaryDTO, 

    type SpecificationCheckResponseDTO, 

    type SpecificationInjectionResponseDTO,

    type SpecificationComplianceReportDTO

} from '@/api/specificationCheck'



const caseId = ref<number>()

const matchLoading = ref(false)

const checkLoading = ref(false)

const injectLoading = ref(false)

const reportLoading = ref(false)



const matchedSpecs = ref<SpecificationSummaryDTO[]>([])

const checkResult = ref<SpecificationCheckResponseDTO>()

const injectionResult = ref<SpecificationInjectionResponseDTO>()

const reportResult = ref<SpecificationComplianceReportDTO>()



const handleMatch = async () => {

    if (!caseId.value) {

        ElMessage.warning('请输入测试用例ID')

        return

    }

    matchLoading.value = true

    matchedSpecs.value = [] 

    try {

        const res = await specificationCheckApi.matchSpecifications(caseId.value)

        const data = res.data || []

        matchedSpecs.value = data

        if (data.length === 0) {

            ElMessage.info('未匹配到适用规约')

        }

    } catch (e) {

        console.error(e)

    } finally {

        matchLoading.value = false

    }

}



const handleCheck = async () => {

    if (!caseId.value) {

        ElMessage.warning('请输入测试用例ID')

        return

    }

    checkLoading.value = true

    checkResult.value = undefined

    try {

        const res = await specificationCheckApi.checkCompliance({ caseId: caseId.value })

        const data = res.data

        checkResult.value = data

        // Also update matched specs if available in response

        if (data && data.matchedSpecifications) {

            matchedSpecs.value = data.matchedSpecifications

        }

    } catch (e) {

        console.error(e)

    } finally {

        checkLoading.value = false

    }

}



const handleInject = async () => {

    if (!caseId.value) {

        ElMessage.warning('请输入测试用例ID')

        return

    }

    injectLoading.value = true

    injectionResult.value = undefined

    try {

        const res = await specificationCheckApi.injectSpecification({ caseId: caseId.value })

        injectionResult.value = res.data

         ElMessage.success('规约注入成功')

    } catch (e) {

        console.error(e)

    } finally {

        injectLoading.value = false

    }

}



const handleReport = async () => {

     if (!caseId.value) {

        ElMessage.warning('请输入测试用例ID')

        return

    }

    reportLoading.value = true

    reportResult.value = undefined

    try {

        const res = await specificationCheckApi.generateComplianceReport({ caseId: caseId.value })

        reportResult.value = res.data

        ElMessage.success('报告生成成功')

    } catch (e) {

        console.error(e)

    } finally {

        reportLoading.value = false

    }

}



const getSeverityType = (severity: string) => {

    switch (severity) {

        case 'HIGH': return 'danger'

        case 'MEDIUM': return 'warning'

        case 'LOW': return 'info'

        default: return 'info'

    }

}



</script>



<style scoped lang="scss">

.specification-check {

  .page-header {

    margin-bottom: 24px;

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



  .action-card {

      margin-bottom: 24px;

      border: none;

      .action-form {

          margin-bottom: 0;

      }

  }



  .results-container {

      display: flex;

      flex-direction: column;

      gap: 20px;

  }



  .result-card {

      border: none;

      .card-header {

          display: flex;

          justify-content: space-between;

          align-items: center;

          font-weight: bold;

      }

  }

}

</style>