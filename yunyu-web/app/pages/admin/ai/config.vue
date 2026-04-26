<script setup lang="ts">
import type {
  AdminAiProviderConfigForm,
  AdminAiProviderProfileForm
} from '../../../types/admin-site-config'

/**
 * 后台 AI 配置页。
 * 作用：集中管理 AI 上游配置，支持多配置维护、单配置启用、连接测试与 API Key 可见性切换。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminSiteConfig = useAdminSiteConfig()

const isLoading = ref(false)
const isSubmitting = ref(false)
const isSavingAiConfig = ref(false)
const isTestingAiConnection = ref(false)
const isActivatingAiProfile = ref(false)
const selectedAiProfileKey = ref('')
const lastSavedAiSnapshot = ref('')
const aiApiKeyVisibleMap = ref<Record<string, boolean>>({})

const aiUpstreamProtocolOptions = [
  { label: 'Chat Completions', value: 'COMPLETIONS' },
  { label: 'Responses', value: 'RESPONSES' }
] as const

/**
 * AI 配置表单状态。
 * 作用：承载后台 AI 多配置集合与当前启用配置键。
 */
const aiFormState = reactive<AdminAiProviderConfigForm>({
  activeProfileKey: '',
  profiles: []
})

/**
 * 当前配置是否存在未保存修改。
 * 作用：通过快照比较给出页面头部提醒。
 */
const hasUnsavedChanges = computed(() => serializeAiFormState(aiFormState) !== lastSavedAiSnapshot.value)

/**
 * 当前正在编辑的 AI 配置项。
 * 作用：统一给右侧详情面板提供已选配置引用。
 */
const currentAiProfile = computed(() =>
  aiFormState.profiles.find(profile => profile.profileKey === selectedAiProfileKey.value) || null
)

/**
 * 创建默认 AI 配置项。
 * 作用：为新增配置和首次进入页面时提供稳定默认值。
 *
 * @param profileKey 配置键
 * @param enabled 是否启用
 * @returns 默认配置项
 */
function createDefaultAiProfile(profileKey: string, enabled = false): AdminAiProviderProfileForm {
  return {
    profileKey,
    name: enabled ? '默认 AI 配置' : '新 AI 配置',
    enabled,
    upstreamBaseUrl: '',
    apiKey: '',
    model: '',
    upstreamProtocol: 'COMPLETIONS',
    connectTimeoutMs: 3000,
    readTimeoutMs: 15000,
    writeTimeoutMs: 15000,
    maxTokens: 800,
    temperature: 0.4
  }
}

/**
 * 将 AI 配置响应同步到表单。
 *
 * @param data AI 配置数据
 */
function assignAiFormState(data: AdminAiProviderConfigForm) {
  const profileList = (data.profiles || []).map(profile => ({
    profileKey: profile.profileKey || '',
    name: profile.name || '',
    enabled: !!profile.enabled,
    upstreamBaseUrl: profile.upstreamBaseUrl || '',
    apiKey: profile.apiKey || '',
    model: profile.model || '',
    upstreamProtocol: profile.upstreamProtocol === 'RESPONSES' ? 'RESPONSES' : 'COMPLETIONS',
    connectTimeoutMs: profile.connectTimeoutMs || 3000,
    readTimeoutMs: profile.readTimeoutMs || 15000,
    writeTimeoutMs: profile.writeTimeoutMs || 15000,
    maxTokens: profile.maxTokens || 800,
    temperature: Number.isFinite(profile.temperature) ? profile.temperature : 0.4
  }))

  aiFormState.activeProfileKey = data.activeProfileKey || ''
  aiFormState.profiles = profileList

  if (!aiFormState.profiles.length) {
    const defaultKey = `ai-${Date.now()}`
    aiFormState.profiles = [createDefaultAiProfile(defaultKey, true)]
    aiFormState.activeProfileKey = defaultKey
  }

  let activeProfile = aiFormState.profiles.find(profile => profile.profileKey === aiFormState.activeProfileKey)
  if (!activeProfile) {
    activeProfile = aiFormState.profiles.find(profile => profile.enabled) || aiFormState.profiles[0]
    aiFormState.activeProfileKey = activeProfile.profileKey
  }

  aiFormState.profiles = aiFormState.profiles.map(profile => ({
    ...profile,
    enabled: profile.profileKey === aiFormState.activeProfileKey
  }))
  selectedAiProfileKey.value = aiFormState.activeProfileKey

  const nextVisibleMap: Record<string, boolean> = {}
  for (const profile of aiFormState.profiles) {
    nextVisibleMap[profile.profileKey] = false
  }
  aiApiKeyVisibleMap.value = nextVisibleMap
}

/**
 * 序列化 AI 配置表单状态。
 * 作用：将表单转换为稳定字符串，用于比较未保存修改。
 *
 * @param data AI 配置数据
 * @returns 稳定序列化字符串
 */
function serializeAiFormState(data: AdminAiProviderConfigForm) {
  return JSON.stringify({
    activeProfileKey: data.activeProfileKey.trim(),
    profiles: data.profiles.map(profile => ({
      profileKey: profile.profileKey.trim(),
      name: profile.name.trim(),
      enabled: profile.enabled,
      upstreamBaseUrl: profile.upstreamBaseUrl.trim(),
      apiKey: profile.apiKey.trim(),
      model: profile.model.trim(),
      upstreamProtocol: profile.upstreamProtocol,
      connectTimeoutMs: Number(profile.connectTimeoutMs),
      readTimeoutMs: Number(profile.readTimeoutMs),
      writeTimeoutMs: Number(profile.writeTimeoutMs),
      maxTokens: Number(profile.maxTokens),
      temperature: Number(profile.temperature)
    }))
  })
}

/**
 * 校验 AI 配置表单。
 * 作用：在保存前确保字段完整且只启用一个配置。
 *
 * @returns 是否通过校验
 */
function validateAiForm() {
  if (!aiFormState.profiles.length) {
    toast.add({ title: '至少保留一个 AI 配置项', color: 'warning' })
    return false
  }

  const enabledProfiles = aiFormState.profiles.filter(profile => profile.enabled)
  if (enabledProfiles.length !== 1) {
    toast.add({ title: 'AI 配置必须且只能启用一个', color: 'warning' })
    return false
  }

  if (aiFormState.activeProfileKey !== enabledProfiles[0].profileKey) {
    aiFormState.activeProfileKey = enabledProfiles[0].profileKey
  }

  for (const profile of aiFormState.profiles) {
    if (!profile.profileKey.trim()) {
      toast.add({ title: 'AI 配置键不能为空', color: 'warning' })
      return false
    }
    if (!profile.name.trim()) {
      toast.add({ title: 'AI 配置名称不能为空', color: 'warning' })
      return false
    }
    if (!profile.upstreamBaseUrl.trim()) {
      toast.add({ title: 'AI 上游地址不能为空', color: 'warning' })
      return false
    }
    if (!profile.apiKey.trim()) {
      toast.add({ title: 'AI API Key 不能为空', color: 'warning' })
      return false
    }
    if (!profile.model.trim()) {
      toast.add({ title: 'AI 模型名称不能为空', color: 'warning' })
      return false
    }
    if (!['COMPLETIONS', 'RESPONSES'].includes(profile.upstreamProtocol)) {
      toast.add({ title: 'AI 协议仅支持 COMPLETIONS 或 RESPONSES', color: 'warning' })
      return false
    }
    if (profile.connectTimeoutMs < 100 || profile.connectTimeoutMs > 120000) {
      toast.add({ title: 'connectTimeoutMs 需在 100-120000 之间', color: 'warning' })
      return false
    }
    if (profile.readTimeoutMs < 100 || profile.readTimeoutMs > 120000) {
      toast.add({ title: 'readTimeoutMs 需在 100-120000 之间', color: 'warning' })
      return false
    }
    if (profile.writeTimeoutMs < 100 || profile.writeTimeoutMs > 120000) {
      toast.add({ title: 'writeTimeoutMs 需在 100-120000 之间', color: 'warning' })
      return false
    }
    if (profile.maxTokens < 1 || profile.maxTokens > 128000) {
      toast.add({ title: 'maxTokens 需在 1-128000 之间', color: 'warning' })
      return false
    }
    if (profile.temperature < 0 || profile.temperature > 2) {
      toast.add({ title: 'temperature 需在 0-2 之间', color: 'warning' })
      return false
    }
  }

  const keys = aiFormState.profiles.map(profile => profile.profileKey.trim())
  if (new Set(keys).size !== keys.length) {
    toast.add({ title: 'AI 配置键不能重复', color: 'warning' })
    return false
  }

  return true
}

/**
 * 新增 AI 配置项。
 * 作用：追加一条可编辑的新配置并自动选中。
 */
function addAiProfile() {
  const profileKey = `ai-${Date.now()}`
  const profile = createDefaultAiProfile(profileKey, false)
  aiFormState.profiles.push(profile)
  selectedAiProfileKey.value = profileKey
  aiApiKeyVisibleMap.value[profileKey] = false
}

/**
 * 切换当前编辑配置。
 *
 * @param profileKey 配置键
 */
function selectAiProfile(profileKey: string) {
  selectedAiProfileKey.value = profileKey
}

/**
 * 判断 API Key 是否明文可见。
 *
 * @param profileKey 配置键
 * @returns 是否可见
 */
function isAiApiKeyVisible(profileKey: string) {
  return !!aiApiKeyVisibleMap.value[profileKey]
}

/**
 * 切换 API Key 可见性。
 * 作用：默认显示星号，按需显示原值。
 *
 * @param profileKey 配置键
 */
function toggleAiApiKeyVisibility(profileKey: string) {
  aiApiKeyVisibleMap.value[profileKey] = !isAiApiKeyVisible(profileKey)
}

/**
 * 将 API Key 转为星号显示。
 *
 * @param apiKey 原始 API Key
 * @returns 星号字符串
 */
function maskAiApiKeyForDisplay(apiKey: string) {
  const normalizedValue = (apiKey || '').trim()
  if (!normalizedValue) {
    return ''
  }
  return '*'.repeat(Math.max(8, normalizedValue.length))
}

/**
 * 设置启用配置。
 * 作用：切换启用项后立即触发保存，保证后端始终单启用。
 *
 * @param profileKey 配置键
 */
async function setActiveAiProfile(profileKey: string) {
  const previousState: AdminAiProviderConfigForm = {
    activeProfileKey: aiFormState.activeProfileKey,
    profiles: aiFormState.profiles.map(profile => ({ ...profile }))
  }

  aiFormState.activeProfileKey = profileKey
  aiFormState.profiles = aiFormState.profiles.map(profile => ({
    ...profile,
    enabled: profile.profileKey === profileKey
  }))
  selectedAiProfileKey.value = profileKey

  if (!validateAiForm()) {
    assignAiFormState(previousState)
    return
  }

  isActivatingAiProfile.value = true

  try {
    await saveAiConfig({ silentSuccessToast: true })
    toast.add({
      title: '启用配置已更新',
      color: 'success'
    })
  } catch (error: any) {
    assignAiFormState(previousState)
    toast.add({
      title: '启用配置失败',
      description: error?.message || '启用配置未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isActivatingAiProfile.value = false
  }
}

/**
 * 删除 AI 配置项。
 *
 * @param profileKey 配置键
 */
function removeAiProfile(profileKey: string) {
  if (aiFormState.profiles.length <= 1) {
    toast.add({ title: '至少保留一个 AI 配置项', color: 'warning' })
    return
  }

  const removingActive = aiFormState.activeProfileKey === profileKey
  aiFormState.profiles = aiFormState.profiles.filter(profile => profile.profileKey !== profileKey)
  delete aiApiKeyVisibleMap.value[profileKey]

  if (removingActive) {
    const nextProfile = aiFormState.profiles[0]
    void setActiveAiProfile(nextProfile.profileKey)
    return
  }

  if (selectedAiProfileKey.value === profileKey) {
    selectedAiProfileKey.value = aiFormState.profiles[0].profileKey
  }
}

/**
 * 构建 AI 配置保存请求体。
 * 作用：统一裁剪字段，避免不同保存入口字段不一致。
 *
 * @returns 保存请求体
 */
function buildAiConfigPayload(): AdminAiProviderConfigForm {
  return {
    activeProfileKey: aiFormState.activeProfileKey.trim(),
    profiles: aiFormState.profiles.map(profile => ({
      profileKey: profile.profileKey.trim(),
      name: profile.name.trim(),
      enabled: profile.enabled,
      upstreamBaseUrl: profile.upstreamBaseUrl.trim(),
      apiKey: profile.apiKey.trim(),
      model: profile.model.trim(),
      upstreamProtocol: profile.upstreamProtocol,
      connectTimeoutMs: Number(profile.connectTimeoutMs),
      readTimeoutMs: Number(profile.readTimeoutMs),
      writeTimeoutMs: Number(profile.writeTimeoutMs),
      maxTokens: Number(profile.maxTokens),
      temperature: Number(profile.temperature)
    }))
  }
}

/**
 * 读取 AI 配置。
 * 作用：进入页面和手动刷新时回填配置与快照。
 */
async function loadAiConfig() {
  isLoading.value = true

  try {
    const response = await adminSiteConfig.getAiProviderConfig()
    assignAiFormState(response)
    lastSavedAiSnapshot.value = serializeAiFormState(response)
  } catch (error: any) {
    toast.add({
      title: '加载 AI 配置失败',
      description: error?.message || '暂时无法获取 AI 配置。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 保存 AI 配置。
 * 作用：提交配置到后端并刷新本地快照。
 *
 * @param options 保存行为选项
 */
async function saveAiConfig(options?: { silentSuccessToast?: boolean }) {
  if (!validateAiForm()) {
    return
  }

  isSavingAiConfig.value = true

  try {
    const response = await adminSiteConfig.updateAiProviderConfig(buildAiConfigPayload())
    assignAiFormState(response)
    lastSavedAiSnapshot.value = serializeAiFormState(response)

    if (!options?.silentSuccessToast) {
      toast.add({
        title: 'AI 配置已保存',
        color: 'success'
      })
    }
  } finally {
    isSavingAiConfig.value = false
  }
}

/**
 * 测试当前配置连接。
 * 作用：实时校验上游地址、密钥和模型可用性。
 */
async function testCurrentAiConnection() {
  if (!currentAiProfile.value) {
    toast.add({ title: '当前没有可测试的 AI 配置', color: 'warning' })
    return
  }

  isTestingAiConnection.value = true

  try {
    const response = await adminSiteConfig.testAiProviderConnection({
      ...currentAiProfile.value,
      profileKey: currentAiProfile.value.profileKey.trim(),
      name: currentAiProfile.value.name.trim(),
      upstreamBaseUrl: currentAiProfile.value.upstreamBaseUrl.trim(),
      apiKey: currentAiProfile.value.apiKey.trim(),
      model: currentAiProfile.value.model.trim(),
      upstreamProtocol: currentAiProfile.value.upstreamProtocol,
      connectTimeoutMs: Number(currentAiProfile.value.connectTimeoutMs),
      readTimeoutMs: Number(currentAiProfile.value.readTimeoutMs),
      writeTimeoutMs: Number(currentAiProfile.value.writeTimeoutMs),
      maxTokens: Number(currentAiProfile.value.maxTokens),
      temperature: Number(currentAiProfile.value.temperature)
    })

    toast.add({
      title: response.success ? '连接测试成功' : '连接测试失败',
      description: response.message,
      color: response.success ? 'success' : 'error'
    })
  } catch (error: any) {
    toast.add({
      title: '连接测试失败',
      description: error?.message || '连接测试请求失败，请稍后重试。',
      color: 'error'
    })
  } finally {
    isTestingAiConnection.value = false
  }
}

/**
 * 保存当前页面配置。
 * 作用：给页面头部主按钮提供统一提交入口。
 */
async function handleSubmit() {
  isSubmitting.value = true

  try {
    await saveAiConfig()
  } catch (error: any) {
    toast.add({
      title: '保存 AI 配置失败',
      description: error?.message || '保存未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  await loadAiConfig()
})
</script>

<template>
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">AI 配置</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
            {{ hasUnsavedChanges ? '存在未保存修改' : '已保存' }}
          </p>
        </div>

        <div class="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto sm:flex-nowrap">
          <AdminButton
            :loading="isLoading"
            icon="i-lucide-refresh-cw"
            label="刷新"
            tone="neutral"
            variant="outline"
            size="sm"
            @click="loadAiConfig"
          />
          <AdminPrimaryButton
            :loading="isSubmitting"
            icon="i-lucide-save"
            label="保存 AI 配置"
            loading-label="AI 配置保存中..."
            @click="handleSubmit"
          />
        </div>
      </div>
    </section>

    <section class="grid gap-4 rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)] lg:grid-cols-[240px_minmax(0,1fr)] lg:p-5">
      <aside class="space-y-3 rounded-[16px] border border-slate-200/80 bg-white/75 p-3 dark:border-white/10 dark:bg-white/4">
        <div class="flex items-center justify-between gap-2">
          <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">AI 配置列表</p>
          <AdminButton
            icon="i-lucide-plus"
            label="新增"
            tone="neutral"
            variant="outline"
            size="xs"
            @click="addAiProfile"
          />
        </div>

        <div class="space-y-2">
          <button
            v-for="profile in aiFormState.profiles"
            :key="profile.profileKey"
            type="button"
            class="w-full rounded-[12px] border px-3 py-2 text-left transition"
            :class="selectedAiProfileKey === profile.profileKey
              ? 'border-sky-200 bg-sky-50/90 text-sky-700 dark:border-sky-400/30 dark:bg-sky-400/10 dark:text-sky-200'
              : 'border-slate-200 bg-white/80 text-slate-600 hover:border-slate-300 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-300'"
            @click="selectAiProfile(profile.profileKey)"
          >
            <div class="flex items-center justify-between gap-2">
              <p class="truncate text-sm font-medium">{{ profile.name || profile.profileKey }}</p>
              <span
                class="rounded-full px-2 py-0.5 text-[10px] font-semibold"
                :class="profile.enabled
                  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-400/20 dark:text-emerald-200'
                  : 'bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-400'"
              >
                {{ profile.enabled ? '启用中' : '未启用' }}
              </span>
            </div>
            <div class="mt-1 flex items-center justify-between gap-2 text-xs text-slate-400 dark:text-slate-500">
              <span class="truncate">{{ profile.profileKey }}</span>
              <span>{{ profile.upstreamProtocol }}</span>
            </div>
          </button>
        </div>
      </aside>

      <section v-if="currentAiProfile" class="space-y-4 rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">当前配置详情</p>
          <div class="flex items-center gap-2">
            <AdminButton
              icon="i-lucide-plug-zap"
              label="连接测试"
              tone="neutral"
              variant="outline"
              size="xs"
              :loading="isTestingAiConnection"
              @click="testCurrentAiConnection"
            />
            <AdminButton
              icon="i-lucide-check"
              label="设为启用"
              tone="neutral"
              variant="outline"
              size="xs"
              :loading="isActivatingAiProfile"
              @click="setActiveAiProfile(currentAiProfile.profileKey)"
            />
            <AdminPrimaryButton
              icon="i-lucide-save"
              label="保存配置"
              loading-label="保存中..."
              size="xs"
              :loading="isSavingAiConfig"
              @click="saveAiConfig"
            />
            <AdminButton
              icon="i-lucide-trash-2"
              label="删除"
              tone="neutral"
              variant="outline"
              size="xs"
              @click="removeAiProfile(currentAiProfile.profileKey)"
            />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">配置键</p>
            <AdminInput
              :model-value="currentAiProfile.profileKey"
              placeholder="例如：openai-prod"
              readonly
            />
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">配置名称</p>
            <AdminInput v-model="currentAiProfile.name" placeholder="例如：OpenAI 生产" />
          </div>
          <div class="space-y-2 md:col-span-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">上游服务地址</p>
            <AdminInput v-model="currentAiProfile.upstreamBaseUrl" placeholder="https://api.openai.com" />
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">API Key</p>
            <div class="flex items-center gap-2">
              <AdminInput
                v-if="isAiApiKeyVisible(currentAiProfile.profileKey)"
                v-model="currentAiProfile.apiKey"
                placeholder="请输入 API Key"
              />
              <AdminInput
                v-else
                :model-value="maskAiApiKeyForDisplay(currentAiProfile.apiKey)"
                placeholder="请输入 API Key"
                readonly
              />
              <AdminButton
                :icon="isAiApiKeyVisible(currentAiProfile.profileKey) ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                :label="isAiApiKeyVisible(currentAiProfile.profileKey) ? '隐藏' : '查看'"
                tone="neutral"
                variant="outline"
                size="xs"
                @click="toggleAiApiKeyVisibility(currentAiProfile.profileKey)"
              />
            </div>
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">模型</p>
            <AdminInput v-model="currentAiProfile.model" placeholder="例如：gpt-4.1-mini" />
          </div>
          <div class="space-y-2 md:col-span-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">上游协议</p>
            <AdminSelect
              v-model="currentAiProfile.upstreamProtocol"
              :items="aiUpstreamProtocolOptions"
              placeholder="请选择上游协议"
            />
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">连接超时（ms）</p>
            <AdminInput v-model="currentAiProfile.connectTimeoutMs" type="number" placeholder="3000" />
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">读取超时（ms）</p>
            <AdminInput v-model="currentAiProfile.readTimeoutMs" type="number" placeholder="15000" />
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">写入超时（ms）</p>
            <AdminInput v-model="currentAiProfile.writeTimeoutMs" type="number" placeholder="15000" />
          </div>
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">最大 Token</p>
            <AdminInput v-model="currentAiProfile.maxTokens" type="number" placeholder="800" />
          </div>
          <div class="space-y-2 md:col-span-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Temperature</p>
            <AdminInput v-model="currentAiProfile.temperature" type="number" placeholder="0.4" />
            <p class="text-xs text-slate-500 dark:text-slate-400">
              推荐范围 0-2，数值越低输出越稳定。
            </p>
          </div>
        </div>
      </section>
    </section>
  </div>
</template>
