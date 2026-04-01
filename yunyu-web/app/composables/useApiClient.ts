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
   * 发起后端 API 请求。
   * 当前统一使用环境变量中的后端地址，并自动携带认证令牌，
   * 这样后续切换线上环境时只需要调整 `YUNYU_PUBLIC_API_BASE`。
   */
  async function request<T>(
    path: string,
    options: Parameters<typeof $fetch<T>>[1] = {}
  ) {
    const headers = new Headers(options.headers || {})

    if (accessToken.value) {
      headers.set('Authorization', `Bearer ${accessToken.value}`)
    }

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

  return {
    accessToken,
    request
  }
}
