<template>
  <div class="test-case-quality">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用例质量评估管理</h2>
        <p class="page-subtitle">评估测试用例的质量、完整性和规范�</p>
      </div>
    </div>

    <el-card class="action-card" shadow="never">
      <el-form :inline="true" class="action-form">
        <el-form-item label="测试用例ID">
          <el-input-number v-model="caseId" :min="1" placeholder="请输入用例ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAssessQuality" :loading="qualityLoading">
            <el-icon><DataLine /></el-icon> 综合质量评估
          </el-button>
          <el-button type="success" @click="handleCheckCompleteness" :loading="completenessLoading">
            <el-icon><CircleCheck /></el-icon> 完整性检查�?
          </el-button>
          <el-button type="warning" @click="handleCheckStandardization" :loading="standardizationLoading">
            <el-icon><EditPen /></el-icon> 规范性检查�?
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="results-container">
        <!-- Quality Result -->
        <el-card v-if="qualityScore" class="result-card" shadow="never">
             <template #header>
                <div class="card-header">
                    <span>综合质量评分</span>
                    <el-tag :type="getLevelType(qualityScore.qualityLevel)">{{ qualityScore.qualityLevel }}</el-tag>
                </div>
            </template>
            <div class="score-display">
                <el-progress type="dashboard" :percentage="qualityScore.totalScore" :color="getScoreColor" />
                <div class="score-details">
                    <el-descriptions :column="3" border>
                        <el-descriptions-item label="完整性得�">{{ qualityScore.completenessScore }}</el-descriptions-item>
                        <el-descriptions-item label="规范性得�">{{ qualityScore.standardizationScore }}</el-descriptions-item>
                        <el-descriptions-item label="可执行人中性得�">{{ qualityScore.executabilityScore }}</el-descriptions-item>
                    </el-descriptions>
                </div>
            </div>
        </el-card>

        <!-- Completeness Result -->
        <el-card v-if="completenessScore" class="result-card" shadow="never">
             <template #header>
                <div class="card-header">
                    <span>完整性详情�</span>
                    <el-tag effect="plain">总分: {{ completenessScore.totalScore }}</el-tag>
                </div>
            </template>
            <el-descriptions :column="2" border>
                <el-descriptions-item label="基本信息">{{ completenessScore.basicInfoScore }}</el-descriptions-item>
                <el-descriptions-item label="前置条件">{{ completenessScore.preConditionScore }}</el-descriptions-item>
                <el-descriptions-item label="测试步骤">{{ completenessScore.testStepScore }}</el-descriptions-item>
                <el-descriptions-item label="预期结果">{{ completenessScore.expectedResultScore }}</el-descriptions-item>
            </el-descriptions>
        </el-card>

         <!-- Standardization Result -->
        <el-card v-if="standardizationScore" class="result-card" shadow="never">
             <template #header>
                <div class="card-header">
                    <span>规范性详情�</span>
                    <el-tag effect="plain">总分: {{ standardizationScore.totalScore }}</el-tag>
                </div>
            </template>
            <el-descriptions :column="3" border>
                <el-descriptions-item label="命名规范">{{ standardizationScore.namingScore }}</el-descriptions-item>
                <el-descriptions-item label="格式规范">{{ standardizationScore.formatScore }}</el-descriptions-item>
                <el-descriptions-item label="内容规范">{{ standardizationScore.contentScore }}</el-descriptions-item>
            </el-descriptions>
        </el-card>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DataLine, CircleCheck, EditPen } from '@element-plus/icons-vue'
import { 
    testCaseQualityApi, 
    type QualityScore, 
    type CompletenessScore, 
    type StandardizationScore 
} from '@/api/testCaseQuality'

const caseId = ref<number>()
const qualityLoading = ref(false)
const completenessLoading = ref(false)
const standardizationLoading = ref(false)

const qualityScore = ref<QualityScore>()
const completenessScore = ref<CompletenessScore>()
const standardizationScore = ref<StandardizationScore>()

const handleAssessQuality = async () => {
    if (!caseId.value) {
        ElMessage.warning('请输入测试用例ID')
        return
    }
    qualityLoading.value = true
    try {
        const res = await testCaseQualityApi.assessQuality(caseId.value)
        qualityScore.value = res.data
        // Also fetch details'Maybe not needed if generic quality level is enough for this button
    } catch (e) {
        console.error(e)
    } finally {
        qualityLoading.value = false
    }
}

const handleCheckCompleteness = async () => {
    if (!caseId.value) {
        ElMessage.warning('请输入测试用例ID')
        return
    }
    completenessLoading.value = true
    try {
        const res = await testCaseQualityApi.checkCompleteness(caseId.value)
        completenessScore.value = res.data
    } catch (e) {
        console.error(e)
    } finally {
        completenessLoading.value = false
    }
}

const handleCheckStandardization = async () => {
    if (!caseId.value) {
        ElMessage.warning('请输入测试用例ID')
        return
    }
    standardizationLoading.value = true
    try {
        const res = await testCaseQualityApi.checkStandardization(caseId.value)
        standardizationScore.value = res.data
    } catch (e) {
        console.error(e)
    } finally {
        standardizationLoading.value = false
    }
}

const getLevelType = (level: string) => {
    if (level === '优秀') return 'success'
    if (level === '良好') return 'primary'
    if (level === '一�') return 'warning'
    return 'danger'
}

const getScoreColor = (percentage: number) => {
    if (percentage >= 90) return '#67C23A'
    if (percentage >= 80) return '#409EFF'
    if (percentage >= 60) return '#E6A23C'
    return '#F56C6C'
}

</script>

<style scoped lang="scss">
.test-case-quality {
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
      .score-display {
          display: flex;
          align-items: center;
          gap: 40px;
          .score-details {
              flex: 1;
          }
      }
  }
}
</style>