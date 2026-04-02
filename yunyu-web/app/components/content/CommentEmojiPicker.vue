<script setup lang="ts">
import { COMMENT_EMOJI_PACK, type CommentEmojiItem } from '../../constants/commentEmoji'

/**
 * 评论表情选择器组件。
 * 作用：为评论输入框提供可复用的自定义表情选择面板，后续可接入更多内容输入场景。
 */
const props = withDefaults(defineProps<{
  disabled?: boolean
}>(), {
  disabled: false
})

const emit = defineEmits<{
  select: [emoji: CommentEmojiItem]
}>()

const isOpen = ref(false)
const rootRef = ref<HTMLElement | null>(null)

/**
 * 切换表情面板显隐状态。
 * 作用：控制表情弹层开关，并在禁用态下阻止交互。
 */
function togglePanel() {
  if (props.disabled) {
    return
  }

  isOpen.value = !isOpen.value
}

/**
 * 选择表情。
 * 作用：将当前选中的表情透传给父组件，并在选择后自动关闭面板。
 *
 * @param emoji 当前表情项
 */
function selectEmoji(emoji: CommentEmojiItem) {
  emit('select', emoji)
  isOpen.value = false
}

/**
 * 关闭表情面板。
 * 作用：在点击外部区域时收起弹层，保持评论工具栏简洁。
 *
 * @param event 鼠标事件
 */
function handleDocumentClick(event: MouseEvent) {
  if (!rootRef.value?.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<template>
  <div ref="rootRef" class="relative">
    <button
      type="button"
      class="inline-flex size-9 items-center justify-center rounded-full border border-slate-200/80 bg-white text-slate-500 transition hover:border-slate-300 hover:text-slate-900 disabled:cursor-not-allowed disabled:opacity-55 dark:border-white/10 dark:bg-slate-950/60 dark:text-slate-400 dark:hover:border-white/15 dark:hover:text-slate-100"
      :disabled="props.disabled"
      @click="togglePanel"
    >
      <UIcon name="i-lucide-laugh" class="size-4.5" />
    </button>

    <div
      v-if="isOpen"
      class="absolute left-0 top-full z-20 mt-2 w-[356px] rounded-[20px] border border-slate-200/80 bg-white/96 p-3.5 shadow-[0_24px_60px_-38px_rgba(15,23,42,0.3)] backdrop-blur dark:border-white/10 dark:bg-slate-950/92"
    >
      <p class="mb-3 px-1 text-xs font-medium text-slate-400 dark:text-slate-500">
        自定义表情
      </p>

      <div class="grid grid-cols-4 gap-2">
        <button
          v-for="emoji in COMMENT_EMOJI_PACK"
          :key="emoji.id"
          type="button"
          class="flex flex-col items-center gap-1.5 rounded-[16px] border border-transparent px-2.5 py-2.5 text-[11px] text-slate-500 transition hover:border-slate-200 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-400 dark:hover:border-white/10 dark:hover:bg-white/6 dark:hover:text-slate-100"
          @click="selectEmoji(emoji)"
        >
          <img
            :src="emoji.src"
            :alt="emoji.name"
            class="size-11 object-contain"
          >
          <span class="leading-none">{{ emoji.name }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
