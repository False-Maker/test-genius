<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="解析格式">
      <el-select v-model="config.format">
        <el-option label="JSON" value="json" />
        <el-option label="Markdown表格" value="markdown_table" />
        <el-option label="纯文本" value="text" />
        <el-option label="XML" value="xml" />
      </el-select>
    </el-form-item>
    <el-form-item label="提取规则">
      <el-input v-model="config.rule" placeholder="例如: ```json(.*)```" />
    </el-form-item>
    <el-form-item label="失败处理">
      <el-select v-model="config.onFailure">
        <el-option label="抛出错误" value="error" />
        <el-option label="返回原始内容" value="raw" />
        <el-option label="使用默认值" value="default" />
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
  get: () => props.modelValue || { format: 'json', onFailure: 'error' },
  set: (val) => emit('update:modelValue', val)
})
</script>
