<template>
  <el-container class="app-container">
    <!-- Aurora Background Effect -->
    <div class="aurora-bg">
      <div class="aurora-blob aurora-blob-1" />
      <div class="aurora-blob aurora-blob-2" />
      <div class="aurora-blob aurora-blob-3" />
    </div>

    <el-aside
      :width="isCollapse ? '72px' : '260px'"
      class="app-sidebar"
    >

      <div class="logo-container">
        <div class="logo-icon">
          <span class="logo-text-inner">TG</span>
        </div>

        <h1
          class="logo-text"
          v-show="!isCollapse"
        >
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
        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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

        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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

        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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

        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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

        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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

        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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

        <div
          class="menu-group-title"
          v-show="!isCollapse"
        >
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
          <div
            class="collapse-btn"
            @click="toggleCollapse"
          >
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
              <el-avatar
                :size="32"
                class="user-avatar"
              >
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
          <transition
            name="fade-transform"
            mode="out-in"
          >
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
// Aurora Dark Mode Colors (scoped copy)

$bg-deep: #0a0a0f;

$bg-sidebar: #0d0d14;

$bg-header: rgba(13, 13, 20, 0.85);

$neon-cyan: #00d4ff;

$neon-purple: #8b5cf6;

$neon-magenta: #ff00ff;

$text-main: #ffffff;

$text-secondary: #9ca3af;

$text-muted: #6b7280;

$border-color: rgba(255, 255, 255, 0.1);

$gradient-aurora: linear-gradient(135deg, $neon-cyan 0%, $neon-purple 50%, $neon-magenta 100%);



.app-container {

  height: 100vh;

  background-color: $bg-deep;

  position: relative;

  overflow: hidden;

}



// Aurora Background Effect

.aurora-bg {

  position: fixed;

  top: 0;

  left: 0;

  right: 0;

  bottom: 0;

  pointer-events: none;

  z-index: 0;

  overflow: hidden;



  .aurora-blob {

    position: absolute;

    border-radius: 50%;

    filter: blur(80px);

    opacity: 0.4;

    animation: float 20s ease-in-out infinite;

  }



  .aurora-blob-1 {

    width: 600px;

    height: 600px;

    background: radial-gradient(circle, rgba($neon-cyan, 0.3) 0%, transparent 70%);

    top: -200px;

    right: -100px;

    animation-delay: 0s;

  }



  .aurora-blob-2 {

    width: 500px;

    height: 500px;

    background: radial-gradient(circle, rgba($neon-purple, 0.25) 0%, transparent 70%);

    bottom: -150px;

    left: 200px;

    animation-delay: -7s;

  }



  .aurora-blob-3 {

    width: 400px;

    height: 400px;

    background: radial-gradient(circle, rgba($neon-magenta, 0.2) 0%, transparent 70%);

    top: 40%;

    left: 50%;

    animation-delay: -14s;

  }

}



@keyframes float {

  0%, 100% {

    transform: translate(0, 0) scale(1);

  }

  33% {

    transform: translate(30px, -30px) scale(1.05);

  }

  66% {

    transform: translate(-20px, 20px) scale(0.95);

  }

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



  // Glow border effect

  &::after {

    content: '';

    position: absolute;

    top: 0;

    right: 0;

    width: 1px;

    height: 100%;

    background: linear-gradient(180deg, 

      transparent 0%, 

      rgba($neon-cyan, 0.5) 20%, 

      rgba($neon-purple, 0.5) 50%, 

      rgba($neon-magenta, 0.3) 80%, 

      transparent 100%

    );

  }

  

  .logo-container {

    height: 72px;

    display: flex;

    align-items: center;

    padding: 0 20px;

    border-bottom: 1px solid $border-color;

    

    .logo-icon {

      width: 40px;

      height: 40px;

      background: $gradient-aurora;

      border-radius: 10px;

      display: flex;

      align-items: center;

      justify-content: center;

      margin-right: 14px;

      box-shadow: 0 0 20px rgba($neon-cyan, 0.4), 0 0 40px rgba($neon-purple, 0.2);

      

      .logo-text-inner {

        font-weight: bold;

        color: #000;

        font-size: 14px;

      }

    }

    

    .logo-text {

      font-size: 20px;

      font-weight: 700;

      color: white;

      margin: 0;

      background: $gradient-aurora;

      -webkit-background-clip: text;

      -webkit-text-fill-color: transparent;

      background-clip: text;

    }

  }

  

  .sidebar-menu {

    border-right: none;

    flex: 1;

    padding: 16px 0;

    overflow-y: auto;

    

    .menu-group-title {

      padding: 16px 24px 8px;

      font-size: 11px;

      color: $text-muted;

      text-transform: uppercase;

      letter-spacing: 0.1em;

      font-weight: 600;

    }

    

    :deep(.el-menu-item) {

      margin: 4px 12px;

      border-radius: 10px;

      height: 46px;

      line-height: 46px;

      transition: all 0.3s;

      position: relative;

      overflow: hidden;

      

      &:hover {

        background: rgba($neon-cyan, 0.1);

        color: $neon-cyan !important;

        

        .el-icon {

          color: $neon-cyan;

        }

      }

      

      &.is-active {

        background: linear-gradient(90deg, rgba($neon-cyan, 0.2) 0%, rgba($neon-purple, 0.15) 100%);

        color: white !important;

        border: 1px solid rgba($neon-cyan, 0.3);

        box-shadow: 0 0 15px rgba($neon-cyan, 0.2), inset 0 0 20px rgba($neon-cyan, 0.05);

        

        &::before {

          content: '';

          position: absolute;

          left: 0;

          top: 50%;

          transform: translateY(-50%);

          width: 3px;

          height: 24px;

          background: $gradient-aurora;

          border-radius: 0 2px 2px 0;

          box-shadow: 0 0 10px $neon-cyan;

        }

        

        .el-icon {

          color: $neon-cyan;

        }

      }

      

      .el-icon {

        margin-right: 12px;

        font-size: 18px;

        transition: color 0.3s;

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

  background: $bg-header;

  backdrop-filter: blur(20px);

  -webkit-backdrop-filter: blur(20px);

  height: 72px;

  display: flex;

  align-items: center;

  justify-content: space-between;

  padding: 0 28px;

  border-bottom: 1px solid $border-color;

  position: relative;

  z-index: 5;

  

  // Subtle glow line at bottom

  &::after {

    content: '';

    position: absolute;

    bottom: 0;

    left: 0;

    right: 0;

    height: 1px;

    background: linear-gradient(90deg, 

      transparent 0%, 

      rgba($neon-cyan, 0.3) 20%, 

      rgba($neon-purple, 0.3) 50%, 

      rgba($neon-cyan, 0.3) 80%, 

      transparent 100%

    );

  }

  

  .header-left {

    display: flex;

    align-items: center;

    

    .collapse-btn {

      display: flex;

      align-items: center;

      justify-content: center;

      width: 40px;

      height: 40px;

      margin-right: 16px;

      cursor: pointer;

      border-radius: 8px;

      color: $text-secondary;

      transition: all 0.3s;

      border: 1px solid transparent;

      

      &:hover {

        background: rgba($neon-cyan, 0.1);

        color: $neon-cyan;

        border-color: rgba($neon-cyan, 0.2);

      }

    }

  }

  

  .header-right {

    .user-profile {

      display: flex;

      align-items: center;

      cursor: pointer;

      padding: 6px 12px;

      border-radius: 10px;

      transition: all 0.3s;

      border: 1px solid transparent;

      

      &:hover {

        background: rgba($neon-cyan, 0.1);

        border-color: rgba($neon-cyan, 0.2);

      }

      

      .user-avatar {

        background: $gradient-aurora;

        font-size: 13px;

        font-weight: 600;

        color: #000;

      }

      

      .username {

        margin: 0 10px;

        font-size: 14px;

        color: $text-main;

        font-weight: 500;

      }

      

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

