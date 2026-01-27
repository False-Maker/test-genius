<template>
  <div class="monitoring-dashboard">
    <div class="header">
      <h2>监控Dashboard</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DDTHH:mm:ss"
          @change="handleTimeRangeChange"
        />
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">总请求数</div>
            <div class="stat-value">{{ performanceStats.totalCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">成功率</div>
            <div class="stat-value">{{ (performanceStats.successRate || 0).toFixed(2) }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">平均响应时间</div>
            <div class="stat-value">{{ (performanceStats.avgResponseTime || 0).toFixed(0) }}ms</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">总成本</div>
            <div class="stat-value">¥{{ (costStats.totalCost || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 响应时间趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>响应时间趋势</span>
          </template>
          <div ref="responseTimeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 成功率统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>成功率统计</span>
          </template>
          <div ref="successRateChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- Token使用量 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>Token使用量</span>
          </template>
          <div ref="tokenUsageChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 成本统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>成本统计</span>
          </template>
          <div ref="costChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 模型使用情况 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>模型使用情况</span>
          </template>
          <div ref="modelUsageChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 应用使用情况 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>应用使用情况</span>
          </template>
          <div ref="appUsageChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { monitoringApi, type PerformanceStats, type CostStats } from '@/api/monitoring'

// 时间范围
const timeRange = ref<[string, string]>(() => {
  const end = new Date()
  const start = new Date(end.getTime() - 7 * 24 * 60 * 60 * 1000) // 默认最近7天
  return [
    start.toISOString().slice(0, 19),
    end.toISOString().slice(0, 19)
  ]
})

// 统计数据
const performanceStats = ref<PerformanceStats>({
  totalCount: 0,
  successCount: 0,
  failedCount: 0,
  successRate: 0,
  failureRate: 0,
  avgResponseTime: 0,
  p50ResponseTime: 0,
  p95ResponseTime: 0,
  p99ResponseTime: 0
})

const costStats = ref<CostStats>({
  totalCost: 0,
  avgCost: 0,
  requestCount: 0
})

// 图表引用
const responseTimeChartRef = ref<HTMLDivElement>()
const successRateChartRef = ref<HTMLDivElement>()
const tokenUsageChartRef = ref<HTMLDivElement>()
const costChartRef = ref<HTMLDivElement>()
const modelUsageChartRef = ref<HTMLDivElement>()
const appUsageChartRef = ref<HTMLDivElement>()

// 图表实例
let responseTimeChart: echarts.ECharts | null = null
let successRateChart: echarts.ECharts | null = null
let tokenUsageChart: echarts.ECharts | null = null
let costChart: echarts.ECharts | null = null
let modelUsageChart: echarts.ECharts | null = null
let appUsageChart: echarts.ECharts | null = null

// 加载数据
const loadData = async () => {
  if (!timeRange.value || timeRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  try {
    // 加载性能统计
    const perfRes = await monitoringApi.getPerformanceStats(
      timeRange.value[0],
      timeRange.value[1]
    )
    performanceStats.value = perfRes.data

    // 加载成本统计
    const costRes = await monitoringApi.getCostStats(
      timeRange.value[0],
      timeRange.value[1]
    )
    costStats.value = costRes.data

    // 加载时间序列数据
    await loadTimeSeriesData()
    
    // 加载使用情况统计
    await loadUsageStats()
  } catch (error: any) {
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
  }
}

// 加载时间序列数据
const loadTimeSeriesData = async () => {
  if (!timeRange.value || timeRange.value.length !== 2) return

  try {
    // 响应时间趋势
    const responseTimeRes = await monitoringApi.getTimeSeriesData(
      timeRange.value[0],
      timeRange.value[1],
      'DAY',
      'RESPONSE_TIME'
    )
    updateResponseTimeChart(responseTimeRes.data.data)

    // 成功率趋势
    const successRateRes = await monitoringApi.getTimeSeriesData(
      timeRange.value[0],
      timeRange.value[1],
      'DAY',
      'SUCCESS_RATE'
    )
    updateSuccessRateChart(successRateRes.data.data)

    // Token使用量
    const tokenRes = await monitoringApi.getTimeSeriesData(
      timeRange.value[0],
      timeRange.value[1],
      'DAY',
      'TOKEN_USAGE'
    )
    updateTokenUsageChart(tokenRes.data.data)

    // 成本趋势
    const costRes = await monitoringApi.getTimeSeriesData(
      timeRange.value[0],
      timeRange.value[1],
      'DAY',
      'COST'
    )
    updateCostChart(costRes.data.data)
  } catch (error) {
    console.error('加载时间序列数据失败', error)
  }
}

// 加载使用情况统计
const loadUsageStats = async () => {
  if (!timeRange.value || timeRange.value.length !== 2) return

  try {
    // 模型使用情况
    const modelRes = await monitoringApi.getModelUsageStats(
      timeRange.value[0],
      timeRange.value[1]
    )
    updateModelUsageChart(modelRes.data.modelUsage)

    // 应用使用情况
    const appRes = await monitoringApi.getAppUsageStats(
      timeRange.value[0],
      timeRange.value[1]
    )
    updateAppUsageChart(appRes.data.appUsage)
  } catch (error) {
    console.error('加载使用情况统计失败', error)
  }
}

// 更新响应时间图表
const updateResponseTimeChart = (data: Array<{ time: string; value: number }>) => {
  if (!responseTimeChart || !responseTimeChartRef.value) return

  responseTimeChart.setOption({
    xAxis: {
      type: 'category',
      data: data.map(item => item.time)
    },
    yAxis: {
      type: 'value',
      name: '响应时间(ms)'
    },
    series: [{
      data: data.map(item => item.value),
      type: 'line',
      smooth: true
    }]
  })
}

// 更新成功率图表
const updateSuccessRateChart = (data: Array<{ time: string; value: number }>) => {
  if (!successRateChart || !successRateChartRef.value) return

  const successCount = performanceStats.value.successCount
  const failedCount = performanceStats.value.failedCount

  successRateChart.setOption({
    series: [{
      type: 'pie',
      data: [
        { value: successCount, name: '成功' },
        { value: failedCount, name: '失败' }
      ]
    }]
  })
}

// 更新Token使用量图表
const updateTokenUsageChart = (data: Array<{ time: string; value: number }>) => {
  if (!tokenUsageChart || !tokenUsageChartRef.value) return

  tokenUsageChart.setOption({
    xAxis: {
      type: 'category',
      data: data.map(item => item.time)
    },
    yAxis: {
      type: 'value',
      name: 'Token数量'
    },
    series: [{
      data: data.map(item => item.value),
      type: 'bar'
    }]
  })
}

// 更新成本图表
const updateCostChart = (data: Array<{ time: string; value: number }>) => {
  if (!costChart || !costChartRef.value) return

  costChart.setOption({
    xAxis: {
      type: 'category',
      data: data.map(item => item.time)
    },
    yAxis: {
      type: 'value',
      name: '成本(元)'
    },
    series: [{
      data: data.map(item => item.value),
      type: 'line',
      smooth: true
    }]
  })
}

// 更新模型使用情况图表
const updateModelUsageChart = (data: Record<string, number>) => {
  if (!modelUsageChart || !modelUsageChartRef.value) return

  modelUsageChart.setOption({
    xAxis: {
      type: 'category',
      data: Object.keys(data)
    },
    yAxis: {
      type: 'value',
      name: '请求数'
    },
    series: [{
      data: Object.values(data),
      type: 'bar'
    }]
  })
}

// 更新应用使用情况图表
const updateAppUsageChart = (data: Record<string, number>) => {
  if (!appUsageChart || !appUsageChartRef.value) return

  appUsageChart.setOption({
    xAxis: {
      type: 'category',
      data: Object.keys(data)
    },
    yAxis: {
      type: 'value',
      name: '请求数'
    },
    series: [{
      data: Object.values(data),
      type: 'bar'
    }]
  })
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    if (responseTimeChartRef.value) {
      responseTimeChart = echarts.init(responseTimeChartRef.value)
    }
    if (successRateChartRef.value) {
      successRateChart = echarts.init(successRateChartRef.value)
    }
    if (tokenUsageChartRef.value) {
      tokenUsageChart = echarts.init(tokenUsageChartRef.value)
    }
    if (costChartRef.value) {
      costChart = echarts.init(costChartRef.value)
    }
    if (modelUsageChartRef.value) {
      modelUsageChart = echarts.init(modelUsageChartRef.value)
    }
    if (appUsageChartRef.value) {
      appUsageChart = echarts.init(appUsageChartRef.value)
    }

    // 监听窗口大小变化
    window.addEventListener('resize', handleResize)
  })
}

// 处理窗口大小变化
const handleResize = () => {
  responseTimeChart?.resize()
  successRateChart?.resize()
  tokenUsageChart?.resize()
  costChart?.resize()
  modelUsageChart?.resize()
  appUsageChart?.resize()
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  loadData()
}

// 处理刷新
const handleRefresh = () => {
  loadData()
}

// 组件挂载
onMounted(() => {
  initCharts()
  loadData()
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  responseTimeChart?.dispose()
  successRateChart?.dispose()
  tokenUsageChart?.dispose()
  costChart?.dispose()
  modelUsageChart?.dispose()
  appUsageChart?.dispose()
})
</script>

<style scoped lang="scss">
.monitoring-dashboard {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        .stat-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 10px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;

    .chart-card {
      .chart-container {
        width: 100%;
        height: 300px;
      }
    }
  }
}
</style>
