<template>
  <el-form :model="config" label-width="80px">
    <el-form-item label="循环类型">
      <el-select v-model="config.loopType">
        <el-option label="按次数循环" value="count" />
        <el-option label="遍历集合" value="collection" />
        <el-option label="条件循环 (While)" value="while" />
      </el-select>
    </el-form-item>
    <el-form-item label="循环次数" v-if="config.loopType === 'count'">
      <el-input-number v-model="config.count" :min="1" :max="100" />
    </el-form-item>
    <el-form-item label="集合变量" v-if="config.loopType === 'collection'">
      <el-input v-model="config.collectionVar" placeholder="例如: items" />
    </el-form-item>
    <el-form-item label="终止条件" v-if="config.loopType === 'while'">
      <el-input v-model="config.condition" placeholder="例如: i < 10" />
    </el-form-item>
    <el-form-item label="最大迭代">
      <el-input-number v-model="config.maxIterations" :min="1" :max="1000" />
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
  get: () => props.modelValue || { loopType: 'count', count: 3, maxIterations: 100 },
  set: (val) => emit('update:modelValue', val)
})
</script>
