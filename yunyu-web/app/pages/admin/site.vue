<script setup lang="ts">
import type { AdminSiteConfigForm } from '../../types/admin-site-config'

/**
 * 后台站点设置页。
 * 作用：提供简洁的站点配置编辑入口，通过横向标签分组承载基础信息、SEO 与视觉风格配置。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

/**
 * 站点设置标签项类型。
 * 作用：统一描述配置页标签栏的分组键值和显示信息。
 */
interface SiteConfigTabItem {
  key: 'basic' | 'seo' | 'theme'
  label: string
  icon: string
}

const toast = useToast()
const adminSiteConfig = useAdminSiteConfig()

const isLoading = ref(false)
const isSubmitting = ref(false)
const activeTab = ref<SiteConfigTabItem['key']>('basic')
const lastSavedSnapshot = ref('')

/**
 * 站点设置表单状态。
 * 作用：承载当前页面全部站点配置字段，并与后台接口保持一致。
 */
const formState = reactive<AdminSiteConfigForm>({
  siteName: '',
  siteSubTitle: '',
  footerText: '',
  logoUrl: '',
  faviconUrl: '',
  defaultTitle: '',
  defaultDescription: '',
  defaultShareImage: '',
  primaryColor: '#38BDF8',
  secondaryColor: '#FB923C',
  homeStyle: 'default'
})

const homeStyleOptions = [
  { label: '默认风格', value: 'default' },
  { label: 'Editorial 风格', value: 'editorial' },
  { label: 'Minimal 风格', value: 'minimal' }
] as const

const tabItems: SiteConfigTabItem[] = [
  { key: 'basic', label: '基础信息', icon: 'i-lucide-badge-info' },
  { key: 'seo', label: 'SEO 配置', icon: 'i-lucide-search-check' },
  { key: 'theme', label: '视觉风格', icon: 'i-lucide-palette' }
]

/**
 * 未保存修改状态。
 * 作用：比较当前表单与最近一次保存快照，提示页面是否存在待保存内容。
 */
const hasUnsavedChanges = computed(() => serializeFormState(formState) !== lastSavedSnapshot.value)

/**
 * 读取站点配置。
 * 页面进入时调用，用于回填数据库中已保存的站点设置。
 */
async function loadSiteConfig() {
  isLoading.value = true

  try {
    const response = await adminSiteConfig.getSiteConfig()
    assignFormState(response)
    lastSavedSnapshot.value = serializeFormState(response)
  } catch (error: any) {
    toast.add({
      title: '加载站点配置失败',
      description: error?.message || '暂时无法获取站点设置。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 将接口响应同步到表单。
 *
 * @param data 站点配置数据
 */
function assignFormState(data: AdminSiteConfigForm) {
  formState.siteName = data.siteName || ''
  formState.siteSubTitle = data.siteSubTitle || ''
  formState.footerText = data.footerText || ''
  formState.logoUrl = data.logoUrl || ''
  formState.faviconUrl = data.faviconUrl || ''
  formState.defaultTitle = data.defaultTitle || ''
  formState.defaultDescription = data.defaultDescription || ''
  formState.defaultShareImage = data.defaultShareImage || ''
  formState.primaryColor = data.primaryColor || '#38BDF8'
  formState.secondaryColor = data.secondaryColor || '#FB923C'
  formState.homeStyle = data.homeStyle || 'default'
}

/**
 * 序列化表单状态。
 * 作用：将表单转换为稳定字符串，用于比较未保存修改状态。
 *
 * @param data 站点配置数据
 * @returns 稳定序列化后的字符串
 */
function serializeFormState(data: AdminSiteConfigForm) {
  return JSON.stringify({
    siteName: data.siteName.trim(),
    siteSubTitle: data.siteSubTitle.trim(),
    footerText: data.footerText.trim(),
    logoUrl: data.logoUrl.trim(),
    faviconUrl: data.faviconUrl.trim(),
    defaultTitle: data.defaultTitle.trim(),
    defaultDescription: data.defaultDescription.trim(),
    defaultShareImage: data.defaultShareImage.trim(),
    primaryColor: data.primaryColor.trim(),
    secondaryColor: data.secondaryColor.trim(),
    homeStyle: data.homeStyle.trim()
  })
}

/**
 * 切换配置标签。
 * 作用：在不同站点配置分组之间切换当前编辑视图。
 *
 * @param key 标签键值
 */
function switchTab(key: SiteConfigTabItem['key']) {
  activeTab.value = key
}

/**
 * 校验表单。
 * 作用：在保存前进行必要字段和颜色格式校验。
 */
function validateForm() {
  if (!formState.siteName.trim()) {
    activeTab.value = 'basic'
    toast.add({ title: '请输入站点名称', color: 'warning' })
    return false
  }

  if (!formState.siteSubTitle.trim()) {
    activeTab.value = 'basic'
    toast.add({ title: '请输入站点副标题', color: 'warning' })
    return false
  }

  if (!formState.defaultTitle.trim()) {
    activeTab.value = 'seo'
    toast.add({ title: '请输入默认标题', color: 'warning' })
    return false
  }

  if (!formState.defaultDescription.trim()) {
    activeTab.value = 'seo'
    toast.add({ title: '请输入默认描述', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(formState.primaryColor.trim())) {
    activeTab.value = 'theme'
    toast.add({ title: '主色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(formState.secondaryColor.trim())) {
    activeTab.value = 'theme'
    toast.add({ title: '辅助色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  return true
}

/**
 * 保存站点配置。
 * 作用：将当前表单提交到后台接口，并在成功后回填最新保存结果。
 */
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const response = await adminSiteConfig.updateSiteConfig({
      siteName: formState.siteName.trim(),
      siteSubTitle: formState.siteSubTitle.trim(),
      footerText: formState.footerText.trim(),
      logoUrl: formState.logoUrl.trim(),
      faviconUrl: formState.faviconUrl.trim(),
      defaultTitle: formState.defaultTitle.trim(),
      defaultDescription: formState.defaultDescription.trim(),
      defaultShareImage: formState.defaultShareImage.trim(),
      primaryColor: formState.primaryColor.trim(),
      secondaryColor: formState.secondaryColor.trim(),
      homeStyle: formState.homeStyle.trim()
    })

    assignFormState(response)
    lastSavedSnapshot.value = serializeFormState(response)
    toast.add({
      title: '站点配置已保存',
      color: 'success'
    })
  } catch (error: any) {
    toast.add({
      title: '保存站点配置失败',
      description: error?.message || '站点配置保存未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  await loadSiteConfig()
})
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="站点设置">
        <template #right>
          <div class="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto sm:flex-nowrap sm:gap-3">
            <UBadge
              :color="hasUnsavedChanges ? 'warning' : 'success'"
              variant="soft"
              class="rounded-[8px] px-3 py-1"
            >
              {{ hasUnsavedChanges ? '待保存' : '已保存' }}
            </UBadge>
            <UButton
              :loading="isLoading"
              icon="i-lucide-refresh-cw"
              label="刷新"
              color="neutral"
              variant="outline"
              size="sm"
              class="rounded-[8px]"
              @click="loadSiteConfig"
            />
            <AdminPrimaryButton
              :loading="isSubmitting"
              icon="i-lucide-save"
              label="保存"
              loading-label="保存中..."
              @click="handleSubmit"
            />
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-4 p-4 lg:p-6">
        <section class="admin-surface p-2">
          <div class="overflow-x-auto [scrollbar-width:thin]">
            <div class="flex min-w-max items-center gap-2">
              <button
                v-for="item in tabItems"
                :key="item.key"
                type="button"
                :class="[
                  'flex min-w-[168px] items-center gap-3 rounded-[10px] px-4 py-3 text-left transition duration-200',
                  activeTab === item.key
                    ? 'bg-sky-50 text-slate-900 dark:bg-sky-400/10 dark:text-slate-50'
                    : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-white/5 dark:hover:text-slate-50'
                ]"
                @click="switchTab(item.key)"
              >
                <div class="flex size-8 shrink-0 items-center justify-center rounded-[8px] bg-slate-100 text-slate-700 dark:bg-slate-900 dark:text-slate-200">
                  <UIcon :name="item.icon" class="size-4" />
                </div>
                <span class="whitespace-nowrap text-sm font-medium">{{ item.label }}</span>
              </button>
            </div>
          </div>
        </section>

        <section class="admin-surface p-4 lg:p-5">
          <div v-if="activeTab === 'basic'" class="grid gap-4 md:grid-cols-2">
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">站点名称</p>
              <AdminInput v-model="formState.siteName" placeholder="站点名称" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">页脚文案</p>
              <AdminInput v-model="formState.footerText" placeholder="页脚文案" />
            </div>

            <div class="space-y-2 md:col-span-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">站点副标题</p>
              <AdminTextarea v-model="formState.siteSubTitle" :rows="4" placeholder="站点副标题" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Logo 地址</p>
              <AdminInput v-model="formState.logoUrl" placeholder="Logo 地址" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Favicon 地址</p>
              <AdminInput v-model="formState.faviconUrl" placeholder="Favicon 地址" />
            </div>
          </div>

          <div v-else-if="activeTab === 'seo'" class="grid gap-4">
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认标题</p>
              <AdminInput v-model="formState.defaultTitle" placeholder="默认标题" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认描述</p>
              <AdminTextarea v-model="formState.defaultDescription" :rows="5" placeholder="默认描述" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认分享图地址</p>
              <AdminInput v-model="formState.defaultShareImage" placeholder="默认分享图地址" />
            </div>
          </div>

          <div v-else class="grid gap-4 md:grid-cols-2">
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">主色</p>
              <div class="flex items-center gap-3">
                <input
                  v-model="formState.primaryColor"
                  type="color"
                  class="h-12 w-16 cursor-pointer rounded-[10px] border border-slate-200 bg-white p-1 dark:border-slate-700 dark:bg-slate-950"
                />
                <AdminInput v-model="formState.primaryColor" placeholder="#38BDF8" />
              </div>
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">辅助色</p>
              <div class="flex items-center gap-3">
                <input
                  v-model="formState.secondaryColor"
                  type="color"
                  class="h-12 w-16 cursor-pointer rounded-[10px] border border-slate-200 bg-white p-1 dark:border-slate-700 dark:bg-slate-950"
                />
                <AdminInput v-model="formState.secondaryColor" placeholder="#FB923C" />
              </div>
            </div>

            <div class="space-y-2 md:col-span-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">首页风格</p>
              <AdminSelect v-model="formState.homeStyle" :items="homeStyleOptions" placeholder="首页风格" />
            </div>
          </div>
        </section>
      </div>
    </template>
  </UDashboardPanel>
</template>
