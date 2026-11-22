import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', 'VITE_')
  const gatewayProxy = {
    target: env.VITE_API_GATEWAY_URL || 'http://localhost:8080',
    changeOrigin: true,
  }

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': gatewayProxy,
        '/customer': gatewayProxy,
      },
    },
  }
})
