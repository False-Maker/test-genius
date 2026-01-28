<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="保存目标">
      <el-select v-model="config.target">
        <el-option label="测试用例库" value="case_repo" />
        <el-option label="临时文件" value="temp_file" />
        <el-option label="外部API" value="api" />
      </el-select>
    </el-form-item>
    <el-form-item label="关联项目">
      <el-input v-model="config.projectId" placeholder="请输入项目ID" />
    </el-form-item>
    <el-form-item label="标签设置">
      <el-select
        v-model="config.tags"
        multiple
        filterable
        allow-create
        default-first-option
        placeholder="输入标签"
      >
        <el-option label="自动化生成" value="auto-generated" />
        <el-option label="待审核" value="pending-review" />
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
  get: () => props.modelValue || { target: 'case_repo', tags: ['auto-generated'] },
  set: (val) => emit('update:modelValue', val)
})
</script>
