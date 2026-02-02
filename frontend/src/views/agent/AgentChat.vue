<template>
  <div class="agent-chat">
    <el-row :gutter="20">
      <!-- 左侧：会话列表 -->
      <el-col :span="6">
        <el-card class="session-list-card">
          <template #header>
            <div class="card-header">
              <span>会话列表</span>
              <el-button type="primary" size="small" @click="handleCreateSession">新建会话</el-button>
            </div>
          </template>
          <div class="session-list">
            <div
              v-for="session in sessionList"
              :key="session.id"
              :class="['session-item', { active: currentSession?.id === session.id }]"
              @click="handleSelectSession(session)"
            >
              <div class="session-title">
                {{ session.sessionTitle || `会话 ${session.id}` }}
              </div>
              <div class="session-meta">
                <el-tag :type="getStatusType(session.status!)" size="small">
                  {{ getStatusText(session.status!) }}
                </el-tag>
                <span class="session-time">{{ formatTime(session.lastActiveTime!) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：对话区域 -->
      <el-col :span="18">
        <el-card class="chat-card">
          <!-- 会话信息 -->
          <div class="chat-header" v-if="currentSession">
            <div class="agent-info">
              <el-icon><User /></el-icon>
              <span>{{ agentInfo?.agentName || 'Agent' }}</span>
            </div>
            <div class="session-actions">
              <el-button size="small" @click="handleViewHistory">会话历史</el-button>
              <el-button size="small" @click="handleCloseSession">关闭会话</el-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div
              v-for="message in messageList"
              :key="message.id"
              :class="['message-item', message.role]"
            >
              <div class="message-header">
                <el-icon v-if="message.role === 'user'"><User /></el-icon>
                <el-icon v-else><Service /></el-icon>
                <span class="message-role">
                  {{ message.role === 'user' ? '用户' : message.role === 'assistant' ? 'Agent' : '工具' }}
                </span>
                <span class="message-time">{{ formatTime(message.createTime!) }}</span>
              </div>
              <div class="message-content">
                <div v-if="message.role === 'user'" class="user-message">
                  {{ message.content }}
                </div>
                <div v-else-if="message.role === 'assistant'" class="assistant-message">
                  <div v-html="formatContent(message.content)"></div>
                  <!-- 工具调用信息 -->
                  <div v-if="message.toolCalls && message.toolCalls.length > 0" class="tool-calls">
                    <div v-for="(call, index) in message.toolCalls" :key="index" class="tool-call">
                      <el-icon><Tools /></el-icon>
                      <span>调用工具: {{ call.function?.name }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="tool-message">
                  <div class="tool-content">{{ message.content }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="请输入消息..."
              @keydown.enter.prevent="handleEnterKey"
              :disabled="!currentSession || sending"
            />
            <div class="input-actions">
              <div class="input-stats" v-if="currentSession">
                <span>Tokens: {{ currentSession.totalTokens }}</span>
                <span>迭代: {{ currentSession.totalIterations }}</span>
              </div>
              <el-button
                type="primary"
                :loading="sending"
                :disabled="!inputMessage.trim() || !currentSession"
                @click="handleSendMessage"
              >
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 会话历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="会话历史"
      width="800px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="message in messageList"
          :key="message.id"
          :timestamp="formatTime(message.createTime!)"
          placement="top"
        >
          <el-tag :type="message.role === 'user' ? 'primary' : 'success'">
            {{ message.role === 'user' ? '用户' : message.role === 'assistant' ? 'Agent' : '工具' }}
          </el-tag>
          <div class="history-content">{{ message.content }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Robot, Tools } from '@element-plus/icons-vue'
import { agentApi, type Agent, type AgentSession, type AgentMessage } from '@/api/agent'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const agentId = ref<number>(Number(route.params.agentId))
const agentInfo = ref<Agent>()
const sessionList = ref<AgentSession[]>([])
const currentSession = ref<AgentSession>()
const messageList = ref<AgentMessage[]>([])
const inputMessage = ref('')
const sending = ref(false)
const messageListRef = ref<HTMLElement>()
const historyDialogVisible = ref(false)

// 获取Agent信息
const loadAgentInfo = async () => {
  try {
    const { data } = await agentApi.getAgentById(agentId.value)
    agentInfo.value = data
  } catch (error) {
    ElMessage.error('获取Agent信息失败')
  }
}

// 获取会话列表
const loadSessionList = async () => {
  try {
    const { data } = await agentApi.getSessionsByAgentId(agentId.value)
    sessionList.value = data || []
    if (sessionList.value.length > 0 && !currentSession.value) {
      handleSelectSession(sessionList.value[0])
    }
  } catch (error) {
    ElMessage.error('获取会话列表失败')
  }
}

// 创建会话
const handleCreateSession = async () => {
  try {
    // 从用户状态管理获取用户信息
    const userId = userStore.userInfo?.id || 1
    const userName = userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
    
    const { data } = await agentApi.createSession({
      sessionCode: `SESSION-${Date.now()}`,
      agentId: agentId.value,
      userId: userId,
      userName: userName,
      sessionTitle: `新会话 ${new Date().toLocaleString()}`,
      status: 'ACTIVE'
    })
    ElMessage.success('创建会话成功')
    await loadSessionList()
    handleSelectSession(data!)
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

// 选择会话
const handleSelectSession = async (session: AgentSession) => {
  currentSession.value = session
  await loadMessages(session.id!)
}

// 加载消息
const loadMessages = async (sessionId: number) => {
  try {
    const { data } = await agentApi.getSessionHistory(sessionId)
    messageList.value = data || []
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSession.value) return

  sending.value = true
  try {
    // 添加用户消息到列表
    const userMessage: AgentMessage = {
      sessionId: currentSession.value.id!,
      messageType: 'USER',
      role: 'user',
      content: inputMessage.value,
      createTime: new Date().toISOString()
    }
    messageList.value.push(userMessage)

    // 调用Agent对话接口
    const { data } = await agentApi.chatWithAgent(currentSession.value.id!, inputMessage.value)

    // 添加Assistant消息
    const assistantMessage: AgentMessage = {
      sessionId: currentSession.value.id!,
      messageType: 'ASSISTANT',
      role: 'assistant',
      content: data.content || data.response || '',
      toolCalls: data.tool_calls,
      toolResults: data.tool_results,
      tokensUsed: data.tokens_used,
      responseTime: data.response_time,
      modelCode: data.model_code,
      iterationNumber: data.iteration_number,
      createTime: new Date().toISOString()
    }
    messageList.value.push(assistantMessage)

    // 清空输入框
    inputMessage.value = ''
    scrollToBottom()
    ElMessage.success('发送成功')
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

// 回车发送
const handleEnterKey = (e: KeyboardEvent) => {
  if (e.ctrlKey || e.metaKey) {
    // Ctrl+Enter 或 Cmd+Enter 发送
    handleSendMessage()
  }
}

// 关闭会话
const handleCloseSession = async () => {
  if (!currentSession.value) return
  try {
    await agentApi.closeSession(currentSession.value.id!)
    ElMessage.success('关闭会话成功')
    currentSession.value = undefined
    messageList.value = []
    await loadSessionList()
  } catch (error) {
    ElMessage.error('关闭会话失败')
  }
}

// 查看历史
const handleViewHistory = () => {
  historyDialogVisible.value = true
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

// 格式化内容
const formatContent = (content: string) => {
  // 简单的Markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
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

// 初始化
onMounted(async () => {
  await loadAgentInfo()
  await loadSessionList()
})
</script>

<style scoped lang="scss">
.agent-chat {
  .session-list-card {
    height: calc(100vh - 120px);
    overflow: hidden;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .session-list {
      height: calc(100% - 60px);
      overflow-y: auto;

      .session-item {
        padding: 12px;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background-color: #f5f5f5;
        }

        &.active {
          background-color: #e6f4ff;
          border-color: #1890ff;
        }

        .session-title {
          font-weight: bold;
          margin-bottom: 8px;
        }

        .session-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 12px;
          color: #666;
        }
      }
    }
  }

  .chat-card {
    height: calc(100vh - 120px);
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 16px;
      border-bottom: 1px solid #e0e0e0;

      .agent-info {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: bold;
        font-size: 16px;
      }
    }

    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 20px 0;

      .message-item {
        margin-bottom: 20px;

        &.user {
          .message-content {
            .user-message {
              background-color: #e6f4ff;
              border-radius: 8px 8px 0 8px;
            }
          }
        }

        &.assistant {
          .message-content {
            .assistant-message {
              background-color: #f6f6f6;
              border-radius: 8px 8px 8px 0;
            }
          }
        }

        .message-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          font-size: 12px;
          color: #666;

          .message-role {
            font-weight: bold;
          }
        }

        .message-content {
          padding: 12px;
          max-width: 80%;

          .tool-calls {
            margin-top: 8px;
            padding-top: 8px;
            border-top: 1px solid #e0e0e0;

            .tool-call {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 12px;
              color: #1890ff;
              margin-bottom: 4px;
            }
          }
        }
      }
    }

    .chat-input {
      border-top: 1px solid #e0e0e0;
      padding-top: 16px;

      .input-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12px;

        .input-stats {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #666;
        }
      }
    }
  }
}

.history-content {
  margin-top: 8px;
  padding: 8px;
  background-color: #f6f6f6;
  border-radius: 4px;
}
</style>

