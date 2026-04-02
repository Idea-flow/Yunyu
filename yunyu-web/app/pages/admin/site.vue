<script setup lang="ts">
import type { AdminSiteConfigForm } from '../../types/admin-site-config'

/**
 * 后台站点设置页。
 * 作用：为站长提供站点基础信息、SEO 配置和主题配置的统一编辑入口，作为站点运营配置工作台。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminSiteConfig = useAdminSiteConfig()

const isLoading = ref(false)
const isSubmitting = ref(false)

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

/**
 * 站点预览卡片数据。
 * 作用：将当前表单内容实时映射成更容易扫描的页面预览摘要，帮助站长保存前做快速确认。
 */
const previewItems = computed(() => [
  {
    label: '站点名称',
    value: formState.siteName || '云屿'
  },
  {
    label: '默认标题',
    value: formState.defaultTitle || formState.siteName || '云屿'
  },
  {
    label: '首页风格',
    value: formState.homeStyle || 'default'
  },
  {
    label: '主色 / 辅助色',
    value: `${formState.primaryColor} / ${formState.secondaryColor}`
  }
])

/**
 * 读取站点配置。
 * 页面进入时调用，用于回填数据库已保存的站点设置。
 */
async function loadSiteConfig() {
  isLoading.value = true

  try {
    const response = await adminSiteConfig.getSiteConfig()
    assignFormState(response)
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
 * 校验表单。
 * 用于在发起保存前给出最基本反馈，避免无效请求进入后端。
 */
function validateForm() {
  if (!formState.siteName.trim()) {
    toast.add({ title: '请输入站点名称', color: 'warning' })
    return false
  }

  if (!formState.siteSubTitle.trim()) {
    toast.add({ title: '请输入站点副标题', color: 'warning' })
    return false
  }

  if (!formState.defaultTitle.trim()) {
    toast.add({ title: '请输入默认标题', color: 'warning' })
    return false
  }

  if (!formState.defaultDescription.trim()) {
    toast.add({ title: '请输入默认描述', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(formState.primaryColor.trim())) {
    toast.add({ title: '主色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(formState.secondaryColor.trim())) {
    toast.add({ title: '辅助色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  return true
}

/**
 * 保存站点配置。
 * 会将当前表单提交到后台接口，并在成功后回填最新数据。
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
            <UButton
              :loading="isLoading"
              icon="i-lucide-refresh-cw"
              label="重新加载"
              color="neutral"
              variant="outline"
              size="sm"
              class="shrink-0 rounded-[10px]"
              @click="loadSiteConfig"
            />
            <AdminPrimaryButton
              :loading="isSubmitting"
              icon="i-lucide-save"
              label="保存配置"
              loading-label="保存中..."
              @click="handleSubmit"
            />
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <section class="sticky top-3 z-20 lg:hidden">
          <div class="admin-surface flex items-center justify-between gap-3 p-3">
            <div class="min-w-0">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">站点设置</p>
              <p class="truncate text-xs text-slate-500 dark:text-slate-400">配置修改后记得及时保存，避免刷新丢失。</p>
            </div>

            <div class="flex shrink-0 items-center gap-2">
              <UButton
                :loading="isLoading"
                icon="i-lucide-refresh-cw"
                color="neutral"
                variant="outline"
                size="sm"
                class="rounded-[10px]"
                @click="loadSiteConfig"
              />
              <AdminPrimaryButton
                :loading="isSubmitting"
                icon="i-lucide-save"
                label="保存配置"
                loading-label="保存中..."
                @click="handleSubmit"
              />
            </div>
          </div>
        </section>

        <section class="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <div class="space-y-4">
            <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Site Configuration Workspace</p>
            <h1 class="max-w-2xl text-3xl font-semibold tracking-tight text-slate-900 lg:text-[2.15rem] dark:text-slate-50">
              把站点品牌、SEO 与首页风格放进同一块后台配置面板。
            </h1>
            <p class="max-w-2xl text-sm leading-8 text-slate-600 dark:text-slate-300">
              这里保存后的配置会直接影响前台首页标题、描述、页脚以及主题色。首次进入没有数据库数据时，系统会自动用默认值回填，不需要手动补初始化 SQL。
            </p>
          </div>

          <section class="admin-surface-soft p-5">
            <div class="space-y-4">
              <div class="flex items-center gap-3">
                <div class="flex size-11 items-center justify-center rounded-[10px] text-sm font-semibold text-white shadow-[0_14px_26px_-22px_rgba(14,165,233,0.55)]" :style="{ background: `linear-gradient(135deg, ${formState.primaryColor}, ${formState.secondaryColor})` }">
                  Y
                </div>
                <div>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">{{ formState.siteName || '云屿' }}</p>
                  <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ formState.siteSubTitle || '在二次元场景与情绪里漫游的内容站' }}</p>
                </div>
              </div>

              <div class="grid gap-3 sm:grid-cols-2">
                <div
                  v-for="item in previewItems"
                  :key="item.label"
                  class="admin-surface p-4"
                >
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">{{ item.label }}</p>
                  <p class="mt-2 text-sm font-medium text-slate-900 dark:text-slate-50">{{ item.value }}</p>
                </div>
              </div>
            </div>
          </section>
        </section>

        <div class="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
          <section class="admin-surface p-5">
            <div>
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Brand Layer</p>
              <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">基础信息</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">维护站点名称、品牌文案、页脚和图标资源。</p>
            </div>

            <div class="grid gap-5 md:grid-cols-2">
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">站点名称</p>
                <AdminInput v-model="formState.siteName" placeholder="例如：云屿 Yunyu" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">页脚文案</p>
                <AdminInput v-model="formState.footerText" placeholder="例如：云屿 Yunyu" />
              </div>
              <div class="space-y-2 md:col-span-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">站点副标题</p>
                <AdminTextarea v-model="formState.siteSubTitle" :rows="3" placeholder="输入前台首页首屏展示的站点副标题" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Logo 地址</p>
                <AdminInput v-model="formState.logoUrl" placeholder="https://example.com/logo.png" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Favicon 地址</p>
                <AdminInput v-model="formState.faviconUrl" placeholder="https://example.com/favicon.ico" />
              </div>
            </div>
          </section>

          <section class="admin-surface p-5">
            <div>
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">SEO Layer</p>
              <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">SEO 配置</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">维护默认标题、描述和分享图，供首页与详情页兜底使用。</p>
            </div>

            <div class="mt-5 space-y-5">
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认标题</p>
                <AdminInput v-model="formState.defaultTitle" placeholder="例如：云屿 Yunyu" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认描述</p>
                <AdminTextarea v-model="formState.defaultDescription" :rows="4" placeholder="输入站点默认 SEO 描述" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认分享图地址</p>
                <AdminInput v-model="formState.defaultShareImage" placeholder="https://example.com/share-cover.png" />
              </div>
            </div>
          </section>
        </div>

        <section class="admin-surface p-5">
          <div>
            <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Theme Layer</p>
            <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">主题与首页风格</p>
            <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">配置前台色彩基调与首页展示风格，便于后续多模板切换。</p>
          </div>

          <div class="grid gap-5 md:grid-cols-3">
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">主色</p>
              <div class="flex items-center gap-3">
                <input v-model="formState.primaryColor" type="color" class="h-12 w-16 cursor-pointer rounded-[10px] border border-slate-200 bg-white p-1 dark:border-slate-700 dark:bg-slate-950" />
                <AdminInput v-model="formState.primaryColor" placeholder="#38BDF8" />
              </div>
            </div>
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">辅助色</p>
              <div class="flex items-center gap-3">
                <input v-model="formState.secondaryColor" type="color" class="h-12 w-16 cursor-pointer rounded-[10px] border border-slate-200 bg-white p-1 dark:border-slate-700 dark:bg-slate-950" />
                <AdminInput v-model="formState.secondaryColor" placeholder="#FB923C" />
              </div>
            </div>
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">首页风格</p>
              <AdminSelect v-model="formState.homeStyle" :items="homeStyleOptions" placeholder="选择首页风格" />
            </div>
          </div>
        </section>
      </div>
    </template>
  </UDashboardPanel>
</template>
