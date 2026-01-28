<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="输入方式">
      <el-select v-model="config.inputType" placeholder="请选择输入方式">
        <el-option label="文本输入" value="text" />
        <el-option label="参数传递" value="param" />
      </el-select>
    </el-form-item>
    <el-form-item label="默认内容" v-if="config.inputType === 'text'">
      <el-input 
        v-model="config.defaultValue" 
        type="textarea" 
        :rows="4" 
        placeholder="请输入默认需求内容"
      />
    </el-form-item>
    <el-form-item label="参数名称" v-else>
      <el-input v-model="config.paramName" placeholder="例如: requirement_text" />
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
  get: () => props.modelValue || {},
  set: (val) => emit('update:modelValue', val)
})
</script>
