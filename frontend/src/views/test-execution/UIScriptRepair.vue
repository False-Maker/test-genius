<template>

  <div class="ui-script-repair">

    <el-card>

      <template #header>

        <div class="card-header">

          <h2>UI脚本修复管理</h2>

        </div>

      </template>



      <el-form :model="form" :rules="formRules" ref="formRef" label-width="140px">

        <el-form-item label="脚本输入方式">

          <el-radio-group v-model="scriptInputType">

            <el-radio label="input">直接输入</el-radio>

            <el-radio label="upload">上传文件</el-radio>

          </el-radio-group>

        </el-form-item>



        <el-form-item label="UI脚本" prop="scriptContent">

          <el-input

            v-if="scriptInputType === 'input'"

            v-model="form.scriptContent"

            type="textarea"

            :rows="15"

            placeholder="请粘贴需要修复的UI自动化脚本"

            style="font-family: 'Courier New', monospace"

          />

          <FileUpload

            v-else

            v-model="scriptFileUrl"

            :limit="1"

            accept=".py,.java,.js"

            tip-text="支持上传Python、Java、JavaScript脚本文件"

            @success="handleScriptFileUpload"

          />

          <div v-if="scriptInputType === 'upload' && scriptFileUrl" style="margin-top: 10px">

            <el-button size="small" @click="loadScriptFromUrl">加载脚本内容</el-button>

          </div>

        </el-form-item>



        <el-form-item label="错误日志输入方式">

          <el-radio-group v-model="errorLogInputType">

            <el-radio label="input">直接输入</el-radio>

            <el-radio label="upload">上传文件</el-radio>

          </el-radio-group>

        </el-form-item>



        <el-form-item label="错误日志" prop="errorLog">

          <el-input

            v-if="errorLogInputType === 'input'"

            v-model="form.errorLog"

            type="textarea"

            :rows="10"

            placeholder="请粘贴执行过程中的错误日志"

          />

          <FileUpload

            v-else

            v-model="errorLogFileUrl"

            :limit="1"

            accept=".txt,.log"

            tip-text="支持上传TXT、LOG文件"

            @success="handleErrorLogFileUpload"

          />

          <div v-if="errorLogInputType === 'upload' && errorLogFileUrl" style="margin-top: 10px">

            <el-button size="small" @click="loadErrorLogFromUrl">加载错误日志</el-button>

          </div>

        </el-form-item>



        <el-form-item label="页面代码URL（可选）">

          <el-input

            v-model="form.newPageCodeUrl"

            placeholder="可选：提供新值的页面代码URL，用于检测页面变更更更更"

            clearable

          />

        </el-form-item>



        <el-form-item label="脚本类型">

          <el-select v-model="form.scriptType" placeholder="请选择脚本类型" style="width: 100%">

            <el-option label="Selenium" value="SELENIUM" />

            <el-option label="Playwright" value="PLAYWRIGHT" />

          </el-select>

        </el-form-item>



        <el-form-item label="脚本语言">

          <el-select v-model="form.scriptLanguage" placeholder="请选择脚本语言" style="width: 100%">

            <el-option label="Python" value="PYTHON" />

            <el-option label="Java" value="JAVA" />

            <el-option label="JavaScript" value="JAVASCRIPT" />

          </el-select>

        </el-form-item>



        <el-form-item label="使用LLM优化">

          <el-switch v-model="form.useLlm" />

          <span style="margin-left: 10px; color: #909399; font-size: 12px">

            启用后使用大语言模型优化脚本修复

          </span>

        </el-form-item>



        <el-form-item>

          <el-button type="primary" @click="handleRepair" :loading="repairLoading">

            分析并修复

          </el-button>

          <el-button @click="handleReset">重置</el-button>

          <el-button @click="handleAnalyzeError" :loading="analyzeLoading">仅分析错误</el-button>

        </el-form-item>

      </el-form>



      <!-- 错误分析结果 -->

      <el-card v-if="errorAnalysisResult" class="result-card" style="margin-top: 20px">

        <template #header>

          <div class="card-header">

            <span>错误分析结果</span>

          </div>

        </template>



        <el-descriptions :column="2" border>

          <el-descriptions-item label="错误类型" :span="2">

            <el-tag :type="errorAnalysisResult.errorType === 'ELEMENT_NOT_FOUND' ? 'danger' : 'warning'">

              {{ errorAnalysisResult.errorType }}

            </el-tag>

          </el-descriptions-item>

          <el-descriptions-item label="错误信息" :span="2">

            {{ errorAnalysisResult.errorMessage }}

          </el-descriptions-item>

          <el-descriptions-item label="错误位置" :span="2" v-if="errorAnalysisResult.errorLocation">

            {{ errorAnalysisResult.errorLocation }}

          </el-descriptions-item>

          <el-descriptions-item label="修复建议" :span="2" v-if="errorAnalysisResult.suggestions">

            <ul style="margin: 0; padding-left: 20px">

              <li v-for="(suggestion, index) in errorAnalysisResult.suggestions" :key="index">

                {{ suggestion }}

              </li>

            </ul>

          </el-descriptions-item>

        </el-descriptions>

      </el-card>



      <!-- 修复结果 -->

      <el-card v-if="repairResult" class="result-card" style="margin-top: 20px">

        <template #header>

          <div class="card-header">

            <span>修复结果</span>

          </div>

        </template>



        <el-alert

          v-if="repairResult.errorMessage"

          type="error"

          :closable="false"

          show-icon

          style="margin-bottom: 20px"

        >

          {{ repairResult.errorMessage }}

        </el-alert>



        <el-alert

          v-else-if="repairResult.repairedScript"

          type="success"

          :closable="false"

          show-icon

          style="margin-bottom: 20px"

        >

          脚本修复成功

        </el-alert>



        <el-tabs v-model="repairActiveTab">

          <el-tab-pane label="修复后的脚本" name="repaired">

            <el-input

              v-model="repairResult.repairedScript"

              type="textarea"

              :rows="20"

              readonly

              style="font-family: 'Courier New', monospace"

            />

            <div style="margin-top: 10px">

              <el-button type="primary" @click="handleCopyRepairedScript">复制脚本</el-button>

              <el-button type="success" @click="handleDownloadRepairedScript">下载脚本</el-button>

            </div>

          </el-tab-pane>



          <el-tab-pane label="修复变更" name="changes" v-if="repairResult.changes && repairResult.changes.length > 0">

            <el-table :data="repairResult.changes" border stripe>

              <el-table-column prop="type" label="变更类型" width="120" />

              <el-table-column prop="location" label="位置" width="150" />

              <el-table-column prop="oldValue" label="原值" show-overflow-tooltip />

              <el-table-column prop="newValue" label="新值" show-overflow-tooltip />

              <el-table-column prop="reason" label="原因" show-overflow-tooltip />

            </el-table>

          </el-tab-pane>



          <el-tab-pane label="修复建议" name="suggestions" v-if="repairResult.suggestions && repairResult.suggestions.length > 0">

            <ul style="margin: 0; padding-left: 20px">

              <li v-for="(suggestion, index) in repairResult.suggestions" :key="index" style="margin-bottom: 10px">

                {{ suggestion }}

              </li>

            </ul>

          </el-tab-pane>

        </el-tabs>

      </el-card>

    </el-card>

  </div>

</template>



<script setup lang="ts">

import { ref, reactive, onUnmounted } from 'vue'

import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { uiScriptRepairApi, type UIScriptRepairRequest, type UIScriptRepairResult, type ErrorAnalysisResult } from '@/api/uiScriptRepair'

import FileUpload from '@/components/FileUpload.vue'



// 响应式数据

const formRef = ref<FormInstance>()

const repairLoading = ref(false)

const analyzeLoading = ref(false)

const scriptInputType = ref<'input' | 'upload'>('input')

const errorLogInputType = ref<'input' | 'upload'>('input')

const scriptFileUrl = ref('')

const errorLogFileUrl = ref('')

const repairActiveTab = ref('repaired')



const form = reactive<UIScriptRepairRequest>({

  scriptContent: '',

  errorLog: '',

  newPageCodeUrl: '',

  scriptType: 'SELENIUM',

  scriptLanguage: 'PYTHON',

  useLlm: true

})



const errorAnalysisResult = ref<ErrorAnalysisResult | null>(null)

const repairResult = ref<UIScriptRepairResult | null>(null)



// 表单验证规则

const formRules: FormRules = {

  scriptContent: [

    { required: true, message: '请输入或上传UI脚本', trigger: 'blur' }

  ],

  errorLog: [

    { required: true, message: '请输入或上传错误日志', trigger: 'blur' }

  ]

}



// 脚本文件上传成功

const handleScriptFileUpload = (response: any) => {

  if (response && response.fileUrl) {

    scriptFileUrl.value = response.fileUrl

  }

}



// 错误日志文件上传成功

const handleErrorLogFileUpload = (response: any) => {

  if (response && response.fileUrl) {

    errorLogFileUrl.value = response.fileUrl

  }

}



// 从URL加载脚本内容

const loadScriptFromUrl = async () => {

  if (!scriptFileUrl.value) {

    ElMessage.warning('请先上传脚本文件')

    return

  }



  try {

    const response = await fetch(scriptFileUrl.value)

    const text = await response.text()

    form.scriptContent = text

    ElMessage.success('脚本内容加载成功')

  } catch (error) {

    console.error('加载脚本失败:', error)

    ElMessage.error('加载脚本内容失败')

  }

}



// 从URL加载错误日志

const loadErrorLogFromUrl = async () => {

  if (!errorLogFileUrl.value) {

    ElMessage.warning('请先上传错误日志文件')

    return

  }



  try {

    const response = await fetch(errorLogFileUrl.value)

    const text = await response.text()

    form.errorLog = text

    ElMessage.success('错误日志加载成功')

  } catch (error) {

    console.error('加载错误日志失败:', error)

    ElMessage.error('加载错误日志失败')

  }

}



// 分析错误

const handleAnalyzeError = async () => {

  if (!formRef.value) return



  await formRef.value.validate(async (valid) => {

    if (valid) {

      analyzeLoading.value = true

      try {

        const response = await uiScriptRepairApi.analyzeError(form.scriptContent, form.errorLog)

        if (response.data) {

          errorAnalysisResult.value = response.data

          ElMessage.success('错误分析完成')

        }

      } catch (error: any) {

        console.error('分析错误失败:', error)

        ElMessage.error(error.message || '分析错误失败')

      } finally {

        analyzeLoading.value = false

      }

    }

  })

}



// 修复脚本

const handleRepair = async () => {

  if (!formRef.value) return



  await formRef.value.validate(async (valid) => {

    if (valid) {

      repairLoading.value = true

      repairResult.value = null

      errorAnalysisResult.value = null



      try {

        // 先分析错误

        const analyzeResponse = await uiScriptRepairApi.analyzeError(form.scriptContent, form.errorLog)

        if (analyzeResponse.data) {

          errorAnalysisResult.value = analyzeResponse.data

          form.errorAnalysis = analyzeResponse.data

        }



        // 如果有新值页面代码URL，检测页面变更

        if (form.newPageCodeUrl) {

          // 这里需要先解析旧值页面元素和新值页面元素，简化处理

          // 实际应该调用 parsePageCode API

        }



        // 执行智能修复

        const repairResponse = await uiScriptRepairApi.repairScript(form)

        if (repairResponse.data) {

          repairResult.value = repairResponse.data

          repairActiveTab.value = 'repaired'

          ElMessage.success('脚本修复完成')

        }

      } catch (error: any) {

        console.error('修复脚本失败:', error)

        ElMessage.error(error.message || '修复脚本失败')

      } finally {

        repairLoading.value = false

      }

    }

  })

}



// 重置表单

const handleReset = () => {

  formRef.value?.resetFields()

  form.scriptContent = ''

  form.errorLog = ''

  form.newPageCodeUrl = ''

  scriptInputType.value = 'input'

  errorLogInputType.value = 'input'

  scriptFileUrl.value = ''

  errorLogFileUrl.value = ''

  errorAnalysisResult.value = null

  repairResult.value = null

  repairActiveTab.value = 'repaired'

}



// 复制修复后的脚本

const handleCopyRepairedScript = async () => {

  if (repairResult.value?.repairedScript) {

    try {

      await navigator.clipboard.writeText(repairResult.value.repairedScript)

      ElMessage.success('脚本已复制到剪贴板')

    } catch (error) {

      ElMessage.error('复制失败，请手动复制')

    }

  }

}



// 下载修复后的脚本

const handleDownloadRepairedScript = () => {

  if (repairResult.value?.repairedScript) {

    const extension = form.scriptLanguage === 'PYTHON' ? 'py' : form.scriptLanguage === 'JAVA' ? 'java' : 'js'

    const blob = new Blob([repairResult.value.repairedScript], { type: 'text/plain' })

    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')

    link.href = url

    link.download = `repaired-script-${Date.now()}.${extension}`

    document.body.appendChild(link)

    link.click()

    document.body.removeChild(link)

    URL.revokeObjectURL(url)

    ElMessage.success('脚本下载成功')

  }

}

</script>



<style scoped lang="scss">

.ui-script-repair {

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

