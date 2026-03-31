/**
 * 后台用户条目类型。
 * 作用：统一描述后台用户管理页中的单个用户结构，供列表展示与编辑表单复用。
 */
export interface AdminUserItem {
  id: number
  email: string
  userName: string
  avatarUrl: string | null
  role: 'SUPER_ADMIN' | 'USER'
  status: 'ACTIVE' | 'DISABLED'
  lastLoginAt: string | null
  lastLoginIp: string | null
  createdTime: string
  updatedTime: string
}

/**
 * 后台用户列表响应类型。
 * 作用：匹配后端用户列表接口的返回结构，统一承接列表和总数。
 */
export interface AdminUserListResponse {
  list: AdminUserItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台用户列表查询参数类型。
 * 作用：统一描述后台用户列表的搜索和筛选条件。
 */
export interface AdminUserQuery {
  keyword?: string
  role?: '' | 'SUPER_ADMIN' | 'USER'
  status?: '' | 'ACTIVE' | 'DISABLED'
  pageNo?: number
  pageSize?: number
}

/**
 * 后台用户表单类型。
 * 作用：承接新建和编辑用户时的表单字段，避免页面中散落字段定义。
 */
export interface AdminUserForm {
  email: string
  userName: string
  avatarUrl: string
  password: string
  role: 'SUPER_ADMIN' | 'USER'
  status: 'ACTIVE' | 'DISABLED'
}
