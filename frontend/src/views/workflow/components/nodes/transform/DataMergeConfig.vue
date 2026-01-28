<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="合并策略">
      <el-select v-model="config.strategy">
        <el-option label="顺序拼接 (Append)" value="append" />
        <el-option label="按键合并 (Merge by Key)" value="key_merge" />
        <el-option label="覆盖 (Override)" value="override" />
      </el-select>
    </el-form-item>
    <el-form-item label="主键字段" v-if="config.strategy === 'key_merge'">
      <el-input v-model="config.primaryKey" placeholder="例如: id" />
    </el-form-item>
    <el-form-item label="冲突处理">
      <el-radio-group v-model="config.conflictResolution">
        <el-radio label="use_first">保留前者</el-radio>
        <el-radio label="use_last">覆盖前者</el-radio>
        <el-radio label="error">报错</el-radio>
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
  get: () => props.modelValue || { strategy: 'append', conflictResolution: 'use_last' },
  set: (val) => emit('update:modelValue', val)
})
</script>
