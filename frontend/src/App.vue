<template>
  <el-container class="app-container">
    <el-aside
      :width="isCollapse ? '72px' : '260px'"
      class="app-sidebar"
    >
      <div class="logo-container">
        <div class="logo-icon">
          <span class="logo-text-inner">TG</span>
        </div>
        <h1 class="logo-text" v-show="!isCollapse">
          Test Genius
        </h1>
      </div>

      <el-menu
        router
        :default-active="$route.path"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#9ca3af"
        active-text-color="#ffffff"
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <div class="menu-group-title" v-show="!isCollapse">
          核心功能
        </div>

        <el-menu-item index="/requirement">
          <el-icon><Document /></el-icon>
          <span>需求ID管理</span>
        </el-menu-item>

        <el-menu-item index="/test-case">
          <el-icon><List /></el-icon>
          <span>用例管理</span>
        </el-menu-item>

        <div class="menu-group-title" v-show="!isCollapse">
          AI 能力
        </div>

        <el-menu-item index="/case-generation">
          <el-icon><MagicStick /></el-icon>
          <span>智能用例生成</span>
        </el-menu-item>

        <el-menu-item index="/case-reuse">
          <el-icon><CopyDocument /></el-icon>
          <span>智能复用</span>
        </el-menu-item>

        <div class="menu-group-title" v-show="!isCollapse">
          规约与设计
        </div>

        <el-menu-item index="/test-specification">
          <el-icon><Notebook /></el-icon>
          <span>测试规约</span>
        </el-menu-item>

        <el-menu-item index="/specification-check">
          <el-icon><Check /></el-icon>
          <span>规约检查</span>
        </el-menu-item>

        <div class="menu-group-title" v-show="!isCollapse">
          测试评估
        </div>

        <el-menu-item index="/test-report">
          <el-icon><DataAnalysis /></el-icon>
          <span>测试报告</span>
        </el-menu-item>

        <el-menu-item index="/test-report-template">
          <el-icon><Files /></el-icon>
          <span>报告模板</span>
        </el-menu-item>

        <el-menu-item index="/test-coverage">
          <el-icon><Odometer /></el-icon>
          <span>覆盖率率分析</span>
        </el-menu-item>

        <el-menu-item index="/test-risk-assessment">
          <el-icon><Warning /></el-icon>
          <span>风险评估</span>
        </el-menu-item>

        <el-menu-item index="/test-case-quality">
          <el-icon><Medal /></el-icon>
          <span>质量评估</span>
        </el-menu-item>

        <div class="menu-group-title" v-show="!isCollapse">
          测试执行
        </div>

        <el-menu-item index="/page-element">
          <el-icon><Pointer /></el-icon>
          <span>页面元素</span>
        </el-menu-item>

        <el-menu-item index="/ui-script-template">
          <el-icon><DocumentCopy /></el-icon>
          <span>脚本模板</span>
        </el-menu-item>

        <el-menu-item index="/ui-script-generation">
          <el-icon><Edit /></el-icon>
          <span>UI脚本生成</span>
        </el-menu-item>

        <el-menu-item index="/ui-script-repair">
          <el-icon><Tools /></el-icon>
          <span>UI脚本修复</span>
        </el-menu-item>

        <el-menu-item index="/test-execution">
          <el-icon><VideoPlay /></el-icon>
          <span>执行管理</span>
        </el-menu-item>

        <div class="menu-group-title" v-show="!isCollapse">
          设计文档
        </div>

        <el-menu-item index="/flow-document">
          <el-icon><Share /></el-icon>
          <span>流程文档生成</span>
        </el-menu-item>

        <el-menu-item index="/data-document">
          <el-icon><Grid /></el-icon>
          <span>数据文档生成</span>
        </el-menu-item>

        <div class="menu-group-title" v-show="!isCollapse">
          配置与资产
        </div>

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
          <div class="collapse-btn" @click="toggleCollapse">
            <el-icon :size="20">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </div>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title || '当前页面' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-profile">
              <el-avatar :size="32" class="user-avatar">
                A
              </el-avatar>
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
            <keep-alive :include="['RequirementList', 'TestCaseList', 'PromptTemplateList', 'ModelConfigList', 'KnowledgeBaseList']">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, List, MagicStick, CopyDocument, Reading, ChatDotRound, Setting, ArrowDown, Edit, Tools, VideoPlay, Share, Grid, Expand, Fold, DataAnalysis, Files, Odometer, Warning, Notebook, Check, Medal, Pointer, DocumentCopy } from '@element-plus/icons-vue'

const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.app-container {
  height: 100vh;
  position: relative;
  overflow: hidden;
  // Background is handled by body styles for the grid pattern
}

.app-sidebar {
  background: $bg-sidebar;
  color: white;
  display: flex;
  flex-direction: column;
  z-index: 10;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-right: 1px solid $border-color;

  .logo-container {
    height: 72px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid $border-color;
    
    .logo-icon {
      width: 32px;
      height: 32px;
      background: $tech-white;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 14px;
      
      .logo-text-inner {
        font-weight: 800;
        color: #000;
        font-size: 14px;
        letter-spacing: -0.05em;
      }
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: $text-main;
      margin: 0;
      letter-spacing: -0.02em;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    flex: 1;
    padding: 16px 0;
    overflow-y: auto;
    
    .menu-group-title {
      padding: 16px 24px 8px;
      font-size: 10px;
      color: $text-muted;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      font-weight: 600;
      font-family: $font-mono;
    }
    
    :deep(.el-menu-item) {
      margin: 2px 12px;
      border-radius: 4px;
      height: 40px;
      line-height: 40px;
      transition: all 0.2s;
      
      &:hover {
        background: rgba($tech-white, 0.05);
        color: $tech-white !important;
        
        .el-icon {
          color: $tech-white;
        }
      }
      
      &.is-active {
        background: $tech-white;
        color: #000 !important;
        font-weight: 600;
        
        .el-icon {
          color: #000;
        }
      }
      
      .el-icon {
        margin-right: 12px;
        font-size: 16px;
        transition: color 0.2s;
      }
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.app-header {
  background: rgba($bg-deep, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  border-bottom: 1px solid $border-color;
  position: relative;
  z-index: 5;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      margin-right: 16px;
      cursor: pointer;
      border-radius: 4px;
      color: $text-secondary;
      transition: all 0.2s;
      border: 1px solid transparent;
      
      &:hover {
        background: rgba($tech-white, 0.1);
        color: $text-main;
        border-color: $border-light;
      }
    }
  }
  
  .header-right {
    .user-profile {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: $text-regular;
      
      .user-avatar {
        background: $primary-dark;
        color: #000;
        font-weight: 600;
        margin-right: 12px;
        border: 1px solid $tech-white;
      }
      
      .username {
        margin-right: 8px;
        font-size: 14px;
        font-weight: 500;

      .el-icon {

        color: $text-secondary;

      }

    }

  }

}



.app-content {

  flex: 1;

  padding: 28px;

  overflow-y: auto;

  background: transparent;

  

  /* Custom Scrollbar - Aurora Style */

  &::-webkit-scrollbar {

    width: 8px;

  }

  

  &::-webkit-scrollbar-thumb {

    background: rgba(255, 255, 255, 0.1);

    border-radius: 4px;

    

    &:hover {

      background: rgba($neon-cyan, 0.3);

    }

  }

  

  &::-webkit-scrollbar-track {

    background: transparent;

  }

}

</style>

