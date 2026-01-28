<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="判断条件">
      <el-input 
        v-model="config.condition" 
        type="textarea" 
        :rows="2" 
        placeholder="例如: success_rate > 0.8"
      />
    </el-form-item>
    <el-form-item label="比较模式">
      <el-radio-group v-model="config.mode">
        <el-radio label="expression">表达式</el-radio>
        <el-radio label="script">脚本</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="默认分支">
      <el-switch v-model="config.hasDefaultBranch" active-text="开启" inactive-text="关闭" />
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
  get: () => props.modelValue || { mode: 'expression', hasDefaultBranch: true },
  set: (val) => emit('update:modelValue', val)
})
</script>
