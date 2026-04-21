import type { ApiResponse } from '../types/auth'

const SUCCESS_CODE = 0
const ACCESS_TOKEN_STORAGE_KEY = 'yunyu_access_token'

/**
 * 前端接口请求选项。
 * 作用：在保留 `$fetch` 原生能力的基础上，补充是否自动附带访问令牌的控制项，
 * 便于登录、注册等匿名接口显式跳过 Bearer Token 请求头。
 */
interface ApiClientRequestOptions<T> extends Parameters<typeof $fetch<T>>[1] {
  withAuth?: boolean
}

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
   * 从浏览器缓存恢复访问令牌。
   * 作用：在前台页面刷新后优先恢复本地保存的登录凭证，避免仅依赖 Cookie 导致前台登录态丢失。
   */
  function hydratePersistedAccessToken() {
    if (!import.meta.client || accessToken.value) {
      return accessToken.value
    }

    const storedToken = localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)

    if (!storedToken) {
      return null
    }

    accessToken.value = storedToken
    return storedToken
  }

  /**
   * 同步设置访问令牌。
   * 作用：统一把访问令牌同时写入 Cookie 与 localStorage，兼顾后台鉴权和前台刷新后的登录态恢复。
   *
   * @param token 访问令牌
   */
  function setAccessToken(token: string | null) {
    accessToken.value = token

    if (!import.meta.client) {
      return
    }

    if (token) {
      localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token)
      return
    }

    localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
  }

  /**
   * 构建请求头。
   * 作用：统一合并外部传入请求头并补充当前登录态的 Bearer Token，
   * 避免业务接口与 Actuator 接口重复处理认证逻辑。
   *
   * @param headersInit 原始请求头配置
   * @returns 最终请求头对象
   */
  function buildHeaders(headersInit?: HeadersInit, withAuth = true) {
    if (withAuth) {
      hydratePersistedAccessToken()
    }

    const headers = new Headers(headersInit || {})

    if (withAuth && accessToken.value) {
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
    options: ApiClientRequestOptions<T> = {}
  ) {
    const { withAuth = true, ...fetchOptions } = options
    const headers = buildHeaders(fetchOptions.headers, withAuth)
    const requestUrl = `${config.public.apiBase}${path}`

    try {
      const response = await $fetch<ApiResponse<T>>(requestUrl, {
        ...fetchOptions,
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
        (responseStatusCode === 404
          ? `请求的接口不存在：${path}。当前前端连接的后端地址为 ${config.public.apiBase}，请确认是否连到了最新后端服务。`
          : null) ||
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
    options: ApiClientRequestOptions<T> = {}
  ) {
    const { withAuth = true, ...fetchOptions } = options
    const headers = buildHeaders(fetchOptions.headers, withAuth)

    try {
      return await $fetch<T>(`${config.public.apiBase}${path}`, {
        ...fetchOptions,
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
    hydratePersistedAccessToken,
    setAccessToken,
    request,
    rawRequest
  }
}
