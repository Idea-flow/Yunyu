/**
 * 后台权限中间件。
 * 作用：在进入后台页面前统一检查登录态与站长角色，
 * 若未登录或非站长，则重定向到共享登录页并附带原因参数。
 */
export default defineNuxtRouteMiddleware(async (to) => {
  const auth = useAuth()
  const result = await auth.ensureAdminAccess()

  if (result.ok) {
    return
  }

  return navigateTo({
    path: '/login',
    query: {
      redirect: to.fullPath,
      reason: result.reason === 'FORBIDDEN' ? 'not-owner' : 'login-required'
    }
  }, {
    replace: true
  })
})
