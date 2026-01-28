<template>
  <div class="agent-session-history">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Agent 会话历史</span>
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="Agent">
          <el-select v-model="searchForm.agentId" placeholder="请选择Agent" clearable @change="handleAgentChange">
            <el-option
              v-for="agent in agentList"
              :key="agent.id"
              :label="agent.agentName"
              :value="agent.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会话标题">
          <el-input v-model="searchForm.sessionTitle" placeholder="请输入会话标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 会话列表 -->
      <el-table :data="sessionList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="sessionCode" label="会话编码" width="180" />
        <el-table-column prop="sessionTitle" label="会话标题" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status!)">
              {{ getStatusText(row.status!) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="totalTokens" label="Token使用" width="120">
          <template #default="{ row }">
            {{ row.totalTokens || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalIterations" label="迭代次数" width="120">
          <template #default="{ row }">
            {{ row.totalIterations || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="lastActiveTime" label="最后活跃时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewMessages(row)">查看消息</el-button>
            <el-button link type="primary" @click="handleViewDetails(row)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-if="row.status === 'CLOSED'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="messageDialogVisible"
      title="会话消息"
      width="1000px"
      top="5vh"
    >
      <el-timeline v-loading="messageLoading">
        <el-timeline-item
          v-for="message in messageList"
          :key="message.id"
          :timestamp="formatTime(message.createTime!)"
          placement="top"
          :type="getMessageType(message)"
        >
          <div class="message-item">
            <div class="message-header">
              <el-tag :type="message.role === 'user' ? 'primary' : 'success'" size="small">
                {{ message.role === 'user' ? '用户' : message.role === 'assistant' ? 'Agent' : '工具' }}
              </el-tag>
              <span class="message-meta">
                <span v-if="message.tokensUsed">Tokens: {{ message.tokensUsed }}</span>
                <span v-if="message.responseTime">响应: {{ message.responseTime }}ms</span>
                <span v-if="message.iterationNumber">迭代: {{ message.iterationNumber }}</span>
              </span>
            </div>
            <div class="message-content">{{ message.content }}</div>
            <!-- 工具调用信息 -->
            <div v-if="message.toolCalls && message.toolCalls.length > 0" class="tool-calls">
              <el-divider content-position="left">工具调用</el-divider>
              <div v-for="(call, index) in message.toolCalls" :key="index" class="tool-call">
                <div class="tool-name">
                  <el-icon><Tools /></el-icon>
                  <span>{{ call.function?.name }}</span>
                </div>
                <div class="tool-params">
                  <pre>{{ JSON.stringify(call.function?.arguments, null, 2) }}</pre>
                </div>
              </div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 会话详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="会话详情"
      width="600px"
    >
      <el-descriptions :column="1" border v-if="currentSession">
        <el-descriptions-item label="会话ID">{{ currentSession.id }}</el-descriptions-item>
        <el-descriptions-item label="会话编码">{{ currentSession.sessionCode }}</el-descriptions-item>
        <el-descriptions-item label="会话标题">{{ currentSession.sessionTitle }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentSession.status!)">
            {{ getStatusText(currentSession.status!) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentSession.userName }}</el-descriptions-item>
        <el-descriptions-item label="Token使用">{{ currentSession.totalTokens || 0 }}</el-descriptions-item>
        <el-descriptions-item label="迭代次数">{{ currentSession.totalIterations || 0 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentSession.createTime!) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentSession.updateTime!) }}</el-descriptions-item>
        <el-descriptions-item label="最后活跃时间">{{ formatTime(currentSession.lastActiveTime!) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tools } from '@element-plus/icons-vue'
import { agentApi, type Agent, type AgentSession, type AgentMessage } from '@/api/agent'

// 搜索表单
const searchForm = reactive({
  agentId: undefined as number | undefined,
  sessionTitle: '',
  status: ''
})

// 列表数据
const agentList = ref<Agent[]>([])
const sessionList = ref<AgentSession[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 对话框
const messageDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentSession = ref<AgentSession>()
const messageList = ref<AgentMessage[]>([])
const messageLoading = ref(false)

// 获取Agent列表
const loadAgentList = async () => {
  try {
    const { data } = await agentApi.getAllActiveAgents()
    agentList.value = data || []
  } catch (error) {
    ElMessage.error('获取Agent列表失败')
  }
}

// 获取会话列表
const loadSessionList = async () => {
  loading.value = true
  try {
    let data: AgentSession[] = []
    if (searchForm.agentId) {
      const response = await agentApi.getSessionsByAgentId(searchForm.agentId)
      data = response.data || []
    } else {
      // 获取所有活跃Agent的会话
      for (const agent of agentList.value) {
        const response = await agentApi.getSessionsByAgentId(agent.id!)
        data = data.concat(response.data || [])
      }
    }

    // 本地过滤
    sessionList.value = data.filter(session => {
      if (searchForm.sessionTitle && !session.sessionTitle?.includes(searchForm.sessionTitle)) {
        return false
      }
      if (searchForm.status && session.status !== searchForm.status) {
        return false
      }
      return true
    })

    total.value = sessionList.value.length
  } catch (error) {
    ElMessage.error('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadSessionList()
}

// 重置
const handleReset = () => {
  searchForm.agentId = undefined
  searchForm.sessionTitle = ''
  searchForm.status = ''
  currentPage.value = 1
  loadSessionList()
}

// 刷新
const handleRefresh = () => {
  loadSessionList()
}

// Agent改变
const handleAgentChange = () => {
  loadSessionList()
}

// 查看消息
const handleViewMessages = async (session: AgentSession) => {
  currentSession.value = session
  messageDialogVisible.value = true
  messageLoading.value = true
  try {
    const { data } = await agentApi.getSessionHistory(session.id!)
    messageList.value = data || []
  } catch (error) {
    ElMessage.error('获取消息失败')
  } finally {
    messageLoading.value = false
  }
}

// 查看详情
const handleViewDetails = (session: AgentSession) => {
  currentSession.value = session
  detailDialogVisible.value = true
}

// 删除会话
const handleDelete = (session: AgentSession) => {
  ElMessageBox.confirm(`确定要删除会话 "${session.sessionTitle}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await agentApi.deleteSession(session.id!)
      ElMessage.success('删除成功')
      loadSessionList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadSessionList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadSessionList()
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'CLOSED': return 'info'
    case 'EXPIRED': return 'warning'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE': return '活跃'
    case 'CLOSED': return '已关闭'
    case 'EXPIRED': return '已过期'
    default: return status
  }
}

// 获取消息类型
const getMessageType = (message: AgentMessage) => {
  if (message.role === 'user') return 'primary'
  if (message.role === 'assistant') return 'success'
  return 'warning'
}

// 初始化
onMounted(async () => {
  await loadAgentList()
  loadSessionList()
})
</script>

<style scoped lang="scss">
.agent-session-history {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .message-item {
    padding: 12px;
    background-color: #f6f6f6;
    border-radius: 4px;

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .message-meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #666;
      }
    }

    .message-content {
      margin-bottom: 8px;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .tool-calls {
      margin-top: 12px;

      .tool-call {
        margin-bottom: 12px;

        .tool-name {
          display: flex;
          align-items: center;
          gap: 6px;
          font-weight: bold;
          margin-bottom: 4px;
        }

        .tool-params {
          background-color: #fff;
          padding: 8px;
          border-radius: 4px;
          overflow-x: auto;

          pre {
            margin: 0;
            font-size: 12px;
            line-height: 1.5;
          }
        }
      }
    }
  }
}
</style>

