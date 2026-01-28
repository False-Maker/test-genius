<template>
  <div class="workflow-editor">
    <div class="header">
      <h2>工作流编辑器</h2>
      <div class="header-actions">
        <el-button @click="handleLoad">加载</el-button>
        <el-button @click="handleSave">保存</el-button>
        <el-button @click="handleValidate">验证</el-button>
        <el-button type="primary" @click="handleExecute">执行</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：节点面板 -->
      <el-col :span="4">
        <el-card class="node-panel" shadow="never">
          <template #header>
            <span>节点库</span>
          </template>
          <div class="node-list">
            <div class="node-category">
              <div class="category-title">输入节点</div>
              <div
                v-for="node in inputNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                {{ node.name }}
              </div>
            </div>
            <div class="node-category">
              <div class="category-title">处理节点</div>
              <div
                v-for="node in processNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                {{ node.name }}
              </div>
            </div>
            <div class="node-category">
              <div class="category-title">转换节点</div>
              <div
                v-for="node in transformNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                {{ node.name }}
              </div>
            </div>
            <div class="node-category">
              <div class="category-title">输出节点</div>
              <div
                v-for="node in outputNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                {{ node.name }}
              </div>
            </div>
            <div class="node-category">
              <div class="category-title">控制节点</div>
              <div
                v-for="node in controlNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                {{ node.name }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：画布区域 -->
      <el-col :span="16">
        <el-card class="canvas-area" shadow="never">
          <template #header>
            <span>工作流画布</span>
          </template>
          <div class="canvas-wrapper">
            <VueFlow
              v-model="elements"
              :default-viewport="{ zoom: 1 }"
              :min-zoom="0.2"
              :max-zoom="4"
              class="vue-flow-container"
              @node-click="handleNodeClick"
              @edge-click="handleEdgeClick"
              @pane-click="handlePaneClick"
              @connect="handleConnect"
            >
              <Background />
              <Controls />
              <MiniMap />
            </VueFlow>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：配置面板 -->
      <el-col :span="4">
        <el-card class="config-panel" shadow="never">
          <template #header>
            <span>{{ selectedNode ? '节点配置' : selectedEdge ? '连线配置' : '配置面板' }}</span>
          </template>
          <div v-if="selectedNode" class="config-content">
            <el-form :model="selectedNode.data.config" label-width="80px">
              <el-form-item label="节点名称">
                <el-input v-model="selectedNode.data.name" />
              </el-form-item>
              <el-form-item label="节点类型">
                <el-input v-model="selectedNode.data.type" disabled />
              </el-form-item>
              <!-- 动态配置组件 -->
              <component
                v-if="selectedNode.data.type === NODE_TYPES.REQUIREMENT_INPUT"
                :is="RequirementInputConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.TEST_CASE_INPUT"
                :is="TestCaseInputConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.FILE_UPLOAD"
                :is="FileUploadConfig"
                v-model="selectedNode.data.config"
              />

              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.REQUIREMENT_ANALYSIS"
                :is="RequirementAnalysisConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.TEMPLATE_SELECT"
                :is="TemplateSelectConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.PROMPT_GENERATE"
                :is="PromptGenerateConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.LLM_CALL"
                :is="LLMCallConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.RESULT_PARSE"
                :is="ResultParseConfig"
                v-model="selectedNode.data.config"
              />

              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.FORMAT_TRANSFORM"
                :is="FormatTransformConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.DATA_CLEAN"
                :is="DataCleanConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.DATA_MERGE"
                :is="DataMergeConfig"
                v-model="selectedNode.data.config"
              />

              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.CASE_SAVE"
                :is="CaseSaveConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.REPORT_GENERATE"
                :is="ReportGenerateConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.FILE_EXPORT"
                :is="FileExportConfig"
                v-model="selectedNode.data.config"
              />

              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.CONDITION"
                :is="ConditionConfig"
                v-model="selectedNode.data.config"
              />
              <component
                v-else-if="selectedNode.data.type === NODE_TYPES.LOOP"
                :is="LoopConfig"
                v-model="selectedNode.data.config"
              />
              
              <!-- 兜底提示 -->
              <div v-else class="config-placeholder">
                该节点类型暂未配置组件，请联系管理员
              </div>
            </el-form>
            <el-button type="danger" size="small" @click="handleDeleteNode" style="width: 100%; margin-top: 10px">
              删除节点
            </el-button>
          </div>
          <div v-else-if="selectedEdge" class="config-content">
            <el-form label-width="80px">
              <el-form-item label="源节点">
                <el-input :value="selectedEdge.source" disabled />
              </el-form-item>
              <el-form-item label="目标节点">
                <el-input :value="selectedEdge.target" disabled />
              </el-form-item>
            </el-form>
            <el-button type="danger" size="small" @click="handleDeleteEdge" style="width: 100%; margin-top: 10px">
              删除连线
            </el-button>
          </div>
          <div v-else class="config-empty">
            请选择一个节点或连线进行配置
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 工作流选择对话框 -->
    <WorkflowSelectionDialog
      v-model="selectionDialogVisible"
      @select="handleWorkflowSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineAsyncComponent } from 'vue'
import { ElMessage, ElDialog } from 'element-plus'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import { workflowApi, type WorkflowNode, type WorkflowEdge, type WorkflowConfig, type WorkflowDefinition } from '@/api/workflow'
import WorkflowSelectionDialog from './components/WorkflowSelectionDialog.vue'
import { NODE_TYPES, NODE_CONFIGS, NODE_CATEGORIES } from '@/config/workflow-nodes'

// 动态加载配置组件
const RequirementInputConfig = defineAsyncComponent(() => import('./components/nodes/input/RequirementInputConfig.vue'))
const TestCaseInputConfig = defineAsyncComponent(() => import('./components/nodes/input/TestCaseInputConfig.vue'))
const FileUploadConfig = defineAsyncComponent(() => import('./components/nodes/input/FileUploadConfig.vue'))
const RequirementAnalysisConfig = defineAsyncComponent(() => import('./components/nodes/process/RequirementAnalysisConfig.vue'))
const TemplateSelectConfig = defineAsyncComponent(() => import('./components/nodes/process/TemplateSelectConfig.vue'))
const PromptGenerateConfig = defineAsyncComponent(() => import('./components/nodes/process/PromptGenerateConfig.vue'))
const LLMCallConfig = defineAsyncComponent(() => import('./components/nodes/process/LLMCallConfig.vue'))
const ResultParseConfig = defineAsyncComponent(() => import('./components/nodes/process/ResultParseConfig.vue'))
const FormatTransformConfig = defineAsyncComponent(() => import('./components/nodes/transform/FormatTransformConfig.vue'))
const DataCleanConfig = defineAsyncComponent(() => import('./components/nodes/transform/DataCleanConfig.vue'))
const DataMergeConfig = defineAsyncComponent(() => import('./components/nodes/transform/DataMergeConfig.vue'))
const CaseSaveConfig = defineAsyncComponent(() => import('./components/nodes/output/CaseSaveConfig.vue'))
const ReportGenerateConfig = defineAsyncComponent(() => import('./components/nodes/output/ReportGenerateConfig.vue'))
const FileExportConfig = defineAsyncComponent(() => import('./components/nodes/output/FileExportConfig.vue'))
const ConditionConfig = defineAsyncComponent(() => import('./components/nodes/control/ConditionConfig.vue'))
const LoopConfig = defineAsyncComponent(() => import('./components/nodes/control/LoopConfig.vue'))

// 节点列表计算属性
const getNodeListByCategory = (categoryKey: string) => {
  return Object.keys(NODE_CONFIGS)
    .filter(type => NODE_CONFIGS[type].category === categoryKey)
    .map(type => ({
      type,
      ...NODE_CONFIGS[type]
    }))
}

// Vue Flow 元素
const elements = ref<any[]>([])
const selectedNode = ref<any>(null)
const selectedEdge = ref<any>(null)
const currentWorkflowId = ref<number | null>(null)
const selectionDialogVisible = ref(false)

// 拖拽处理
const handleDragStart = (event: DragEvent, node: { type: string; name: string }) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('node', JSON.stringify(node))
    event.dataTransfer.effectAllowed = 'copy'
  }
}

// 节点点击
const handleNodeClick = (event: any) => {
  selectedNode.value = event
  selectedEdge.value = null
}

// 连线点击
const handleEdgeClick = (event: any) => {
  selectedEdge.value = event
  selectedNode.value = null
}

// 画布点击
const handlePaneClick = () => {
  selectedNode.value = null
  selectedEdge.value = null
}

// 连接处理
const handleConnect = (connection: any) => {
  const edge = {
    id: `edge-${connection.source}-${connection.target}`,
    source: connection.source,
    target: connection.target,
    type: 'default',
    animated: false
  }
  elements.value.push(edge)
}

// 删除节点
const handleDeleteNode = () => {
  if (selectedNode.value) {
    // 删除节点
    elements.value = elements.value.filter(
      (el: any) => el.id !== selectedNode.value.id
    )
    // 删除相关的连线
    elements.value = elements.value.filter(
      (el: any) => el.source !== selectedNode.value.id && el.target !== selectedNode.value.id
    )
    selectedNode.value = null
    ElMessage.success('节点已删除')
  }
}

// 删除连线
const handleDeleteEdge = () => {
  if (selectedEdge.value) {
    elements.value = elements.value.filter(
      (el: any) => el.id !== selectedEdge.value.id
    )
    selectedEdge.value = null
    ElMessage.success('连线已删除')
  }
}

// 将Vue Flow元素转换为工作流配置
const elementsToWorkflowConfig = (): WorkflowConfig => {
  const nodes: WorkflowNode[] = []
  const edges: WorkflowEdge[] = []

  elements.value.forEach((el: any) => {
    if (el.type === 'input' || el.type === 'default' || !el.type) {
      // 这是节点
      if (el.data) {
        nodes.push({
          id: el.id,
          type: el.data.type,
          name: el.data.name || el.data.type,
          config: el.data.config || {}
        })
      }
    } else if (el.type === 'default' || el.source) {
      // 这是连线
      edges.push({
        source: el.source,
        target: el.target
      })
    }
  })

  return { nodes, edges }
}

// 将工作流配置转换为Vue Flow元素
const workflowConfigToElements = (config: WorkflowConfig, workflowId?: number) => {
  const flowElements: any[] = []
  let x = 100
  let y = 100
  const nodeHeight = 150

  // 创建节点
  config.nodes.forEach((node, index) => {
    flowElements.push({
      id: node.id,
      type: 'default',
      position: { x, y: y + index * nodeHeight },
      data: {
        label: node.name,
        type: node.type,
        name: node.name,
        config: node.config || {}
      },
      style: {
        background: getNodeColor(node.type),
        color: '#fff',
        border: '2px solid #409EFF',
        borderRadius: '8px',
        padding: '10px',
        minWidth: '150px'
      }
    })
  })

  // 创建连线
  config.edges.forEach((edge) => {
    flowElements.push({
      id: `edge-${edge.source}-${edge.target}`,
      source: edge.source,
      target: edge.target,
      type: 'default',
      animated: false
    })
  })

  return flowElements
}

// 获取节点颜色
const getNodeColor = (type: string): string => {
  return NODE_CONFIGS[type]?.color || '#409EFF'
}

// 加载工作流
const handleLoad = async () => {
  selectionDialogVisible.value = true
}

// 选择工作流处理
const handleWorkflowSelect = async (workflow: WorkflowDefinition) => {
  try {
    if (workflow.workflowConfig) {
      const config = JSON.parse(workflow.workflowConfig)
      elements.value = workflowConfigToElements(config, workflow.id)
      currentWorkflowId.value = workflow.id
      ElMessage.success(`已加载工作流: ${workflow.workflowName}`)
    } else {
      ElMessage.warning('该工作流配置为空')
    }
  } catch (error) {
    console.error('加载工作流配置失败:', error)
    ElMessage.error('加载工作流配置失败')
  }
}

// 保存工作流
const handleSave = async () => {
  try {
    const config = elementsToWorkflowConfig()
    
    if (config.nodes.length === 0) {
      ElMessage.warning('请至少添加一个节点')
      return
    }

    const workflow: WorkflowDefinition = {
      workflowCode: `WORKFLOW_${Date.now()}`,
      workflowName: '新工作流',
      workflowType: 'CASE_GENERATION',
      workflowConfig: JSON.stringify(config),
      version: 1,
      isActive: true,
      isDefault: false,
      executionCount: 0
    }

    if (currentWorkflowId.value) {
      // 更新现有工作流
      await workflowApi.updateWorkflow(currentWorkflowId.value, workflow)
      ElMessage.success('工作流更新成功')
    } else {
      // 创建新工作流
      const result = await workflowApi.createWorkflow(workflow)
      if (result.data && result.data.id) {
        currentWorkflowId.value = result.data.id
      }
      ElMessage.success('工作流保存成功')
    }
  } catch (error: any) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  }
}

// 验证工作流
const handleValidate = async () => {
  try {
    const config = elementsToWorkflowConfig()
    
    if (config.nodes.length === 0) {
      ElMessage.warning('请至少添加一个节点')
      return
    }

    const result = await workflowApi.validateWorkflowConfig(JSON.stringify(config))
    if (result.data.valid) {
      ElMessage.success('工作流配置有效')
    } else {
      ElMessage.warning('工作流配置有错误: ' + result.data.errors.join(', '))
    }
  } catch (error: any) {
    ElMessage.error('验证失败: ' + (error.message || '未知错误'))
  }
}

// 执行工作流
const handleExecute = async () => {
  try {
    const config = elementsToWorkflowConfig()
    
    if (config.nodes.length === 0) {
      ElMessage.warning('请至少添加一个节点')
      return
    }

    const result = await workflowApi.executeWorkflow(
      JSON.stringify(config),
      { requirement_text: '测试需求' },
      currentWorkflowId.value || undefined
    )

    if (result.data.status === 'success') {
      ElMessage.success('工作流执行成功')
    } else {
      ElMessage.error('工作流执行失败: ' + (result.data.error || '未知错误'))
    }
  } catch (error: any) {
    ElMessage.error('执行失败: ' + (error.message || '未知错误'))
  }
}

// 初始化：监听画布的drop事件
onMounted(() => {
  const canvas = document.querySelector('.vue-flow-container')
  if (canvas) {
    canvas.addEventListener('drop', (event: Event) => {
      const e = event as DragEvent
      e.preventDefault()
      const nodeData = e.dataTransfer?.getData('node')
      if (!nodeData) return

      const node = JSON.parse(nodeData)
      const rect = canvas.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top

      const newNode = {
        id: `node_${Date.now()}`,
        type: 'default',
        position: { x, y },
        data: {
          label: node.name,
          type: node.type,
          name: node.name,
          config: {}
        },
        style: {
          background: getNodeColor(node.type),
          color: '#fff',
          border: '2px solid #409EFF',
          borderRadius: '8px',
          padding: '10px',
          minWidth: '150px'
        }
      }

      elements.value.push(newNode)
    })

    canvas.addEventListener('dragover', (event: Event) => {
      event.preventDefault()
    })
  }
})
</script>

<style scoped lang="scss">
.workflow-editor {
  padding: 20px;
  height: calc(100vh - 100px);

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

  .node-panel {
    height: calc(100vh - 200px);
    overflow-y: auto;

    .node-list {
      .node-category {
        margin-bottom: 20px;

        .category-title {
          font-weight: bold;
          margin-bottom: 10px;
          color: #666;
          font-size: 14px;
        }

        .node-item {
          padding: 8px 12px;
          margin-bottom: 8px;
          background: #f5f7fa;
          border-radius: 4px;
          cursor: move;
          user-select: none;
          font-size: 13px;
          transition: all 0.3s;

          &:hover {
            background: #e4e7ed;
            transform: translateX(5px);
          }

          &:active {
            opacity: 0.7;
          }
        }
      }
    }
  }

  .canvas-area {
    height: calc(100vh - 200px);

    .canvas-wrapper {
      width: 100%;
      height: 100%;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      overflow: hidden;

      .vue-flow-container {
        width: 100%;
        height: 100%;
      }
    }
  }

  .config-panel {
    height: calc(100vh - 200px);
    overflow-y: auto;

    .config-content {
      .el-form {
        .el-form-item {
          margin-bottom: 15px;
        }
      }
    }

    .config-empty {
      text-align: center;
      color: #999;
      padding: 40px 0;
    }
  }
}

// Vue Flow 节点样式
:deep(.vue-flow__node) {
  cursor: pointer;
  
  &.selected {
    box-shadow: 0 0 0 2px #409EFF;
  }
}

:deep(.vue-flow__edge) {
  cursor: pointer;
  
  &.selected {
    .vue-flow__edge-path {
      stroke: #409EFF;
      stroke-width: 3;
    }
  }
}

:deep(.vue-flow__handle) {
  width: 8px;
  height: 8px;
  background: #409EFF;
  border: 2px solid #fff;
}
</style>
