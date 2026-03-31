<script setup lang="ts">
import type { AdminTaxonomyKind } from '../../types/taxonomy'

/**
 * 后台分类管理页。
 * 作用：为站长提供分类的新增、编辑、删除和列表筛选能力，作为文章分类管理入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const categoryPageConfig = {
  kind: 'category' as AdminTaxonomyKind,
  pageTitle: '分类管理',
  tableTitle: '分类列表',
  operationTitle: '分类操作区',
  operationDescription: '先完成后台分类 CRUD 工作台，后续再替换为真实接口联调。',
  searchPlaceholder: '搜索分类名称、Slug 或描述',
  itemLabel: '分类',
  descriptionLabel: '分类描述',
  hasCoverField: true,
  hasSortField: true
}
</script>

<template>
  <AdminTaxonomyCrudPage :config="categoryPageConfig" />
</template>
