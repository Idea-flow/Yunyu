<script setup lang="ts">
import type { ActuatorThreadDumpResponse, AdminSystemMonitorOverview } from '../../types/system-monitor'

/**
 * 后台系统监控页。
 * 作用：为站长提供 JVM、线程、CPU 与 Actuator 指标的可视化查看入口，
 * 作为后台运维排查与运行时观测的管理页面。
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
 * 仪表卡片数据。
 * 作用：将监控概览映射为页面首屏可扫描的数值摘要，帮助站长快速判断服务状态。
 */
const insightCards = computed(() => [
  {
    title: 'Heap 已用内存',
    value: formatBytes(overview.value?.heapUsedBytes),
    description: overview.value?.heapMaxBytes
      ? `最大堆 ${formatBytes(overview.value.heapMaxBytes)}`
      : '尚未读取到堆上限',
    icon: 'i-lucide-hard-drive-download',
    tone: 'sky'
  },
  {
    title: 'Non-Heap 已用内存',
    value: formatBytes(overview.value?.nonHeapUsedBytes),
    description: '包含类元数据、JIT 与框架基线开销',
    icon: 'i-lucide-layers-3',
    tone: 'amber'
  },
  {
    title: '活动线程数',
    value: typeof overview.value?.liveThreads === 'number' ? String(Math.round(overview.value.liveThreads)) : '--',
    description: '用于观察线程是否持续攀升',
    icon: 'i-lucide-git-branch-plus',
    tone: 'emerald'
  },
  {
    title: '系统 CPU 使用率',
    value: formatRatioAsPercent(overview.value?.cpuUsageRatio),
    description: '结合内存与线程指标判断运行压力',
    icon: 'i-lucide-cpu',
    tone: 'violet'
  }
])

/**
 * 线程摘要列表。
 * 作用：从线程栈响应中提取最关键的前几条线程快照，避免页面首次展示信息过量。
 */
const topThreads = computed(() => threadDump.value?.threads?.slice(0, 8) || [])

/**
 * 计算堆内存使用比例。
 * 用于页面中的进度展示和风险感知。
 */
const heapUsagePercent = computed(() => {
  if (!overview.value?.heapUsedBytes || !overview.value?.heapMaxBytes) {
    return 0
  }

  return Math.min(100, Math.round((overview.value.heapUsedBytes / overview.value.heapMaxBytes) * 100))
})

/**
 * 读取系统监控总览。
 * 会在进入页面和手动刷新时执行，并同步更新时间展示。
 */
async function loadOverview() {
  isLoading.value = true

  try {
    overview.value = await systemMonitor.fetchOverview()
    lastUpdatedAt.value = Date.now()
  } catch (error: any) {
    toast.add({
      title: '系统监控加载失败',
      description: error?.message || '暂时无法获取 JVM 监控信息。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 打开线程栈面板。
 * 仅在用户主动查看时请求线程快照，降低首屏负担。
 */
async function openThreadDump() {
  threadDumpOpen.value = true

  if (threadDump.value) {
    return
  }

  await refreshThreadDump()
}

/**
 * 刷新线程栈数据。
 * 适用于排查接口卡顿、线程阻塞与活动线程数异常增长场景。
 */
async function refreshThreadDump() {
  isRefreshingThreadDump.value = true

  try {
    threadDump.value = await systemMonitor.fetchThreadDump()
  } catch (error: any) {
    toast.add({
      title: '线程快照加载失败',
      description: error?.message || '暂时无法读取线程栈信息。',
      color: 'error'
    })
  } finally {
    isRefreshingThreadDump.value = false
  }
}

/**
 * 格式化字节数。
 * 用于统一展示 JVM 内存相关指标。
 *
 * @param value 字节值
 * @returns 友好的容量文本
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
 * 格式化比例值为百分比。
 * 适用于 CPU 使用率等 0 到 1 之间的监控指标。
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
 * 用于展示页面最近一次刷新时间和 JVM 启动时间。
 *
 * @param value 时间戳
 * @returns 本地时间文本
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
 * 用于将 JVM 运行毫秒数转为更适合人工扫描的表达。
 *
 * @param value 毫秒值
 * @returns 运行时长文本
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
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="系统监控">
        <template #right>
          <div class="flex items-center gap-3">
            <UBadge color="neutral" variant="soft" class="rounded-full px-3 py-1">
              最近刷新：{{ formatDateTime(lastUpdatedAt) }}
            </UBadge>
            <UButton
              icon="i-lucide-activity"
              label="线程快照"
              color="neutral"
              variant="outline"
              class="rounded-2xl"
              @click="openThreadDump"
            />
            <UButton
              :loading="isLoading"
              icon="i-lucide-refresh-cw"
              label="刷新监控"
              class="rounded-2xl"
              @click="loadOverview"
            />
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <UCard class="overflow-hidden rounded-[34px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
          <div class="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
            <div class="space-y-5">
              <div class="space-y-3">
                <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Runtime Observatory</p>
                <h1 class="max-w-2xl text-3xl font-semibold tracking-tight text-slate-900 lg:text-[2.35rem] dark:text-slate-50">
                  把 JVM、线程与运行参数放到同一张后台工作面板里。
                </h1>
                <p class="max-w-2xl text-sm leading-8 text-slate-600 dark:text-slate-300">
                  这里聚合了 Actuator 的 `info`、`metrics` 与 `threaddump` 能力，方便你在不进服务器的情况下先判断当前服务是堆高、线程高，还是只是框架基线偏大。
                </p>
              </div>

              <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <div
                  v-for="card in insightCards"
                  :key="card.title"
                  class="rounded-[24px] border border-slate-200/80 bg-slate-50/85 p-4 shadow-[0_16px_28px_-24px_rgba(15,23,42,0.25)] dark:border-slate-700 dark:bg-slate-900/75"
                >
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
                    <div class="flex size-10 items-center justify-center rounded-2xl bg-white/70 text-slate-900 dark:bg-white/8 dark:text-slate-50">
                      <UIcon :name="card.icon" class="size-[18px]" />
                    </div>
                  </div>
                  <p class="mt-4 text-3xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ card.value }}</p>
                  <p class="mt-2 text-xs leading-6 text-slate-500 dark:text-slate-400">{{ card.description }}</p>
                </div>
              </div>
            </div>

            <div class="relative overflow-hidden rounded-[28px] border border-slate-200/80 bg-slate-50/80 p-5 dark:border-slate-700 dark:bg-slate-900/70">
              <div class="absolute inset-0 bg-[linear-gradient(to_right,rgba(148,163,184,0.08)_1px,transparent_1px),linear-gradient(to_bottom,rgba(148,163,184,0.08)_1px,transparent_1px)] bg-[size:24px_24px] opacity-70 [mask-image:linear-gradient(180deg,rgba(0,0,0,0.4),transparent_100%)]" />
              <div class="relative space-y-5">
                <div>
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Heap Pressure</p>
                  <div class="mt-3 flex items-end justify-between gap-4">
                    <div>
                      <p class="text-3xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ heapUsagePercent }}%</p>
                      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
                        已用 {{ formatBytes(overview?.heapUsedBytes) }} / {{ formatBytes(overview?.heapMaxBytes) }}
                      </p>
                    </div>
                    <UBadge color="primary" variant="soft" class="rounded-full px-3 py-1">
                      {{ overview?.jvm?.availableProcessors || '--' }} vCPU
                    </UBadge>
                  </div>
                </div>

                <UProgress :value="heapUsagePercent" color="primary" class="h-2.5" />

                <div class="grid gap-3 sm:grid-cols-2">
                  <div class="rounded-[22px] border border-slate-200 bg-white/60 p-4 dark:border-slate-700 dark:bg-white/5">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">JVM 运行时长</p>
                    <p class="mt-2 text-sm font-medium text-slate-900 dark:text-slate-50">{{ formatDuration(overview?.jvm?.uptimeMs) }}</p>
                  </div>
                  <div class="rounded-[22px] border border-slate-200 bg-white/60 p-4 dark:border-slate-700 dark:bg-white/5">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">默认时区</p>
                    <p class="mt-2 text-sm font-medium text-slate-900 dark:text-slate-50">{{ overview?.jvm?.defaultTimeZone || '--' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </UCard>

        <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
            <template #header>
              <div>
                <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">JVM Profile</p>
                <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">运行时画像</p>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">确认版本、启动时刻、内存基线与厂商信息。</p>
              </div>
            </template>

            <div class="space-y-3">
              <div class="grid gap-3 sm:grid-cols-2">
                <div class="rounded-[22px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Java Version</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.javaVersion || '--' }}</p>
                </div>
                <div class="rounded-[22px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">VM Name</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.vmName || '--' }}</p>
                </div>
                <div class="rounded-[22px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Java Vendor</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.javaVendor || '--' }}</p>
                </div>
                <div class="rounded-[22px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">JVM Start Time</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ formatDateTime(overview?.jvm?.startTime) }}</p>
                </div>
              </div>

              <div class="rounded-[26px] border border-slate-200/80 bg-[linear-gradient(135deg,rgba(14,165,233,0.06),rgba(59,130,246,0.02))] p-5 dark:border-slate-700 dark:bg-[linear-gradient(135deg,rgba(14,165,233,0.12),rgba(30,41,59,0.24))]">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Startup Arguments</p>
                    <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">这里可以直接确认容器识别、GC 日志和本地内存跟踪参数是否已生效。</p>
                  </div>
                  <UBadge color="info" variant="soft" class="rounded-full px-3 py-1">
                    {{ overview?.jvm?.inputArguments?.length || 0 }} 条
                  </UBadge>
                </div>

                <div class="mt-4 flex flex-wrap gap-2">
                  <UBadge
                    v-for="argument in overview?.jvm?.inputArguments || []"
                    :key="argument"
                    color="neutral"
                    variant="soft"
                    class="max-w-full rounded-full px-3 py-1 text-left"
                  >
                    <span class="truncate">{{ argument }}</span>
                  </UBadge>
                </div>
              </div>
            </div>
          </UCard>

          <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Metric Index</p>
                  <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">可用指标导航</p>
                  <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">用于确认当前服务暴露了哪些 JVM 与系统指标。</p>
                </div>
                <UBadge color="neutral" variant="soft" class="rounded-full px-3 py-1">
                  {{ overview?.metricsNames?.length || 0 }} 项
                </UBadge>
              </div>
            </template>

            <div class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="metricName in overview?.metricsNames?.slice(0, 12) || []"
                :key="metricName"
                class="rounded-[20px] border border-slate-200/80 bg-slate-50/80 px-4 py-3 text-sm text-slate-700 dark:border-slate-700 dark:bg-slate-900/70 dark:text-slate-200"
              >
                {{ metricName }}
              </div>

              <div
                v-if="!(overview?.metricsNames?.length)"
                class="col-span-full rounded-[20px] border border-dashed border-slate-300/80 bg-slate-50/50 px-4 py-6 text-sm text-slate-500 dark:border-slate-700 dark:bg-slate-900/50 dark:text-slate-400"
              >
                当前还没有读取到指标列表，请确认后端已启用 Actuator `metrics` 端点。
              </div>
            </div>
          </UCard>
        </div>

        <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Operator Notes</p>
                <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">排查提示</p>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">先看哪一项，能更快判断当前 300MB 内存是否属于正常框架基线。</p>
              </div>
            </div>
          </template>

          <div class="grid gap-4 lg:grid-cols-3">
            <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-5 dark:border-slate-700 dark:bg-slate-900/70">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">先看 Heap</p>
              <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                如果 `Heap 已用内存` 不高，但容器总体占用仍然高，通常说明大头不在业务对象，而在框架、类元数据、线程栈或本地内存。
              </p>
            </div>
            <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-5 dark:border-slate-700 dark:bg-slate-900/70">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">再看线程</p>
              <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                活动线程数如果持续走高，再结合线程快照查看阻塞线程，可以快速排除线程泄漏或数据库等待问题。
              </p>
            </div>
            <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-5 dark:border-slate-700 dark:bg-slate-900/70">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">最后看参数</p>
              <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                通过启动参数列表确认 `GC` 日志、容器识别和 `NativeMemoryTracking` 是否已打开，这样后续再进容器排查才不会走回头路。
              </p>
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>

  <UModal v-model:open="threadDumpOpen" :ui="{ content: 'sm:max-w-5xl rounded-[28px]' }">
    <template #content>
      <div class="rounded-[28px] border border-slate-200/80 bg-white/95 p-6 shadow-[0_28px_60px_-36px_rgba(15,23,42,0.35)] dark:border-slate-800 dark:bg-slate-950/95">
        <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Thread Snapshot</p>
            <p class="mt-1 text-xl font-semibold text-slate-900 dark:text-slate-50">线程快照</p>
            <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
              首屏只展示前 8 条线程摘要；需要更深排查时，再结合后端文档中的 `jcmd` 继续看完整线程栈。
            </p>
          </div>

          <div class="flex items-center gap-3">
            <UBadge color="neutral" variant="soft" class="rounded-full px-3 py-1">
              {{ threadDump?.threads?.length || 0 }} 条线程
            </UBadge>
            <UButton
              :loading="isRefreshingThreadDump"
              icon="i-lucide-refresh-cw"
              label="刷新快照"
              class="rounded-2xl"
              @click="refreshThreadDump"
            />
          </div>
        </div>

        <div class="mt-6 space-y-4">
          <div
            v-for="thread in topThreads"
            :key="`${thread.threadId}-${thread.threadName}`"
            class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/75"
          >
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ thread.threadName }}</p>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  线程 ID: {{ thread.threadId }} · 状态: {{ thread.threadState }} · {{ thread.daemon ? 'Daemon' : 'User Thread' }}
                </p>
              </div>
              <UBadge color="neutral" variant="soft" class="rounded-full px-3 py-1">
                {{ thread.threadState }}
              </UBadge>
            </div>

            <pre class="mt-4 max-h-56 overflow-auto rounded-[18px] bg-slate-950 px-4 py-3 text-xs leading-6 text-slate-100">{{ (thread.stackTrace || []).slice(0, 12).join('\n') || '当前线程未返回可展示的栈信息。' }}</pre>
          </div>

          <div
            v-if="!topThreads.length && !isRefreshingThreadDump"
            class="rounded-[24px] border border-dashed border-slate-300/80 bg-slate-50/50 px-4 py-8 text-sm text-slate-500 dark:border-slate-700 dark:bg-slate-900/50 dark:text-slate-400"
          >
            当前没有读取到线程快照数据，请确认后端 `threaddump` 端点可访问。
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>
