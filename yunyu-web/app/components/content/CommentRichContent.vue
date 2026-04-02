<script setup lang="ts">
import { parseCommentRichContent } from '../../utils/commentEmoji'

/**
 * 评论富内容渲染组件。
 * 作用：将评论正文中的自定义表情编码解析为图片，确保前后台评论展示保持一致。
 */
const props = withDefaults(defineProps<{
  content: string
  emojiSize?: 'sm' | 'md'
}>(), {
  emojiSize: 'md'
})

/**
 * 解析评论内容片段。
 * 作用：将评论文本拆成普通文字和表情资源片段，供模板逐段安全渲染。
 */
const segments = computed(() => parseCommentRichContent(props.content))
</script>

<template>
  <div class="whitespace-pre-wrap break-words text-inherit">
    <template v-for="(segment, index) in segments" :key="`${segment.type}-${index}`">
      <span
        v-if="segment.type === 'text'"
        class="whitespace-pre-wrap"
      >
        {{ segment.value }}
      </span>

      <img
        v-else
        :src="segment.value.src"
        :alt="segment.value.name"
        :title="segment.value.name"
        class="mx-0.5 inline-block object-contain align-[-0.3em]"
        :class="props.emojiSize === 'sm' ? 'size-5' : 'size-6'"
      >
    </template>
  </div>
</template>

