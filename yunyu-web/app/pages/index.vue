<script setup lang="ts">
/**
 * 首页占位页。
 * 当前用于承接云屿前端项目初始化后的首个可访问页面，
 * 后续会逐步替换为正式的品牌首页与内容入口。
 */
const config = useRuntimeConfig()
const colorMode = useColorMode()
</script>

<template>
  <main
    class="min-h-screen bg-default text-default transition-colors duration-300"
  >
    <div class="mx-auto flex min-h-screen w-full max-w-6xl items-center px-6 py-16">
      <section
        class="w-full overflow-hidden rounded-[32px] border border-default bg-elevated/80 shadow-2xl shadow-slate-900/5 backdrop-blur"
      >
        <div class="grid gap-10 p-8 lg:grid-cols-[1.35fr_0.95fr] lg:p-12">
          <div class="space-y-8">
            <div class="space-y-4">
              <p class="text-sm uppercase tracking-[0.3em] text-primary">
                Yunyu / 云屿
              </p>
              <h1 class="max-w-3xl text-4xl font-semibold leading-tight text-highlighted lg:text-6xl">
                Nuxt UI、Tailwind CSS 与 Color Mode 已接入
              </h1>
              <p class="max-w-2xl text-base leading-8 text-toned lg:text-lg">
                当前前端骨架已经具备统一主题机制。接下来可以在这个基础上继续搭建最美博客的前台视觉系统，以及后台管理体验。
              </p>
            </div>

            <div class="flex flex-wrap gap-3">
              <UBadge color="primary" variant="soft" size="lg">Nuxt 4</UBadge>
              <UBadge color="primary" variant="soft" size="lg">Tailwind CSS</UBadge>
              <UBadge color="neutral" variant="soft" size="lg">@nuxt/ui</UBadge>
              <UBadge color="neutral" variant="soft" size="lg">@nuxtjs/color-mode</UBadge>
            </div>

            <div class="rounded-2xl border border-default bg-default/80 p-5">
              <p class="mb-3 text-sm font-medium text-muted">主题切换</p>
              <ThemeModeSwitch />
            </div>
          </div>

          <div class="flex flex-col gap-4">
            <UCard>
              <template #header>
                <div class="flex items-center justify-between gap-3">
                  <span class="text-sm font-medium text-highlighted">运行时配置</span>
                  <UBadge color="success" variant="soft">
                    {{ colorMode.preference }}
                  </UBadge>
                </div>
              </template>

              <div class="space-y-3 text-sm text-toned">
                <p>默认开发端口：<code class="rounded bg-muted px-2 py-1 text-default">19999</code></p>
                <p>后端接口地址：<code class="rounded bg-muted px-2 py-1 text-default">{{ config.public.apiBase }}</code></p>
                <p>默认主题策略：<code class="rounded bg-muted px-2 py-1 text-default">system</code></p>
              </div>
            </UCard>

            <UCard>
              <template #header>
                <span class="text-sm font-medium text-highlighted">当前约定</span>
              </template>

              <ul class="space-y-3 text-sm leading-7 text-toned">
                <li>前后台共享统一主题机制与设计底层。</li>
                <li>语义色、圆角、排版和层级通过设计 token 管理。</li>
                <li>后续页面开发优先遵循文档中的前后台边界与主题规范。</li>
              </ul>
            </UCard>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
