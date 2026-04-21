import { formatChineseDateTime } from '../utils/date'

/**
 * 前台友链条目类型。
 * 作用：统一描述友链展示页卡片所需的站点名称、地址、图标与主题色信息。
 */
export interface SiteFriendLinkItem {
  id: number
  siteName: string
  siteUrl: string
  logoUrl: string
  description: string
  themeColor: string
}

/**
 * 前台友链申请请求类型。
 * 作用：统一描述友链申请页提交的站点资料和联系信息。
 */
export interface SiteFriendLinkApplyRequest {
  siteName: string
  siteUrl: string
  logoUrl: string
  description: string
  contactName: string
  contactEmail: string
  contactMessage: string
  themeColor: string
}

/**
 * 前台友链申请响应类型。
 * 作用：承接友链申请提交后的状态与提示信息。
 */
export interface SiteFriendLinkApplyResponse {
  friendLinkId: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'OFFLINE'
  message: string
}

/**
 * 后台友链状态类型。
 * 作用：统一约束友链管理页和状态切换操作的枚举值。
 */
export type AdminFriendLinkStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'OFFLINE'

/**
 * 后台友链条目类型。
 * 作用：统一描述后台友链管理页展示与编辑所需的全部字段。
 */
export interface AdminFriendLinkItem {
  id: number
  siteName: string
  siteUrl: string
  logoUrl: string
  description: string
  contactName: string
  contactEmail: string
  contactMessage: string
  themeColor: string
  sortOrder: number
  status: AdminFriendLinkStatus
  createdTime: string
  updatedTime: string
}

/**
 * 后台友链列表响应类型。
 * 作用：承接后台友链管理页的分页结果。
 */
export interface AdminFriendLinkListResponse {
  list: AdminFriendLinkItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台友链查询参数类型。
 * 作用：统一描述友链管理页的搜索、状态筛选与分页参数。
 */
export interface AdminFriendLinkQuery {
  keyword?: string
  status?: AdminFriendLinkStatus
  pageNo?: number
  pageSize?: number
}

/**
 * 后台友链表单类型。
 * 作用：统一描述创建和编辑友链时提交的表单结构。
 */
export interface AdminFriendLinkForm {
  siteName: string
  siteUrl: string
  logoUrl: string
  description: string
  contactName: string
  contactEmail: string
  contactMessage: string
  themeColor: string
  sortOrder: number
  status: AdminFriendLinkStatus
}

/**
 * 友链状态标签映射。
 * 作用：为页面层提供统一的状态中文文案，避免在不同页面重复维护。
 */
export const FRIEND_LINK_STATUS_LABEL_MAP: Record<AdminFriendLinkStatus, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  OFFLINE: '已下线'
}

/**
 * 后台友链接口条目类型。
 * 作用：匹配后端友链接口返回结构，再转换为前端页面所需格式。
 */
interface AdminFriendLinkApiItem {
  id: number
  siteName: string
  siteUrl: string
  logoUrl: string | null
  description: string | null
  contactName: string | null
  contactEmail: string | null
  contactMessage: string | null
  themeColor: string | null
  sortOrder: number
  status: AdminFriendLinkStatus
  createdTime: string
  updatedTime: string
}

/**
 * 转换后台友链条目。
 * 作用：清洗可空字段并格式化时间，保证管理页始终得到稳定可显示的数据。
 *
 * @param item 后端返回条目
 * @returns 页面可直接消费的友链条目
 */
export function toAdminFriendLinkItem(item: AdminFriendLinkApiItem): AdminFriendLinkItem {
  return {
    id: item.id,
    siteName: item.siteName,
    siteUrl: item.siteUrl,
    logoUrl: item.logoUrl || '',
    description: item.description || '',
    contactName: item.contactName || '',
    contactEmail: item.contactEmail || '',
    contactMessage: item.contactMessage || '',
    themeColor: item.themeColor || '#7CC6B8',
    sortOrder: item.sortOrder ?? 0,
    status: item.status,
    createdTime: formatChineseDateTime(item.createdTime, '-'),
    updatedTime: formatChineseDateTime(item.updatedTime, '-')
  }
}
