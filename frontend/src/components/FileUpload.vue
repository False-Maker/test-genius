<template>

  <div class="file-upload">

    <el-upload

      ref="uploadRef"

      :http-request="customUpload"

      :before-upload="beforeUpload"

      :on-remove="handleRemove"

      :file-list="fileList"

      :limit="limit"

      :accept="accept"

      :drag="drag"

      :disabled="disabled"

      :auto-upload="autoUpload"

    >

      <template v-if="drag">

        <el-icon class="el-icon--upload"><upload-filled /></el-icon>

        <div class="el-upload__text">

          将文件拖到此处，或<em>点击上传</em>

        </div>

      </template>

      <template v-else>

        <el-button type="primary" :disabled="disabled">

          <el-icon><upload /></el-icon>

          选择文件

        </el-button>

      </template>

      <template #tip>

        <div class="el-upload__tip">

          {{ tipText }}

        </div>

      </template>

    </el-upload>



    <!-- 文件列表 -->

    <div v-if="fileList.length > 0" class="file-list">

      <div v-for="file in fileList" :key="file.uid" class="file-item">

        <el-link :href="file.url" target="_blank" type="primary">

          {{ file.name }}

        </el-link>

        <el-button

          v-if="!disabled"

          type="danger"

          size="small"

          text

          @click="handleDelete(file)"

        >

          删除

        </el-button>

      </div>

    </div>

  </div>

</template>



<script setup lang="ts">

import { ref, computed, watch } from 'vue'

import { ElMessage, type UploadFile, type UploadFiles, type UploadInstance } from 'element-plus'

import { Upload, UploadFilled } from '@element-plus/icons-vue'

import { fileUploadApi, type FileUploadResponse } from '@/api/fileUpload'

import type { ApiResult } from '@/api/types'



interface Props {

  modelValue?: string | string[] // 文件URL或URL数组

  limit?: number // 文件数量限制

  accept?: string // 接受的文件类型

  drag?: boolean // 是否支持拖拽

  disabled?: boolean // 是否禁用

  autoUpload?: boolean // 是否自动上传

  tipText?: string // 提示文本

}



const props = withDefaults(defineProps<Props>(), {

  limit: 1,

  accept: '.doc,.docx,.pdf',

  drag: false,

  disabled: false,

  autoUpload: true,

  tipText: '支持上传Word文档（.doc, .docx）和PDF文档（.pdf），单个文件不超过100MB'

})



const emit = defineEmits<{

  'update:modelValue': [value: string | string[]]

  'change': [files: UploadFiles]

  'success': [response: FileUploadResponse, file: UploadFile]

  'error': [error: Error, file: UploadFile]

}>()



const uploadRef = ref<UploadInstance>()

const fileList = ref<UploadFile[]>([])



// 自定义上传

const customUpload = async (options: any) => {

  const file = options.file

  try {

    const response = await fileUploadApi.uploadFile(file)

    if (response.data) {

      handleSuccess(response, file as UploadFile)

    } else {

      handleError(new Error(response.message || '上传失败'), file as UploadFile)

    }

  } catch (error: any) {

    handleError(error, file as UploadFile)

  }

}



// 初始化文件列表

const initFileList = () => {

  if (!props.modelValue) {

    fileList.value = []

    return

  }



  const urls = Array.isArray(props.modelValue) ?props.modelValue : [props.modelValue]

  fileList.value = urls.map((url, index) => ({

    uid: Date.now() + index,

    name: url.split('/').pop() || `文件${index + 1}`,

    url: url,

    status: 'success'

  }))

}



// 上传前验证

const beforeUpload = (file: File) => {

  // 检查文件类型

  const validTypes = ['.doc', '.docx', '.pdf']

  const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()

  

  if (!validTypes.includes(fileExtension)) {

    ElMessage.error('只能上传Word文档（.doc, .docx）和PDF文档（.pdf）')

    return false

  }



  // 检查文件大小（100MB）

  const maxSize = 100 * 1024 * 1024

  if (file.size > maxSize) {

    ElMessage.error('文件大小不能超过100MB')

    return false

  }



  return true

}



// 上传成功

const handleSuccess = async (response: ApiResult<FileUploadResponse>, file: UploadFile) => {

  if (response.code === 200 && response.data) {

    const fileUrl = response.data.fileUrl

    

    // 更新文件列表

    const fileIndex = fileList.value.findIndex(f => f.uid === file.uid)

    if (fileIndex !== -1) {

      fileList.value[fileIndex].url = fileUrl

      fileList.value[fileIndex].status = 'success'

    }



    // 更新v-model

    if (props.limit === 1) {

      emit('update:modelValue', fileUrl)

    } else {

      const urls = fileList.value

        .filter(f => f.status === 'success' && f.url)

        .map(f => f.url as string)

      emit('update:modelValue', urls)

    }



    emit('success', response.data, file)

    ElMessage.success('文件上传成功')

  } else {

    ElMessage.error(response.message || '文件上传失败')

    file.status = 'fail'

  }

}



// 上传失败

const handleError = (error: Error, file: UploadFile) => {

  ElMessage.error('文件上传失败：' + error.message)

  file.status = 'fail'

  emit('error', error, file)

}



// 删除文件

const handleRemove = (file: UploadFile) => {

  const index = fileList.value.findIndex(f => f.uid === file.uid)

  if (index !== -1) {

    fileList.value.splice(index, 1)

  }



  // 更新v-model

  if (props.limit === 1) {

    emit('update:modelValue', '')

  } else {

    const urls = fileList.value

      .filter(f => f.status === 'success' && f.url)

      .map(f => f.url as string)

    emit('update:modelValue', urls)

  }



  emit('change', fileList.value)

}



// 手动删除文件

const handleDelete = async (file: UploadFile) => {

  if (file.url) {

    try {

      // 从URL中提取文件路径（需要根据实际URL格式调整）

      const urlParts = file.url.split('/')

      const filePath = urlParts.slice(-2).join('/') // 获取最后两部分作为路径

      

      await fileUploadApi.deleteFile(filePath)

      handleRemove(file)

      ElMessage.success('文件删除成功')

    } catch (error) {

      console.error('删除文件失败:', error)

      ElMessage.error('删除文件失败')

    }

  } else {

    handleRemove(file)

  }

}



// 手动上传

const upload = () => {

  uploadRef.value?.submit()

}



// 清空文件列表

const clearFiles = () => {

  fileList.value = []

  emit('update:modelValue', props.limit === 1 ? '' : [])

  emit('change', [])

}



// 暴露方法

defineExpose({

  upload,

  clearFiles,

  submit: upload

})



// 初始化

initFileList()



// 监听modelValue变化

watch(() => props.modelValue, () => {

  initFileList()

}, { deep: true })

</script>





<style scoped>

.file-upload {

  width: 100%;

}



.file-list {

  margin-top: 10px;

}



.file-item {

  display: flex;

  align-items: center;

  justify-content: space-between;

  padding: 8px;

  border: 1px solid #dcdfe6;

  border-radius: 4px;

  margin-bottom: 8px;

}



.file-item:last-child {

  margin-bottom: 0;

}

</style>



