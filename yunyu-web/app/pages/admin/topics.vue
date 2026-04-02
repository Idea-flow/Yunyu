<script setup lang="ts">
import type { AdminTaxonomyKind } from '../../types/taxonomy'

/**
 * 后台专题管理页。
 * 作用：为站长提供专题的新增、编辑、删除和列表筛选能力，作为文章专题管理入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const topicPageConfig = {
  kind: 'topic' as AdminTaxonomyKind,
  pageTitle: '专题管理',
  tableTitle: '专题列表',
  operationTitle: '',
  operationDescription: '',
  searchPlaceholder: '搜索专题名称、Slug 或简介',
  itemLabel: '专题',
  descriptionLabel: '专题简介',
  hasCoverField: true,
  hasSortField: true
}
</script>

<template>
  <AdminTaxonomyCrudPage :config="topicPageConfig" />
</template>
