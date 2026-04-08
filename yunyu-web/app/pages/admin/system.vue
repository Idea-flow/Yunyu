<script setup lang="ts">
import { formatChineseDateTime } from '~/utils/date'
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
 * 监控主卡片数据。
 * 作用：将首屏最值得先看的监控信息整理成四张摘要卡片。
 */
const insightCards = computed(() => [
  {
    title: '系统状态',
    value: resolveHealthLabel(overview.value?.healthStatus),
    hint: '综合健康检查结果'
  },
  {
    title: 'CPU',
    value: formatRatioAsPercent(overview.value?.cpuUsageRatio),
    hint: '当前进程 CPU 使用率'
  },
  {
    title: '线程',
    value: typeof overview.value?.liveThreads === 'number' ? String(Math.round(overview.value.liveThreads)) : '--',
    hint: '当前 JVM 活跃线程数'
  },
  {
    title: '待审核评论',
    value: formatCount(overview.value?.pendingCommentTotal || 0),
    hint: '当前需要人工处理的评论'
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
 * 当前进程运行内存使用量。
 * 作用：根据 JVM runtime 基线信息展示当前进程已分配内存中的使用情况。
 */
const runtimeUsedMemoryBytes = computed(() => {
  if (
    typeof overview.value?.jvm?.totalMemory !== 'number' ||
    typeof overview.value?.jvm?.freeMemory !== 'number'
  ) {
    return null
  }

  return Math.max(0, overview.value.jvm.totalMemory - overview.value.jvm.freeMemory)
})

/**
 * 资源详情卡片。
 * 作用：集中展示内存、线程和运行时长等更细粒度但仍高频有用的信息。
 */
const resourceDetailCards = computed(() => [
  {
    title: 'Heap 已使用',
    value: formatBytes(overview.value?.heapUsedBytes),
    hint: `最大 ${formatBytes(overview.value?.heapMaxBytes)}`
  },
  {
    title: '进程内存已使用',
    value: formatBytes(runtimeUsedMemoryBytes.value),
    hint: `已分配 ${formatBytes(overview.value?.jvm?.totalMemory)}`
  },
  {
    title: 'Non-Heap',
    value: formatBytes(overview.value?.nonHeapUsedBytes),
    hint: '类元数据、JIT 等非堆内存'
  },
  {
    title: '运行时长',
    value: formatDuration(overview.value?.jvm?.uptimeMs),
    hint: `启动于 ${formatDateTime(overview.value?.jvm?.startTime)}`
  }
])

/**
 * 健康状态卡片。
 * 作用：把整体、数据库、磁盘等关键组件健康情况单独列出来，便于快速判断故障位置。
 */
const healthCards = computed(() => [
  {
    title: '整体健康',
    status: overview.value?.healthStatus,
    value: resolveHealthLabel(overview.value?.healthStatus),
    hint: '综合健康检查'
  },
  {
    title: '数据库',
    status: overview.value?.databaseStatus,
    value: resolveHealthLabel(overview.value?.databaseStatus),
    hint: '数据库连通情况'
  },
  {
    title: '磁盘空间',
    status: overview.value?.diskStatus,
    value: resolveHealthLabel(overview.value?.diskStatus),
    hint: '服务节点磁盘状态'
  }
])

/**
 * 线程状态统计卡片。
 * 作用：将线程快照聚合成状态分布，优先展示最需要关注的线程状态。
 */
const threadStateCards = computed(() => {
  const counts = overview.value?.threadStateCounts || {}

  return Object.entries(counts)
    .sort((left, right) => right[1] - left[1])
    .slice(0, 6)
    .map(([state, count]) => ({
      state,
      count,
      hint: resolveThreadStateHint(state)
    }))
})

/**
 * 启动参数预览列表。
 * 作用：将低频但排障时仍有价值的启动参数保留在辅助区域，避免挤占首屏注意力。
 */
const launchArgumentsPreview = computed(() => overview.value?.jvm?.inputArguments || [])

/**
 * 指标名称预览列表。
 * 作用：保留当前已接入的 Actuator 指标索引，方便后续继续扩展监控项。
 */
const metricsPreview = computed(() => (overview.value?.metricsNames || []).slice(0, 12))

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
  return formatChineseDateTime(value, '--')
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

/**
 * 格式化统计数字。
 * 作用：统一将后台监控页中的业务数量转换为中文格式，便于快速扫读。
 *
 * @param value 原始数值
 * @returns 格式化后的数量文本
 */
function formatCount(value?: number | null) {
  return new Intl.NumberFormat('zh-CN').format(Math.max(0, value || 0))
}

/**
 * 解析健康状态展示文案。
 * 作用：统一把健康检查状态转换成后台更容易理解的中文表述。
 *
 * @param status 健康状态
 * @returns 状态文案
 */
function resolveHealthLabel(status?: string | null) {
  if (!status) {
    return '未知'
  }

  if (status === 'UP') {
    return '正常'
  }

  if (status === 'DOWN' || status === 'OUT_OF_SERVICE') {
    return '异常'
  }

  return status
}

/**
 * 解析健康状态徽标颜色。
 * 作用：让整体健康、数据库和磁盘状态具备稳定的视觉反馈。
 *
 * @param status 健康状态
 * @returns 徽标颜色
 */
function resolveHealthBadgeColor(status?: string | null) {
  if (status === 'UP') {
    return 'success'
  }

  if (status === 'DOWN' || status === 'OUT_OF_SERVICE') {
    return 'error'
  }

  if (status) {
    return 'warning'
  }

  return 'neutral'
}

/**
 * 解析线程状态提示语。
 * 作用：帮助站长快速理解不同线程状态的含义，而不是只看到英文枚举值。
 *
 * @param state 线程状态
 * @returns 提示语
 */
function resolveThreadStateHint(state: string) {
  if (state === 'RUNNABLE') {
    return '正在运行或等待 CPU 调度'
  }

  if (state === 'WAITING' || state === 'TIMED_WAITING') {
    return '处于等待、休眠或定时阻塞状态'
  }

  if (state === 'BLOCKED') {
    return '等待锁资源，建议重点关注'
  }

  if (state === 'NEW' || state === 'TERMINATED') {
    return '线程尚未启动或已经结束'
  }

  return '当前线程状态统计'
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
          <UBadge
            :color="resolveHealthBadgeColor(overview?.healthStatus)"
            variant="soft"
            class="rounded-[8px] px-3 py-1"
          >
            {{ resolveHealthLabel(overview?.healthStatus) }}
          </UBadge>
          <AdminButton
            icon="i-lucide-activity"
            label="线程快照"
            tone="neutral"
            variant="outline"
            @click="openThreadDump"
          />
          <AdminButton
            :loading="isLoading"
            icon="i-lucide-refresh-cw"
            label="刷新"
            tone="primary"
            variant="solid"
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
            <p class="mt-2 text-xs leading-5 text-slate-500 dark:text-slate-400">{{ card.hint }}</p>
          </div>
      </section>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]">
        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <div>
                <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">资源详情</h2>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">聚焦内存、线程、运行时长这些最常用的运行时信息。</p>
              </div>
              <UBadge color="primary" variant="soft" class="rounded-[8px] px-3 py-1">
                Heap {{ heapUsagePercent }}%
              </UBadge>
            </div>

            <div class="mt-4 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <div
                v-for="card in resourceDetailCards"
                :key="card.title"
                class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5"
              >
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
                <p class="mt-3 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ card.value }}</p>
                <p class="mt-2 text-xs leading-5 text-slate-500 dark:text-slate-400">{{ card.hint }}</p>
              </div>
            </div>

            <div class="mt-4 rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
              <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">Heap 使用率</p>
                <span class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ heapUsagePercent }}%</span>
              </div>
              <UProgress :value="heapUsagePercent" color="primary" class="mt-4" />
            </div>
        </section>

        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <div>
                <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">健康状态</h2>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">快速判断整体、数据库和磁盘是否存在明显异常。</p>
              </div>
            </div>

            <div class="mt-4 space-y-3">
              <div
                v-for="card in healthCards"
                :key="card.title"
                class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5"
              >
                <div class="flex items-center justify-between gap-3">
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
                  <UBadge :color="resolveHealthBadgeColor(card.status)" variant="soft" class="rounded-[8px] px-3 py-1">
                    {{ card.value }}
                  </UBadge>
                </div>
                <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">{{ card.hint }}</p>
              </div>
            </div>

            <div class="mt-4 grid gap-3 sm:grid-cols-2">
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">Java</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.javaVersion || '--' }}</p>
              </div>
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">VM</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.vmName || '--' }}</p>
              </div>
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">时区</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.defaultTimeZone || '--' }}</p>
              </div>
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">处理器</p>
                <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.availableProcessors || '--' }}</p>
              </div>
            </div>
        </section>
      </div>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <div>
                <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">线程状态统计</h2>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">优先看是否存在 `BLOCKED` 线程堆积，再看等待类线程占比。</p>
              </div>
              <UBadge color="neutral" variant="soft" class="rounded-[8px] px-3 py-1">
                Daemon {{ formatCount(overview?.daemonThreads || 0) }}
              </UBadge>
            </div>

            <div class="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="card in threadStateCards"
                :key="card.state"
                class="rounded-[14px] border border-white/60 bg-white/56 px-4 py-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5"
              >
                <div class="flex items-center justify-between gap-3">
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.state }}</p>
                  <span class="text-lg font-semibold text-slate-900 dark:text-slate-50">{{ formatCount(card.count) }}</span>
                </div>
                <p class="mt-2 text-xs leading-5 text-slate-500 dark:text-slate-400">{{ card.hint }}</p>
              </div>
            </div>

            <div class="mt-4 rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
              <div class="grid gap-3 md:grid-cols-3">
                <div>
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">活跃线程</p>
                  <p class="mt-2 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ formatCount(overview?.liveThreads || 0) }}</p>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">守护线程</p>
                  <p class="mt-2 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ formatCount(overview?.daemonThreads || 0) }}</p>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">BLOCKED</p>
                  <p class="mt-2 text-lg font-semibold text-slate-900 dark:text-slate-50">{{ formatCount(overview?.blockedThreads || 0) }}</p>
                </div>
              </div>
            </div>
        </section>

        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <div>
                <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">运行环境</h2>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">保留排障时常用的 JVM 参数和指标索引，默认放在辅助区。</p>
              </div>
              <UBadge color="neutral" variant="soft" class="rounded-[8px] px-3 py-1">
                指标 {{ overview?.metricsNames?.length || 0 }}
              </UBadge>
            </div>

            <div class="mt-4 rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
              <div class="grid gap-3 sm:grid-cols-2">
                <div>
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">Java Vendor</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ overview?.jvm?.javaVendor || '--' }}</p>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-500 dark:text-slate-400">启动时间</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ formatDateTime(overview?.jvm?.startTime) }}</p>
                </div>
              </div>
            </div>

            <div class="mt-4">
              <p class="text-sm font-medium text-slate-500 dark:text-slate-400">启动参数</p>
              <div class="mt-3 flex flex-wrap gap-2">
                <UBadge
                  v-for="argument in launchArgumentsPreview"
                  :key="argument"
                  color="neutral"
                  variant="soft"
                  class="max-w-full rounded-[8px] px-3 py-1 text-left"
                >
                  <span class="truncate">{{ argument }}</span>
                </UBadge>
                <span v-if="launchArgumentsPreview.length === 0" class="text-sm text-slate-500 dark:text-slate-400">当前没有额外启动参数。</span>
              </div>
            </div>

            <div class="mt-4">
              <p class="text-sm font-medium text-slate-500 dark:text-slate-400">已接入指标</p>
              <div class="mt-3 grid gap-3 sm:grid-cols-2">
                <div
                  v-for="metricName in metricsPreview"
                  :key="metricName"
                  class="rounded-[14px] border border-white/60 bg-white/56 px-4 py-3 text-sm text-slate-700 backdrop-blur-md dark:border-white/10 dark:bg-white/5 dark:text-slate-200"
                >
                  {{ metricName }}
                </div>
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
            <AdminButton
              :loading="isRefreshingThreadDump"
              icon="i-lucide-refresh-cw"
              label="刷新"
              tone="neutral"
              variant="outline"
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
