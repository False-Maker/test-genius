<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="输入来源">
      <el-select v-model="config.source" placeholder="请选择输入来源">
        <el-option label="手动粘贴" value="manual" />
        <el-option label="用例库导入" value="repository" />
        <el-option label="文件上传" value="file" />
      </el-select>
    </el-form-item>
    <el-form-item label="用例内容" v-if="config.source === 'manual'">
      <el-input 
        v-model="config.content" 
        type="textarea" 
        :rows="6" 
        placeholder="请输入JSON格式的测试用例"
      />
    </el-form-item>
    <el-form-item label="用例ID" v-if="config.source === 'repository'">
      <el-input v-model="config.caseId" placeholder="请输入用例ID或名称" />
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
  get: () => props.modelValue || { source: 'manual' },
  set: (val) => emit('update:modelValue', val)
})
</script>
