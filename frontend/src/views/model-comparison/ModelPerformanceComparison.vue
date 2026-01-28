<template>
  <div class="model-performance-comparison">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>模型性能对比</span>
          <div class="header-actions">
            <el-select v-model="selectedTimeRange" @change="handleTimeRangeChange" style="width: 150px; margin-right: 10px;">
              <el-option label="最近1小时" value="1h" />
              <el-option label="最近24小时" value="24h" />
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
            </el-select>
            <el-button type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 模型性能对比表格 -->
      <el-table :data="modelPerformanceData" v-loading="loading" stripe>
        <el-table-column prop="modelCode" label="模型" width="180" fixed="left" />
        <el-table-column prop="totalRequests" label="总请求数" width="120" sortable />
        <el-table-column prop="successCount" label="成功数" width="120" sortable />
        <el-table-column prop="failureCount" label="失败数" width="120" sortable />
        <el-table-column prop="successRate" label="成功率" width="100" sortable>
          <template #default="{ row }">
            <span :style="{ color: getSuccessRateColor(row.successRate) }">
              {{ (row.successRate * 100).toFixed(2) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="150" sortable>
          <template #default="{ row }">
            <span :style="{ color: getResponseTimeColor(row.avgResponseTime) }">
              {{ row.avgResponseTime.toFixed(0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="p50ResponseTime" label="P50响应时间(ms)" width="150" sortable />
        <el-table-column prop="p95ResponseTime" label="P95响应时间(ms)" width="150" sortable />
        <el-table-column prop="p99ResponseTime" label="P99响应时间(ms)" width="150" sortable />
        <el-table-column prop="totalTokens" label="总Token数" width="120" sortable />
        <el-table-column prop="avgTokens" label="平均Token数" width="130" sortable />
        <el-table-column prop="totalCost" label="总成本(¥)" width="120" sortable>
          <template #default="{ row }">
            <span>¥{{ row.totalCost.toFixed(4) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="costPerRequest" label="平均成本(¥/请求)" width="150" sortable>
          <template #default="{ row }">
            <span>¥{{ row.costPerRequest.toFixed(6) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="performanceScore" label="综合评分" width="100" sortable>
          <template #default="{ row }">
            <el-tag :type="getScoreTagType(row.performanceScore)">
              {{ row.performanceScore.toFixed(2) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommended" type="success">推荐</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 性能对比图表 -->
      <el-row :gutter="20" class="charts-row" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>响应时间对比</span>
            </template>
            <div ref="responseTimeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>成功率对比</span>
            </template>
            <div ref="successRateChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>成本对比</span>
            </template>
            <div ref="costChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>综合评分对比</span>
            </template>
            <div ref="scoreChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 模型选择建议 -->
      <el-card class="recommendation-card" style="margin-top: 20px;">
        <template #header>
          <span>智能模型选择建议</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="recommendation-item">
              <div class="recommendation-label">速度优先</div>
              <div class="recommendation-value">{{ fastestModel?.modelCode || '-' }}</div>
              <div class="recommendation-desc">响应时间: {{ fastestModel?.avgResponseTime?.toFixed(0) }}ms</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="recommendation-item">
              <div class="recommendation-label">成功率优先</div>
              <div class="recommendation-value">{{ mostReliableModel?.modelCode || '-' }}</div>
              <div class="recommendation-desc">成功率: {{ ((mostReliableModel?.successRate || 0) * 100).toFixed(2) }}%</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="recommendation-item">
              <div class="recommendation-label">成本优先</div>
              <div class="recommendation-value">{{ cheapestModel?.modelCode || '-' }}</div>
              <div class="recommendation-desc">平均成本: ¥{{ cheapestModel?.costPerRequest?.toFixed(6) }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="24">
            <el-alert
              title="综合推荐"
              type="success"
              :description="`根据综合评分，推荐使用 ${recommendedModel?.modelCode}（评分: ${recommendedModel?.performanceScore?.toFixed(2)}）`"
              show-icon
              :closable="false"
            />
          </el-col>
        </el-row>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { monitoringApi } from '@/api/monitoring'

// 时间范围
const selectedTimeRange = ref('24h')

// 加载状态
const loading = ref(false)

// 模型性能数据
const modelPerformanceData = ref<any[]>([])

// 推荐模型
const fastestModel = ref<any>()
const mostReliableModel = ref<any>()
const cheapestModel = ref<any>()
const recommendedModel = ref<any>()

// 图表引用
const responseTimeChartRef = ref<HTMLElement>()
const successRateChartRef = ref<HTMLElement>()
const costChartRef = ref<HTMLElement>()
const scoreChartRef = ref<HTMLElement>()

// 图表实例
let responseTimeChart: ECharts | null = null
let successRateChart: ECharts | null = null
let costChart: ECharts | null = null
let scoreChart: ECharts | null = null

// 获取模型性能数据
const loadModelPerformance = async () => {
  loading.value = true
  try {
    // 获取性能统计
    const startTime = getStartTime(selectedTimeRange.value)
    const endTime = new Date().toISOString()

    const { data: modelPerformance } = await monitoringApi.getModelPerformanceStats(startTime, endTime)
    modelPerformanceData.value = modelPerformance || []

    // 计算推荐模型
    calculateRecommendations()

    // 渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('获取模型性能数据失败')
  } finally {
    loading.value = false
  }
}

// 计算推荐模型
const calculateRecommendations = () => {
  if (modelPerformanceData.value.length === 0) return

  // 速度优先：响应时间最短
  fastestModel.value = [...modelPerformanceData.value].sort((a, b) => a.avgResponseTime - b.avgResponseTime)[0]

  // 成功率优先：成功率最高
  mostReliableModel.value = [...modelPerformanceData.value].sort((a, b) => b.successRate - a.successRate)[0]

  // 成本优先：平均成本最低
  cheapestModel.value = [...modelPerformanceData.value].sort((a, b) => a.costPerRequest - b.costPerRequest)[0]

  // 综合推荐：评分最高
  recommendedModel.value = [...modelPerformanceData.value].sort((a, b) => b.performanceScore - a.performanceScore)[0]
}

// 渲染图表
const renderCharts = () => {
  renderResponseTimeChart()
  renderSuccessRateChart()
  renderCostChart()
  renderScoreChart()
}

// 渲染响应时间图表
const renderResponseTimeChart = () => {
  if (!responseTimeChartRef.value) return

  if (!responseTimeChart) {
    responseTimeChart = echarts.init(responseTimeChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['P50', 'P95', 'P99']
    },
    xAxis: {
      type: 'category',
      data: modelPerformanceData.value.map(item => item.modelCode)
    },
    yAxis: {
      type: 'value',
      name: '响应时间(ms)'
    },
    series: [
      {
        name: 'P50',
        type: 'bar',
        data: modelPerformanceData.value.map(item => item.p50ResponseTime),
        itemStyle: { color: '#5470c6' }
      },
      {
        name: 'P95',
        type: 'bar',
        data: modelPerformanceData.value.map(item => item.p95ResponseTime),
        itemStyle: { color: '#91cc75' }
      },
      {
        name: 'P99',
        type: 'bar',
        data: modelPerformanceData.value.map(item => item.p99ResponseTime),
        itemStyle: { color: '#fac858' }
      }
    ]
  }

  responseTimeChart.setOption(option)
}

// 渲染成功率图表
const renderSuccessRateChart = () => {
  if (!successRateChartRef.value) return

  if (!successRateChart) {
    successRateChart = echarts.init(successRateChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    xAxis: {
      type: 'category',
      data: modelPerformanceData.value.map(item => item.modelCode)
    },
    yAxis: {
      type: 'value',
      name: '成功率(%)',
      min: 0,
      max: 100
    },
    series: [
      {
        type: 'line',
        data: modelPerformanceData.value.map(item => (item.successRate * 100).toFixed(2)),
        itemStyle: { color: '#5470c6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(84, 112, 198, 0.3)' },
            { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
          ])
        }
      }
    ]
  }

  successRateChart.setOption(option)
}

// 渲染成本图表
const renderCostChart = () => {
  if (!costChartRef.value) return

  if (!costChart) {
    costChart = echarts.init(costChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: modelPerformanceData.value.map(item => item.modelCode)
    },
    yAxis: {
      type: 'value',
      name: '成本(¥)'
    },
    series: [
      {
        type: 'bar',
        data: modelPerformanceData.value.map(item => item.totalCost.toFixed(4)),
        itemStyle: {
          color: (params: any) => {
            if (params.dataIndex === modelPerformanceData.value.indexOf(cheapestModel.value)) {
              return '#67c23a'
            }
            return '#5470c6'
          }
        }
      }
    ]
  }

  costChart.setOption(option)
}

// 渲染评分图表
const renderScoreChart = () => {
  if (!scoreChartRef.value) return

  if (!scoreChart) {
    scoreChart = echarts.init(scoreChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: modelPerformanceData.value.map(item => item.modelCode)
    },
    yAxis: {
      type: 'value',
      name: '评分',
      min: 0,
      max: 100
    },
    series: [
      {
        type: 'bar',
        data: modelPerformanceData.value.map(item => item.performanceScore),
        itemStyle: {
          color: (params: any) => {
            const score = params.data
            if (score >= 90) return '#67c23a'
            if (score >= 80) return '#409eff'
            if (score >= 70) return '#e6a23c'
            return '#f56c6c'
          }
        }
      }
    ]
  }

  scoreChart.setOption(option)
}

// 获取开始时间
const getStartTime = (range: string) => {
  const now = new Date()
  switch (range) {
    case '1h':
      now.setHours(now.getHours() - 1)
      break
    case '24h':
      now.setHours(now.getHours() - 24)
      break
    case '7d':
      now.setDate(now.getDate() - 7)
      break
    case '30d':
      now.setDate(now.getDate() - 30)
      break
  }
  return now.toISOString()
}

// 获取成功率颜色
const getSuccessRateColor = (rate: number) => {
  if (rate >= 0.99) return '#67c23a'
  if (rate >= 0.95) return '#409eff'
  if (rate >= 0.9) return '#e6a23c'
  return '#f56c6c'
}

// 获取响应时间颜色
const getResponseTimeColor = (time: number) => {
  if (time < 1000) return '#67c23a'
  if (time < 2000) return '#409eff'
  if (time < 3000) return '#e6a23c'
  return '#f56c6c'
}

// 获取评分标签类型
const getScoreTagType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

// 时间范围改变
const handleTimeRangeChange = () => {
  loadModelPerformance()
}

// 刷新
const handleRefresh = () => {
  loadModelPerformance()
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  responseTimeChart?.resize()
  successRateChart?.resize()
  costChart?.resize()
  scoreChart?.resize()
}

// 初始化
onMounted(() => {
  loadModelPerformance()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  responseTimeChart?.dispose()
  successRateChart?.dispose()
  costChart?.dispose()
  scoreChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.model-performance-comparison {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-actions {
    display: flex;
    align-items: center;
  }

  .charts-row {
    margin-top: 20px;
  }

  .chart-card {
    .chart-container {
      height: 400px;
    }
  }

  .recommendation-card {
    .recommendation-item {
      text-align: center;
      padding: 20px;
      border: 1px solid #e0e0e0;
      border-radius: 4px;

      .recommendation-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 10px;
      }

      .recommendation-value {
        font-size: 24px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 8px;
      }

      .recommendation-desc {
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>

