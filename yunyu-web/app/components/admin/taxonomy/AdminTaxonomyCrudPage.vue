<script setup lang="ts">
import type {
  AdminTaxonomyForm,
  AdminTaxonomyItem,
  AdminTaxonomyKind
} from '../../../types/taxonomy'

/**
 * 后台内容编排页面配置类型。
 * 作用：为分类、标签、专题三个页面提供可复用的页面标题、字段可见性和文案配置。
 */
interface AdminTaxonomyPageConfig {
  kind: AdminTaxonomyKind
  pageTitle: string
  tableTitle: string
  operationTitle: string
  operationDescription?: string
  searchPlaceholder: string
  itemLabel: string
  descriptionLabel: string
  hasCoverField: boolean
  hasSortField: boolean
}

/**
 * 后台内容编排通用 CRUD 页面组件。
 * 作用：统一承载分类、标签、专题的列表、筛选、新增、编辑和删除流程，减少重复页面代码。
 */
const props = defineProps<{
  config: AdminTaxonomyPageConfig
}>()

const toast = useToast()
const adminTaxonomy = useAdminTaxonomy()

type ItemStatusFilter = 'ALL' | 'ACTIVE' | 'DISABLED'

const isLoading = ref(false)
const isSubmitting = ref(false)
const isDeleteSubmitting = ref(false)
const searchKeyword = ref('')
const activeStatus = ref<ItemStatusFilter>('ALL')
const editingItemId = ref<number | null>(null)
const isFormModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const deletingItem = ref<AdminTaxonomyItem | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)

const items = ref<AdminTaxonomyItem[]>([])

const formState = reactive<AdminTaxonomyForm>({
  name: '',
  slug: '',
  description: '',
  coverUrl: '',
  status: 'ACTIVE',
  sortOrder: 0
})

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '正常', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' }
] as const

/**
 * 判断当前表单是否处于编辑状态。
 * 作用：用于切换弹窗标题与主按钮文案。
 */
const isEditing = computed(() => editingItemId.value !== null)

/**
 * 统一内容编排编辑弹窗标题。
 * 作用：让分类、标签、专题在新增与编辑时共享一致的标题生成逻辑。
 */
const formModalTitle = computed(() =>
  isEditing.value ? `修改${props.config.itemLabel}` : `新增${props.config.itemLabel}`
)

/**
 * 统一内容编排编辑弹窗提交按钮文案。
 * 作用：避免不同模块在主按钮命名上出现不一致。
 */
const submitButtonLabel = computed(() =>
  isEditing.value ? '保存修改' : `新增${props.config.itemLabel}`
)

/**
 * 解析当前模块使用的弹窗图标。
 * 作用：让分类、标签、专题的编辑弹窗具备轻量区分，但仍保持同一套容器结构。
 */
const formModalIcon = computed(() => {
  switch (props.config.kind) {
    case 'tag':
      return 'i-lucide-tags'
    case 'topic':
      return 'i-lucide-book-open-text'
    default:
      return 'i-lucide-folders'
  }
})

/**
 * 统一内容编排编辑弹窗分组容器样式。
 * 作用：让弹窗内部表单区和后台其他编辑工作区保持同一套轻悬浮面板节奏。
 */
const modalSurfaceClass = 'rounded-[12px] border border-white/60 bg-white/58 p-4 shadow-[0_12px_22px_-22px_rgba(15,23,42,0.14)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]'

/**
 * 加载当前模块列表。
 * 作用：按当前页面配置和筛选条件刷新列表数据。
 */
async function loadItems() {
  isLoading.value = true

  try {
    const response = await adminTaxonomy.listItems(props.config.kind, {
      keyword: searchKeyword.value || undefined,
      status: activeStatus.value === 'ALL' ? undefined : activeStatus.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })

    items.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    toast.add({
      title: `加载${props.config.itemLabel}失败`,
      description: error?.message || `暂时无法获取${props.config.itemLabel}列表。`,
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 重置表单状态。
 * 作用：在新建和保存完成后恢复默认表单，避免旧数据残留。
 */
function resetForm() {
  editingItemId.value = null
  formState.name = ''
  formState.slug = ''
  formState.description = ''
  formState.coverUrl = ''
  formState.status = 'ACTIVE'
  formState.sortOrder = 0
}

/**
 * 打开新建弹窗。
 * 作用：重置表单并进入创建模式。
 */
function openCreateModal() {
  resetForm()
  isFormModalOpen.value = true
}

/**
 * 打开编辑弹窗。
 * 作用：将当前条目数据加载到表单中进行修改。
 *
 * @param item 当前条目
 */
function startEdit(item: AdminTaxonomyItem) {
  editingItemId.value = item.id
  formState.name = item.name
  formState.slug = item.slug
  formState.description = item.description
  formState.coverUrl = item.coverUrl || ''
  formState.status = item.status
  formState.sortOrder = item.sortOrder
  isFormModalOpen.value = true
}

/**
 * 校验表单。
 * 作用：在提交前给出最低限度的输入约束反馈。
 */
function validateForm() {
  if (!formState.name.trim()) {
    toast.add({ title: `请输入${props.config.itemLabel}名称`, color: 'warning' })
    return false
  }

  if (!formState.slug.trim()) {
    toast.add({ title: `请输入${props.config.itemLabel} Slug`, color: 'warning' })
    return false
  }

  if (formState.sortOrder < 0) {
    toast.add({ title: '排序值不能小于 0', color: 'warning' })
    return false
  }

  return true
}

/**
 * 提交表单。
 * 作用：根据当前模式调用真实接口完成创建或更新，并在成功后刷新列表。
 */
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const payload: AdminTaxonomyForm = {
      name: formState.name.trim(),
      slug: formState.slug.trim(),
      description: formState.description.trim(),
      coverUrl: formState.coverUrl.trim(),
      status: formState.status,
      sortOrder: formState.sortOrder
    }

    if (editingItemId.value) {
      await adminTaxonomy.updateItem(props.config.kind, editingItemId.value, payload)
      toast.add({ title: `${props.config.itemLabel}已更新`, color: 'success' })
    } else {
      await adminTaxonomy.createItem(props.config.kind, payload)
      toast.add({ title: `${props.config.itemLabel}已创建`, color: 'success' })
    }

    resetForm()
    isFormModalOpen.value = false
    await loadItems()
  } catch (error: any) {
    toast.add({
      title: isEditing.value ? '更新失败' : '创建失败',
      description: error?.message || `${props.config.itemLabel}保存未成功，请稍后重试。`,
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

/**
 * 打开删除确认弹窗。
 * 作用：记录待删除条目，避免误删。
 *
 * @param item 当前条目
 */
function openDeleteModal(item: AdminTaxonomyItem) {
  deletingItem.value = item
  isDeleteModalOpen.value = true
}

/**
 * 确认删除当前条目。
 * 作用：调用真实接口删除条目并刷新列表。
 */
async function confirmDelete() {
  if (!deletingItem.value) {
    return
  }

  isDeleteSubmitting.value = true

  try {
    await adminTaxonomy.deleteItem(props.config.kind, deletingItem.value.id)
    toast.add({ title: `${props.config.itemLabel}已删除`, color: 'success' })

    if (items.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }

    deletingItem.value = null
    isDeleteModalOpen.value = false
    await loadItems()
  } catch (error: any) {
    toast.add({
      title: '删除失败',
      description: error?.message || `当前${props.config.itemLabel}暂时无法删除。`,
      color: 'error'
    })
  } finally {
    isDeleteSubmitting.value = false
  }
}

/**
 * 提交搜索。
 * 作用：更新筛选条件后回到第一页并刷新列表。
 */
async function handleSearch() {
  currentPage.value = 1
  await loadItems()
}

/**
 * 重置筛选条件。
 * 作用：将关键词和状态筛选恢复默认值，并重新加载第一页数据。
 */
async function handleResetFilters() {
  searchKeyword.value = ''
  activeStatus.value = 'ALL'
  currentPage.value = 1
  await loadItems()
}

/**
 * 处理分页切换。
 * 作用：按新页码重新查询当前模块列表。
 *
 * @param page 新页码
 */
async function handlePageChange(page: number) {
  if (page === currentPage.value) {
    return
  }

  currentPage.value = page
  await loadItems()
}

/**
 * 处理每页条数切换。
 * 作用：切换分页容量后回到第一页并重新请求列表数据。
 *
 * @param value 新的每页条数
 */
async function handlePageSizeChange(value: string | number | null) {
  const nextPageSize = Number(value)

  if (!Number.isFinite(nextPageSize) || nextPageSize < 1 || nextPageSize === pageSize.value) {
    return
  }

  pageSize.value = nextPageSize
  currentPage.value = 1
  await loadItems()
}

/**
 * 解析状态文案。
 * 作用：统一展示状态中文标签。
 *
 * @param status 状态值
 * @returns 中文状态文案
 */
function resolveStatusLabel(status: AdminTaxonomyItem['status']) {
  return status === 'ACTIVE' ? '正常' : '禁用'
}

/**
 * 解析状态颜色。
 * 作用：为不同状态提供一致的视觉反馈。
 *
 * @param status 状态值
 * @returns Nuxt UI 颜色值
 */
function resolveStatusColor(status: AdminTaxonomyItem['status']) {
  return status === 'ACTIVE' ? 'success' as const : 'warning' as const
}

await loadItems()
</script>

<template>
  <div class="space-y-4">
    <AdminListPageHeader :title="props.config.pageTitle">
      <template #actions>
        <AdminPrimaryButton :label="`新增${props.config.itemLabel}`" icon="i-lucide-plus" @click="openCreateModal" />
      </template>
    </AdminListPageHeader>

    <div class="space-y-4">
      <AdminListFilterBar>
        <template #search>
            <AdminInput
              v-model="searchKeyword"
              icon="i-lucide-search"
              class="w-full"
              :placeholder="props.config.searchPlaceholder"
            />
        </template>

        <template #filters>
            <div class="min-w-[9.5rem] flex-1 sm:max-w-[calc(50%-0.375rem)] lg:w-[10rem] lg:flex-none">
              <AdminSelect
                v-model="activeStatus"
                :items="statusOptions"
                class="w-full"
                placeholder="状态"
              />
            </div>
        </template>

        <template #actions>
          <UButton
            label="重置"
            color="neutral"
            variant="ghost"
            class="cursor-pointer rounded-[10px]"
            @click="handleResetFilters"
          />
          <AdminPrimaryButton label="搜索" icon="i-lucide-search" @click="handleSearch" />
        </template>
      </AdminListFilterBar>

      <AdminTableCard :title="props.config.tableTitle">
        <AdminDataTable
          :is-loading="isLoading"
          :has-data="items.length > 0"
          min-width="980px"
          header-class="grid-cols-[minmax(0,1.45fr)_0.7fr_0.8fr_0.9fr_0.85fr]"
          :empty-title="`没有找到匹配的${props.config.itemLabel}`"
        >
          <template #header>
            <p>{{ props.config.itemLabel }}</p>
            <p>状态</p>
            <p>排序/文章</p>
            <p>更新时间</p>
            <p class="text-right">操作</p>
          </template>

          <article
            v-for="item in items"
            :key="item.id"
            class="grid items-center gap-4 px-4 py-3.5 transition duration-200 hover:bg-white/60 dark:hover:bg-white/5"
            :class="'grid-cols-[minmax(0,1.45fr)_0.7fr_0.8fr_0.9fr_0.85fr]'"
          >
            <div class="min-w-0">
              <div class="flex items-center gap-3">
                <div
                  v-if="props.config.hasCoverField && item.coverUrl"
                  class="size-12 shrink-0 overflow-hidden rounded-[8px] border border-white/60 dark:border-white/10"
                >
                  <img :src="item.coverUrl" :alt="item.name" class="h-full w-full object-cover">
                </div>

                <div class="min-w-0">
                  <p class="truncate text-[15px] font-semibold text-highlighted">{{ item.name }}</p>
                  <div class="mt-1.5 flex flex-wrap items-center gap-2 text-sm text-muted">
                    <span>ID {{ item.id }}</span>
                    <span class="text-border">·</span>
                    <span>{{ item.slug }}</span>
                    <template v-if="item.description">
                      <span class="text-border">·</span>
                      <span>{{ item.description }}</span>
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <div>
              <UBadge :color="resolveStatusColor(item.status)" variant="soft">
                {{ resolveStatusLabel(item.status) }}
              </UBadge>
            </div>

            <div class="space-y-1 text-sm text-toned">
              <p v-if="props.config.hasSortField">排序 {{ item.sortOrder }}</p>
              <p>文章 {{ item.relatedPostCount }} 篇</p>
            </div>

            <div class="text-sm text-toned">
              <p>{{ item.updatedTime }}</p>
            </div>

            <div class="flex items-center justify-end gap-2">
              <AdminActionIconButton
                icon="i-lucide-pencil-line"
                :label="`编辑${props.config.itemLabel}`"
                @click="startEdit(item)"
              />
              <AdminActionIconButton
                icon="i-lucide-trash-2"
                :label="`删除${props.config.itemLabel}`"
                tone="danger"
                @click="openDeleteModal(item)"
              />
            </div>
          </article>
        </AdminDataTable>

        <template #footer>
          <AdminPaginationBar
            :page="currentPage"
            :page-size="pageSize"
            :total="total"
            :total-pages="totalPages"
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </template>
      </AdminTableCard>
    </div>

    <AdminFormModal
      v-model:open="isFormModalOpen"
      :eyebrow="props.config.pageTitle"
      :title="formModalTitle"
      :icon="formModalIcon"
      width="wide"
    >
      <template #body>
        <form class="space-y-5" @submit.prevent="handleSubmit">
          <div class="flex flex-wrap items-center gap-2">
            <UBadge color="neutral" variant="soft">{{ props.config.itemLabel }}名称</UBadge>
            <UBadge color="neutral" variant="soft">Slug</UBadge>
            <UBadge color="info" variant="soft">{{ isEditing ? '编辑模式' : '新增模式' }}</UBadge>
          </div>

          <div :class="modalSurfaceClass">
            <div class="mb-4 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">基础信息</p>
              <UBadge color="neutral" variant="soft">{{ props.config.itemLabel }}</UBadge>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <UFormField name="name" :label="`${props.config.itemLabel}名称`">
                <AdminInput
                  v-model="formState.name"
                  :placeholder="`请输入${props.config.itemLabel}名称`"
                />
              </UFormField>

              <UFormField name="slug" label="Slug">
                <AdminInput
                  v-model="formState.slug"
                  placeholder="请输入唯一标识"
                />
              </UFormField>
            </div>
          </div>

          <div :class="modalSurfaceClass">
            <div class="mb-4 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">内容信息</p>
              <UBadge color="neutral" variant="soft">{{ props.config.descriptionLabel }}</UBadge>
            </div>

            <UFormField name="description" :label="props.config.descriptionLabel">
              <AdminTextarea
                v-model="formState.description"
                :rows="4"
                autoresize
                :placeholder="`请输入${props.config.descriptionLabel}`"
              />
            </UFormField>
          </div>

          <div :class="modalSurfaceClass">
            <div class="mb-4 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">发布设置</p>
              <UBadge color="info" variant="soft">状态</UBadge>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <UFormField v-if="props.config.hasCoverField" name="coverUrl" label="封面地址">
                <AdminInput
                  v-model="formState.coverUrl"
                  placeholder="请输入封面地址"
                />
              </UFormField>

              <UFormField v-if="props.config.hasSortField" name="sortOrder" label="排序值">
                <AdminInput
                  v-model="formState.sortOrder"
                  type="number"
                  placeholder="请输入排序值"
                />
              </UFormField>

              <UFormField name="status" label="状态">
                <AdminSelect
                  v-model="formState.status"
                  :items="statusOptions.slice(1)"
                />
              </UFormField>
            </div>
          </div>
        </form>
      </template>

      <template #footer>
        <div class="flex w-full justify-end gap-3">
          <UButton
            label="取消"
            color="neutral"
            variant="ghost"
            @click="isFormModalOpen = false"
          />
          <AdminPrimaryButton
            :label="submitButtonLabel"
            loading-label="保存中..."
            :loading="isSubmitting"
            :icon="isEditing ? 'i-lucide-save' : 'i-lucide-plus'"
            @click="handleSubmit"
          />
        </div>
      </template>
    </AdminFormModal>

    <AdminConfirmModal
      v-model:open="isDeleteModalOpen"
      :title="`确认删除${props.config.itemLabel}`"
      :description="deletingItem ? `删除后将无法继续在后台管理“${deletingItem.name}”。` : `请确认是否继续删除当前${props.config.itemLabel}。`"
      confirm-label="确认删除"
      :loading="isDeleteSubmitting"
      @confirm="confirmDelete"
    />
  </div>
</template>
