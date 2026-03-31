/**
 * 认证用户信息类型。
 * 作用：统一描述前端登录态中的当前用户结构，
 * 方便共享登录页、后台守卫与后台页面复用同一套身份数据。
 */
export interface AuthCurrentUser {
  userId: number
  email: string
  userName: string
  role: string
  status: string
}

/**
 * 登录响应类型。
 * 作用：承接后端登录接口返回的访问令牌与用户基础信息，
 * 用于服务端设置 Cookie 和前端完成登录后续逻辑。
 */
export interface AuthLoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: number
  email: string
  userName: string
  role: string
}

/**
 * 统一接口响应结构。
 * 作用：匹配后端 `ApiResponse` 的标准格式，
 * 方便前端服务端代理层解析业务码与消息。
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
