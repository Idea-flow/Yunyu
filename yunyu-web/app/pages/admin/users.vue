<script setup lang="ts">
import type { AdminUserForm, AdminUserItem } from '../../types/user'

/**
 * 后台用户管理页。
 * 作用：为站长提供用户查询、创建、编辑、删除与状态管理的统一工作页面，
 * 作为后台账号体系维护的核心入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminUsers = useAdminUsers()

const isLoading = ref(false)
const isSubmitting = ref(false)
const isDeleteSubmitting = ref(false)
const searchKeyword = ref('')
const activeRole = ref<'' | 'SUPER_ADMIN' | 'USER'>('')
const activeStatus = ref<'' | 'ACTIVE' | 'DISABLED'>('')
const editingUserId = ref<number | null>(null)
const isFormModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const deletingUser = ref<AdminUserItem | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)

const users = ref<AdminUserItem[]>([])

const formState = reactive<AdminUserForm>({
  email: '',
  userName: '',
  avatarUrl: '',
  password: '',
  role: 'USER',
  status: 'ACTIVE'
})

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '站长', value: 'SUPER_ADMIN' },
  { label: '普通用户', value: 'USER' }
] as const

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '正常', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' }
] as const

/**
 * 计算当前表单是否处于编辑状态。
 * 用于切换页面中的主标题、按钮文案和密码提示文案。
 */
const isEditing = computed(() => editingUserId.value !== null)

/**
 * 计算当前筛选后的站长数量。
 * 用于在页面顶部概览区展示权限结构。
 */
const adminCount = computed(() =>
  users.value.filter(user => user.role === 'SUPER_ADMIN').length
)

/**
 * 计算当前筛选后的禁用用户数量。
 * 用于辅助站长快速定位账号状态问题。
 */
const disabledCount = computed(() =>
  users.value.filter(user => user.status === 'DISABLED').length
)

/**
 * 拉取用户列表。
 * 会根据当前搜索关键词、角色和状态筛选条件请求后台接口。
 */
async function loadUsers() {
  isLoading.value = true

  try {
    const response = await adminUsers.listUsers({
      keyword: searchKeyword.value || undefined,
      role: activeRole.value || undefined,
      status: activeStatus.value || undefined,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })
    users.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    toast.add({
      title: '加载用户失败',
      description: error?.message || '暂时无法获取用户列表。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 重置用户表单。
 * 在创建新用户或保存完成后恢复默认表单状态。
 */
function resetForm() {
  editingUserId.value = null
  formState.email = ''
  formState.userName = ''
  formState.avatarUrl = ''
  formState.password = ''
  formState.role = 'USER'
  formState.status = 'ACTIVE'
}

/**
 * 打开新建用户弹窗。
 * 会先清空表单，再展示创建窗口。
 */
function openCreateModal() {
  resetForm()
  isFormModalOpen.value = true
}

/**
 * 将指定用户加载到编辑表单中。
 *
 * @param user 用户数据
 */
function startEdit(user: AdminUserItem) {
  editingUserId.value = user.id
  formState.email = user.email
  formState.userName = user.userName
  formState.avatarUrl = user.avatarUrl || ''
  formState.password = ''
  formState.role = user.role
  formState.status = user.status
  isFormModalOpen.value = true
}

/**
 * 校验用户表单。
 * 用于在提交前给出最基本的表单反馈，避免无效请求进入后端。
 */
function validateForm() {
  if (!formState.email.trim()) {
    toast.add({ title: '请输入邮箱', color: 'warning' })
    return false
  }

  if (!formState.userName.trim()) {
    toast.add({ title: '请输入用户名', color: 'warning' })
    return false
  }

  if (!isEditing.value && !formState.password.trim()) {
    toast.add({ title: '请输入密码', color: 'warning' })
    return false
  }

  if (formState.password && formState.password.trim().length < 6) {
    toast.add({ title: '密码长度至少 6 位', color: 'warning' })
    return false
  }

  return true
}

/**
 * 提交用户表单。
 * 会根据当前状态自动区分创建和更新用户动作。
 */
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const payload: AdminUserForm = {
      email: formState.email.trim(),
      userName: formState.userName.trim(),
      avatarUrl: formState.avatarUrl.trim(),
      password: formState.password.trim(),
      role: formState.role,
      status: formState.status
    }

    if (editingUserId.value) {
      await adminUsers.updateUser(editingUserId.value, payload)
      toast.add({ title: '用户已更新', color: 'success' })
    } else {
      await adminUsers.createUser(payload)
      toast.add({ title: '用户已创建', color: 'success' })
    }

    resetForm()
    isFormModalOpen.value = false
    await loadUsers()
  } catch (error: any) {
    toast.add({
      title: isEditing.value ? '更新失败' : '创建失败',
      description: error?.message || '用户保存未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

/**
 * 删除指定用户。
 * 删除前会进行浏览器确认，避免在后台管理页误删账号。
 *
 * @param user 用户数据
 */
function openDeleteModal(user: AdminUserItem) {
  deletingUser.value = user
  isDeleteModalOpen.value = true
}

/**
 * 确认删除当前选中的用户。
 * 仅在二次确认弹窗确认后执行真实删除请求。
 */
async function confirmDelete() {
  if (!deletingUser.value) {
    return
  }

  isDeleteSubmitting.value = true

  try {
    await adminUsers.deleteUser(deletingUser.value.id)
    toast.add({ title: '用户已删除', color: 'success' })

    if (editingUserId.value === deletingUser.value.id) {
      resetForm()
      isFormModalOpen.value = false
    }

    isDeleteModalOpen.value = false
    deletingUser.value = null
    await loadUsers()
  } catch (error: any) {
    toast.add({
      title: '删除失败',
      description: error?.message || '当前用户暂时无法删除。',
      color: 'error'
    })
  } finally {
    isDeleteSubmitting.value = false
  }
}

/**
 * 解析角色文案。
 *
 * @param role 角色值
 * @returns 角色文案
 */
function resolveRoleLabel(role: AdminUserItem['role']) {
  return role === 'SUPER_ADMIN' ? '站长' : '普通用户'
}

/**
 * 解析角色颜色。
 *
 * @param role 角色值
 * @returns Nuxt UI 颜色名
 */
function resolveRoleColor(role: AdminUserItem['role']) {
  return role === 'SUPER_ADMIN' ? 'primary' as const : 'neutral' as const
}

/**
 * 解析状态文案。
 *
 * @param status 状态值
 * @returns 状态文案
 */
function resolveStatusLabel(status: AdminUserItem['status']) {
  return status === 'ACTIVE' ? '正常' : '禁用'
}

/**
 * 解析状态颜色。
 *
 * @param status 状态值
 * @returns Nuxt UI 颜色名
 */
function resolveStatusColor(status: AdminUserItem['status']) {
  return status === 'ACTIVE' ? 'success' as const : 'warning' as const
}

/**
 * 提交搜索。
 * 当搜索条件变化后，会回到第一页再重新拉取用户列表。
 */
async function handleSearch() {
  currentPage.value = 1
  loadUsers()
}

/**
 * 处理分页切换。
 *
 * @param page 新页码
 */
async function handlePageChange(page: number) {
  currentPage.value = page
  await loadUsers()
}

await loadUsers()
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="用户管理">
        <template #right>
          <div class="flex items-center gap-3">
            <UButton
              icon="i-lucide-user-plus"
              label="新建用户"
              color="primary"
              class="rounded-2xl"
              @click="openCreateModal"
            />
          </div>
        </template>
      </UDashboardNavbar>

      <UDashboardToolbar>
        <template #left>
          <div>
            <p class="text-sm font-medium text-highlighted">账号中心</p>
            <p class="text-xs text-muted">统一维护后台账号、用户状态与角色权限</p>
          </div>
        </template>
      </UDashboardToolbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <div class="grid gap-4 md:grid-cols-3">
          <UCard class="rounded-[28px] border border-default/70 bg-default/95 shadow-sm">
            <div class="space-y-3">
              <p class="text-sm font-medium text-muted">当前用户数</p>
              <p class="text-4xl font-semibold tracking-tight text-highlighted">{{ users.length }}</p>
            </div>
          </UCard>

          <UCard class="rounded-[28px] border border-default/70 bg-default/95 shadow-sm">
            <div class="space-y-3">
              <p class="text-sm font-medium text-muted">站长账号</p>
              <p class="text-4xl font-semibold tracking-tight text-highlighted">{{ adminCount }}</p>
            </div>
          </UCard>

          <UCard class="rounded-[28px] border border-default/70 bg-default/95 shadow-sm">
            <div class="space-y-3">
              <p class="text-sm font-medium text-muted">禁用账号</p>
              <p class="text-4xl font-semibold tracking-tight text-highlighted">{{ disabledCount }}</p>
            </div>
          </UCard>
        </div>

        <div class="grid gap-6 xl:grid-cols-[minmax(0,1.55fr)_24rem]">
          <div class="space-y-6">
            <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
              <div class="flex flex-col gap-4 xl:flex-row xl:items-center">
                <UInput
                  v-model="searchKeyword"
                  icon="i-lucide-search"
                  size="xl"
                  class="w-full"
                  placeholder="搜索邮箱或用户名"
                  :ui="{
                    base: 'min-h-13 rounded-2xl'
                  }"
                />

                <USelect
                  v-model="activeRole"
                  size="xl"
                  :items="roleOptions"
                  class="min-w-40"
                  placeholder="角色"
                />

                <USelect
                  v-model="activeStatus"
                  size="xl"
                  :items="statusOptions"
                  class="min-w-40"
                  placeholder="状态"
                />

                <UButton
                  icon="i-lucide-search"
                  label="搜索"
                  size="xl"
                  color="primary"
                  class="rounded-2xl"
                  @click="handleSearch"
                />
              </div>
            </UCard>

            <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-base font-semibold text-highlighted">操作区域</p>
                  <p class="text-sm text-muted">在这里可以新增账号并管理现有用户</p>
                </div>

                <UButton
                  icon="i-lucide-user-plus"
                  label="增加"
                  color="primary"
                  class="rounded-2xl"
                  @click="openCreateModal"
                />
              </div>
            </UCard>

            <AdminTableCard
              title="用户列表"
              description="列表最右侧提供修改和删除操作"
              :total="total"
            >
              <div v-if="isLoading" class="space-y-3">
                <USkeleton class="h-18 rounded-2xl" />
                <USkeleton class="h-18 rounded-2xl" />
                <USkeleton class="h-18 rounded-2xl" />
              </div>

              <div v-else class="overflow-hidden rounded-[24px] border border-default/70">
                <div class="hidden grid-cols-[minmax(0,1.5fr)_0.75fr_0.7fr_0.9fr_0.9fr] gap-4 border-b border-default/70 bg-muted/40 px-5 py-4 text-xs font-semibold uppercase tracking-[0.14em] text-muted lg:grid">
                  <p>用户</p>
                  <p>角色</p>
                  <p>状态</p>
                  <p>最近登录</p>
                  <p class="text-right">操作</p>
                </div>

                <div class="divide-y divide-default/70">
                  <article
                    v-for="user in users"
                    :key="user.id"
                    class="grid gap-4 px-5 py-5 transition hover:bg-muted/30 lg:grid-cols-[minmax(0,1.5fr)_0.75fr_0.7fr_0.9fr_0.9fr] lg:items-center"
                  >
                    <div class="min-w-0">
                      <p class="truncate text-base font-semibold text-highlighted">{{ user.userName }}</p>
                      <div class="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted">
                        <span>{{ user.email }}</span>
                        <span class="text-border">·</span>
                        <span>{{ user.updatedTime }}</span>
                      </div>
                    </div>

                    <div>
                      <UBadge :color="resolveRoleColor(user.role)" variant="soft">
                        {{ resolveRoleLabel(user.role) }}
                      </UBadge>
                    </div>

                    <div>
                      <UBadge :color="resolveStatusColor(user.status)" variant="soft">
                        {{ resolveStatusLabel(user.status) }}
                      </UBadge>
                    </div>

                    <div class="text-sm text-toned">
                      {{ user.lastLoginAt || '暂无记录' }}
                    </div>

                    <div class="flex items-center justify-start gap-2 lg:justify-end">
                      <UButton
                        icon="i-lucide-pencil-line"
                        color="neutral"
                        variant="ghost"
                        aria-label="编辑用户"
                        @click="startEdit(user)"
                      />
                      <UButton
                        icon="i-lucide-trash-2"
                        color="error"
                        variant="ghost"
                        aria-label="删除用户"
                        @click="openDeleteModal(user)"
                      />
                    </div>
                  </article>

                  <div v-if="!users.length" class="px-6 py-14 text-center">
                    <p class="text-base font-medium text-highlighted">没有找到匹配的用户</p>
                    <p class="mt-2 text-sm text-muted">可以尝试调整搜索关键词或筛选条件。</p>
                  </div>
                </div>
              </div>
              <template #footer>
                <div class="flex items-center justify-end">
                  <UPagination
                    :model-value="currentPage"
                    :total="total"
                    :items-per-page="pageSize"
                    @update:model-value="handlePageChange"
                  />
                </div>
              </template>
            </AdminTableCard>
          </div>
        </div>
      </div>

      <UModal
        v-model:open="isFormModalOpen"
        :title="isEditing ? '修改用户' : '增加用户'"
        :description="isEditing ? '修改用户基础资料、角色与状态。' : '创建新的后台或站内账号。'"
      >
        <template #body>
          <form class="space-y-5" @submit.prevent="handleSubmit">
            <UFormField name="email" label="邮箱">
              <UInput
                v-model="formState.email"
                size="xl"
                class="w-full"
                placeholder="请输入邮箱"
                :ui="{ base: 'w-full rounded-2xl' }"
              />
            </UFormField>

            <UFormField name="userName" label="用户名">
              <UInput
                v-model="formState.userName"
                size="xl"
                class="w-full"
                placeholder="请输入用户名"
                :ui="{ base: 'w-full rounded-2xl' }"
              />
            </UFormField>

            <UFormField name="avatarUrl" label="头像地址">
              <UInput
                v-model="formState.avatarUrl"
                size="xl"
                class="w-full"
                placeholder="可选"
                :ui="{ base: 'w-full rounded-2xl' }"
              />
            </UFormField>

            <UFormField name="password" :label="isEditing ? '重置密码' : '密码'">
              <UInput
                v-model="formState.password"
                type="password"
                size="xl"
                class="w-full"
                :placeholder="isEditing ? '留空则不修改' : '请输入密码'"
                :ui="{ base: 'w-full rounded-2xl' }"
              />
            </UFormField>

            <div class="grid gap-4 md:grid-cols-2">
              <UFormField name="role" label="角色">
                <USelect
                  v-model="formState.role"
                  size="xl"
                  :items="roleOptions.slice(1)"
                  class="w-full"
                />
              </UFormField>

              <UFormField name="status" label="状态">
                <USelect
                  v-model="formState.status"
                  size="xl"
                  :items="statusOptions.slice(1)"
                  class="w-full"
                />
              </UFormField>
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
            <UButton
              :label="isSubmitting ? '保存中...' : isEditing ? '保存修改' : '创建用户'"
              color="primary"
              :loading="isSubmitting"
              @click="handleSubmit"
            />
          </div>
        </template>
      </UModal>

      <AdminConfirmModal
        v-model:open="isDeleteModalOpen"
        title="确认删除用户"
        :description="deletingUser ? `删除后将无法在后台列表中继续管理“${deletingUser.userName}”。` : '请确认是否继续删除当前用户。'"
        confirm-label="确认删除"
        :loading="isDeleteSubmitting"
        @confirm="confirmDelete"
      />
    </template>
  </UDashboardPanel>
</template>
