<template>
  <div class="ab-test-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>A/B测试管理</span>
          <el-button size="small" type="primary" @click="handleCreateAbTest">创建A/B测试</el-button>
        </div>
      </template>

      <!-- A/B测试列表 -->
      <el-table :data="abTests" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="testName" label="测试名称" min-width="200" />
        <el-table-column prop="testDescription" label="测试描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="版本A" width="120">
          <template #default="scope">
            <el-tag>版本 {{ getVersionNumber(scope.row.versionAId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本B" width="120">
          <template #default="scope">
            <el-tag>版本 {{ getVersionNumber(scope.row.versionBId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流量分配" width="150">
          <template #default="scope">
            {{ scope.row.trafficSplitA }}% / {{ scope.row.trafficSplitB }}%
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="autoSelectEnabled" label="自动选择" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.autoSelectEnabled === '1' ? 'success' : 'info'">
              {{ scope.row.autoSelectEnabled === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleViewStatistics(scope.row)">
              统计
            </el-button>
            <el-button
              v-if="scope.row.status === 'draft'"
              size="small"
              link
              type="success"
              @click="handleStartAbTest(scope.row)"
            >
              启动
            </el-button>
            <el-button
              v-if="scope.row.status === 'running'"
              size="small"
              link
              type="warning"
              @click="handlePauseAbTest(scope.row)"
            >
              暂停
            </el-button>
            <el-button
              v-if="scope.row.status === 'running' || scope.row.status === 'paused'"
              size="small"
              link
              type="info"
              @click="handleStopAbTest(scope.row)"
            >
              停止
            </el-button>
            <el-button
              v-if="scope.row.status !== 'running'"
              size="small"
              link
              type="danger"
              @click="handleDeleteAbTest(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建A/B测试对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建A/B测试" width="800px">
      <el-form ref="createFormRef" :model="createForm" :rules="createFormRules" label-width="120px">
        <el-form-item label="测试名称" prop="testName">
          <el-input v-model="createForm.testName" placeholder="请输入测试名称" />
        </el-form-item>
        <el-form-item label="测试描述">
          <el-input
            v-model="createForm.testDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入测试描述"
          />
        </el-form-item>
        <el-form-item label="版本A" prop="versionAId">
          <el-select v-model="createForm.versionAId" placeholder="请选择版本A" style="width: 100%">
            <el-option
              v-for="version in availableVersions"
              :key="version.id"
              :label="`版本 ${version.versionNumber} - ${version.versionName || '未命名'}`"
              :value="version.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="版本B" prop="versionBId">
          <el-select v-model="createForm.versionBId" placeholder="请选择版本B" style="width: 100%">
            <el-option
              v-for="version in availableVersions"
              :key="version.id"
              :label="`版本 ${version.versionNumber} - ${version.versionName || '未命名'}`"
              :value="version.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="流量分配">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-input-number
                v-model="createForm.trafficSplitA"
                :min="0"
                :max="100"
                :step="5"
                placeholder="版本A流量比例"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="12">
              <el-input-number
                v-model="createForm.trafficSplitB"
                :min="0"
                :max="100"
                :step="5"
                placeholder="版本B流量比例"
                style="width: 100%"
              />
            </el-col>
          </el-row>
          <div class="form-tip">流量分配比例之和必须等于100</div>
        </el-form-item>
        <el-form-item label="自动选择">
          <el-radio-group v-model="createForm.autoSelectEnabled">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="createForm.autoSelectEnabled === '1'" label="最小样本数">
          <el-input-number
            v-model="createForm.minSamples"
            :min="10"
            :max="10000"
            placeholder="达到此数量后才进行自动选择"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="createForm.autoSelectEnabled === '1'" label="选择标准">
          <el-select v-model="createForm.selectionCriteria" style="width: 100%">
            <el-option label="成功率" value="success_rate" />
            <el-option label="响应时间" value="response_time" />
            <el-option label="用户评分" value="user_rating" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleSubmitCreate">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog v-model="statisticsDialogVisible" title="A/B测试统计" width="1000px">
      <div v-if="statistics" class="statistics-content">
        <!-- 总体统计 -->
        <el-card style="margin-bottom: 20px">
          <template #header>
            <span>总体统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">总执行次数</div>
                <div class="stat-value">{{ statistics.totalExecutions }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">更优版本</div>
                <div class="stat-value" :class="statistics.betterVersion === 'A' ? 'success' : 'warning'">
                  版本 {{ statistics.betterVersion }}
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 版本对比 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>版本A统计</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="执行次数">
                  {{ statistics.versionA.totalExecutions }}
                </el-descriptions-item>
                <el-descriptions-item label="成功次数">
                  {{ statistics.versionA.successCount }}
                </el-descriptions-item>
                <el-descriptions-item label="成功率">
                  {{ statistics.versionA.successRate }}%
                </el-descriptions-item>
                <el-descriptions-item label="平均响应时间">
                  {{ statistics.versionA.avgResponseTime || '-' }}ms
                </el-descriptions-item>
                <el-descriptions-item label="平均评分">
                  {{ statistics.versionA.avgRating || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>版本B统计</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="执行次数">
                  {{ statistics.versionB.totalExecutions }}
                </el-descriptions-item>
                <el-descriptions-item label="成功次数">
                  {{ statistics.versionB.successCount }}
                </el-descriptions-item>
                <el-descriptions-item label="成功率">
                  {{ statistics.versionB.successRate }}%
                </el-descriptions-item>
                <el-descriptions-item label="平均响应时间">
                  {{ statistics.versionB.avgResponseTime || '-' }}ms
                </el-descriptions-item>
                <el-descriptions-item label="平均评分">
                  {{ statistics.versionB.avgRating || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>

        <!-- 对比图表 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>性能对比图表</span>
          </template>
          <div ref="chartContainer" style="width: 100%; height: 300px"></div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import * as echarts from 'echarts'
import {
  promptTemplateAbTestApi,
  promptTemplateVersionApi,
  type PromptTemplateAbTest,
  type PromptTemplateAbTestRequest,
  type AbTestStatistics,
  type PromptTemplateVersion
} from '@/api/promptTemplate'

interface Props {
  templateId: number
}

const props = defineProps<Props>()

// 响应式数据
const loading = ref(false)
const abTests = ref<PromptTemplateAbTest[]>([])
const availableVersions = ref<PromptTemplateVersion[]>([])

const createDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)
const createLoading = ref(false)
const currentAbTest = ref<PromptTemplateAbTest | null>(null)
const statistics = ref<AbTestStatistics | null>(null)

const createFormRef = ref<FormInstance>()
const createForm = reactive<PromptTemplateAbTestRequest>({
  testName: '',
  testDescription: '',
  versionAId: 0,
  versionBId: 0,
  trafficSplitA: 50,
  trafficSplitB: 50,
  autoSelectEnabled: '0',
  minSamples: 100,
  selectionCriteria: 'success_rate'
})

const createFormRules: FormRules = {
  testName: [{ required: true, message: '请输入测试名称', trigger: 'blur' }],
  versionAId: [{ required: true, message: '请选择版本A', trigger: 'change' }],
  versionBId: [{ required: true, message: '请选择版本B', trigger: 'change' }]
}

const chartContainer = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 计算属性
const versionMap = computed(() => {
  const map = new Map<number, number>()
  availableVersions.value.forEach(v => {
    if (v.id && v.versionNumber) {
      map.set(v.id, v.versionNumber)
    }
  })
  return map
})

// 方法
const loadAbTests = async () => {
  loading.value = true
  try {
    const response = await promptTemplateAbTestApi.getAbTests(props.templateId)
    if (response.data) {
      abTests.value = response.data
    }
  } catch (error) {
    console.error('加载A/B测试列表失败:', error)
    ElMessage.error('加载A/B测试列表失败')
  } finally {
    loading.value = false
  }
}

const loadVersions = async () => {
  try {
    const response = await promptTemplateVersionApi.getVersions(props.templateId)
    if (response.data) {
      availableVersions.value = response.data
    }
  } catch (error) {
    console.error('加载版本列表失败:', error)
  }
}

const getVersionNumber = (versionId: number) => {
  return versionMap.value.get(versionId) || '-'
}

const getStatusType = (status?: string) => {
  switch (status) {
    case 'running':
      return 'success'
    case 'paused':
      return 'warning'
    case 'completed':
      return 'info'
    default:
      return ''
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'draft':
      return '草稿'
    case 'running':
      return '运行中'
    case 'paused':
      return '已暂停'
    case 'completed':
      return '已完成'
    default:
      return status || '-'
  }
}

const handleCreateAbTest = () => {
  createForm.testName = ''
  createForm.testDescription = ''
  createForm.versionAId = 0
  createForm.versionBId = 0
  createForm.trafficSplitA = 50
  createForm.trafficSplitB = 50
  createForm.autoSelectEnabled = '0'
  createForm.minSamples = 100
  createForm.selectionCriteria = 'success_rate'
  createDialogVisible.value = true
}

const handleSubmitCreate = async () => {
  if (!createFormRef.value) return

  // 验证流量分配
  if (createForm.trafficSplitA! + createForm.trafficSplitB! !== 100) {
    ElMessage.warning('流量分配比例之和必须等于100')
    return
  }

  // 验证版本选择
  if (createForm.versionAId === createForm.versionBId) {
    ElMessage.warning('版本A和版本B不能相同')
    return
  }

  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      createLoading.value = true
      try {
        await promptTemplateAbTestApi.createAbTest(props.templateId, createForm)
        ElMessage.success('创建A/B测试成功')
        createDialogVisible.value = false
        loadAbTests()
      } catch (error: any) {
        console.error('创建A/B测试失败:', error)
        ElMessage.error(error.response?.data?.message || '创建A/B测试失败')
      } finally {
        createLoading.value = false
      }
    }
  })
}

const handleStartAbTest = async (abTest: PromptTemplateAbTest) => {
  try {
    await promptTemplateAbTestApi.startAbTest(props.templateId, abTest.id!)
    ElMessage.success('启动A/B测试成功')
    loadAbTests()
  } catch (error: any) {
    console.error('启动A/B测试失败:', error)
    ElMessage.error(error.response?.data?.message || '启动A/B测试失败')
  }
}

const handlePauseAbTest = async (abTest: PromptTemplateAbTest) => {
  try {
    await promptTemplateAbTestApi.pauseAbTest(props.templateId, abTest.id!)
    ElMessage.success('暂停A/B测试成功')
    loadAbTests()
  } catch (error: any) {
    console.error('暂停A/B测试失败:', error)
    ElMessage.error(error.response?.data?.message || '暂停A/B测试失败')
  }
}

const handleStopAbTest = async (abTest: PromptTemplateAbTest) => {
  try {
    await ElMessageBox.confirm('确定要停止A/B测试吗？', '确认停止', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await promptTemplateAbTestApi.stopAbTest(props.templateId, abTest.id!)
    ElMessage.success('停止A/B测试成功')
    loadAbTests()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('停止A/B测试失败:', error)
      ElMessage.error(error.response?.data?.message || '停止A/B测试失败')
    }
  }
}

const handleDeleteAbTest = async (abTest: PromptTemplateAbTest) => {
  try {
    await ElMessageBox.confirm('确定要删除A/B测试吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await promptTemplateAbTestApi.deleteAbTest(props.templateId, abTest.id!)
    ElMessage.success('删除成功')
    loadAbTests()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleViewStatistics = async (abTest: PromptTemplateAbTest) => {
  currentAbTest.value = abTest
  try {
    const response = await promptTemplateAbTestApi.getAbTestStatistics(props.templateId, abTest.id!)
    if (response.data) {
      statistics.value = response.data
      statisticsDialogVisible.value = true
      nextTick(() => {
        updateChart()
      })
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
    ElMessage.error('获取统计信息失败')
  }
}

const updateChart = () => {
  if (!chartContainer.value || !statistics.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }

  const option = {
    title: {
      text: 'A/B测试性能对比',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['版本A', '版本B'],
      top: 30
    },
    xAxis: {
      type: 'category',
      data: ['成功率(%)', '平均响应时间(ms)', '平均评分']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '版本A',
        type: 'bar',
        data: [
          statistics.value.versionA.successRate,
          statistics.value.versionA.avgResponseTime || 0,
          statistics.value.versionA.avgRating || 0
        ],
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '版本B',
        type: 'bar',
        data: [
          statistics.value.versionB.successRate,
          statistics.value.versionB.avgResponseTime || 0,
          statistics.value.versionB.avgRating || 0
        ],
        itemStyle: { color: '#67C23A' }
      }
    ]
  }

  chartInstance.setOption(option)
}

// 监听流量分配变化
watch(
  () => createForm.trafficSplitA,
  (newVal) => {
    if (newVal !== undefined) {
      createForm.trafficSplitB = 100 - newVal
    }
  }
)

watch(
  () => createForm.trafficSplitB,
  (newVal) => {
    if (newVal !== undefined) {
      createForm.trafficSplitA = 100 - newVal
    }
  }
)

// 初始化
onMounted(async () => {
  await loadVersions()
  await loadAbTests()
})
</script>

<style scoped lang="scss">
.ab-test-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .statistics-content {
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

        &.warning {
          color: #e6a23c;
        }
      }
    }
  }
}
</style>
