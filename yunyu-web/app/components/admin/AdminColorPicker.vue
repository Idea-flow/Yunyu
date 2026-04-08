<script setup lang="ts">
import { computed } from 'vue'

/**
 * 后台颜色选择组件。
 * 作用：为后台站点视觉风格提供更直观的颜色预览、预设色板和原生取色能力，
 * 让站长无需记忆十六进制值也能快速完成品牌配色调整。
 */
const props = withDefaults(defineProps<{
  modelValue?: string
  label?: string
  description?: string
  placeholder?: string
  presets?: string[]
}>(), {
  modelValue: '',
  label: '',
  description: '',
  placeholder: '#38BDF8',
  presets: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

/**
 * 当前是否为合法十六进制颜色。
 * 作用：保证组件在输入未完成时仍能稳定渲染，并在预览区回退到安全默认色。
 */
const isValidHexColor = computed(() => /^#([A-Fa-f0-9]{6})$/.test(props.modelValue.trim()))

/**
 * 当前预览色值。
 * 作用：统一为色块预览、原生取色器和选中态提供一致的颜色来源。
 */
const previewColor = computed(() => isValidHexColor.value ? props.modelValue.trim() : '#CBD5E1')

/**
 * 规范化颜色输入。
 *
 * @param value 原始颜色值
 */
function updateColor(value: string) {
  emit('update:modelValue', value.toUpperCase())
}

/**
 * 选择预设颜色。
 *
 * @param color 预设颜色值
 */
function selectPreset(color: string) {
  updateColor(color)
}
</script>

<template>
  <section class="rounded-[16px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.95),rgba(248,250,252,0.92))] p-4 shadow-[0_16px_36px_-28px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.74),rgba(2,6,23,0.7))] dark:shadow-[0_20px_42px_-30px_rgba(0,0,0,0.42)]">
    <div class="flex items-start justify-between gap-4">
      <div class="min-w-0">
        <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ label }}</p>
        <p v-if="description" class="mt-1 text-xs leading-5 text-slate-500 dark:text-slate-400">{{ description }}</p>
      </div>

      <div
        class="flex h-14 w-14 shrink-0 items-center justify-center rounded-[16px] border border-white/80 shadow-[0_18px_36px_-22px_rgba(15,23,42,0.28)] dark:border-white/10"
        :style="{ backgroundColor: previewColor }"
      >
        <span class="rounded-full bg-white/78 px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.16em] text-slate-700 shadow-sm dark:bg-slate-950/70 dark:text-slate-100">
          {{ isValidHexColor ? 'OK' : 'HEX' }}
        </span>
      </div>
    </div>

    <div class="mt-4 flex flex-wrap items-center gap-3">
      <label class="group relative inline-flex cursor-pointer items-center gap-3 rounded-[14px] border border-slate-200/85 bg-white/92 px-3.5 py-2.5 text-sm font-medium text-slate-700 shadow-[0_10px_24px_-22px_rgba(15,23,42,0.24)] transition hover:border-slate-300 hover:bg-white dark:border-white/10 dark:bg-white/5 dark:text-slate-200 dark:hover:border-white/16 dark:hover:bg-white/8">
        <span
          class="h-7 w-7 rounded-full border border-white/90 shadow-[0_8px_16px_-12px_rgba(15,23,42,0.35)] dark:border-white/10"
          :style="{ backgroundColor: previewColor }"
        />
        <span>自定义取色</span>
        <input
          :value="previewColor"
          type="color"
          class="absolute inset-0 cursor-pointer opacity-0"
          @input="updateColor(($event.target as HTMLInputElement).value)"
        >
      </label>

      <div class="min-w-[12rem] flex-1">
        <AdminInput
          :model-value="modelValue"
          :placeholder="placeholder"
          @update:model-value="updateColor(String($event || ''))"
        />
      </div>
    </div>

    <div class="mt-4">
      <p class="mb-2 text-xs font-medium uppercase tracking-[0.14em] text-slate-400 dark:text-slate-500">推荐色板</p>
      <div class="grid grid-cols-5 gap-2 sm:grid-cols-6">
        <button
          v-for="color in presets"
          :key="color"
          type="button"
          class="group relative h-11 rounded-[14px] border transition hover:-translate-y-0.5 hover:shadow-[0_14px_24px_-20px_rgba(15,23,42,0.28)]"
          :class="modelValue.trim().toUpperCase() === color.toUpperCase()
            ? 'border-slate-900/14 ring-2 ring-slate-900/8 dark:border-white/20 dark:ring-white/10'
            : 'border-slate-200/80 dark:border-white/10'"
          :style="{ backgroundColor: color }"
          :title="color"
          @click="selectPreset(color)"
        >
          <span
            v-if="modelValue.trim().toUpperCase() === color.toUpperCase()"
            class="absolute inset-x-1.5 bottom-1.5 rounded-full bg-white/82 px-1.5 py-0.5 text-[10px] font-semibold tracking-[0.12em] text-slate-700 shadow-sm dark:bg-slate-950/72 dark:text-slate-100"
          >
            已选
          </span>
        </button>
      </div>
    </div>
  </section>
</template>
