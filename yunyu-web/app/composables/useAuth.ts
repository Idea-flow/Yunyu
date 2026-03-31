import type { AuthCurrentUser, AuthLoginResponse } from '../types/auth'

/**
 * 认证组合式函数。
 * 作用：统一管理共享登录页、当前用户恢复、退出登录与后台管理员权限校验，
 * 避免页面各自维护重复的登录态逻辑。
 */
export function useAuth() {
  const apiClient = useApiClient()
  const currentUser = useState<AuthCurrentUser | null>('auth.current-user', () => null)
  const initialized = useState<boolean>('auth.initialized', () => false)
  const loading = useState<boolean>('auth.loading', () => false)

  /**
   * 判断当前用户是否为站长。
   * 站长角色使用后端约定的 `SUPER_ADMIN` 标识。
   */
  const isSuperAdmin = computed(() => currentUser.value?.role === 'SUPER_ADMIN')

  /**
   * 获取当前登录用户。
   * 当前方法会直接请求后端认证接口，用于恢复当前登录用户并同步后台访问状态。
   */
  async function fetchCurrentUser(force = false) {
    if (loading.value) {
      return currentUser.value
    }

    if (!force && initialized.value) {
      return currentUser.value
    }

    loading.value = true

    try {
      if (!apiClient.accessToken.value) {
        currentUser.value = null
        initialized.value = true
        return null
      }

      const user = await apiClient.request<AuthCurrentUser>('/api/auth/me')
      currentUser.value = user
      initialized.value = true
      return user
    } catch (error) {
      currentUser.value = null
      initialized.value = true
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 执行登录。
   * 登录请求会直接发送到后端认证接口，成功后将访问令牌写入前端 Cookie。
   */
  async function login(payload: { account: string; password: string }) {
    const loginResponse = await apiClient.request<AuthLoginResponse>('/api/auth/login', {
      method: 'POST',
      body: payload
    })

    apiClient.accessToken.value = loginResponse.accessToken

    return await fetchCurrentUser(true)
  }

  /**
   * 执行退出登录。
   * 当前会清理前端 Cookie 和缓存的用户信息。
   */
  async function logout() {
    apiClient.accessToken.value = null
    currentUser.value = null
    initialized.value = true
  }

  /**
   * 确保后台访问权限。
   * 若未登录返回未认证状态，若不是站长则返回权限不足状态。
   */
  async function ensureAdminAccess() {
    const user = await fetchCurrentUser(true)

    if (!user) {
      return { ok: false as const, reason: 'UNAUTHORIZED' as const }
    }

    if (user.role !== 'SUPER_ADMIN') {
      return { ok: false as const, reason: 'FORBIDDEN' as const }
    }

    return { ok: true as const, user }
  }

  return {
    currentUser,
    initialized,
    loading,
    isSuperAdmin,
    fetchCurrentUser,
    login,
    logout,
    ensureAdminAccess
  }
}
