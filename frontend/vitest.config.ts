import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    // 测试环境：使用happy-dom（比jsdom更快）
    environment: 'happy-dom',
    // 全局设置文件
    setupFiles: ['./tests/setup.ts'],
    // 包含的测试文件
    include: ['**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
    // 排除的文件和目录
    exclude: ['node_modules', 'dist', '.idea', '.git', '.cache'],
    // 覆盖率配置
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockData/**',
        'dist/',
        '.vite/',
        'coverage/'
      ],
      // 覆盖率阈值
      thresholds: {
        lines: 70,
        functions: 70,
        branches: 70,
        statements: 70
      }
    },
    // 测试超时时间（毫秒）
    testTimeout: 10000,
    // 全局测试变量
    globals: true
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})

