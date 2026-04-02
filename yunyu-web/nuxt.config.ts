// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: [
    '@nuxt/icon',
    '@nuxt/ui',
    '@nuxtjs/color-mode'
  ],
  css: ['~/assets/css/main.css'],
  runtimeConfig: {
    public: {
      apiBase: process.env.YUNYU_PUBLIC_API_BASE || 'http://127.0.0.1:20000'
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
          rel: 'preconnect',
          href: 'https://fonts.googleapis.com'
        },
        {
          rel: 'preconnect',
          href: 'https://fonts.gstatic.com',
          crossorigin: ''
        },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap'
        },
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
