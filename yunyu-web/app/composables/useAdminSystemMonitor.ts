import type {
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
   * 聚合系统监控总览数据。
   * 会并发请求 JVM 信息、内存、线程、CPU 和指标名称列表，
   * 供后台监控页首屏直接渲染。
   */
  async function fetchOverview(): Promise<AdminSystemMonitorOverview> {
    const [
      info,
      heapUsedMetric,
      nonHeapUsedMetric,
      heapMaxMetric,
      liveThreadsMetric,
      cpuUsageMetric,
      metricsIndex
    ] = await Promise.all([
      apiClient.rawRequest<ActuatorInfoResponse>('/actuator/info'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.memory.used?tag=area:heap'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.memory.used?tag=area:nonheap'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.memory.max?tag=area:heap'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/jvm.threads.live'),
      apiClient.rawRequest<ActuatorMetricResponse>('/actuator/metrics/system.cpu.usage'),
      apiClient.rawRequest<{ names?: string[] }>('/actuator/metrics')
    ])

    return {
      jvm: info.jvm ?? null,
      heapUsedBytes: resolvePrimaryValue(heapUsedMetric),
      nonHeapUsedBytes: resolvePrimaryValue(nonHeapUsedMetric),
      heapMaxBytes: resolvePrimaryValue(heapMaxMetric),
      liveThreads: resolvePrimaryValue(liveThreadsMetric),
      cpuUsageRatio: resolvePrimaryValue(cpuUsageMetric),
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
