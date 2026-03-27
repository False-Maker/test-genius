<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-gradient"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <span class="logo-text-inner">TG</span>
        </div>
        <h1 class="login-title">Test Genius</h1>
        <p class="login-subtitle">当前环境未启用服务端认证，直接进入系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '进入中...' : '进入系统' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span class="copyright">© 2024 Test Genius</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      userStore.login(
        {
          id: 1,
          username: loginForm.username,
          nickname: loginForm.username
        },
        'dev-bypass-token'
      )

      ElMessage.info('当前环境未启用服务端认证，已直接进入系统')

      // 跳转到目标页面或首页
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;

  .bg-gradient {
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 50% 0%, rgba($acid-green, 0.15) 0%, transparent 50%);
  }

  .bg-grid {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba($border-color, 0.3) 1px, transparent 1px),
      linear-gradient(90deg, rgba($border-color, 0.3) 1px, transparent 1px);
    background-size: 50px 50px;
    opacity: 0.5;
  }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-large;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo-icon {
    width: 56px;
    height: 56px;
    background: $tech-white;
    border-radius: $radius-base;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;

    .logo-text-inner {
      font-weight: 800;
      color: #000;
      font-size: 20px;
      letter-spacing: -0.05em;
    }
  }

  .login-title {
    font-size: 24px;
    font-weight: 600;
    color: $text-main;
    margin: 0 0 8px;
    letter-spacing: -0.02em;
  }

  .login-subtitle {
    font-size: 14px;
    color: $text-secondary;
    margin: 0;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    background: rgba($bg-deep, 0.5);
    border: 1px solid $border-color;
    box-shadow: none;
    border-radius: $radius-base;
    padding: 4px 12px;

    &:hover {
      border-color: $border-light;
    }

    &.is-focus {
      border-color: $tech-white;
    }
  }

  :deep(.el-input__inner) {
    color: $text-main;
    height: 40px;

    &::placeholder {
      color: $text-placeholder;
    }
  }

  :deep(.el-input__prefix) {
    color: $text-muted;
  }

  :deep(.el-form-item__error) {
    color: $danger;
    font-size: 12px;
    padding-top: 4px;
  }
}

.login-button {
  width: 100%;
  height: 44px;
  background: $tech-white;
  color: #000;
  border: none;
  border-radius: $radius-base;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #fff;
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid $border-color;

  .copyright {
    font-size: 12px;
    color: $text-muted;
  }
}
</style>
