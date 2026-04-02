/**
 * JVM 信息类型。
 * 作用：承接 Actuator `info` 端点中的 JVM 详情数据，
 * 方便后台系统监控页统一展示运行时版本、启动参数与内存基线。
 */
export interface JvmInfo {
  javaVersion?: string
  javaVendor?: string
  vmName?: string
  vmVendor?: string
  vmVersion?: string
  inputArguments?: string[]
  startTime?: number
  uptimeMs?: number
  availableProcessors?: number
  maxMemory?: number
  totalMemory?: number
  freeMemory?: number
  defaultTimeZone?: string
}

/**
 * Actuator info 响应类型。
 * 作用：描述当前项目 `info` 端点返回的最小结构，
 * 便于系统监控页安全读取 JVM 详情。
 */
export interface ActuatorInfoResponse {
  jvm?: JvmInfo
  [key: string]: unknown
}

/**
 * Actuator 指标测量项类型。
 * 作用：统一描述监控指标的单个测量值与统计方式，
 * 便于页面对 CPU、线程和内存指标进行解析。
 */
export interface ActuatorMetricMeasurement {
  statistic: string
  value: number
}

/**
 * Actuator 指标标签类型。
 * 作用：描述指标查询可用的标签项，帮助前端调试和补充信息说明。
 */
export interface ActuatorAvailableTag {
  tag: string
  values: string[]
}

/**
 * Actuator 指标响应类型。
 * 作用：承接 `metrics` 端点响应，供监控页读取 JVM 与系统运行指标。
 */
export interface ActuatorMetricResponse {
  name: string
  description?: string
  baseUnit?: string
  measurements: ActuatorMetricMeasurement[]
  availableTags?: ActuatorAvailableTag[]
}

/**
 * Actuator 线程栈线程条目类型。
 * 作用：用于展示线程名、状态与堆栈片段，辅助后台排查阻塞和线程数量异常。
 */
export interface ActuatorThreadDumpThread {
  threadName: string
  threadId: number
  threadState: string
  daemon?: boolean
  stackTrace?: string[]
}

/**
 * Actuator 线程栈响应类型。
 * 作用：兼容 Spring Boot `threaddump` 端点的线程列表结构，
 * 便于前端按需展示线程快照摘要。
 */
export interface ActuatorThreadDumpResponse {
  threads: ActuatorThreadDumpThread[]
}

/**
 * 系统监控总览类型。
 * 作用：将多个 Actuator 端点聚合为后台页面可直接消费的统一视图模型。
 */
export interface AdminSystemMonitorOverview {
  jvm: JvmInfo | null
  heapUsedBytes: number | null
  nonHeapUsedBytes: number | null
  heapMaxBytes: number | null
  liveThreads: number | null
  cpuUsageRatio: number | null
  metricsNames: string[]
}
