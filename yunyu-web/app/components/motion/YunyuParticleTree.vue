<script setup lang="ts">
import YunyuParticleTreeDark from '~/components/motion/YunyuParticleTreeDark.vue'
import YunyuParticleTreeLight from '~/components/motion/YunyuParticleTreeLight.vue'

/**
 * 云屿首页氛围组件包装层。
 * 作用：根据当前明暗主题切换不同的首页装饰实现，
 * 让暗黑模式保留树影，明亮模式改为轻量云朵，并在切换主题时强制重新渲染。
 */
interface YunyuParticleTreeProps {
  treeAreaRatio?: number
  maxTreeWidth?: number
  minTreeWidth?: number
  anchorOffsetX?: number
}

const colorMode = useColorMode()
const props = withDefaults(defineProps<YunyuParticleTreeProps>(), {
  treeAreaRatio: 0.42,
  maxTreeWidth: 760,
  minTreeWidth: 320,
  anchorOffsetX: 0
})

/**
 * 计算当前应该渲染的氛围组件。
 * 作用：将明亮模式和暗黑模式的视觉语言完全拆开，避免一个组件内兼顾两套样式。
 *
 * @returns 当前主题对应的装饰组件
 */
const currentAtmosphereComponent = computed(() => {
  return colorMode.value === 'dark' ? YunyuParticleTreeDark : YunyuParticleTreeLight
})

/**
 * 生成当前主题对应的组件 key。
 * 作用：在切换明暗主题时强制重新挂载组件，确保对应动画重新初始化。
 *
 * @returns 当前主题对应的组件 key
 */
const currentAtmosphereKey = computed(() => {
  return colorMode.value === 'dark' ? 'yunyu-particle-tree-dark' : 'yunyu-particle-tree-light'
})
</script>

<template>
  <component
    :is="currentAtmosphereComponent"
    :key="currentAtmosphereKey"
    v-bind="props"
  />
</template>
