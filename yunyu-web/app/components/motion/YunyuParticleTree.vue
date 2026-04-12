<script setup lang="ts">
import YunyuParticleTreeDark from '~/components/motion/YunyuParticleTreeDark.vue'

/**
 * 云屿首页氛围组件包装层。
 * 作用：根据当前明暗主题切换不同的首页装饰实现，
 * 让暗黑模式保留树影，明亮模式不再额外渲染装饰层，并在切换主题时强制重新渲染。
 */
interface YunyuParticleTreeProps {
  treeAreaRatio?: number
  maxTreeWidth?: number
  minTreeWidth?: number
  anchorOffsetX?: number
}

const colorMode = useColorMode()
const renderVersion = ref(0)
const props = withDefaults(defineProps<YunyuParticleTreeProps>(), {
  treeAreaRatio: 0.42,
  maxTreeWidth: 760,
  minTreeWidth: 320,
  anchorOffsetX: 0
})

/**
 * 计算当前应该渲染的氛围组件。
 * 作用：仅在暗黑模式下渲染树影装饰，明亮模式直接返回空，避免浅色云层在亮底上失去存在感。
 *
 * @returns 当前主题对应的装饰组件
 */
const currentAtmosphereComponent = computed(() => {
  return colorMode.value === 'dark' ? YunyuParticleTreeDark : null
})

/**
 * 生成当前主题对应的组件 key。
 * 作用：在切换明暗主题时强制重新挂载组件，确保对应动画重新初始化。
 *
 * @returns 当前主题对应的组件 key
 */
const currentAtmosphereKey = computed(() => {
  return `${colorMode.value === 'dark' ? 'yunyu-particle-tree-dark' : 'yunyu-particle-tree-empty'}-${renderVersion.value}`
})

/**
 * 监听主题实际值与用户偏好变化。
 * 作用：在明亮、暗黑、系统三种模式切换时强制增加版本号，
 * 确保首页氛围组件一定会被重新挂载，而不是只复用旧实例。
 */
watch(
  () => [colorMode.value, colorMode.preference],
  () => {
    renderVersion.value += 1
  }
)
</script>

<template>
  <component
    v-if="currentAtmosphereComponent"
    :is="currentAtmosphereComponent"
    :key="currentAtmosphereKey"
    v-bind="props"
  />
</template>
