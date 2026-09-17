import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({

  plugins: [
    react(),
  ],

  /*
   * Production:
   * فایل‌های React زیر این مسیر از Spring Boot سرو می‌شوند:
   *
   * /admin/react/
   */
  base: '/admin/react/',

  /*
   * Development:
   * npm run dev همچنان مثل قبل کار می‌کند
   * و API را به Spring Boot روی 8080 می‌فرستد.
   */
  server: {

    port: 5173,

    strictPort: true,

    proxy: {

      '/admin/api': {

        target:
          'http://localhost:8080',

        changeOrigin:
          true,

      },
    },
  },

  /*
   * npm run build
   *
   * خروجی React مستقیماً داخل static
   * پروژه Spring Boot قرار می‌گیرد.
   */
  build: {

    outDir:
      '../src/main/resources/static/admin/react',

    emptyOutDir:
      true,

    assetsDir:
      'assets',

    sourcemap:
      false,
  },
})
