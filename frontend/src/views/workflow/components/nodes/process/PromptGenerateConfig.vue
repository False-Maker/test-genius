<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="生成策略">
      <el-select v-model="config.strategy">
        <el-option label="直接生成 (Zero-Shot)" value="zero_shot" />
        <el-option label="少样本学习 (Few-Shot)" value="few_shot" />
        <el-option label="思维链 (CoT)" value="cot" />
      </el-select>
    </el-form-item>
    <el-form-item label="上下文注入">
      <el-switch v-model="config.injectContext" active-text="注入知识库上下文" />
    </el-form-item>
    <el-form-item label="提示词模板">
      <el-input 
        v-model="config.template" 
        type="textarea" 
        :rows="5" 
        placeholder="输入自定义提示词模板，使用 {{variable}} 作为占位符"
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
  get: () => props.modelValue || { strategy: 'cot', injectContext: true },
  set: (val) => emit('update:modelValue', val)
})
</script>
