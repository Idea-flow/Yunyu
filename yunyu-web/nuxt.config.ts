// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: [
    '@nuxt/icon',
    '@nuxt/ui',
    '@nuxtjs/color-mode'
  ],
  ui: {
    // 关闭 @nuxt/ui 自动注入的 @nuxt/fonts，避免开发环境请求 Google 字体元数据。
    fonts: false
  },
  icon: {
    customCollections: [
      {
        prefix: 'social',
        dir: './app/assets/svg'
      }
    ]
  },
  css: ['katex/dist/katex.min.css', '~/assets/css/main.css'],
  runtimeConfig: {
    // 仅服务端可见：SSR 阶段调用后端使用容器内部地址，避免走 nginx 或公网
    yunyuApiBaseInternal: process.env.NUXT_YUNYU_API_BASE_INTERNAL || '',
    public: {
      // 浏览器端使用：留空时请求走相对路径，由 nginx 同域转发到后端
      apiBase: process.env.YUNYU_PUBLIC_API_BASE || ''
    }
  },
  colorMode: {
    preference: 'system',
    fallback: 'light',
    classSuffix: ''
  },
  app: {
    head: {
      title: '云屿 Yunyu',
      link: [
        {
          rel: 'icon',
          type: 'image/x-icon',
          href: '/favicon.ico',
          sizes: 'any'
        },
        {
          rel: 'apple-touch-icon',
          href: '/apple-touch-icon.png'
        },
        {
          rel: 'manifest',
          href: '/site.webmanifest'
        }
      ],
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'theme-color', content: '#f7f5ef' },
        {
          name: 'description',
          content: '云屿前端项目，基于 Nuxt 4 构建内容站与后台管理界面。'
        }
      ]
    }
  }
})
