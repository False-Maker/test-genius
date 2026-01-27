<template>
  <div class="batch-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>批量测试</span>
          <el-button size="small" type="primary" @click="handleBatchTest" :loading="loading">
            开始批量测试
          </el-button>
        </div>
      </template>

      <!-- 配置区域 -->
      <div class="config-section">
        <el-form :model="form" label-width="120px" size="default">
          <el-form-item label="测试用例">
            <el-input
              v-model="form.testCases"
              type="textarea"
              :rows="8"
              placeholder="每行一个测试用例（提示词），支持多行输入"
            />
            <div class="form-tip">每行一个测试用例，共{{ testCaseCount }}个用例</div>
          </el-form-item>

          <el-form-item label="选择模型">
            <el-checkbox-group v-model="form.selectedModels">
              <el-checkbox
                v-for="model in availableModels"
                :key="model.modelCode"
                :label="model.modelCode"
              >
                {{ model.modelName }}
              </el-checkbox>
            </el-checkbox-group>
            <div class="form-tip">至少选择一个模型进行测试</div>
          </el-form-item>

          <el-form-item label="高级参数">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-input-number
                  v-model="form.maxTokens"
                  :min="1"
                  :max="100000"
                  placeholder="最大Token数"
                  style="width: 100%"
                />
              </el-col>
              <el-col :span="12">
                <el-input-number
                  v-model="form.temperature"
                  :min="0"
                  :max="2"
                  :step="0.1"
                  :precision="1"
                  placeholder="温度参数"
                  style="width: 100%"
                />
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>

      <!-- 测试结果 -->
      <div v-if="testResults.length > 0" class="results-section">
        <el-divider>测试结果统计</el-divider>

        <!-- 统计信息 -->
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">总用例数</div>
                <div class="stat-value">{{ totalCases }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">成功数</div>
                <div class="stat-value success">{{ successCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">失败数</div>
                <div class="stat-value danger">{{ failCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">成功率</div>
                <div class="stat-value">{{ successRate }}%</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 模型成功率对比图表 -->
        <div class="chart-section">
          <div ref="chartContainer" style="width: 100%; height: 300px"></div>
        </div>

        <!-- 详细结果表格 -->
        <el-table :data="testResults" stripe style="width: 100%; margin-top: 20px" max-height="400">
          <el-table-column prop="testCase" label="测试用例" min-width="200" show-overflow-tooltip />
          <el-table-column prop="model_code" label="模型" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.error ? 'danger' : 'success'">
                {{ scope.row.error ? '失败' : '成功' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="response_time" label="响应时间(ms)" width="120">
            <template #default="scope">
              {{ scope.row.response_time || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="tokens_used" label="Token数" width="120">
            <template #default="scope">
              {{ scope.row.tokens_used || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="error" label="错误信息" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.error" class="error-message">{{ scope.row.error }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { llmApi, type LLMResponse } from '@/api/llm'
import { useCacheStore } from '@/store/cache'
import type { ModelConfig } from '@/api/modelConfig'

const cacheStore = useCacheStore()

// 响应式数据
const loading = ref(false)
const form = reactive({
  testCases: '',
  selectedModels: [] as string[],
  maxTokens: undefined as number | undefined,
  temperature: undefined as number | undefined
})

const testResults = ref<Array<LLMResponse & { testCase: string }>>([])

const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 计算属性
const availableModels = computed(() => {
  return cacheStore.modelList.filter((m: ModelConfig) => m.isActive === '1')
})

const testCaseCount = computed(() => {
  if (!form.testCases.trim()) return 0
  return form.testCases.split('\n').filter(line => line.trim()).length
})

const totalCases = computed(() => {
  return testResults.value.length
})

const successCount = computed(() => {
  return testResults.value.filter(r => !r.error).length
})

const failCount = computed(() => {
  return testResults.value.filter(r => r.error).length
})

const successRate = computed(() => {
  if (totalCases.value === 0) return 0
  return Math.round((successCount.value / totalCases.value) * 100)
})

// 方法
const handleBatchTest = async () => {
  if (!form.testCases.trim()) {
    ElMessage.warning('请输入测试用例')
    return
  }

  if (form.selectedModels.length === 0) {
    ElMessage.warning('请至少选择一个模型')
    return
  }

  // 解析测试用例
  const testCases = form.testCases
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)

  if (testCases.length === 0) {
    ElMessage.warning('没有有效的测试用例')
    return
  }

  loading.value = true
  testResults.value = []

  try {
    // 对每个测试用例，并行调用所有选中的模型
    const allResults: Array<LLMResponse & { testCase: string }> = []

    for (const testCase of testCases) {
      // 并行调用所有模型
      const response = await llmApi.parallelCall({
        prompt: testCase,
        model_codes: form.selectedModels,
        max_tokens: form.maxTokens,
        temperature: form.temperature,
        max_workers: 5
      })

      if (response.data) {
        // 为每个结果添加测试用例标识
        response.data.results.forEach(result => {
          allResults.push({
            ...result,
            testCase
          })
        })
      }
    }

    testResults.value = allResults

    // 更新图表
    nextTick(() => {
      updateChart()
    })

    ElMessage.success(`批量测试完成：${testCases.length}个用例，${form.selectedModels.length}个模型`)
  } catch (error: any) {
    console.error('批量测试失败:', error)
    ElMessage.error(error.response?.data?.detail || '批量测试失败')
  } finally {
    loading.value = false
  }
}

const updateChart = () => {
  if (!chartContainer.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }

  // 按模型统计成功率
  const modelStats: Record<string, { total: number; success: number }> = {}

  testResults.value.forEach(result => {
    const modelCode = result.model_code
    if (!modelStats[modelCode]) {
      modelStats[modelCode] = { total: 0, success: 0 }
    }
    modelStats[modelCode].total++
    if (!result.error) {
      modelStats[modelCode].success++
    }
  })

  const modelNames: string[] = []
  const successRates: number[] = []

  Object.keys(modelStats).forEach(modelCode => {
    const model = availableModels.value.find((m: ModelConfig) => m.modelCode === modelCode)
    modelNames.push(model?.modelName || modelCode)
    const stats = modelStats[modelCode]
    successRates.push(Math.round((stats.success / stats.total) * 100))
  })

  const option = {
    title: {
      text: '模型成功率对比',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>成功率: ${param.value}%`
      }
    },
    xAxis: {
      type: 'category',
      data: modelNames,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '成功率(%)',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '成功率',
        type: 'bar',
        data: successRates,
        itemStyle: {
          color: (params: any) => {
            if (params.value >= 90) return '#67c23a'
            if (params.value >= 70) return '#e6a23c'
            return '#f56c6c'
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

// 初始化
onMounted(async () => {
  // 加载模型列表
  await cacheStore.loadModelList()
})
</script>

<style scoped lang="scss">
.batch-test {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .config-section {
    margin-bottom: 20px;

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .results-section {
    margin-top: 20px;

    .stats-section {
      margin: 20px 0;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 4px;

      .stat-item {
        text-align: center;

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;

          &.success {
            color: #67c23a;
          }

          &.danger {
            color: #f56c6c;
          }
        }
      }
    }

    .chart-section {
      margin: 20px 0;
    }

    .error-message {
      color: #f56c6c;
      font-size: 12px;
    }
  }
}
</style>
