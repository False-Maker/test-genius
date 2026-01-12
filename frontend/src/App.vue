<template>
  <el-container>
    <el-header>
      <h1>测试设计助手系统</h1>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router>
          <el-menu-item index="/requirement">需求管理</el-menu-item>
          <el-menu-item index="/test-case">用例管理</el-menu-item>
          <el-menu-item index="/case-generation">用例生成</el-menu-item>
          <el-menu-item index="/prompt-template">提示词模板</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
// 应用主组件
</script>

<style scoped>
.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.el-aside {
  background-color: #f5f5f5;
}
</style>

