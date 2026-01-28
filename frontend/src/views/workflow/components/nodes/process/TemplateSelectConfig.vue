<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="测试类型">
      <el-select v-model="config.testType" placeholder="选择测试类型">
        <el-option label="功能测试" value="functional" />
        <el-option label="接口测试" value="api" />
        <el-option label="UI自动化" value="ui" />
        <el-option label="安全测试" value="security" />
      </el-select>
    </el-form-item>
    <el-form-item label="模板选择">
      <el-select v-model="config.templateId" placeholder="选择具体模板">
        <el-option label="通用功能测试模板 v1.0" value="func_v1" />
        <el-option label="Restful API测试模板" value="api_rest" />
        <el-option label="OWASP Top 10 安全模板" value="sec_owasp" />
      </el-select>
    </el-form-item>
    <el-form-item label="自定义字段">
      <el-input 
        v-model="config.customFields" 
        type="textarea" 
        :rows="3" 
        placeholder="JSON格式的扩展字段定义"
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
  get: () => props.modelValue || { testType: 'functional' },
  set: (val) => emit('update:modelValue', val)
})
</script>
