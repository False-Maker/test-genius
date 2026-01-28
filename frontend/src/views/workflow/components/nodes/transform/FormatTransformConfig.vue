<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="源格式">
      <el-select v-model="config.sourceFormat">
        <el-option label="JSON" value="json" />
        <el-option label="XML" value="xml" />
        <el-option label="YAML" value="yaml" />
        <el-option label="CSV" value="csv" />
      </el-select>
    </el-form-item>
    <el-form-item label="目标格式">
      <el-select v-model="config.targetFormat">
        <el-option label="JSON" value="json" />
        <el-option label="XML" value="xml" />
        <el-option label="YAML" value="yaml" />
        <el-option label="HTML" value="html" />
      </el-select>
    </el-form-item>
    <el-form-item label="映射模板">
      <el-input 
        v-model="config.mappingTemplate" 
        type="textarea" 
        :rows="4" 
        placeholder="输入字段映射模板 (可选)"
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
  get: () => props.modelValue || { sourceFormat: 'json', targetFormat: 'json' },
  set: (val) => emit('update:modelValue', val)
})
</script>
