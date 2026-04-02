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
  <section class="admin-surface overflow-hidden">
    <div class="admin-toolbar px-5 py-4">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">数据列表</p>
          <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">{{ title }}</p>
          <p v-if="description" class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ description }}</p>
        </div>

        <div class="flex flex-wrap items-center gap-3 md:justify-end">
          <slot name="actions" />
          <UBadge
            v-if="typeof total === 'number'"
            color="neutral"
            variant="soft"
            class="rounded-full border border-default/70 px-3 py-1"
          >
            共 {{ total }} 条
          </UBadge>
        </div>
      </div>
    </div>

    <div class="px-5 py-5">
      <slot />
    </div>

    <div v-if="$slots.footer" class="border-t border-slate-200/80 px-5 py-4 dark:border-slate-800">
      <slot name="footer" />
    </div>
  </section>
</template>
