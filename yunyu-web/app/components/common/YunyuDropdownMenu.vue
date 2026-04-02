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
    return 'border border-white/12 bg-[linear-gradient(180deg,rgba(15,23,42,0.88)_0%,rgba(15,23,42,0.8)_100%)] text-white shadow-[0_24px_60px_-30px_rgba(2,6,23,0.65)] backdrop-blur-[18px]'
  }

  return 'border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.96)_0%,rgba(248,250,252,0.94)_100%)] text-slate-800 shadow-[0_26px_60px_-36px_rgba(15,23,42,0.28)] backdrop-blur-[18px] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.96)_0%,rgba(15,23,42,0.9)_100%)] dark:text-slate-100'
})

/**
 * 计算菜单项说明文字样式。
 * 作用：确保描述信息在明暗主题下都保持层次感而不过分抢占注意力。
 */
const itemDescriptionClassName = computed(() => {
  return props.variant === 'overlay'
    ? 'text-white/56'
    : 'text-slate-500 dark:text-slate-400'
})

/**
 * 计算菜单组之间的分隔线样式。
 * 作用：增强多组菜单项之间的视觉分区，但避免分隔线过重。
 */
const groupSeparatorClassName = computed(() => {
  return props.variant === 'overlay'
    ? 'border-white/10'
    : 'border-slate-200/70 dark:border-white/10'
})

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
    return props.variant === 'overlay'
      ? 'cursor-not-allowed border-transparent text-white/28'
      : 'cursor-not-allowed border-transparent text-slate-300 dark:text-slate-600'
  }

  if (item.tone === 'danger') {
    return props.variant === 'overlay'
      ? 'text-rose-200 hover:border-white/10 hover:bg-white/8'
      : 'text-rose-600 hover:border-rose-100 hover:bg-rose-50/92 dark:text-rose-300 dark:hover:border-rose-400/20 dark:hover:bg-rose-400/10'
  }

  return props.variant === 'overlay'
    ? 'text-white/88 hover:border-white/10 hover:bg-white/8'
    : 'text-slate-700 hover:border-slate-200 hover:bg-slate-50/92 dark:text-slate-200 dark:hover:border-white/10 dark:hover:bg-white/6'
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
  <div ref="rootRef" class="relative">
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
        class="absolute top-[calc(100%+0.75rem)] z-50 min-w-[224px] overflow-hidden rounded-[22px] px-2 py-2"
      >
        <div
          v-for="(group, groupIndex) in props.items"
          :key="`group-${groupIndex}`"
          :class="[
            'space-y-1 py-1',
            groupIndex > 0 ? `border-t ${groupSeparatorClassName} mt-1 pt-2` : ''
          ]"
        >
          <button
            v-for="item in group"
            :key="item.key"
            type="button"
            :disabled="item.disabled"
            :class="resolveItemClassName(item)"
            class="flex w-full items-start gap-3 rounded-[16px] border border-transparent px-3 py-3 text-left transition duration-150"
            @click="handleItemSelect(item)"
          >
            <div
              :class="item.disabled ? 'opacity-40' : ''"
              class="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-[12px] bg-black/6 dark:bg-white/6"
            >
              <UIcon v-if="item.icon" :name="item.icon" class="size-4.5" />
            </div>

            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-semibold">
                {{ item.label }}
              </p>
              <p v-if="item.description" :class="itemDescriptionClassName" class="mt-1 text-xs leading-5">
                {{ item.description }}
              </p>
            </div>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>
