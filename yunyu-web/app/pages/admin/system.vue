<script setup lang="ts">
import type { ActuatorThreadDumpResponse, AdminSystemMonitorOverview } from '../../types/system-monitor'

/**
 * 后台系统监控页。
 * 作用：提供 JVM、线程与指标查看入口，用于后台运行状态检查。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const systemMonitor = useAdminSystemMonitor()

const isLoading = ref(false)
const isRefreshingThreadDump = ref(false)
const lastUpdatedAt = ref<number | null>(null)
const overview = ref<AdminSystemMonitorOverview | null>(null)
const threadDump = ref<ActuatorThreadDumpResponse | null>(null)
const threadDumpOpen = ref(false)

/**
 * 监控卡片数据。
 * 作用：将关键指标整理为首屏可直接查看的卡片。
 */
const insightCards = computed(() => [
  {
    title: 'Heap',
    value: formatBytes(overview.value?.heapUsedBytes)
  },
  {
    title: 'Non-Heap',
    value: formatBytes(overview.value?.nonHeapUsedBytes)
  },
  {
    title: '线程',
    value: typeof overview.value?.liveThreads === 'number' ? String(Math.round(overview.value.liveThreads)) : '--'
  },
  {
    title: 'CPU',
    value: formatRatioAsPercent(overview.value?.cpuUsageRatio)
  }
])

/**
 * 线程摘要列表。
 * 作用：展示前几条线程快照，便于快速查看。
 */
const topThreads = computed(() => threadDump.value?.threads?.slice(0, 8) || [])

/**
 * 计算堆内存使用比例。
 * 作用：用于展示当前 Heap 使用率。
 */
const heapUsagePercent = computed(() => {
  if (!overview.value?.heapUsedBytes || !overview.value?.heapMaxBytes) {
    return 0
  }

  return Math.min(100, Math.round((overview.value.heapUsedBytes / overview.value.heapMaxBytes) * 100))
})

/**
 * 读取系统监控总览。
 * 作用：刷新概览数据并记录最近刷新时间。
 */
async function loadOverview() {
  isLoading.value = true

  try {
    overview.value = await systemMonitor.fetchOverview()
    lastUpdatedAt.value = Date.now()
  } catch (error: any) {
    toast.add({
      title: '系统监控加载失败',
      description: error?.message || '暂时无法获取监控信息。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 打开线程快照弹窗。
 * 作用：在首次打开时加载线程数据。
 */
async function openThreadDump() {
  threadDumpOpen.value = true

  if (threadDump.value) {
    return
  }

  await refreshThreadDump()
}

/**
 * 刷新线程快照数据。
 * 作用：重新读取当前线程信息。
 */
async function refreshThreadDump() {
  isRefreshingThreadDump.value = true

  try {
    threadDump.value = await systemMonitor.fetchThreadDump()
  } catch (error: any) {
    toast.add({
      title: '线程快照加载失败',
      description: error?.message || '暂时无法读取线程快照。',
      color: 'error'
    })
  } finally {
    isRefreshingThreadDump.value = false
  }
}

/**
 * 格式化字节数。
 * 作用：统一展示内存相关指标。
 *
 * @param value 字节值
 * @returns 容量文本
 */
function formatBytes(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '--'
  }

  if (value === 0) {
    return '0 B'
  }

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const index = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1)
  const normalized = value / 1024 ** index

  return `${normalized.toFixed(normalized >= 100 || index === 0 ? 0 : 1)} ${units[index]}`
}

/**
 * 格式化比例值。
 * 作用：将监控比例统一转换为百分比文本。
 *
 * @param value 比例值
 * @returns 百分比文本
 */
function formatRatioAsPercent(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '--'
  }

  return `${(value * 100).toFixed(value * 100 >= 10 ? 1 : 2)}%`
}

/**
 * 格式化时间戳。
 * 作用：统一展示时间信息。
 *
 * @param value 时间戳
 * @returns 时间文本
 */
function formatDateTime(value?: number | null) {
  if (!value) {
    return '--'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'medium'
  }).format(value)
}

/**
 * 格式化运行时长。
 * 作用：将毫秒转换为更容易查看的文本。
 *
 * @param value 毫秒值
 * @returns 时长文本
 */
function formatDuration(value?: number | null) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '--'
  }

  const totalSeconds = Math.floor(value / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  if (hours > 0) {
    return `${hours} 小时 ${minutes} 分`
  }

  if (minutes > 0) {
    return `${minutes} 分 ${seconds} 秒`
  }

  return `${seconds} 秒`
}

onMounted(async () => {
  await loadOverview()
})
</script>

<template>
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">系统监控</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ formatDateTime(lastUpdatedAt) }}</p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <UButton
            icon="i-lucide-activity"
            label="线程快照"
            color="neutral"
            variant="outline"
            class="rounded-[10px]"
            @click="openThreadDump"
          />
          <UButton
            :loading="isLoading"
            icon="i-lucide-refresh-cw"
            label="刷新"
            class="rounded-[10px]"
            @click="loadOverview"
          />
        </div>
      </div>
    </section>

    <div class="space-y-4">
      <section class="grid gap-4 md:grid-cols-4">
          <div
            v-for="card in insightCards"
            :key="card.title"
            class="rounded-[16px] border border-white/55 bg-white/70 p-5 shadow-[0_16px_32px_-30px_rgba(15,23,42,0.14)] backdrop-blur-lg dark:border-white/10 dark:bg-white/5 dark:shadow-none"
          >
            <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
            <p class="mt-4 text-3xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ card.value }}</p>
          </div>
      </section>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">Heap 使用率</p>
                <p class="mt-3 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ heapUsagePercent }}%</p>
                <UProgress :value="heapUsagePercent" color="primary" class="mt-4" />
              </div>

              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">运行时长</p>
                <p class="mt-3 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ formatDuration(overview?.jvm?.uptimeMs) }}</p>
              </div>

              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">时区</p>
                <p class="mt-3 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.defaultTimeZone || '--' }}</p>
              </div>

              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">处理器</p>
                <p class="mt-3 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.availableProcessors || '--' }}</p>
              </div>
            </div>
        </section>

        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="space-y-3">
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">Java</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.javaVersion || '--' }}</p>
              </div>
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">VM</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.vmName || '--' }}</p>
              </div>
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">Vendor</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.javaVendor || '--' }}</p>
              </div>
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">启动时间</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ formatDateTime(overview?.jvm?.startTime) }}</p>
              </div>
            </div>
        </section>
      </div>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">启动参数</h2>
              <UBadge color="neutral" variant="soft" class="rounded-[8px] px-3 py-1">
                {{ overview?.jvm?.inputArguments?.length || 0 }}
              </UBadge>
            </div>

            <div class="mt-4 flex flex-wrap gap-2">
              <UBadge
                v-for="argument in overview?.jvm?.inputArguments || []"
                :key="argument"
                color="neutral"
                variant="soft"
                class="max-w-full rounded-[8px] px-3 py-1 text-left"
              >
                <span class="truncate">{{ argument }}</span>
              </UBadge>
            </div>
        </section>

        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">指标</h2>
              <UBadge color="neutral" variant="soft" class="rounded-[8px] px-3 py-1">
                {{ overview?.metricsNames?.length || 0 }}
              </UBadge>
            </div>

            <div class="mt-4 grid gap-3 sm:grid-cols-2">
              <div
                v-for="metricName in overview?.metricsNames?.slice(0, 12) || []"
                :key="metricName"
                class="rounded-[14px] border border-white/60 bg-white/56 px-4 py-3 text-sm text-slate-700 backdrop-blur-md dark:border-white/10 dark:bg-white/5 dark:text-slate-200"
              >
                {{ metricName }}
              </div>
            </div>
        </section>
      </div>
    </div>

    <UModal v-model:open="threadDumpOpen" :ui="{ content: 'sm:max-w-5xl rounded-[14px]' }">
    <template #content>
      <div class="rounded-[14px] border border-slate-200/80 bg-white/95 p-6 dark:border-slate-800 dark:bg-slate-950/95">
        <div class="flex items-center justify-between gap-3">
          <p class="text-xl font-semibold text-slate-900 dark:text-slate-50">线程快照</p>
          <div class="flex items-center gap-2">
            <UBadge color="neutral" variant="soft" class="rounded-[8px] px-3 py-1">
              {{ threadDump?.threads?.length || 0 }}
            </UBadge>
            <UButton
              :loading="isRefreshingThreadDump"
              icon="i-lucide-refresh-cw"
              label="刷新"
              class="rounded-[8px]"
              @click="refreshThreadDump"
            />
          </div>
        </div>

        <div class="mt-6 space-y-4">
          <div
            v-for="thread in topThreads"
            :key="`${thread.threadId}-${thread.threadName}`"
            class="admin-surface-soft p-4"
          >
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ thread.threadName }}</p>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  {{ thread.threadId }} · {{ thread.threadState }} · {{ thread.daemon ? 'Daemon' : 'User' }}
                </p>
              </div>
              <UBadge color="neutral" variant="soft" class="rounded-[8px] px-3 py-1">
                {{ thread.threadState }}
              </UBadge>
            </div>

            <pre class="mt-4 max-h-56 overflow-auto rounded-[10px] bg-slate-950 px-4 py-3 text-xs leading-6 text-slate-100">{{ (thread.stackTrace || []).slice(0, 12).join('\n') || '--' }}</pre>
          </div>
        </div>
      </div>
    </template>
    </UModal>
  </div>
</template>
