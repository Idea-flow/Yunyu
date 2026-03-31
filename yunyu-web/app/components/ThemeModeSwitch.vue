<script setup lang="ts">
/**
 * 主题切换组件。
 * 负责在明亮、暗黑、系统三种模式之间切换，
 * 作为前后台统一主题机制的基础交互入口。
 */
const colorMode = useColorMode()

/**
 * 设置主题偏好。
 * 统一通过 color-mode 管理主题状态，避免组件各自维护主题逻辑。
 */
function setTheme(preference: 'light' | 'dark' | 'system') {
  colorMode.preference = preference
}

/**
 * 判断当前按钮是否为激活状态。
 * 用于在界面上直观表达用户当前选择的主题模式。
 */
function isActive(preference: 'light' | 'dark' | 'system') {
  return colorMode.preference === preference
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-3">
    <UButton
      :variant="isActive('light') ? 'solid' : 'outline'"
      color="primary"
      @click="setTheme('light')"
    >
      明亮
    </UButton>
    <UButton
      :variant="isActive('dark') ? 'solid' : 'outline'"
      color="primary"
      @click="setTheme('dark')"
    >
      暗黑
    </UButton>
    <UButton
      :variant="isActive('system') ? 'solid' : 'outline'"
      color="neutral"
      @click="setTheme('system')"
    >
      跟随系统
    </UButton>
  </div>
</template>
