<template>
  <el-form
    :model="config"
    label-width="80px"
  >
    <el-form-item label="输入来源">
      <el-select
        v-model="config.source"
        placeholder="请选择输入来源"
      >
        <el-option
          label="手动粘贴"
          value="manual"
        />
        <el-option
          label="用例库导入"
          value="repository"
        />
        <el-option
          label="文件上传"
          value="file"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      v-if="config.source === 'manual'"
      label="用例内容"
    >
      <el-input 
        v-model="config.content" 
        type="textarea" 
        :rows="6" 
        placeholder="请输入JSON格式的测试用例"
      />
    </el-form-item>
    <el-form-item
      v-if="config.source === 'repository'"
      label="用例ID"
    >
      <el-input
        v-model="config.caseId"
        placeholder="请输入用例ID或名称"
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TestCaseInputConfig } from '@/types/workflow-nodes'

const props = defineProps<{
  modelValue: TestCaseInputConfig
}>()

const emit = defineEmits<{
  'update:modelValue': [value: TestCaseInputConfig]
}>()

const config = computed({
  get: () => props.modelValue || { source: 'manual' },
  set: (val) => emit('update:modelValue', val)
})
</script>
