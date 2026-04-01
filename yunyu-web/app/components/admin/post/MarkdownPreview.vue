<script setup lang="ts">
import ArticleContentRenderer from '../../content/ArticleContentRenderer.vue'

/**
 * Markdown 预览组件。
 * 作用：作为后台文章编辑场景的兼容层，
 * 逐步把旧预览入口收口到统一的文章内容渲染组件上。
 */
const props = withDefaults(defineProps<{
  html: string
  isLoading?: boolean
  emptyText?: string
  containerClass?: string
  bodyClass?: string
  contentTheme?: 'editorial' | 'documentation' | 'minimal'
  codeTheme?: 'github-light' | 'github-dark' | 'vitesse-light' | 'vitesse-dark'
  codeDefaultExpanded?: boolean
}>(), {
  isLoading: false,
  emptyText: '这里会显示正文预览，先在左侧输入 Markdown 内容。',
  containerClass: '',
  bodyClass: '',
  contentTheme: 'editorial',
  codeTheme: 'github-light',
  codeDefaultExpanded: false
})
</script>

<template>
  <ArticleContentRenderer
    :html="props.html"
    :is-loading="props.isLoading"
    :empty-text="props.emptyText"
    :container-class="props.containerClass"
    :body-class="props.bodyClass"
    :content-theme="props.contentTheme"
    :code-theme="props.codeTheme"
    :code-default-expanded="props.codeDefaultExpanded"
  />
</template>
