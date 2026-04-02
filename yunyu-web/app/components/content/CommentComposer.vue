<script setup lang="ts">
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
</script>

<template>
  <div
    class="rounded-[26px] border shadow-[0_20px_54px_-42px_rgba(15,23,42,0.18)]"
    :class="props.compact
      ? 'border-slate-200/75 bg-slate-50/90 p-4 dark:border-slate-800 dark:bg-slate-950/64'
      : 'border-slate-200/80 bg-[linear-gradient(180deg,rgba(248,250,252,0.92),rgba(255,255,255,0.88))] p-4 dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.74),rgba(2,6,23,0.82))] sm:p-5'"
  >
    <div v-if="props.title" class="mb-3">
      <p
        class="font-medium text-slate-900 dark:text-slate-50"
        :class="props.compact ? 'text-sm' : 'text-base'"
      >
        {{ props.title }}
      </p>
    </div>

    <UTextarea
      :model-value="props.modelValue"
      :rows="props.compact ? 4 : 5"
      :placeholder="props.placeholder"
      :disabled="props.disabled"
      class="w-full"
      :ui="{
        base: props.compact
          ? 'w-full rounded-[20px] border border-slate-200/85 bg-white/95 px-4 py-3 text-sm leading-7 text-slate-700 shadow-none focus:border-sky-300 focus:ring-2 focus:ring-sky-200/70 dark:border-slate-700 dark:bg-slate-950/88 dark:text-slate-100 dark:focus:border-sky-600 dark:focus:ring-sky-500/20'
          : 'w-full rounded-[24px] border border-slate-200/85 bg-white/95 px-4 py-3 text-sm leading-7 text-slate-700 shadow-none focus:border-sky-300 focus:ring-2 focus:ring-sky-200/70 dark:border-slate-700 dark:bg-slate-950/88 dark:text-slate-100 dark:focus:border-sky-600 dark:focus:ring-sky-500/20'
      }"
      @update:model-value="handleValueChange"
    />

    <div
      class="mt-4 flex flex-col gap-3"
      :class="props.compact ? 'sm:flex-row sm:items-center sm:justify-between' : 'sm:flex-row sm:items-center sm:justify-between'"
    >
      <p
        v-if="props.helperText"
        class="leading-6 text-slate-500 dark:text-slate-400"
        :class="props.compact ? 'text-xs' : 'text-xs'"
      >
        {{ props.helperText }}
      </p>
      <div v-else />

      <div class="flex flex-wrap items-center gap-2">
        <UButton
          v-if="props.showCancel"
          color="neutral"
          variant="ghost"
          class="rounded-full"
          @click="handleCancel"
        >
          {{ props.cancelLabel }}
        </UButton>

        <UButton
          v-if="props.showLoginButton"
          color="neutral"
          variant="ghost"
          class="rounded-full"
          @click="handleLogin"
        >
          {{ props.loginLabel }}
        </UButton>

        <UButton
          color="primary"
          class="rounded-full px-5"
          :loading="props.loading"
          :disabled="props.disabled"
          @click="handleSubmit"
        >
          {{ props.submitLabel }}
        </UButton>
      </div>
    </div>
  </div>
</template>
