<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="模型代码">
      <el-select v-model="config.model_code" placeholder="请选择模型">
        <el-option label="DeepSeek Chat" value="DEEPSEEK_CHAT" />
        <el-option label="GPT-4" value="GPT4" />
        <el-option label="Claude 3.5" value="CLAUDE_35" />
      </el-select>
    </el-form-item>
    <el-form-item label="温度参数">
      <el-slider v-model="config.temperature" :min="0" :max="1" :step="0.1" show-input />
    </el-form-item>
    <el-form-item label="最大Token">
      <el-input-number v-model="config.max_tokens" :min="100" :max="4096" :step="100" />
    </el-form-item>
    <el-form-item label="系统提示">
      <el-input 
        v-model="config.system_prompt" 
        type="textarea" 
        :rows="3" 
        placeholder="输入系统级提示词"
      />
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
  get: () => props.modelValue || { temperature: 0.7, max_tokens: 2000 },
  set: (val) => emit('update:modelValue', val)
})
</script>
