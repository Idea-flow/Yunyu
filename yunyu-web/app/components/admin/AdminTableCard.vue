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
  <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
    <template #header>
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-base font-semibold text-highlighted">{{ title }}</p>
          <p v-if="description" class="text-sm text-muted">{{ description }}</p>
        </div>

        <div class="flex items-center gap-3">
          <slot name="actions" />
          <UBadge v-if="typeof total === 'number'" color="neutral" variant="soft">
            共 {{ total }} 条
          </UBadge>
        </div>
      </div>
    </template>

    <slot />

    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </UCard>
</template>
