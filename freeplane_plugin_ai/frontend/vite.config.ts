import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)) // 关键：@ 指向 src 目录
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:6299',
        changeOrigin: true,
        secure: false
      }
    }
  },
  build: {
    outDir: '../src/main/resources/org/freeplane/plugin/ai/web',
    emptyOutDir: true
  }
})