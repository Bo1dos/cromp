import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// В Docker — http://app:8080, локально — http://localhost:8081
const proxyTarget = process.env.VITE_PROXY_TARGET || 'http://localhost:8081';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: proxyTarget,
        changeOrigin: true,
      },
    },
  },
});
