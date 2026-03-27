import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0', // 监听所有网络接口，包括 IPv4 和 IPv6
    allowedHosts: ['test-design-frontend-dev', 'frontend-dev', 'localhost', '127.0.0.1'],
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    // 生产环境构建优化
    target: 'es2015',
    minify: 'terser', // 使用terser压缩
    terserOptions: {
      compress: {
        drop_console: true, // 移除console
        drop_debugger: true // 移除debugger
      }
    },
    // 代码分割优化
    rollupOptions: {
      output: {
        manualChunks: {
          // 将Vue相关库单独打包
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // 将Element Plus单独打包
          'element-plus': ['element-plus'],
          // 将工具库单独打包
          'utils': ['axios']
        },
        // 优化chunk文件名
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    },
    // 启用gzip压缩
    reportCompressedSize: true,
    // 减少chunk大小警告阈值（500kb）
    chunkSizeWarningLimit: 1000
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'element-plus', 'axios']
  }
})
