<template>
  <el-container class="app-container">
    <el-aside width="240px" class="app-sidebar">
      <div class="logo-container">
        <div class="logo-icon">TG</div>
        <h1 class="logo-text">Test Genius</h1>
      </div>
      
      <el-menu
        router
        :default-active="$route.path"
        class="sidebar-menu"
        background-color="#1f2937"
        text-color="#9ca3af"
        active-text-color="#ffffff"
      >
        <div class="menu-group-title">核心功能</div>
        <el-menu-item index="/requirement">
          <el-icon><Document /></el-icon>
          <span>需求管理</span>
        </el-menu-item>
        <el-menu-item index="/test-case">
          <el-icon><List /></el-icon>
          <span>用例管理</span>
        </el-menu-item>
        
        <div class="menu-group-title">AI 能力</div>
        <el-menu-item index="/case-generation">
          <el-icon><MagicStick /></el-icon>
          <span>智能用例生成</span>
        </el-menu-item>
        <el-menu-item index="/case-reuse">
          <el-icon><CopyDocument /></el-icon>
          <span>智能复用</span>
        </el-menu-item>
        
        <div class="menu-group-title">配置与资产</div>
        <el-menu-item index="/knowledge-base">
          <el-icon><Reading /></el-icon>
          <span>知识库</span>
        </el-menu-item>
        <el-menu-item index="/prompt-template">
          <el-icon><ChatDotRound /></el-icon>
          <span>Prompt 模板</span>
        </el-menu-item>
        <el-menu-item index="/model-config">
          <el-icon><Setting /></el-icon>
          <span>模型配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="app-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title || '当前页面' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-profile">
              <el-avatar :size="32" class="user-avatar">Admin</el-avatar>
              <span class="username">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { Document, List, MagicStick, CopyDocument, Reading, ChatDotRound, Setting, ArrowDown } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.app-container {
  height: 100vh;
  background-color: #f3f4f6;
}

.app-sidebar {
  background-color: #1f2937;
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 10;
  
  .logo-container {
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    background-color: #111827;
    border-bottom: 1px solid #374151;
    
    .logo-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      color: white;
      margin-right: 12px;
      font-size: 14px;
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: white;
      margin: 0;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    flex: 1;
    padding-top: 20px;
    
    .menu-group-title {
      padding: 10px 20px;
      font-size: 12px;
      color: #6b7280;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-top: 10px;
      
      &:first-child {
        margin-top: 0;
      }
    }
    
    :deep(.el-menu-item) {
      margin: 4px 12px;
      border-radius: 8px;
      height: 44px;
      line-height: 44px;
      
      &:hover {
        background-color: #374151;
      }
      
      &.is-active {
        background: linear-gradient(90deg, #6366f1 0%, #4f46e5 100%);
        color: white;
        box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.4);
      }
      
      .el-icon {
        margin-right: 10px;
      }
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.app-header {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  z-index: 5;
  
  .header-right {
    .user-profile {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 6px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: rgba(0, 0, 0, 0.05);
      }
      
      .user-avatar {
        background-color: #6366f1;
        font-size: 12px;
      }
      
      .username {
        margin: 0 8px;
        font-size: 14px;
        color: #374151;
        font-weight: 500;
      }
    }
  }
}

.app-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f3f4f6;
  
  /* Custom Scrollbar */
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: #d1d5db;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-track {
    background-color: transparent;
  }
}
</style>

