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
  <UCard class="admin-surface-card rounded-[30px]">
    <template #header>
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="admin-kicker">数据列表</p>
          <p class="mt-1 text-base font-semibold text-[color:var(--admin-text-strong)]">{{ title }}</p>
          <p v-if="description" class="mt-1 text-sm text-[color:var(--admin-text-muted)]">{{ description }}</p>
        </div>

        <div class="flex items-center gap-3">
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
    </template>

    <slot />

    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </UCard>
</template>
