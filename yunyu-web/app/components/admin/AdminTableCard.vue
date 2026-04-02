<script setup lang="ts">
/**
 * 后台列表卡片组件。
 * 作用：为后台管理页提供统一的列表容器、标题区和分页区骨架，
 * 便于用户管理、文章管理等页面复用一致的列表视觉结构。
 */
defineProps<{
  title: string
  description?: string
  total?: number
}>()
</script>

<template>
  <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
    <div class="border-b border-white/60 px-5 py-4 dark:border-white/10">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-base font-semibold text-slate-900 dark:text-slate-50">{{ title }}</p>
        </div>

        <div class="flex flex-wrap items-center gap-3 md:justify-end">
          <slot name="actions" />
          <UBadge
            v-if="typeof total === 'number'"
            color="neutral"
            variant="soft"
            class="rounded-[8px] border border-white/60 px-3 py-1 dark:border-white/10"
          >
            共 {{ total }} 条
          </UBadge>
        </div>
      </div>
    </div>

    <div class="px-5 py-5">
      <slot />
    </div>

    <div v-if="$slots.footer" class="border-t border-white/60 px-5 py-4 dark:border-white/10">
      <slot name="footer" />
    </div>
  </section>
</template>
