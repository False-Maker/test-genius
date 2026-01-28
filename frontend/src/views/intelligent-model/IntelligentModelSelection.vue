<template>
  <div class="intelligent-model-selection">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>智能模型选择</span>
          <el-button type="primary" @click="handleRefreshScores">
            <el-icon><Refresh /></el-icon>
            刷新评分
          </el-button>
        </div>
      </template>

      <!-- 任务类型选择 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="任务类型">
          <el-select v-model="selectedTaskType" @change="handleTaskTypeChange" style="width: 250px;">
            <el-option label="用例生成" value="CASE_GENERATION" />
            <el-option label="UI脚本生成" value="UI_SCRIPT_GENERATION" />
            <el-option label="Agent对话" value="AGENT_CHAT" />
            <el-option label="知识库检索" value="KNOWLEDGE_RETRIEVAL" />
            <el-option label="数据提取" value="DATA_EXTRACTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择场景">
          <el-select v-model="selectedScenario" @change="handleScenarioChange" style="width: 200px;">
            <el-option label="综合优先" value="BALANCED" />
            <el-option label="速度优先" value="SPEED" />
            <el-option label="可靠性优先" value="RELIABILITY" />
            <el-option label="成本优先" value="COST" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 推荐模型卡片 -->
      <el-row :gutter="20" style="margin-bottom: 30px;">
        <el-col :span="8">
          <div class="recommendation-card recommended">
            <div class="card-header">
              <el-icon><Star /></el-icon>
              <span class="card-title">综合推荐</span>
            </div>
            <div class="model-info" v-if="recommendedModel">
              <div class="model-name">{{ recommendedModel.modelName }}</div>
              <div class="model-code">{{ recommendedModel.modelCode }}</div>
              <div class="model-score">
                评分: <span class="score">{{ recommendedModel.performanceScore?.toFixed(2) || 'N/A' }}</span>
              </div>
              <div class="model-priority">优先级: {{ recommendedModel.priority }}</div>
            </div>
            <div v-else class="no-model">
              暂无推荐模型
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="recommendation-card speed">
            <div class="card-header">
              <el-icon><Lightning /></el-icon>
              <span class="card-title">速度优先</span>
            </div>
            <div class="model-info" v-if="speedModel">
              <div class="model-name">{{ speedModel.modelName }}</div>
              <div class="model-code">{{ speedModel.modelCode }}</div>
              <div class="model-score">
                评分: <span class="score">{{ speedModel.performanceScore?.toFixed(2) || 'N/A' }}</span>
              </div>
            </div>
            <div v-else class="no-model">
              暂无可用模型
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="recommendation-card cost">
            <div class="card-header">
              <el-icon><Coin /></el-icon>
              <span class="card-title">成本优先</span>
            </div>
            <div class="model-info" v-if="costModel">
              <div class="model-name">{{ costModel.modelName }}</div>
              <div class="model-code">{{ costModel.modelCode }}</div>
              <div class="model-priority">优先级: {{ costModel.priority }}</div>
            </div>
            <div v-else class="no-model">
              暂无可用模型
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 模型列表 -->
      <el-divider>候选模型列表</el-divider>

      <el-table :data="candidateModels" v-loading="loading" stripe>
        <el-table-column prop="modelCode" label="模型编码" width="150" />
        <el-table-column prop="modelName" label="模型名称" width="200" />
        <el-table-column prop="modelType" label="类型" width="120" />
        <el-table-column prop="performanceScore" label="性能评分" width="120" sortable>
          <template #default="{ row }">
            <el-tag :type="getScoreTagType(row.performanceScore)">
              {{ row.performanceScore?.toFixed(2) || 'N/A' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" sortable />
        <el-table-column prop="maxTokens" label="最大Token" width="120" />
        <el-table-column prop="temperature" label="温度" width="100" />
        <el-table-column prop="isRecommended" label="推荐" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommended === '1'" type="success" effect="dark">
              推荐
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastScoreUpdateTime" label="评分更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastScoreUpdateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleSelectModel(row)">选择此模型</el-button>
            <el-button link type="primary" @click="handleViewDetails(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 评分说明 -->
      <el-alert
        title="评分说明"
        type="info"
        :closable="false"
        style="margin-top: 20px;"
      >
        <p>综合评分计算规则：</p>
        <ul>
          <li>成功率权重：40%</li>
          <li>响应时间权重：60%（基于最近24小时数据）</li>
          <li>评分范围：0-100分，分数越高表示性能越好</li>
          <li>每6小时自动刷新一次评分</li>
          <li>可以手动点击"刷新评分"按钮立即更新</li>
        </ul>
      </el-alert>
    </el-card>

    <!-- 模型详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="模型详情"
      width="600px"
    >
      <el-descriptions :column="1" border v-if="currentModel">
        <el-descriptions-item label="模型ID">{{ currentModel.id }}</el-descriptions-item>
        <el-descriptions-item label="模型编码">{{ currentModel.modelCode }}</el-descriptions-item>
        <el-descriptions-item label="模型名称">{{ currentModel.modelName }}</el-descriptions-item>
        <el-descriptions-item label="模型类型">{{ currentModel.modelType }}</el-descriptions-item>
        <el-descriptions-item label="API端点">{{ currentModel.apiEndpoint }}</el-descriptions-item>
        <el-descriptions-item label="模型版本">{{ currentModel.modelVersion }}</el-descriptions-item>
        <el-descriptions-item label="最大Token数">{{ currentModel.maxTokens }}</el-descriptions-item>
        <el-descriptions-item label="温度参数">{{ currentModel.temperature }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentModel.priority }}</el-descriptions-item>
        <el-descriptions-item label="每日限制">{{ currentModel.dailyLimit }}</el-descriptions-item>
        <el-descriptions-item label="性能评分">
          <el-tag :type="getScoreTagType(currentModel.performanceScore)">
            {{ currentModel.performanceScore?.toFixed(2) || 'N/A' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否推荐">
          <el-tag v-if="currentModel.isRecommended === '1'" type="success" effect="dark">
            推荐
          </el-tag>
          <el-tag v-else type="info">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支持任务类型">
          {{ formatTaskTypes(currentModel.taskTypes) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后评分更新">
          {{ formatTime(currentModel.lastScoreUpdateTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentModel.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentModel.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Star, Lightning, Coin } from '@element-plus/icons-vue'
import { intelligentModelApi, type ModelConfig } from '@/api/intelligentModel'
import { useConfigStore } from '@/store/config'

const configStore = useConfigStore()

// 选中的任务类型和场景
const selectedTaskType = ref('CASE_GENERATION')
const selectedScenario = ref<'SPEED' | 'RELIABILITY' | 'COST' | 'BALANCED'>('BALANCED')

// 模型数据
const candidateModels = ref<ModelConfig[]>([])
const recommendedModel = ref<ModelConfig>()
const speedModel = ref<ModelConfig>()
const reliabilityModel = ref<ModelConfig>()
const costModel = ref<ModelConfig>()

// 加载状态
const loading = ref(false)

// 对话框
const detailDialogVisible = ref(false)
const currentModel = ref<ModelConfig>()

// 加载推荐模型
const loadRecommendedModels = async () => {
  loading.value = true
  try {
    // 综合推荐模型
    const { data: recModel } = await intelligentModelApi.selectOptimalModelByScenario(
      selectedTaskType.value,
      'BALANCED'
    )
    recommendedModel.value = recModel

    // 速度优先模型
    const { data: spdModel } = await intelligentModelApi.selectOptimalModelByScenario(
      selectedTaskType.value,
      'SPEED'
    )
    speedModel.value = spdModel

    // 成本优先模型
    const { data: cstModel } = await intelligentModelApi.selectOptimalModelByScenario(
      selectedTaskType.value,
      'COST'
    )
    costModel.value = cstModel
  } catch (error) {
    ElMessage.error('加载推荐模型失败')
  } finally {
    loading.value = false
  }
}

// 加载候选模型列表
const loadCandidateModels = async () => {
  loading.value = true
  try {
    const { data } = await intelligentModelApi.getCandidateModels(selectedTaskType.value)
    candidateModels.value = data || []
  } catch (error) {
    ElMessage.error('加载模型列表失败')
  } finally {
    loading.value = false
  }
}

// 任务类型改变
const handleTaskTypeChange = () => {
  loadRecommendedModels()
  loadCandidateModels()
}

// 场景改变
const handleScenarioChange = () => {
  loadRecommendedModels()
}

// 刷新评分
const handleRefreshScores = async () => {
  try {
    await intelligentModelApi.refreshScores()
    ElMessage.success('评分刷新成功')
    await loadRecommendedModels()
    await loadCandidateModels()
  } catch (error) {
    ElMessage.error('刷新评分失败')
  }
}

// 选择模型
const handleSelectModel = (model: ModelConfig) => {
  configStore.updateConfig({
    selectedModel: {
      modelCode: model.modelCode,
      modelName: model.modelName,
      provider: model.provider || 'unknown'
    }
  })
  ElMessage.success(`已选择模型: ${model.modelName}`)
}

// 查看详情
const handleViewDetails = (model: ModelConfig) => {
  currentModel.value = model
  detailDialogVisible.value = true
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  return time ? new Date(time).toLocaleString() : '-'
}

// 格式化任务类型
const formatTaskTypes = (taskTypes: string | undefined) => {
  if (!taskTypes) return '-'
  try {
    const types = JSON.parse(taskTypes)
    return Array.isArray(types) ? types.join(', ') : taskTypes
  } catch {
    return taskTypes
  }
}

// 获取评分标签类型
const getScoreTagType = (score: number | undefined) => {
  if (!score) return 'info'
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

// 初始化
onMounted(() => {
  loadRecommendedModels()
  loadCandidateModels()
})
</script>

<style scoped lang="scss">
.intelligent-model-selection {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .recommendation-card {
    padding: 20px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.recommended {
      border-color: #f56c6c;
      background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
    }

    &.speed {
      border-color: #e6a23c;
      background: linear-gradient(135deg, #fdf6ec 0%, #fff 100%);
    }

    &.cost {
      border-color: #67c23a;
      background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 15px;
      font-size: 16px;
      font-weight: bold;
    }

    .model-info {
      text-align: center;

      .model-name {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .model-code {
        color: #666;
        margin-bottom: 8px;
      }

      .model-score {
        margin-bottom: 8px;

        .score {
          font-size: 24px;
          font-weight: bold;
          color: #409eff;
        }
      }

      .model-priority {
        color: #999;
        font-size: 14px;
      }
    }

    .no-model {
      text-align: center;
      color: #999;
      padding: 20px 0;
    }
  }

  ul {
    margin: 10px 0;
    padding-left: 20px;

    li {
      margin-bottom: 5px;
      line-height: 1.6;
    }
  }
}
</style>

