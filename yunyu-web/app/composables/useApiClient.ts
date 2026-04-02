import type { ApiResponse } from '../types/auth'

const SUCCESS_CODE = 0

/**
 * 前端接口客户端。
 * 作用：为前端页面和业务组合式函数提供统一请求入口，
 * 后续所有业务接口都应优先复用这里，而不是在页面中直接写后端地址或 `$fetch`。
 */
export function useApiClient() {
  const config = useRuntimeConfig()
  const accessToken = useCookie<string | null>('yunyu_access_token', {
    sameSite: 'lax'
  })

  /**
   * 构建请求头。
   * 作用：统一合并外部传入请求头并补充当前登录态的 Bearer Token，
   * 避免业务接口与 Actuator 接口重复处理认证逻辑。
   *
   * @param headersInit 原始请求头配置
   * @returns 最终请求头对象
   */
  function buildHeaders(headersInit?: HeadersInit) {
    const headers = new Headers(headersInit || {})

    if (accessToken.value) {
      headers.set('Authorization', `Bearer ${accessToken.value}`)
    }

    return headers
  }

  /**
   * 发起后端 API 请求。
   * 当前统一使用环境变量中的后端地址，并自动携带认证令牌，
   * 这样后续切换线上环境时只需要调整 `YUNYU_PUBLIC_API_BASE`。
   */
  async function request<T>(
    path: string,
    options: Parameters<typeof $fetch<T>>[1] = {}
  ) {
    const headers = buildHeaders(options.headers)

    try {
      const response = await $fetch<ApiResponse<T>>(`${config.public.apiBase}${path}`, {
        ...options,
        headers
      })

      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || '接口请求失败')
      }

      return response.data
    } catch (error: any) {
      const responseMessage = error?.response?._data?.message
      const responseStatusCode = error?.response?.status

      throw new Error(
        error?.data?.message ||
        responseMessage ||
        (responseStatusCode === 404 ? '请求的接口不存在，请确认后端服务已重启并加载最新接口。' : null) ||
        error?.statusMessage ||
        error?.message ||
        '接口请求失败'
      )
    }
  }

  /**
   * 发起原始后端请求。
   * 作用：用于请求 Actuator 这类不走 `ApiResponse` 包装的原生 JSON 接口，
   * 保持认证逻辑一致，同时避免业务响应结构校验误伤系统端点。
   *
   * @param path 请求路径
   * @param options 请求配置
   * @returns 原始响应数据
   */
  async function rawRequest<T>(
    path: string,
    options: Parameters<typeof $fetch<T>>[1] = {}
  ) {
    const headers = buildHeaders(options.headers)

    try {
      return await $fetch<T>(`${config.public.apiBase}${path}`, {
        ...options,
        headers
      })
    } catch (error: any) {
      const responseMessage = error?.response?._data?.message
      const responseStatusCode = error?.response?.status

      throw new Error(
        responseMessage ||
        (responseStatusCode === 403 ? '当前账号没有系统监控访问权限。' : null) ||
        (responseStatusCode === 404 ? '系统监控端点不存在，请确认后端已启用 Actuator 监控端点。' : null) ||
        error?.statusMessage ||
        error?.message ||
        '原始接口请求失败'
      )
    }
  }

  return {
    accessToken,
    request,
    rawRequest
  }
}
