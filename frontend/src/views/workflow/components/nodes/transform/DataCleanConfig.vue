<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="清洗规则">
      <el-checkbox-group v-model="config.rules">
        <el-checkbox label="trim">去除首尾空格</el-checkbox>
        <el-checkbox label="remove_empty">去除空字段</el-checkbox>
        <el-checkbox label="remove_duplicates">去重</el-checkbox>
        <el-checkbox label="normalize_date">标准化日期格式</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="敏感脱敏">
      <el-select v-model="config.masking" multiple placeholder="选择脱敏字段">
        <el-option label="手机号" value="phone" />
        <el-option label="身份证" value="id_card" />
        <el-option label="邮箱" value="email" />
        <el-option label="姓名" value="name" />
      </el-select>
    </el-form-item>
    <el-form-item label="自定义清洗">
      <el-input 
        v-model="config.customScript" 
        type="textarea" 
        :rows="3" 
        placeholder="输入自定义清洗脚本 (JavaScript)"
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
  get: () => props.modelValue || { rules: ['trim', 'remove_empty'] },
  set: (val) => emit('update:modelValue', val)
})
</script>
