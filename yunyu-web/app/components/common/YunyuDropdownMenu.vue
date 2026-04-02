<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

/**
 * 云屿通用下拉菜单项类型。
 * 作用：统一约束下拉菜单中单个操作项的数据结构，方便前台多个场景复用同一套菜单组件。
 */
export interface YunyuDropdownMenuItem {
  key: string
  label: string
  description?: string
  icon?: string
  to?: string
  disabled?: boolean
  tone?: 'default' | 'danger'
}

/**
 * 云屿通用下拉菜单组件。
 * 作用：提供不依赖 Nuxt UI 下拉菜单的通用弹层能力，统一前台菜单的主题适配、分组结构与交互行为。
 */
const props = withDefaults(defineProps<{
  items: YunyuDropdownMenuItem[][]
  align?: 'start' | 'end'
  variant?: 'solid' | 'overlay'
}>(), {
  align: 'end',
  variant: 'solid'
})

const emit = defineEmits<{
  select: [item: YunyuDropdownMenuItem]
}>()

const route = useRoute()
const rootRef = ref<HTMLElement | null>(null)
const isOpen = ref(false)

/**
 * 计算菜单面板定位方式。
 * 作用：根据传入的对齐方式决定菜单向左还是向右展开，方便复用在不同布局位置。
 */
const panelPositionClassName = computed(() => {
  return props.align === 'start' ? 'left-0 origin-top-left' : 'right-0 origin-top-right'
})

/**
 * 计算菜单面板基础样式。
 * 作用：统一封装覆盖态和实底态下的菜单背景、边框、阴影与文字对比度。
 */
const panelClassName = computed(() => {
  if (props.variant === 'overlay') {
    return 'border border-slate-200/88 bg-[linear-gradient(180deg,rgba(255,255,255,0.94)_0%,rgba(249,250,252,0.92)_100%)] text-slate-800 shadow-[0_24px_56px_-26px_rgba(15,23,42,0.28),0_12px_28px_-24px_rgba(15,23,42,0.2)] ring-1 ring-white/80 backdrop-blur-[22px] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(31,41,55,0.84)_0%,rgba(15,23,42,0.8)_100%)] dark:text-slate-100 dark:ring-white/8 dark:shadow-[0_24px_56px_-26px_rgba(2,6,23,0.6),0_12px_28px_-24px_rgba(15,23,42,0.42)]'
  }

  return 'border border-slate-200/85 bg-[linear-gradient(180deg,rgba(255,255,255,0.86)_0%,rgba(247,249,252,0.82)_100%)] text-slate-800 shadow-[0_24px_56px_-26px_rgba(15,23,42,0.24),0_12px_28px_-24px_rgba(15,23,42,0.18)] ring-1 ring-white/70 backdrop-blur-[26px] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(31,41,55,0.82)_0%,rgba(15,23,42,0.78)_100%)] dark:text-slate-100 dark:ring-white/8 dark:shadow-[0_24px_56px_-26px_rgba(2,6,23,0.6),0_12px_28px_-24px_rgba(15,23,42,0.42)]'
})

/**
 * 计算菜单组之间的分隔线样式。
 * 作用：增强多组菜单项之间的视觉分区，但避免分隔线过重。
 */
const groupSeparatorClassName = computed(() => {
  return props.variant === 'overlay'
    ? 'border-slate-200/75 dark:border-white/8'
    : 'border-slate-200/75 dark:border-white/8'
})

/**
 * 计算菜单项图标容器样式。
 * 作用：让图标块更接近 macOS 浮层菜单里柔和、低对比的符号承载方式。
 *
 * @param item 菜单项
 * @returns 图标容器样式类名
 */
function resolveIconWrapperClassName(item: YunyuDropdownMenuItem) {
  if (item.disabled) {
    return 'bg-slate-900/[0.03] text-slate-300 dark:bg-white/[0.04] dark:text-slate-600'
  }

  if (item.tone === 'danger') {
    return 'bg-rose-50 text-rose-500 dark:bg-rose-400/10 dark:text-rose-300'
  }

  return 'bg-slate-900/[0.035] text-slate-600 dark:bg-white/[0.06] dark:text-slate-200'
}

/**
 * 切换菜单展开状态。
 * 作用：在触发器点击时统一控制菜单开关，避免父组件重复处理菜单显隐状态。
 */
function toggleMenu() {
  isOpen.value = !isOpen.value
}

/**
 * 关闭菜单。
 * 作用：供路由切换、点击外部和菜单项选择后复用同一套收起逻辑。
 */
function closeMenu() {
  isOpen.value = false
}

/**
 * 处理触发器点击。
 * 作用：为插槽触发器提供统一的点击入口，并防止默认行为影响菜单开关。
 *
 * @param event 点击事件
 */
function handleTriggerClick(event: MouseEvent) {
  event.preventDefault()
  toggleMenu()
}

/**
 * 处理菜单项选择。
 * 作用：统一拦截禁用态项，并在触发选择后优先处理路由跳转，
 * 再通知父组件处理额外业务动作，例如退出登录。
 *
 * @param item 被选择的菜单项
 */
async function handleItemSelect(item: YunyuDropdownMenuItem) {
  if (item.disabled) {
    return
  }

  if (item.to) {
    await navigateTo(item.to)
  }

  emit('select', item)
  closeMenu()
}

/**
 * 判断菜单项是否处于当前选中态。
 * 作用：让具备路由地址的菜单项在当前页面或其子页面下显示明确的选中色彩。
 *
 * @param item 菜单项
 * @returns 是否选中
 */
function isItemActive(item: YunyuDropdownMenuItem) {
  if (!item.to) {
    return false
  }

  return route.path === item.to || route.path.startsWith(`${item.to}/`)
}

/**
 * 处理点击外部区域。
 * 作用：当用户点击菜单外部时自动收起菜单，保持交互符合常见下拉菜单习惯。
 *
 * @param event 指针事件
 */
function handleDocumentPointerDown(event: PointerEvent) {
  const eventTarget = event.target

  if (!(eventTarget instanceof Node)) {
    return
  }

  if (!rootRef.value?.contains(eventTarget)) {
    closeMenu()
  }
}

/**
 * 处理键盘按键。
 * 作用：支持使用 `Escape` 快速关闭菜单，提升键盘交互体验。
 *
 * @param event 键盘事件
 */
function handleWindowKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeMenu()
  }
}

/**
 * 计算单个菜单项样式。
 * 作用：根据菜单项状态和危险操作语义输出对应的视觉样式。
 *
 * @param item 菜单项
 * @returns 菜单项样式类名
 */
function resolveItemClassName(item: YunyuDropdownMenuItem) {
  if (item.disabled) {
    return 'cursor-not-allowed border-transparent text-slate-300 dark:text-slate-600'
  }

  if (item.tone === 'danger') {
    return 'text-rose-600 hover:border-rose-200/70 hover:bg-rose-50/95 active:scale-[0.992] dark:text-rose-300 dark:hover:border-rose-400/20 dark:hover:bg-rose-400/12'
  }

  return 'text-slate-700 active:scale-[0.992] dark:text-slate-200'
}

/**
 * 计算插槽触发器需要绑定的属性。
 * 作用：让父组件自定义触发器结构时仍可复用统一的无障碍和点击行为。
 */
const triggerProps = computed(() => {
  return {
    type: 'button',
    'aria-haspopup': 'menu',
    'aria-expanded': isOpen.value,
    onClick: handleTriggerClick
  }
})

watch(() => route.fullPath, () => {
  closeMenu()
})

onMounted(() => {
  if (!import.meta.client) {
    return
  }

  document.addEventListener('pointerdown', handleDocumentPointerDown)
  window.addEventListener('keydown', handleWindowKeydown)
})

onBeforeUnmount(() => {
  if (!import.meta.client) {
    return
  }

  document.removeEventListener('pointerdown', handleDocumentPointerDown)
  window.removeEventListener('keydown', handleWindowKeydown)
})
</script>

<template>
  <div ref="rootRef" class="relative yunyu-dropdown-theme">
    <slot name="trigger" :open="isOpen" :trigger-props="triggerProps">
      <button
        v-bind="triggerProps"
        class="inline-flex min-h-10 items-center justify-center rounded-full px-4 text-sm font-medium"
      >
        菜单
      </button>
    </slot>

    <transition
      enter-active-class="transition duration-160 ease-out"
      enter-from-class="translate-y-1.5 scale-[0.98] opacity-0"
      enter-to-class="translate-y-0 scale-100 opacity-100"
      leave-active-class="transition duration-120 ease-in"
      leave-from-class="translate-y-0 scale-100 opacity-100"
      leave-to-class="translate-y-1 scale-[0.98] opacity-0"
    >
      <div
        v-if="isOpen"
        :class="[panelPositionClassName, panelClassName]"
        class="absolute top-[calc(100%+0.75rem)] z-50 min-w-[236px] overflow-hidden rounded-[16px] px-2 py-2.5"
      >
        <div
          v-for="(group, groupIndex) in props.items"
          :key="`group-${groupIndex}`"
          :class="[
            'space-y-1 py-0.5',
            groupIndex > 0 ? `border-t ${groupSeparatorClassName} mt-1.5 pt-2.5` : ''
          ]"
        >
          <button
            v-for="item in group"
            :key="item.key"
            type="button"
            :disabled="item.disabled"
            :class="resolveItemClassName(item)"
            :data-tone="item.tone || 'default'"
            :data-variant="props.variant"
            :data-active="isItemActive(item)"
            class="yunyu-dropdown-item flex w-full items-start gap-3 rounded-[11px] border border-transparent px-2.5 py-2.5 text-left transition duration-150 ease-out focus-visible:outline-none"
            @click="handleItemSelect(item)"
          >
            <div
              :class="[resolveIconWrapperClassName(item), item.disabled ? 'opacity-40' : '']"
              class="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-[9px] border border-black/5 dark:border-white/6"
            >
              <UIcon v-if="item.icon" :name="item.icon" class="size-4.5" />
            </div>

            <div class="min-w-0 flex-1">
              <p class="truncate text-[13px] font-medium tracking-[-0.01em]">
                {{ item.label }}
              </p>
            </div>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"][data-active="true"] {
  color: color-mix(in srgb, var(--ui-primary) 74%, rgb(15 23 42) 26%);
  border-color: color-mix(in srgb, var(--ui-primary) 22%, white 78%);
  background: color-mix(in srgb, var(--ui-primary) 16%, white 84%);
}

.yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"][data-variant="solid"]:not(:disabled):hover {
  border-color: color-mix(in srgb, var(--ui-primary) 24%, white 76%);
  background: color-mix(in srgb, var(--ui-primary) 10%, white 90%);
}

.yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"][data-variant="overlay"]:not(:disabled):hover {
  border-color: color-mix(in srgb, var(--ui-primary) 24%, white 76%);
  background: color-mix(in srgb, var(--ui-primary) 10%, white 90%);
}

.dark .yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"][data-active="true"] {
  color: color-mix(in srgb, var(--ui-primary) 70%, white 30%);
  border-color: color-mix(in srgb, var(--ui-primary) 26%, rgba(255, 255, 255, 0.08));
  background: color-mix(in srgb, var(--ui-primary) 18%, rgba(255, 255, 255, 0.04));
}

.dark .yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"][data-variant="solid"]:not(:disabled):hover {
  border-color: color-mix(in srgb, var(--ui-primary) 28%, rgba(255, 255, 255, 0.08));
  background: color-mix(in srgb, var(--ui-primary) 16%, rgba(255, 255, 255, 0.04));
}

.dark .yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"][data-variant="overlay"]:not(:disabled):hover {
  border-color: color-mix(in srgb, var(--ui-primary) 28%, rgba(255, 255, 255, 0.08));
  background: color-mix(in srgb, var(--ui-primary) 16%, rgba(255, 255, 255, 0.04));
}

.yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="danger"][data-active="true"] {
  border-color: rgb(254 205 211 / 0.9);
  background: rgb(255 241 242 / 0.95);
}

.dark .yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="danger"][data-active="true"] {
  border-color: rgb(251 113 133 / 0.22);
  background: rgb(244 63 94 / 0.12);
}

.yunyu-dropdown-theme .yunyu-dropdown-item[data-tone="default"]:not(:disabled):focus-visible {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--ui-primary) 42%, transparent);
}
</style>
