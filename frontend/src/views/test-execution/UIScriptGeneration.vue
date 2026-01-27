<template>


  <div class="ui-script-generation">


    <el-card>


      <template #header>


        <div class="card-header">


          <h2>UI脚本生成管理</h2>


        </div>


      </template>





      <el-form :model="form" :rules="formRules" ref="formRef" label-width="140px">


        <el-form-item label="自然语言描述" prop="naturalLanguageDesc">


          <el-input


            v-model="form.naturalLanguageDesc"


            type="textarea"


            :rows="4"


            placeholder="请输入操作描述，例如：点击登录按钮，输入用户名和密码，然后点击提交按钮"


            maxlength="1000"


            show-word-limit


          />


        </el-form-item>





        <el-form-item label="页面代码">


          <el-radio-group v-model="pageCodeInputType">


            <el-radio label="upload">上传文件</el-radio>


            <el-radio label="url">URL地址</el-radio>


            <el-radio label="input">直接输入</el-radio>


          </el-radio-group>


        </el-form-item>





        <el-form-item v-if="pageCodeInputType === 'upload'" label="上传页面代码文件">


          <FileUpload


            v-model="form.pageCodeUrl"


            :limit="1"


            accept=".html,.htm,.js,.jsx,.vue"


            tip-text="支持上传HTML、JavaScript、Vue等页面代码文件"


            @success="handleFileUploadSuccess"


          />


        </el-form-item>





        <el-form-item v-if="pageCodeInputType === 'url'" label="页面代码URL">


          <el-input


            v-model="form.pageCodeUrl"


            placeholder="请输入页面代码的URL地址"


            clearable


          />


        </el-form-item>





        <el-form-item v-if="pageCodeInputType === 'input'" label="页面代码内容">


          <el-input


            v-model="pageCodeContent"


            type="textarea"


            :rows="10"


            placeholder="请直接粘贴页面代码（HTML/JavaScript/Vue等）"


          />


        </el-form-item>





        <el-form-item label="页面URL">


          <el-input


            v-model="form.pageUrl"


            placeholder="可选：输入目标页面的URL，用于脚本中的页面打开"


            clearable


          />


        </el-form-item>





        <el-form-item label="脚本类型" prop="scriptType">


          <el-select


            v-model="form.scriptType"


            placeholder="请选择脚本类型"


            style="width: 100%"


          >


            <el-option label="Selenium" value="SELENIUM" />


            <el-option label="Playwright" value="PLAYWRIGHT" />


          </el-select>


        </el-form-item>





        <el-form-item label="脚本语言" prop="scriptLanguage">


          <el-select


            v-model="form.scriptLanguage"


            placeholder="请选择脚本语言"


            style="width: 100%"


          >


            <el-option label="Python" value="PYTHON" />


            <el-option label="Java" value="JAVA" />


            <el-option label="JavaScript" value="JAVASCRIPT" />


          </el-select>


        </el-form-item>





        <el-form-item label="使用LLM优化">


          <el-switch v-model="form.useLlm" />


          <span style="margin-left: 10px; color: #909399; font-size: 12px">


            启用后使用大语言模型优化脚本生成


          </span>


        </el-form-item>





        <el-form-item label="关联需求ID（可选）">


          <el-select


            v-model="form.requirementId"


            placeholder="请选择需求ID（可选）"


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





        <el-form-item>


          <el-button type="primary" @click="handleGenerate" :loading="generateLoading">


            生成脚本


          </el-button>


          <el-button @click="handleReset">重置</el-button>


        </el-form-item>


      </el-form>





      <!-- 生成结果 -->


      <el-card v-if="generationResult" class="result-card" style="margin-top: 20px">


        <template #header>


          <div class="card-header">


            <span>生成结果</span>


            <el-tag :type="getStatusType(generationResult.taskStatus)">


              {{ getStatusText(generationResult.taskStatus) }}


            </el-tag>


          </div>


        </template>





        <div v-if="generationResult.taskStatus === 'PROCESSING' || generationResult.taskStatus === 'PENDING'">


          <el-progress :percentage="generationResult.progress || 0" />


          <p style="margin-top: 10px; color: #909399">


            {{ generationResult.taskStatus === 'PENDING' ? '任务已提交，等待处理中...' : '正在生成脚本，请稍�?..' }}


          </p>


        </div>





        <div v-else-if="generationResult.taskStatus === 'SUCCESS'">


          <el-alert


            type="success"


            :closable="false"


            show-icon


            style="margin-bottom: 20px"


          >


            脚本生成成功


          </el-alert>





          <el-tabs v-model="activeTab">


            <el-tab-pane label="生成的脚本�" name="script">


              <el-input


                v-model="generationResult.scriptContent"


                type="textarea"


                :rows="20"


                readonly


                style="font-family: 'Courier New', monospace"


              />


              <div style="margin-top: 10px">


                <el-button type="primary" @click="handleCopyScript">复制脚本</el-button>


                <el-button type="success" @click="handleDownloadScript">下载脚本</el-button>


              </div>


            </el-tab-pane>





            <el-tab-pane v-if="generationResult.elementsUsed && generationResult.elementsUsed.length > 0" label="使用的元素�" name="elements">


              <el-table :data="generationResult.elementsUsed" border stripe>


                <el-table-column prop="elementType" label="元素类型" width="120" />


                <el-table-column prop="elementLocatorType" label="定位值方式" width="120" />


                <el-table-column prop="elementLocatorValue" label="定位值�" show-overflow-tooltip />


                <el-table-column prop="elementText" label="元素文本" width="150" />


              </el-table>


            </el-tab-pane>





            <el-tab-pane v-if="generationResult.steps && generationResult.steps.length > 0" label="操作步骤" name="steps">


              <el-timeline>


                <el-timeline-item


                  v-for="(step, index) in generationResult.steps"


                  :key="index"


                  :timestamp="`步骤 ${index + 1}`"


                  placement="top"


                >


                  <el-card>


                    <h4>{{ step.action || step.step }}</h4>


                    <p v-if="step.description">{{ step.description }}</p>


                    <p v-if="step.target" style="color: #909399">目标: {{ step.target }}</p>


                  </el-card>


                </el-timeline-item>


              </el-timeline>


            </el-tab-pane>


          </el-tabs>


        </div>





        <div v-else-if="generationResult.taskStatus === 'FAILED'">


          <el-alert


            type="error"


            :closable="false"


            show-icon


          >


            <template #title>


              <div>脚本生成失败</div>


              <div v-if="generationResult.errorMessage" style="margin-top: 10px; font-size: 12px">


                {{ generationResult.errorMessage }}


              </div>


            </template>


          </el-alert>


        </div>


      </el-card>


    </el-card>


  </div>


</template>





<script setup lang="ts">


import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'


import { useRouter } from 'vue-router'


import { ElMessage, type FormInstance, type FormRules } from 'element-plus'


import { uiScriptGenerationApi, type UIScriptGenerationRequest, type UIScriptGenerationResult } from '@/api/uiScriptGeneration'


import { workflowApi, type WorkflowDefinition } from '@/api/workflow'


import { useCacheStore } from '@/store/cache'


import FileUpload from '@/components/FileUpload.vue'


import { fileUploadApi } from '@/api/fileUpload'





// 使用缓存store


const cacheStore = useCacheStore()





// 响应式数据从store获取数据


const requirementList = computed(() => cacheStore.requirementList)


const requirementLoading = computed(() => cacheStore.loading.requirementList)





const form = reactive<UIScriptGenerationRequest & { workflowId?: number }>({


  naturalLanguageDesc: '',


  pageCodeUrl: '',


  pageUrl: '',


  scriptType: 'SELENIUM',


  scriptLanguage: 'PYTHON',


  useLlm: true,


  requirementId: undefined,


  workflowId: undefined


})

// 工作流相关
const workflowList = ref<WorkflowDefinition[]>([])
const workflowLoading = ref(false)
const router = useRouter()





const generationResult = ref<UIScriptGenerationResult | null>(null)


let pollTimer: number | null = null





// 表单验证规则


const formRules: FormRules = {


  naturalLanguageDesc: [


    { required: true, message: '请输入自然语言描述', trigger: 'blur' }


  ],


  scriptType: [


    { required: true, message: '请选择脚本类型', trigger: 'change' }


  ],


  scriptLanguage: [


    { required: true, message: '请选择脚本语言', trigger: 'change' }


  ]


}





// 获取状态文�?


const getStatusText = (status?: string) => {


  const statusMap: Record<string, string> = {


    PENDING: '等待中',


    PROCESSING: '处理中',


    SUCCESS: '成功',


    FAILED: '失败'


  }


  return statusMap[status || ''] || status || '-'


}





// 获取状态类�?


const getStatusType = (status?: string) => {


  const typeMap: Record<string, string> = {


    PENDING: 'info',


    PROCESSING: 'warning',


    SUCCESS: 'success',


    FAILED: 'danger'


  }


  return typeMap[status || ''] || ''


}





// 文件上传成功


const handleFileUploadSuccess = (response: any) => {


  if (response && response.fileUrl) {


    form.pageCodeUrl = response.fileUrl


  }


}





// 加载需求ID列�?


const loadRequirementList = async () => {


  try {


    await cacheStore.loadRequirementList()


  } catch (error) {


    console.error('加载需求ID列表失败', error)


  }


}

// 加载工作流列表
const loadWorkflows = async () => {
  workflowLoading.value = true
  try {
    const response = await workflowApi.getWorkflowsByType('UI_SCRIPT_GENERATION')
    if (response.data) {
      workflowList.value = response.data.filter(w => w.isActive)
    }
  } catch (error) {
    console.error('加载工作流列表失败:', error)
    ElMessage.error('加载工作流列表失败')
  } finally {
    workflowLoading.value = false
  }
}

// 打开工作流编辑器
const handleOpenWorkflowEditor = () => {
  router.push('/workflow')
}

// 使用工作流生成脚本
const handleGenerateWithWorkflow = async () => {
  try {
    const workflowResponse = await workflowApi.getWorkflow(form.workflowId!)
    if (!workflowResponse.data) {
      ElMessage.error('工作流不存在')
      generateLoading.value = false
      return
    }
    const workflow = workflowResponse.data
    const inputData: Record<string, any> = {
      natural_language_desc: form.naturalLanguageDesc,
      page_code_url: form.pageCodeUrl || undefined,
      page_url: form.pageUrl || undefined,
      script_type: form.scriptType,
      script_language: form.scriptLanguage,
      use_llm: form.useLlm,
      requirement_id: form.requirementId
    }
    const result = await workflowApi.executeWorkflow(
      workflow.workflowConfig,
      inputData,
      workflow.id,
      workflow.workflowCode,
      workflow.version
    )
    if (result.data.status === 'success') {
      generationResult.value = {
        taskCode: result.data.execution_id || '',
        taskStatus: 'SUCCESS',
        progress: 100,
        scriptContent: result.data.output?.script_content || '',
        scriptType: result.data.output?.script_type || form.scriptType,
        scriptLanguage: result.data.output?.script_language || form.scriptLanguage,
        elementsUsed: result.data.output?.elements_used || [],
        steps: result.data.output?.steps || []
      }
      ElMessage.success('脚本生成成功（通过工作流）')
    } else {
      generationResult.value = {
        taskCode: result.data.execution_id || '',
        taskStatus: 'FAILED',
        progress: 0,
        errorMessage: result.data.error || '脚本生成失败'
      }
      ElMessage.error('脚本生成失败: ' + (result.data.error || '未知错误'))
    }
  } catch (error: any) {
    console.error('使用工作流生成脚本失败:', error)
    ElMessage.error('使用工作流生成脚本失败: ' + (error.message || '未知错误'))
    generationResult.value = {
      taskCode: '',
      taskStatus: 'FAILED',
      progress: 0,
      errorMessage: error.message || '使用工作流生成脚本失败'
    }
  } finally {
    generateLoading.value = false
  }
}





// 轮询任务状�?


const pollTaskStatus = async (taskCode: string) => {


  try {


    const response = await uiScriptGenerationApi.getTaskStatus(taskCode)


    if (response.data) {


      generationResult.value = response.data





      // 如果任务完成或失败，停止轮询


      if (response.data.taskStatus === 'SUCCESS' || response.data.taskStatus === 'FAILED') {


        if (pollTimer) {


          clearInterval(pollTimer)


          pollTimer = null


        }


        generateLoading.value = false


      }


    }


  } catch (error) {


    console.error('查询任务状态失�?', error)


    if (pollTimer) {


      clearInterval(pollTimer)


      pollTimer = null


    }


    generateLoading.value = false


  }


}





// 生成脚本


const handleGenerate = async () => {


  if (!formRef.value) return





  await formRef.value.validate(async (valid) => {


    if (valid) {


      // 如果选择直接输入，需要先上传代码内容


      if (pageCodeInputType.value === 'input' && pageCodeContent.value) {


        try {


          // 创建一个临时文件并上传


          const blob = new Blob([pageCodeContent.value], { type: 'text/plain' })


          const file = new File([blob], 'page-code.txt', { type: 'text/plain' })


          const uploadResponse = await fileUploadApi.uploadFile(file)


          if (uploadResponse.data) {


            form.pageCodeUrl = uploadResponse.data.fileUrl


          }


        } catch (error) {


          ElMessage.error('上传页面代码失败')


          return


        }


      }





      if (!form.pageCodeUrl && pageCodeInputType.value !== 'input') {


        ElMessage.warning('请提供页面代码（上传文件、URL或直接输入）')


        return


      }





      generateLoading.value = true


      try {


        // 如果选择了工作流，使用工作流执行


        if (form.workflowId) {


          await handleGenerateWithWorkflow()


          return


        }


        // 否则使用默认流程


        const response = await uiScriptGenerationApi.generateScript(form)


        if (response.data) {


          generationResult.value = response.data





          // 如果是异步任务，开始轮�?


          if (response.data.taskStatus === 'PENDING' || response.data.taskStatus === 'PROCESSING') {


            if (pollTimer) {


              clearInterval(pollTimer)


            }


            pollTimer = window.setInterval(() => {


              if (response.data?.taskCode) {


                pollTaskStatus(response.data.taskCode)


              }


            }, 2000) // �?秒轮询一�?


          } else {


            generateLoading.value = false


          }





          ElMessage.success('脚本生成任务已提交')


        }


      } catch (error: any) {


        console.error('生成脚本失败:', error)


        ElMessage.error(error.message || '生成脚本失败')


        generateLoading.value = false


      }


    }


  })


}





// 重置表单


const handleReset = () => {


  formRef.value?.resetFields()


  generationResult.value = null


  pageCodeInputType.value = 'upload'


  pageCodeContent.value = ''


  activeTab.value = 'script'


  if (pollTimer) {


    clearInterval(pollTimer)


    pollTimer = null


  }


}





// 复制脚本


const handleCopyScript = async () => {


  if (generationResult.value?.scriptContent) {


    try {


      await navigator.clipboard.writeText(generationResult.value.scriptContent)


      ElMessage.success('脚本已复制到剪贴板')


    } catch (error) {


      ElMessage.error('复制失败，请手动复制')


    }


  }


}





// 下载脚本


const handleDownloadScript = () => {


  if (generationResult.value?.scriptContent) {


    const extension = form.scriptLanguage === 'PYTHON' ? 'py' : form.scriptLanguage === 'JAVA' ? 'java' : 'js'


    const blob = new Blob([generationResult.value.scriptContent], { type: 'text/plain' })


    const url = URL.createObjectURL(blob)


    const link = document.createElement('a')


    link.href = url


    link.download = `ui-script-${Date.now()}.${extension}`


    document.body.appendChild(link)


    link.click()


    document.body.removeChild(link)


    URL.revokeObjectURL(url)


    ElMessage.success('脚本下载成功')


  }


}





// 初始�?


onMounted(() => {


  loadRequirementList()


  loadWorkflows()


})





// 组件卸载时清理定时器


onUnmounted(() => {


  if (pollTimer) {


    clearInterval(pollTimer)


    pollTimer = null


  }


})


</script>





<style scoped lang="scss">


.ui-script-generation {


  padding: 20px;





  .card-header {


    display: flex;


    justify-content: space-between;


    align-items: center;





    h2 {


      margin: 0;


    }


  }





  .result-card {


    .card-header {


      display: flex;


      justify-content: space-between;


      align-items: center;


    }


  }


}


</style>


