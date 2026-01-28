<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="分析维度">
      <el-checkbox-group v-model="config.dimensions">
        <el-checkbox label="functional">功能点</el-checkbox>
        <el-checkbox label="non-functional">非功能需求</el-checkbox>
        <el-checkbox label="security">安全需求</el-checkbox>
        <el-checkbox label="performance">性能需求</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="输出深度">
      <el-slider v-model="config.depth" :min="1" :max="5" show-stops />
    </el-form-item>
    <el-form-item label="语言偏好">
      <el-select v-model="config.language">
        <el-option label="中文" value="zh" />
        <el-option label="English" value="en" />
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
  get: () => props.modelValue || { dimensions: ['functional'], depth: 2, language: 'zh' },
  set: (val) => emit('update:modelValue', val)
})
</script>
