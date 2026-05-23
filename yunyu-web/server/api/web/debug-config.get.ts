/**
 * 调试接口：返回运行时 API 地址配置，用于验证 SSR 环境变量是否正确注入。
 * 确认问题后可删除此文件。
 */
export default defineEventHandler((event) => {
  const config = useRuntimeConfig(event)

  const apiBaseInternal = config.yunyuApiBaseInternal || ''
  const apiBasePublic = config.public.apiBase || ''
  const resolvedApiBase = apiBaseInternal || apiBasePublic

  return {
    apiBaseInternal,
    apiBasePublic,
    resolvedApiBase,
    note: 'apiBaseInternal 为空则 SSR 会使用 apiBasePublic，两者均为空则请求相对路径'
  }
})
