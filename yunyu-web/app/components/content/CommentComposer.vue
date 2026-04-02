<script setup lang="ts">
import type { CommentEmojiItem } from '../../constants/commentEmoji'
import CommentEmojiPicker from './CommentEmojiPicker.vue'
import CommentRichEditor from './CommentRichEditor.vue'

/**
 * 通用评论输入组件。
 * 作用：统一承载主评论和楼层回复的输入、提交、取消与登录引导交互，便于多个内容场景复用。
 */
const props = withDefaults(defineProps<{
  modelValue: string
  title?: string
  placeholder?: string
  submitLabel?: string
  cancelLabel?: string
  helperText?: string
  loading?: boolean
  disabled?: boolean
  compact?: boolean
  showCancel?: boolean
  showLoginButton?: boolean
  loginLabel?: string
}>(), {
  title: '',
  placeholder: '写点什么吧',
  submitLabel: '提交',
  cancelLabel: '取消',
  helperText: '',
  loading: false,
  disabled: false,
  compact: false,
  showCancel: false,
  showLoginButton: false,
  loginLabel: '去登录'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
  cancel: []
  login: []
}>()

const richEditorRef = ref<InstanceType<typeof CommentRichEditor> | null>(null)

/**
 * 同步输入内容。
 * 作用：将内部输入变化透传给父组件，由父级统一持有评论草稿状态。
 *
 * @param value 当前输入值
 */
function handleValueChange(value: string | number) {
  emit('update:modelValue', String(value ?? ''))
}

/**
 * 触发提交动作。
 * 作用：将按钮点击统一转交给父组件处理真实发布逻辑。
 */
function handleSubmit() {
  emit('submit')
}

/**
 * 触发取消动作。
 * 作用：允许父组件关闭当前回复框并清理草稿状态。
 */
function handleCancel() {
  emit('cancel')
}

/**
 * 触发登录动作。
 * 作用：在未登录场景下由父组件决定跳转登录页或打开其他登录流程。
 */
function handleLogin() {
  emit('login')
}

/**
 * 处理表情插入。
 * 作用：调用富表情编辑器在当前光标位置插入表情 token，并同步原始编码。
 *
 * @param emoji 当前选中的表情项
 */
function handleEmojiSelect(emoji: CommentEmojiItem) {
  richEditorRef.value?.insertEmoji(emoji)
}
</script>

<template>
  <div
    class="rounded-[20px] border border-slate-200/60 bg-white/58 p-4 dark:border-white/8 dark:bg-slate-950/34 sm:p-5"
  >
    <div v-if="props.title" class="mb-3">
      <p
        class="font-medium text-slate-900 dark:text-slate-50"
        :class="props.compact ? 'text-sm' : 'text-base'"
      >
        {{ props.title }}
      </p>
    </div>

    <div>
      <CommentRichEditor
        ref="richEditorRef"
        :model-value="props.modelValue"
        :placeholder="props.placeholder"
        :disabled="props.disabled"
        :compact="props.compact"
        @update:model-value="handleValueChange"
      />
    </div>

    <div
      class="mt-4 flex flex-col gap-3 border-t border-slate-200/60 pt-3 dark:border-white/8 sm:flex-row sm:items-center sm:justify-between"
    >
      <div class="flex min-h-9 flex-wrap items-center gap-2 text-slate-400 dark:text-slate-500">
        <CommentEmojiPicker
          :disabled="props.disabled"
          @select="handleEmojiSelect"
        />

        <slot name="toolbar" />

        <p
          v-if="props.helperText"
          class="text-xs leading-6 text-slate-500 dark:text-slate-400"
        >
          {{ props.helperText }}
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <button
          v-if="props.showCancel"
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-full px-4 text-sm font-medium text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
          @click="handleCancel"
        >
          {{ props.cancelLabel }}
        </button>

        <button
          v-if="props.showLoginButton"
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-full px-4 text-sm font-medium text-slate-500 transition hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
          @click="handleLogin"
        >
          {{ props.loginLabel }}
        </button>

        <button
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-full bg-sky-500 px-5 text-sm font-medium text-white transition hover:bg-sky-600 disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500 dark:disabled:bg-slate-800 dark:disabled:text-slate-500"
          :disabled="props.disabled || props.loading"
          @click="handleSubmit"
        >
          {{ props.loading ? '提交中...' : props.submitLabel }}
        </button>
      </div>
    </div>
  </div>
</template>
