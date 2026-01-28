<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="文件格式">
      <el-select v-model="config.fileFormat">
        <el-option label="Excel (.xlsx)" value="xlsx" />
        <el-option label="CSV (.csv)" value="csv" />
        <el-option label="JSON (.json)" value="json" />
        <el-option label="XMind (.xmind)" value="xmind" />
      </el-select>
    </el-form-item>
    <el-form-item label="文件名">
      <el-input v-model="config.fileName" placeholder="例如: test_cases_{date}" />
    </el-form-item>
    <el-form-item label="包含表头">
      <el-switch v-model="config.includeHeader" active-text="是" inactive-text="否" />
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
  get: () => props.modelValue || { fileFormat: 'xlsx', includeHeader: true },
  set: (val) => emit('update:modelValue', val)
})
</script>
