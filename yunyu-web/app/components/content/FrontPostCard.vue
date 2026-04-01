<script setup lang="ts">
import type { SitePostSummary } from '../../types/site'

/**
 * 前台文章卡片组件。
 * 作用：统一承接前台列表页、标签页、分类页、专题页和首页内容卡片的展示结构与跳转交互。
 */
const props = withDefaults(defineProps<{
  post: SitePostSummary
  layout?: 'row' | 'stack'
  showCategory?: boolean
  showTopics?: boolean
  showTags?: boolean
  topicLimit?: number
  tagLimit?: number
  imageHeightClass?: string
  titleClass?: string
  summaryClass?: string
  rootClass?: string
}>(), {
  layout: 'row',
  showCategory: true,
  showTopics: true,
  showTags: false,
  topicLimit: 2,
  tagLimit: 2,
  imageHeightClass: '',
  titleClass: '',
  summaryClass: '',
  rootClass: ''
})

/**
 * 计算卡片根容器样式。
 * 作用：根据卡片布局模式统一生成横向列表和纵向推荐卡片的基础容器结构。
 */
const rootClassName = computed(() => {
  const baseClass = [
    'group overflow-hidden rounded-[28px] border border-white/60 bg-white/82 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)]',
    'transition duration-300 hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900'
  ].join(' ')

  if (props.layout === 'stack') {
    return [baseClass, 'hover:-translate-y-0.5', props.rootClass].filter(Boolean).join(' ')
  }

  return [baseClass, 'grid gap-4 p-4 sm:grid-cols-[240px_minmax(0,1fr)]', props.rootClass].filter(Boolean).join(' ')
})

/**
 * 计算图片跳转区域样式。
 * 作用：让图片区在不同布局下都能独立承载文章主跳转，同时避免和内部标签链接产生嵌套链接问题。
 */
const imageLinkClassName = computed(() => {
  return props.layout === 'stack' ? 'block overflow-hidden' : 'block'
})

/**
 * 计算正文容器样式。
 * 作用：统一卡片文字区的内边距和排版承载区域。
 */
const bodyClassName = computed(() => {
  return props.layout === 'stack' ? 'p-5' : 'min-w-0 py-1'
})

/**
 * 计算正文主跳转区域样式。
 * 作用：让标题、摘要和元信息保持整块可点，同时不与专题/标签链接互相嵌套。
 */
const contentLinkClassName = computed(() => {
  return 'block'
})

/**
 * 计算封面图样式。
 * 作用：保证不同布局下的图片高度和圆角表现统一可控。
 */
const imageClassName = computed(() => {
  if (props.layout === 'stack') {
    return ['w-full object-cover transition duration-500 group-hover:scale-[1.04]', props.imageHeightClass || 'h-56'].join(' ')
  }

  return ['w-full rounded-[22px] object-cover sm:h-full', props.imageHeightClass || 'h-52'].join(' ')
})

/**
 * 计算标题样式。
 * 作用：让横向卡片和纵向卡片在标题层级上保持对应节奏。
 */
const titleClassName = computed(() => {
  const defaultClass = props.layout === 'stack'
    ? 'mt-4 text-lg font-semibold leading-8'
    : 'mt-4 text-2xl font-semibold leading-9'

  return [defaultClass, props.titleClass].filter(Boolean).join(' ')
})

/**
 * 计算摘要样式。
 * 作用：统一卡片摘要的行数裁切和阅读节奏。
 */
const summaryClassName = computed(() => {
  const defaultClass = props.layout === 'stack'
    ? 'mt-3 line-clamp-3 text-sm leading-7 text-slate-600 dark:text-slate-300'
    : 'mt-3 line-clamp-3 text-sm leading-7 text-slate-600 dark:text-slate-300'

  return [defaultClass, props.summaryClass].filter(Boolean).join(' ')
})

/**
 * 返回专题跳转地址。
 * 作用：统一生成专题详情页链接。
 *
 * @param slug 专题 slug
 * @returns 专题详情页地址
 */
function getTopicLink(slug: string) {
  return `/topics/${slug}`
}

/**
 * 返回标签跳转地址。
 * 作用：统一生成标签详情页链接。
 *
 * @param slug 标签 slug
 * @returns 标签详情页地址
 */
function getTagLink(slug: string) {
  return `/tags/${slug}`
}
</script>

<template>
  <article :class="rootClassName">
    <NuxtLink :to="`/posts/${post.slug}`" :class="imageLinkClassName">
      <div v-if="layout === 'stack'" class="overflow-hidden">
        <img :src="post.coverUrl" :alt="post.title" :class="imageClassName">
      </div>

      <img
        v-else
        :src="post.coverUrl"
        :alt="post.title"
        :class="imageClassName"
      >
    </NuxtLink>

    <div :class="bodyClassName">
      <div class="flex flex-wrap gap-2">
        <UBadge v-if="showCategory" color="neutral" variant="soft">{{ post.categoryName }}</UBadge>
        <NuxtLink
          v-for="topic in showTopics ? post.topicItems.slice(0, topicLimit) : []"
          :key="`${post.slug}-${topic.slug}`"
          :to="getTopicLink(topic.slug)"
          class="relative z-10"
          @click.stop
        >
          <UBadge color="primary" variant="soft">
            {{ topic.name }}
          </UBadge>
        </NuxtLink>
        <NuxtLink
          v-for="tag in showTags ? post.tagItems.slice(0, tagLimit) : []"
          :key="`${post.slug}-${tag.slug}`"
          :to="getTagLink(tag.slug)"
          class="relative z-10 rounded-full border border-slate-200/80 px-2.5 py-1 text-xs text-slate-500 transition hover:border-sky-200 hover:text-sky-700 dark:border-slate-700 dark:text-slate-400 dark:hover:border-sky-800 dark:hover:text-sky-200"
          @click.stop
        >
          #{{ tag.name }}
        </NuxtLink>
      </div>

      <NuxtLink :to="`/posts/${post.slug}`" :class="contentLinkClassName">
        <h3 :class="titleClassName">{{ post.title }}</h3>
        <p :class="summaryClassName">
          {{ post.summary }}
        </p>

        <div
          :class="layout === 'stack'
            ? 'mt-5 flex items-center justify-between text-xs text-slate-500 dark:text-slate-400'
            : 'mt-5 flex flex-wrap gap-x-5 gap-y-2 text-xs text-slate-500 dark:text-slate-400'"
        >
          <span>{{ post.authorName }}</span>
          <span>{{ post.publishedAt }}</span>
          <span>{{ post.readingMinutes }} 分钟阅读</span>
          <span v-if="layout === 'row'">{{ post.viewCount }} 浏览</span>
        </div>
      </NuxtLink>
    </div>
  </article>
</template>
