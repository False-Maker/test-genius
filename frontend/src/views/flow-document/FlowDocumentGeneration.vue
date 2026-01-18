<template>
  <div class="flow-document-generation">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>流程文档生成管理</h2>
          <span class="subtitle">生成测试场景图和路径图</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 场景图生成 -->
        <el-tab-pane label="场景图生成" name="scene">
          <el-form :model="sceneForm" :rules="sceneFormRules" ref="sceneFormRef" label-width="120px">
            <el-form-item label="数据来源">
              <el-radio-group v-model="sceneForm.sourceType">
                <el-radio label="requirement">按需求检索内容方式/用例复用</el-radio>
                <el-radio label="cases">按用例检索内容方式/用例复用</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="需求" prop="requirementId" v-if="sceneForm.sourceType === 'requirement'">
              <el-select
                v-model="sceneForm.requirementId"
                placeholder="请选择需求"
                filterable
                clearable
                style="width: 100%"
                :loading="requirementLoading"
              >
                <el-option
                  v-for="req in requirementList"
                  :key="req.id"
                  :label="`${req.requirementCode} - ${req.requirementName}`"
                  :value="req.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="用例列表" prop="caseIds" v-if="sceneForm.sourceType === 'cases'">
              <el-select
                v-model="sceneForm.caseIds"
                placeholder="请选择用例（可多选）"
                multiple
                filterable
                clearable
                style="width: 100%"
                :loading="caseLoading"
              >
                <el-option
                  v-for="tc in testCaseList"
                  :key="tc.id"
                  :label="`${tc.caseCode} - ${tc.caseName}`"
                  :value="tc.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="图表标题">
              <el-input v-model="sceneForm.title" placeholder="请输入图表标题（可选）" />
            </el-form-item>

            <el-form-item label="图表方向">
              <el-select v-model="sceneForm.direction" style="width: 100%">
                <el-option label="从左到右 (LR)" value="LR" />
                <el-option label="从上到下 (TB)" value="TB" />
                <el-option label="从右到左 (RL)" value="RL" />
                <el-option label="从下到上 (BT)" value="BT" />
              </el-select>
            </el-form-item>

            <el-form-item label="包含用例详情">
              <el-switch v-model="sceneForm.includeCaseDetails" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleGenerateScene" :loading="sceneLoading">
                生成场景图
              </el-button>
              <el-button @click="resetSceneForm">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 场景图结果展示 -->
          <div v-if="sceneResult" class="result-section">
            <el-divider>生成结果</el-divider>
            <div class="result-header">
              <h3>{{ sceneResult.title }}</h3>
              <div class="result-actions">
                <el-button size="small" @click="copySceneMermaid">复制Mermaid代码</el-button>
                <el-dropdown @command="handleSceneExport">
                  <el-button size="small">
                    导出图<el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="PNG">PNG图片</el-dropdown-item>
                      <el-dropdown-item command="SVG">SVG图片</el-dropdown-item>
                      <el-dropdown-item command="PDF">PDF文档</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div class="mermaid-container">
              <div ref="sceneMermaidRef" class="mermaid-diagram"></div>
            </div>
            <el-collapse>
              <el-collapse-item title="查看Mermaid代码" name="code">
                <pre class="mermaid-code">{{ sceneResult.mermaidCode }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>

        <!-- 路径图生成 -->
        <el-tab-pane label="路径图生成" name="path">
          <el-form :model="pathForm" :rules="pathFormRules" ref="pathFormRef" label-width="120px">
            <el-form-item label="数据来源">
              <el-radio-group v-model="pathForm.sourceType">
                <el-radio label="case">单个用例</el-radio>
                <el-radio label="cases">多个用例</el-radio>
                <el-radio label="requirement">按需求检索内容方式</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="用例" prop="caseId" v-if="pathForm.sourceType === 'case'">
              <el-select
                v-model="pathForm.caseId"
                placeholder="请选择用例"
                filterable
                clearable
                style="width: 100%"
                :loading="caseLoading"
              >
                <el-option
                  v-for="tc in testCaseList"
                  :key="tc.id"
                  :label="`${tc.caseCode} - ${tc.caseName}`"
                  :value="tc.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="用例列表" prop="caseIds" v-if="pathForm.sourceType === 'cases'">
              <el-select
                v-model="pathForm.caseIds"
                placeholder="请选择用例（可多选）"
                multiple
                filterable
                clearable
                style="width: 100%"
                :loading="caseLoading"
              >
                <el-option
                  v-for="tc in testCaseList"
                  :key="tc.id"
                  :label="`${tc.caseCode} - ${tc.caseName}`"
                  :value="tc.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="需求" prop="requirementId" v-if="pathForm.sourceType === 'requirement'">
              <el-select
                v-model="pathForm.requirementId"
                placeholder="请选择需求"
                filterable
                clearable
                style="width: 100%"
                :loading="requirementLoading"
              >
                <el-option
                  v-for="req in requirementList"
                  :key="req.id"
                  :label="`${req.requirementCode} - ${req.requirementName}`"
                  :value="req.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="图表标题">
              <el-input v-model="pathForm.title" placeholder="请输入图表标题（可选）" />
            </el-form-item>

            <el-form-item label="图表方向">
              <el-select v-model="pathForm.direction" style="width: 100%">
                <el-option label="从左到右 (LR)" value="LR" />
                <el-option label="从上到下 (TB)" value="TB" />
                <el-option label="从右到左 (RL)" value="RL" />
                <el-option label="从下到上 (BT)" value="BT" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleGeneratePath" :loading="pathLoading">
                生成路径图
              </el-button>
              <el-button @click="resetPathForm">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 路径图结果展示 -->
          <div v-if="pathResult" class="result-section">
            <el-divider>生成结果</el-divider>
            <div class="result-header">
              <h3>{{ pathResult.title }}</h3>
              <div class="result-actions">
                <el-button size="small" @click="copyPathMermaid">复制Mermaid代码</el-button>
                <el-dropdown @command="handlePathExport">
                  <el-button size="small">
                    导出图<el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="PNG">PNG图片</el-dropdown-item>
                      <el-dropdown-item command="SVG">SVG图片</el-dropdown-item>
                      <el-dropdown-item command="PDF">PDF文档</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div class="mermaid-container">
              <div ref="pathMermaidRef" class="mermaid-diagram"></div>
            </div>
            <el-collapse>
              <el-collapse-item title="查看Mermaid代码" name="code">
                <pre class="mermaid-code">{{ pathResult.mermaidCode }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { flowDocumentApi, type SceneDiagramRequest, type PathDiagramRequest } from '@/api/flowDocument'
import { requirementApi, type TestRequirement } from '@/api/requirement'
import { testCaseApi, type TestCase } from '@/api/testCase'

// 加载Mermaid库（使用CDN?
const loadMermaid = () => {
  return new Promise<void>((resolve) => {
    if ((window as any).mermaid) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js'
    script.onload = () => {
      (window as any).mermaid.initialize({ startOnLoad: false, theme: 'default' })
      resolve()
    }
    script.onerror = () => {
      ElMessage.error('Mermaid库加载失败，图表预览功能可能无法使用')
      resolve()
    }
    document.head.appendChild(script)
  })
}

onMounted(async () => {
  await loadMermaid()
  loadRequirements()
  loadTestCases()
})

// 标签页页页
const activeTab = ref('scene')

// 需求ID列表表表
const requirementList = ref<TestRequirement[]>([])
const requirementLoading = ref(false)

// 用例列表
const testCaseList = ref<TestCase[]>([])
const caseLoading = ref(false)

// 场景图表单单单
const sceneFormRef = ref()
const sceneForm = reactive<SceneDiagramRequest & { sourceType: string }>({
  sourceType: 'requirement',
  requirementId: undefined,
  caseIds: undefined,
  title: '',
  direction: 'LR',
  includeCaseDetails: false
})

const sceneFormRules = {
  requirementId: [
    { required: true, message: '请选择需求', trigger: 'change', 
      validator: (_: any, value: any, callback: any) => {
        if (sceneForm.sourceType === 'requirement' && !value) {
          callback(new Error('请选择需求'))
        } else {
          callback()
        }
      }
    }
  ],
  caseIds: [
    { required: true, message: '请选择用例', trigger: 'change',
      validator: (_: any, value: any, callback: any) => {
        if (sceneForm.sourceType === 'cases' && (!value || value.length === 0)) {
          callback(new Error('请至少选择一个用例'))
        } else {
          callback()
        }
      }
    }
  ]
}

const sceneLoading = ref(false)
const sceneResult = ref<any>(null)
const sceneMermaidRef = ref<HTMLElement>()

// 路径图表单单单
const pathFormRef = ref()
const pathForm = reactive<PathDiagramRequest & { sourceType: string }>({
  sourceType: 'case',
  caseId: undefined,
  caseIds: undefined,
  requirementId: undefined,
  title: '',
  direction: 'LR'
})

const pathFormRules = {
  caseId: [
    { required: true, message: '请选择用例', trigger: 'change',
      validator: (_: any, value: any, callback: any) => {
        if (pathForm.sourceType === 'case' && !value) {
          callback(new Error('请选择用例'))
        } else {
          callback()
        }
      }
    }
  ],
  caseIds: [
    { required: true, message: '请选择用例', trigger: 'change',
      validator: (_: any, value: any, callback: any) => {
        if (pathForm.sourceType === 'cases' && (!value || value.length === 0)) {
          callback(new Error('请至少选择一个用例'))
        } else {
          callback()
        }
      }
    }
  ],
  requirementId: [
    { required: true, message: '请选择需求', trigger: 'change',
      validator: (_: any, value: any, callback: any) => {
        if (pathForm.sourceType === 'requirement' && !value) {
          callback(new Error('请选择需求'))
        } else {
          callback()
        }
      }
    }
  ]
}

const pathLoading = ref(false)
const pathResult = ref<any>(null)
const pathMermaidRef = ref<HTMLElement>()

// 加载需求ID列表
const loadRequirements = async () => {
  requirementLoading.value = true
  try {
    const res = await requirementApi.getRequirementList({ page: 0, size: 1000 })
    requirementList.value = res.data.content || []
  } catch (error) {
    console.error('加载需求ID列表失败', error)
  } finally {
    requirementLoading.value = false
  }
}

// 加载用例列表
const loadTestCases = async () => {
  caseLoading.value = true
  try {
    const res = await testCaseApi.getTestCaseList({ page: 0, size: 1000 })
    testCaseList.value = res.data.content || []
  } catch (error) {
    console.error('加载用例列表失败:', error)
  } finally {
    caseLoading.value = false
  }
}

// 生成场景图图图
const handleGenerateScene = async () => {
  if (!sceneFormRef.value) return
  await sceneFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    sceneLoading.value = true
    try {
      const request: SceneDiagramRequest = {
        title: sceneForm.title || undefined,
        direction: sceneForm.direction as any,
        includeCaseDetails: sceneForm.includeCaseDetails
      }

      if (sceneForm.sourceType === 'requirement') {
        request.requirementId = sceneForm.requirementId
      } else {
        request.caseIds = sceneForm.caseIds
      }

      const res = await flowDocumentApi.generateSceneDiagram(request)
      sceneResult.value = res.data

      await nextTick()
      renderMermaid(sceneMermaidRef.value, res.data.mermaidCode)

      ElMessage.success('场景图生成成')
    } catch (error: any) {
      ElMessage.error(error.message || '生成场景图失')
    } finally {
      sceneLoading.value = false
    }
  })
}

// 生成路径图图图
const handleGeneratePath = async () => {
  if (!pathFormRef.value) return
  await pathFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    pathLoading.value = true
    try {
      const request: PathDiagramRequest = {
        title: pathForm.title || undefined,
        direction: pathForm.direction as any
      }

      if (pathForm.sourceType === 'case') {
        request.caseId = pathForm.caseId
      } else if (pathForm.sourceType === 'cases') {
        request.caseIds = pathForm.caseIds
      } else {
        request.requirementId = pathForm.requirementId
      }

      const res = await flowDocumentApi.generatePathDiagram(request)
      pathResult.value = res.data

      await nextTick()
      renderMermaid(pathMermaidRef.value, res.data.mermaidCode)

      ElMessage.success('路径图生成成功')
    } catch (error: any) {
      ElMessage.error(error.message || '生成路径图失败')
    } finally {
      pathLoading.value = false
    }
  })
}

// 渲染Mermaid图表
const renderMermaid = async (container: HTMLElement | undefined, mermaidCode: string) => {
  if (!container || !(window as any).mermaid) return

  try {
    container.innerHTML = ''
    const id = `mermaid-${Date.now()}`
    container.id = id
    
    const { svg } = await (window as any).mermaid.render(id, mermaidCode)
    container.innerHTML = svg
  } catch (error) {
    console.error('渲染Mermaid图表失败:', error)
    container.innerHTML = '<div style="color: red;">图表渲染失败，请检查Mermaid代码</div>'
  }
}

// 复制Mermaid代码
const copySceneMermaid = () => {
  if (!sceneResult.value) return
  navigator.clipboard.writeText(sceneResult.value.mermaidCode).then(() => {
    ElMessage.success('Mermaid代码已复制到剪贴板')
  })
}

const copyPathMermaid = () => {
  if (!pathResult.value) return
  navigator.clipboard.writeText(pathResult.value.mermaidCode).then(() => {
    ElMessage.success('Mermaid代码已复制到剪贴板')
  })
}

// 导出场景图图图
const handleSceneExport = async (format: string) => {
  if (!sceneResult.value) return
  try {
    const fileName = sceneResult.value.title || 'scene_diagram'
    await flowDocumentApi.exportSceneDiagramFile(sceneResult.value.mermaidCode, format as any, fileName)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 导出路径图图图
const handlePathExport = async (format: string) => {
  if (!pathResult.value) return
  try {
    const fileName = pathResult.value.title || 'path_diagram'
    await flowDocumentApi.exportPathDiagramFile(pathResult.value.mermaidCode, format as any, fileName)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 重置表单
const resetSceneForm = () => {
  sceneFormRef.value?.resetFields()
  sceneResult.value = null
}

const resetPathForm = () => {
  pathFormRef.value?.resetFields()
  pathResult.value = null
}

const handleTabChange = () => {
  // 切换标签页时的处理理理
}
</script>

<style scoped lang="scss">
.flow-document-generation {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }

    .subtitle {
      color: #909399;
      font-size: 14px;
    }
  }

  .result-section {
    margin-top: 30px;

    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        font-size: 18px;
      }

      .result-actions {
        display: flex;
        gap: 10px;
      }
    }

    .mermaid-container {
      background: var(--el-bg-color);
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 20px;
      margin-bottom: 20px;
      overflow-x: auto;

      .mermaid-diagram {
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 300px;
      }
    }

    .mermaid-code {
      background: rgba(255, 255, 255, 0.05);
      padding: 15px;
      border-radius: 4px;
      overflow-x: auto;
      font-family: 'Courier New', monospace;
      font-size: 12px;
      line-height: 1.6;
    }
  }
}
</style>
