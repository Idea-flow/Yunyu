import type {
  ActuatorHealthResponse,
  ActuatorInfoResponse,
  ActuatorMetricResponse,
  ActuatorThreadDumpResponse,
  AdminSystemMonitorOverview
} from '../types/system-monitor'

/**
 * 后台系统监控组合式函数。
 * 作用：统一封装 Actuator JVM 监控端点请求、指标聚合与线程栈按需加载逻辑，
 * 为后台系统监控页面提供稳定的数据访问入口。
 */
export function useAdminSystemMonitor() {
  const apiClient = useApiClient()
  const adminPosts = useAdminPosts()
  const adminComments = useAdminComments()
  const adminTaxonomy = useAdminTaxonomy()
  const adminUsers = useAdminUsers()

  /**
   * 读取单个指标的主要测量值。
   * 当前优先返回第一个测量项，适用于线程数、CPU 使用率等单值指标。
   *
   * @param metric 指标响应
   * @returns 指标值
   */
  function resolvePrimaryValue(metric: ActuatorMetricResponse | null) {
    return metric?.measurements?.[0]?.value ?? null
  }

  /**
   * 读取健康检查中指定组件状态。
   * 作用：从 Actuator 健康响应中提取数据库、磁盘等关键组件状态，避免页面层处理复杂兜底。
   *
   * @param health 健康检查响应
   * @param componentName 组件名称
   * @returns 组件状态
   */
  function resolveComponentStatus(health: ActuatorHealthResponse | null, componentName: string) {
    return health?.components?.[componentName]?.status ?? null
  }

  /**
   * 构建线程状态分布。
   * 作用：将线程快照转换成状态计数字典，便于系统页直接展示 RUNNABLE、WAITING、BLOCKED 等聚合信息。
   *
   * @param threadDump 线程快照
   * @returns 线程状态统计
   */
  function buildThreadStateCounts(threadDump: ActuatorThreadDumpResponse | null) {
    return (threadDump?.threads || []).reduce<Record<string, number>>((accumulator, thread) => {
      const state = thread.threadState || 'UNKNOWN'
      accumulator[state] = (accumulator[state] || 0) + 1
      return accumulator
    }, {})
  }

  /**
   * 聚合系统监控总览数据。
   * 会并发请求 JVM 信息、内存、线程、CPU 和指标名称列表，
   * 供后台监控页首屏直接渲染。
   */
  async function fetchOverview(): Promise<AdminSystemMonitorOverview> {
    const [
      health,
      info,
      heapUsedMetric,
      nonHeapUsedMetric,
      heapMaxMetric,
      liveThreadsMetric,
      cpuUsageMetric,
      metricsIndex,
      threadDump,
      publishedPostsResponse,
      draftPostsResponse,
      pendingCommentsResponse,
      approvedCommentsResponse,
      usersResponse,
      activeUsersResponse,
      categoriesResponse,
      tagsResponse
    ] = await Promise.all([
      apiClient.rawRequest<ActuatorHealthResponse>('/actuator/health'),
      apiClient.rawRequest<ActuatorInfoResponse>('/actuator/info'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.memory.used?tag=area:heap'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.memory.used?tag=area:nonheap'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.memory.max?tag=area:heap'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.threads.live'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/system.cpu.usage'),
      apiClient.rawRequest<{ names?: string[] }>('/actuator/metrics'),
      apiClient.rawRequest<ActuatorThreadDumpResponse>('/actuator/threaddump'),
      adminPosts.listPosts({ status: 'PUBLISHED', pageNo: 1, pageSize: 1 }),
      adminPosts.listPosts({ status: 'DRAFT', pageNo: 1, pageSize: 1 }),
      adminComments.listComments({ status: 'PENDING', pageNo: 1, pageSize: 1 }),
      adminComments.listComments({ status: 'APPROVED', pageNo: 1, pageSize: 1 }),
      adminUsers.listUsers({ pageNo: 1, pageSize: 1 }),
      adminUsers.listUsers({ status: 'ACTIVE', pageNo: 1, pageSize: 1 }),
      adminTaxonomy.listItems('category', { status: 'ACTIVE', pageNo: 1, pageSize: 1 }),
      adminTaxonomy.listItems('tag', { status: 'ACTIVE', pageNo: 1, pageSize: 1 })
    ])

    const threadStateCounts = buildThreadStateCounts(threadDump)
    const daemonThreads = (threadDump.threads || []).filter(thread => Boolean(thread.daemon)).length

    return {
      jvm: info.jvm ?? null,
      healthStatus: health?.status ?? null,
      databaseStatus: resolveComponentStatus(health, 'db'),
      diskStatus: resolveComponentStatus(health, 'diskSpace'),
      heapUsedBytes: resolvePrimaryValue(heapUsedMetric),
      nonHeapUsedBytes: resolvePrimaryValue(nonHeapUsedMetric),
      heapMaxBytes: resolvePrimaryValue(heapMaxMetric),
      liveThreads: resolvePrimaryValue(liveThreadsMetric),
      daemonThreads,
      blockedThreads: threadStateCounts.BLOCKED || 0,
      cpuUsageRatio: resolvePrimaryValue(cpuUsageMetric),
      threadStateCounts,
      publishedPostTotal: publishedPostsResponse.total,
      draftPostTotal: draftPostsResponse.total,
      pendingCommentTotal: pendingCommentsResponse.total,
      approvedCommentTotal: approvedCommentsResponse.total,
      userTotal: usersResponse.total,
      activeUserTotal: activeUsersResponse.total,
      categoryTotal: categoriesResponse.total,
      tagTotal: tagsResponse.total,
      metricsNames: metricsIndex.names || []
    }
  }

  /**
   * 读取线程栈信息。
   * 该方法只在用户主动查看线程快照时调用，避免首屏加载过重。
   */
  async function fetchThreadDump() {
    return await apiClient.rawRequest<ActuatorThreadDumpResponse>('/actuator/threaddump')
  }

  return {
    fetchOverview,
    fetchThreadDump
  }
}
