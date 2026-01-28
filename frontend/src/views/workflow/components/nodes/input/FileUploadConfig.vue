<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="文件类型">
      <el-checkbox-group v-model="config.acceptTypes">
        <el-checkbox label=".pdf">PDF文档</el-checkbox>
        <el-checkbox label=".docx">Word文档</el-checkbox>
        <el-checkbox label=".txt">文本文件</el-checkbox>
        <el-checkbox label=".md">Markdown</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="最大大小">
      <el-input-number v-model="config.maxSize" :min="1" :max="50" />
      <span class="unit">MB</span>
    </el-form-item>
    <el-form-item label="解析方式">
      <el-select v-model="config.parseMode">
        <el-option label="自动识别" value="auto" />
        <el-option label="OCR识别" value="ocr" />
        <el-option label="纯文本提取" value="text" />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: any
}>()

const emit = defineEmits(['update:modelValue'])

const config = computed({
  get: () => props.modelValue || { acceptTypes: ['.docx', '.md'], maxSize: 10, parseMode: 'auto' },
  set: (val) => emit('update:modelValue', val)
})
</script>

<style scoped>
.unit {
  margin-left: 8px;
  color: #606266;
}
</style>
