<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="报告类型">
      <el-select v-model="config.reportType">
        <el-option label="测试计划报告" value="plan" />
        <el-option label="用例评审报告" value="review" />
        <el-option label="执行结果报告" value="execution" />
      </el-select>
    </el-form-item>
    <el-form-item label="包含章节">
      <el-checkbox-group v-model="config.sections">
        <el-checkbox label="summary">摘要</el-checkbox>
        <el-checkbox label="statistics">统计图表</el-checkbox>
        <el-checkbox label="details">详细列表</el-checkbox>
        <el-checkbox label="risks">风险分析</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="导出格式">
      <el-radio-group v-model="config.format">
        <el-radio label="html">HTML</el-radio>
        <el-radio label="pdf">PDF</el-radio>
        <el-radio label="markdown">Markdown</el-radio>
      </el-radio-group>
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
  get: () => props.modelValue || { reportType: 'review', sections: ['summary', 'statistics'], format: 'html' },
  set: (val) => emit('update:modelValue', val)
})
</script>
