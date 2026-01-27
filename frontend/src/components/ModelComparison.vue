<template>
  <div class="model-comparison">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>模型性能对比</span>
          <el-button size="small" type="primary" @click="handleCompare" :loading="loading">
            开始对比
          </el-button>
        </div>
      </template>

      <!-- 配置区域 -->
      <div class="config-section">
        <el-form :model="form" label-width="120px" size="default">
          <el-form-item label="提示词">
            <el-input
              v-model="form.prompt"
              type="textarea"
              :rows="6"
              placeholder="请输入要测试的提示词"
            />
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
            <div class="form-tip">至少选择一个模型进行对比</div>
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

      <!-- 对比结果 -->
      <div v-if="results.length > 0" class="results-section">
        <el-divider>对比结果</el-divider>

        <!-- 性能统计 -->
        <div class="performance-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">总耗时</div>
                <div class="stat-value">{{ totalTime }}ms</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">成功</div>
                <div class="stat-value success">{{ successCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">失败</div>
                <div class="stat-value danger">{{ failCount }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-label">平均响应时间</div>
                <div class="stat-value">{{ avgResponseTime }}ms</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 性能对比图表 -->
        <div class="chart-section">
          <div ref="chartContainer" style="width: 100%; height: 300px"></div>
        </div>

        <!-- 详细结果表格 -->
        <el-table :data="results" stripe style="width: 100%; margin-top: 20px">
          <el-table-column prop="model_code" label="模型" width="150" />
          <el-table-column prop="response_time" label="响应时间(ms)" width="120">
            <template #default="scope">
              <span :class="getResponseTimeClass(scope.row.response_time)">
                {{ scope.row.response_time || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="tokens_used" label="Token数" width="120">
            <template #default="scope">
              {{ scope.row.tokens_used || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.error ? 'danger' : 'success'">
                {{ scope.row.error ? '失败' : '成功' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="响应内容" min-width="300" show-overflow-tooltip>
            <template #default="scope">
              <div v-if="scope.row.error" class="error-message">
                {{ scope.row.error }}
              </div>
              <div v-else class="response-content">
                {{ scope.row.content || '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                link
                type="primary"
                @click="handleViewDetail(scope.row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="模型响应详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模型代码">
          {{ currentDetail?.model_code }}
        </el-descriptions-item>
        <el-descriptions-item label="响应时间">
          {{ currentDetail?.response_time }}ms
        </el-descriptions-item>
        <el-descriptions-item label="Token数">
          {{ currentDetail?.tokens_used || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail?.error ? 'danger' : 'success'">
            {{ currentDetail?.error ? '失败' : '成功' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="响应内容" :span="2">
          <div v-if="currentDetail?.error" class="error-message">
            {{ currentDetail.error }}
          </div>
          <pre v-else class="response-content">{{ currentDetail?.content || '-' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
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
  prompt: '',
  selectedModels: [] as string[],
  maxTokens: undefined as number | undefined,
  temperature: undefined as number | undefined
})

const results = ref<LLMResponse[]>([])
const totalTime = ref(0)
const successCount = ref(0)
const failCount = ref(0)

const detailVisible = ref(false)
const currentDetail = ref<LLMResponse | null>(null)

const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 计算属性
const availableModels = computed(() => {
  return cacheStore.modelList.filter((m: ModelConfig) => m.isActive === '1')
})

const avgResponseTime = computed(() => {
  const successResults = results.value.filter(r => !r.error && r.response_time)
  if (successResults.length === 0) return 0
  const sum = successResults.reduce((acc, r) => acc + (r.response_time || 0), 0)
  return Math.round(sum / successResults.length)
})

// 方法
const handleCompare = async () => {
  if (!form.prompt.trim()) {
    ElMessage.warning('请输入提示词')
    return
  }

  if (form.selectedModels.length === 0) {
    ElMessage.warning('请至少选择一个模型')
    return
  }

  loading.value = true
  try {
    const response = await llmApi.parallelCall({
      prompt: form.prompt,
      model_codes: form.selectedModels,
      max_tokens: form.maxTokens,
      temperature: form.temperature,
      max_workers: 5
    })

    if (response.data) {
      results.value = response.data.results
      totalTime.value = response.data.total_time
      successCount.value = response.data.success_count
      failCount.value = response.data.fail_count

      // 更新图表
      nextTick(() => {
        updateChart()
      })

      ElMessage.success('对比完成')
    }
  } catch (error: any) {
    console.error('模型对比失败:', error)
    ElMessage.error(error.response?.data?.detail || '模型对比失败')
  } finally {
    loading.value = false
  }
}

const updateChart = () => {
  if (!chartContainer.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }

  const successResults = results.value.filter(r => !r.error && r.response_time)
  const modelNames = successResults.map(r => {
    const model = availableModels.value.find((m: ModelConfig) => m.modelCode === r.model_code)
    return model?.modelName || r.model_code
  })
  const responseTimes = successResults.map(r => r.response_time || 0)

  const option = {
    title: {
      text: '模型响应时间对比',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
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
      name: '响应时间(ms)'
    },
    series: [
      {
        name: '响应时间',
        type: 'bar',
        data: responseTimes,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}ms'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const getResponseTimeClass = (time?: number) => {
  if (!time) return ''
  if (time < 1000) return 'fast'
  if (time < 3000) return 'medium'
  return 'slow'
}

const handleViewDetail = (row: LLMResponse) => {
  currentDetail.value = row
  detailVisible.value = true
}

// 初始化
onMounted(async () => {
  // 加载模型列表
  await cacheStore.loadModelList()
})
</script>

<style scoped lang="scss">
.model-comparison {
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

    .performance-stats {
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

    .response-content {
      font-size: 12px;
      line-height: 1.6;
      max-height: 100px;
      overflow-y: auto;
    }

    .fast {
      color: #67c23a;
      font-weight: bold;
    }

    .medium {
      color: #e6a23c;
    }

    .slow {
      color: #f56c6c;
    }
  }
}
</style>
