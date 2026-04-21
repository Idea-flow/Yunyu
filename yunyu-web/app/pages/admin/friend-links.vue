<script setup lang="ts">
import type {
  AdminFriendLinkForm,
  AdminFriendLinkItem,
  AdminFriendLinkStatus
} from '../../types/friend-link'
import { FRIEND_LINK_STATUS_LABEL_MAP } from '../../types/friend-link'

/**
 * 后台友链管理页。
 * 作用：为站长提供友链申请审核、手动新增、排序维护和上下架管理的统一工作页面。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

type FriendLinkStatusFilter = 'ALL' | AdminFriendLinkStatus

const toast = useToast()
const adminFriendLinks = useAdminFriendLinks()
const isLoading = ref(false)
const isSubmitting = ref(false)
const isDeleteSubmitting = ref(false)
const statusUpdatingId = ref<number | null>(null)
const searchKeyword = ref('')
const activeStatus = ref<FriendLinkStatusFilter>('ALL')
const editingItemId = ref<number | null>(null)
const isFormModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const deletingItem = ref<AdminFriendLinkItem | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const friendLinks = ref<AdminFriendLinkItem[]>([])
const themeColorPresets = [
  '#7CC6B8',
  '#5FA8FF',
  '#F2A65A',
  '#F4BF75',
  '#9BCB6D',
  '#E88F8F'
]

const formState = reactive<AdminFriendLinkForm>({
  siteName: '',
  siteUrl: '',
  logoUrl: '',
  description: '',
  contactName: '',
  contactEmail: '',
  contactMessage: '',
  themeColor: '#7CC6B8',
  sortOrder: 0,
  status: 'APPROVED'
})

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已下线', value: 'OFFLINE' }
] as const

/**
 * 当前是否处于编辑模式。
 * 作用：统一切换弹窗标题、提交按钮和表单默认值来源。
 */
const isEditing = computed(() => editingItemId.value !== null)

/**
 * 统一友链编辑弹窗标题。
 * 作用：让新增与编辑场景共享同一套标题来源。
 */
const formModalTitle = computed(() => isEditing.value ? '编辑友链' : '新增友链')

/**
 * 统一友链编辑弹窗提交按钮文案。
 * 作用：让主操作按钮在不同模式下保持一致的命名节奏。
 */
const submitButtonLabel = computed(() => isEditing.value ? '保存修改' : '新增友链')

/**
 * 统一弹窗分组容器样式。
 * 作用：让弹窗内的表单分组与后台工作台保持同一视觉语言。
 */
const modalSurfaceClass = 'rounded-[12px] border border-white/60 bg-white/58 p-4 shadow-[0_12px_22px_-22px_rgba(15,23,42,0.14)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]'

/**
 * 拉取友链列表。
 * 作用：根据当前关键词、状态和分页条件请求后台接口。
 */
async function loadFriendLinks() {
  isLoading.value = true

  try {
    const response = await adminFriendLinks.listFriendLinks({
      keyword: searchKeyword.value.trim() || undefined,
      status: activeStatus.value === 'ALL' ? undefined : activeStatus.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })
    friendLinks.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    toast.add({
      title: '加载友链失败',
      description: error?.message || '暂时无法获取友链列表。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 重置友链表单。
 * 作用：在创建新友链或保存完成后恢复默认表单状态。
 */
function resetForm() {
  editingItemId.value = null
  formState.siteName = ''
  formState.siteUrl = ''
  formState.logoUrl = ''
  formState.description = ''
  formState.contactName = ''
  formState.contactEmail = ''
  formState.contactMessage = ''
  formState.themeColor = '#7CC6B8'
  formState.sortOrder = 0
  formState.status = 'APPROVED'
}

/**
 * 打开新增友链弹窗。
 * 作用：清空表单并进入新增模式。
 */
function openCreateModal() {
  resetForm()
  isFormModalOpen.value = true
}

/**
 * 把指定友链加载到编辑表单中。
 *
 * @param item 友链数据
 */
function startEdit(item: AdminFriendLinkItem) {
  editingItemId.value = item.id
  formState.siteName = item.siteName
  formState.siteUrl = item.siteUrl
  formState.logoUrl = item.logoUrl || ''
  formState.description = item.description || ''
  formState.contactName = item.contactName || ''
  formState.contactEmail = item.contactEmail || ''
  formState.contactMessage = item.contactMessage || ''
  formState.themeColor = item.themeColor || '#7CC6B8'
  formState.sortOrder = item.sortOrder ?? 0
  formState.status = item.status
  isFormModalOpen.value = true
}

/**
 * 校验友链表单。
 * 作用：在提交前给出最基本的表单反馈，减少无效请求进入后端。
 */
function validateForm() {
  if (!formState.siteName.trim()) {
    toast.add({ title: '请输入站点名称', color: 'warning' })
    return false
  }

  if (!/^https?:\/\//.test(formState.siteUrl.trim())) {
    toast.add({ title: '站点地址需以 http:// 或 https:// 开头', color: 'warning' })
    return false
  }

  if (formState.contactEmail.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formState.contactEmail.trim())) {
    toast.add({ title: '请输入正确的联系邮箱', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(formState.themeColor.trim())) {
    toast.add({ title: '主题色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  if (formState.sortOrder < 0) {
    toast.add({ title: '排序值不能小于 0', color: 'warning' })
    return false
  }

  return true
}

/**
 * 提交友链表单。
 * 作用：根据当前状态自动区分创建和更新友链动作。
 */
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const payload: AdminFriendLinkForm = {
      siteName: formState.siteName.trim(),
      siteUrl: formState.siteUrl.trim(),
      logoUrl: formState.logoUrl.trim(),
      description: formState.description.trim(),
      contactName: formState.contactName.trim(),
      contactEmail: formState.contactEmail.trim(),
      contactMessage: formState.contactMessage.trim(),
      themeColor: formState.themeColor.trim().toUpperCase(),
      sortOrder: Number(formState.sortOrder || 0),
      status: formState.status
    }

    if (editingItemId.value) {
      await adminFriendLinks.updateFriendLink(editingItemId.value, payload)
      toast.add({ title: '友链已更新', color: 'success' })
    } else {
      await adminFriendLinks.createFriendLink(payload)
      toast.add({ title: '友链已创建', color: 'success' })
    }

    isFormModalOpen.value = false
    resetForm()
    await loadFriendLinks()
  } catch (error: any) {
    toast.add({
      title: isEditing.value ? '更新失败' : '创建失败',
      description: error?.message || '友链保存未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

/**
 * 打开删除确认弹窗。
 *
 * @param item 友链数据
 */
function openDeleteModal(item: AdminFriendLinkItem) {
  deletingItem.value = item
  isDeleteModalOpen.value = true
}

/**
 * 确认删除当前选中的友链。
 * 作用：仅在二次确认后执行真实删除请求。
 */
async function confirmDelete() {
  if (!deletingItem.value) {
    return
  }

  isDeleteSubmitting.value = true

  try {
    await adminFriendLinks.deleteFriendLink(deletingItem.value.id)
    toast.add({ title: '友链已删除', color: 'success' })

    if (editingItemId.value === deletingItem.value.id) {
      resetForm()
      isFormModalOpen.value = false
    }

    isDeleteModalOpen.value = false
    deletingItem.value = null
    await loadFriendLinks()
  } catch (error: any) {
    toast.add({
      title: '删除失败',
      description: error?.message || '当前友链暂时无法删除。',
      color: 'error'
    })
  } finally {
    isDeleteSubmitting.value = false
  }
}

/**
 * 快速更新友链状态。
 * 作用：用于待审核友链的快速通过或拒绝，也可用于已上线友链的下线操作。
 *
 * @param item 友链数据
 * @param nextStatus 目标状态
 */
async function handleQuickStatusChange(item: AdminFriendLinkItem, nextStatus: AdminFriendLinkStatus) {
  if (statusUpdatingId.value !== null) {
    return
  }

  statusUpdatingId.value = item.id

  try {
    await adminFriendLinks.updateFriendLinkStatus(item.id, nextStatus)
    toast.add({
      title: `友链状态已更新为${FRIEND_LINK_STATUS_LABEL_MAP[nextStatus]}`,
      color: 'success'
    })
    await loadFriendLinks()
  } catch (error: any) {
    toast.add({
      title: '状态更新失败',
      description: error?.message || '友链状态暂时无法更新。',
      color: 'error'
    })
  } finally {
    statusUpdatingId.value = null
  }
}

/**
 * 解析状态颜色。
 *
 * @param status 状态值
 * @returns Nuxt UI 颜色名
 */
function resolveStatusColor(status: AdminFriendLinkStatus) {
  if (status === 'APPROVED') {
    return 'success' as const
  }

  if (status === 'PENDING') {
    return 'warning' as const
  }

  if (status === 'REJECTED') {
    return 'error' as const
  }

  return 'neutral' as const
}

/**
 * 读取站点名称首字母。
 * 作用：在没有 Logo 时提供更有辨识度的头像占位。
 *
 * @param siteName 站点名称
 * @returns 首字母占位
 */
function resolveSiteInitial(siteName: string) {
  return siteName.trim().slice(0, 1).toUpperCase() || 'L'
}

/**
 * 提交搜索。
 * 作用：当筛选条件变化后回到第一页并重新拉取列表。
 */
async function handleSearch() {
  currentPage.value = 1
  await loadFriendLinks()
}

/**
 * 重置筛选条件。
 * 作用：恢复友链管理页默认筛选并重新加载第一页数据。
 */
async function handleResetFilters() {
  searchKeyword.value = ''
  activeStatus.value = 'ALL'
  currentPage.value = 1
  await loadFriendLinks()
}

/**
 * 处理分页切换。
 *
 * @param page 新页码
 */
async function handlePageChange(page: number) {
  if (page === currentPage.value) {
    return
  }

  currentPage.value = page
  await loadFriendLinks()
}

/**
 * 处理每页条数切换。
 *
 * @param nextPageSize 新的每页条数
 */
async function handlePageSizeChange(nextPageSize: number) {
  if (nextPageSize === pageSize.value) {
    return
  }

  pageSize.value = nextPageSize
  currentPage.value = 1
  await loadFriendLinks()
}

await loadFriendLinks()
</script>

<template>
  <div class="space-y-4">
    <AdminListPageHeader title="友链管理">
      <template #actions>
        <AdminPrimaryButton label="新增友链" icon="i-lucide-handshake" @click="openCreateModal" />
      </template>
    </AdminListPageHeader>

    <div class="space-y-4">
      <AdminListFilterBar>
        <template #search>
          <AdminInput
            v-model="searchKeyword"
            icon="i-lucide-search"
            class="w-full"
            placeholder="搜索站点名称、地址、联系人或邮箱"
          />
        </template>

        <template #filters>
          <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] lg:w-[11rem] lg:flex-none">
            <AdminSelect
              v-model="activeStatus"
              :items="statusOptions"
              class="w-full"
              placeholder="状态"
            />
          </div>
        </template>

        <template #actions>
          <AdminButton
            label="重置"
            tone="neutral"
            variant="ghost"
            @click="handleResetFilters"
          />
          <AdminPrimaryButton label="搜索" icon="i-lucide-search" @click="handleSearch" />
        </template>
      </AdminListFilterBar>

      <AdminTableCard title="友链列表" :total="total">
        <AdminDataTable
          :is-loading="isLoading"
          :has-data="friendLinks.length > 0"
          min-width="1220px"
          header-class="grid-cols-[3.2rem_minmax(0,1.45fr)_minmax(0,0.95fr)_0.72fr_0.78fr_1fr]"
          empty-title="还没有匹配的友链记录"
        >
          <template #header>
            <p>ID</p>
            <p>站点信息</p>
            <p>联系信息</p>
            <p>状态</p>
            <p>排序/更新时间</p>
            <p class="text-right">操作</p>
          </template>

          <article
            v-for="item in friendLinks"
            :key="item.id"
            class="grid items-center gap-4 px-4 py-3.5 transition duration-200 hover:bg-white/60 dark:hover:bg-white/5"
            :class="'grid-cols-[3.2rem_minmax(0,1.45fr)_minmax(0,0.95fr)_0.72fr_0.78fr_1fr]'"
          >
            <div class="min-w-0">
              <p class="truncate text-sm font-medium text-toned">{{ item.id }}</p>
            </div>

            <div class="min-w-0">
              <div class="flex items-start gap-3">
                <div
                  class="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-[16px] border border-white/65 bg-white/80 text-sm font-semibold shadow-[0_14px_28px_-24px_rgba(15,23,42,0.24)] dark:border-white/10 dark:bg-white/[0.06]"
                  :style="{ color: item.themeColor, backgroundImage: `linear-gradient(135deg,${item.themeColor}20,rgba(255,255,255,0.82))` }"
                >
                  <img
                    v-if="item.logoUrl"
                    :src="item.logoUrl"
                    :alt="item.siteName"
                    class="h-full w-full object-cover"
                  >
                  <span v-else>{{ resolveSiteInitial(item.siteName) }}</span>
                </div>

                <div class="min-w-0 space-y-1">
                  <p class="truncate text-[15px] font-semibold text-highlighted">{{ item.siteName }}</p>
                  <a
                    :href="item.siteUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="truncate text-sm text-[var(--site-primary-color)] hover:underline"
                  >
                    {{ item.siteUrl }}
                  </a>
                  <p class="line-clamp-2 text-sm text-muted">
                    {{ item.description || '暂无站点简介' }}
                  </p>
                </div>
              </div>
            </div>

            <div class="min-w-0 text-sm text-toned">
              <p class="truncate font-medium text-slate-700 dark:text-slate-200">
                {{ item.contactName || '未填写联系人' }}
              </p>
              <p class="mt-1 truncate">{{ item.contactEmail || '未填写邮箱' }}</p>
            </div>

            <div>
              <UBadge :color="resolveStatusColor(item.status)" variant="soft">
                {{ FRIEND_LINK_STATUS_LABEL_MAP[item.status] }}
              </UBadge>
            </div>

            <div class="text-sm text-toned">
              <p>排序 {{ item.sortOrder }}</p>
              <p class="mt-1">{{ item.updatedTime }}</p>
            </div>

            <div class="flex items-center justify-end gap-2">
              <AdminActionIconButton
                v-if="item.status !== 'APPROVED'"
                icon="i-lucide-badge-check"
                label="通过友链"
                :disabled="statusUpdatingId === item.id"
                @click="handleQuickStatusChange(item, 'APPROVED')"
              />
              <AdminActionIconButton
                v-if="item.status !== 'REJECTED'"
                icon="i-lucide-ban"
                label="拒绝友链"
                :disabled="statusUpdatingId === item.id"
                @click="handleQuickStatusChange(item, 'REJECTED')"
              />
              <AdminActionIconButton
                icon="i-lucide-pencil-line"
                label="编辑友链"
                @click="startEdit(item)"
              />
              <AdminActionIconButton
                icon="i-lucide-trash-2"
                label="删除友链"
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
      eyebrow="友链管理"
      :title="formModalTitle"
      icon="i-lucide-handshake"
      width="wide"
    >
      <template #body>
        <form class="space-y-5" @submit.prevent="handleSubmit">
          <div class="flex flex-wrap items-center gap-2">
            <UBadge color="neutral" variant="soft">站点资料</UBadge>
            <UBadge color="neutral" variant="soft">联系信息</UBadge>
            <UBadge color="info" variant="soft">{{ isEditing ? '编辑模式' : '新增模式' }}</UBadge>
          </div>

          <div :class="modalSurfaceClass">
            <div class="mb-4 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">站点信息</p>
              <UBadge color="neutral" variant="soft">基础</UBadge>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <UFormField name="siteName" label="站点名称">
                <AdminInput v-model="formState.siteName" placeholder="请输入站点名称" />
              </UFormField>

              <UFormField name="siteUrl" label="站点地址">
                <AdminInput v-model="formState.siteUrl" placeholder="https://example.com" />
              </UFormField>

              <UFormField name="logoUrl" label="Logo 地址">
                <AdminInput v-model="formState.logoUrl" placeholder="可选，站点 Logo 或头像地址" />
              </UFormField>

              <UFormField name="sortOrder" label="排序值">
                <AdminInput
                  :model-value="formState.sortOrder"
                  type="number"
                  placeholder="0"
                  @update:model-value="formState.sortOrder = Number($event || 0)"
                />
              </UFormField>
            </div>

            <div class="mt-4">
              <UFormField name="description" label="站点简介">
                <AdminTextarea
                  v-model="formState.description"
                  :rows="4"
                  placeholder="一句话描述这个站点适合被怎样的读者记住"
                />
              </UFormField>
            </div>
          </div>

          <div :class="modalSurfaceClass">
            <div class="mb-4 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">联系与审核</p>
              <UBadge color="info" variant="soft">审核状态</UBadge>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <UFormField name="contactName" label="联系人">
                <AdminInput v-model="formState.contactName" placeholder="请输入联系人名称" />
              </UFormField>

              <UFormField name="contactEmail" label="联系邮箱">
                <AdminInput v-model="formState.contactEmail" placeholder="请输入联系邮箱" />
              </UFormField>
            </div>

            <div class="mt-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_14rem]">
              <UFormField name="contactMessage" label="申请留言">
                <AdminTextarea
                  v-model="formState.contactMessage"
                  :rows="4"
                  placeholder="记录对方申请时的留言，或站长内部备注"
                />
              </UFormField>

              <UFormField name="status" label="当前状态">
                <AdminSelect
                  v-model="formState.status"
                  :items="statusOptions.slice(1)"
                />
              </UFormField>
            </div>
          </div>

          <div :class="modalSurfaceClass">
            <div class="mb-4 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">卡片主色</p>
              <UBadge color="warning" variant="soft">展示风格</UBadge>
            </div>

            <AdminColorPicker
              v-model="formState.themeColor"
              label="友链卡片主题色"
              description="用于前台友链卡片的主色点缀与按钮高亮。"
              :presets="themeColorPresets"
            />
          </div>
        </form>
      </template>

      <template #footer>
        <div class="flex w-full justify-end gap-3">
          <AdminButton
            label="取消"
            tone="neutral"
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
      title="确认删除友链"
      :description="deletingItem ? `删除后将无法在后台继续管理“${deletingItem.siteName}”。` : '请确认是否继续删除当前友链。'"
      confirm-label="确认删除"
      :loading="isDeleteSubmitting"
      @confirm="confirmDelete"
    />
  </div>
</template>
