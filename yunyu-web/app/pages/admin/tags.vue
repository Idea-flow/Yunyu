<script setup lang="ts">
import type { AdminTaxonomyKind } from '../../types/taxonomy'

/**
 * 后台标签管理页。
 * 作用：为站长提供标签的新增、编辑、删除和列表筛选能力，作为文章标签管理入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const tagPageConfig = {
  kind: 'tag' as AdminTaxonomyKind,
  pageTitle: '标签管理',
  tableTitle: '标签列表',
  operationTitle: '标签操作区',
  operationDescription: '先把后台标签增删改查页面跑通，后续再对接真实标签接口。',
  searchPlaceholder: '搜索标签名称、Slug 或描述',
  itemLabel: '标签',
  descriptionLabel: '标签描述',
  hasCoverField: false,
  hasSortField: false
}
</script>

<template>
  <AdminTaxonomyCrudPage :config="tagPageConfig" />
</template>
