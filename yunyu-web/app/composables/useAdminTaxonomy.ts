import { formatChineseDateTime } from '~/utils/date'
import type {
  AdminTaxonomyForm,
  AdminTaxonomyItem,
  AdminTaxonomyKind,
  AdminTaxonomyListResponse,
  AdminTaxonomyQuery
} from '../types/taxonomy'

/**
 * 后台内容编排接口条目类型。
 * 作用：匹配后端分类、标签、专题接口返回的通用字段结构，再转换为前端页面所需格式。
 */
interface AdminTaxonomyApiItem {
  id: number
  name: string
  slug: string
  description: string | null
  coverUrl: string | null
  status: 'ACTIVE' | 'DISABLED'
  sortOrder: number
  relatedPostCount: number
  createdTime: string
  updatedTime: string
}

/**
 * 后台内容编排接口列表响应类型。
 * 作用：匹配后端分类、标签、专题列表接口的分页结构。
 */
interface AdminTaxonomyApiListResponse {
  list: AdminTaxonomyApiItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台内容编排接口路径映射。
 * 作用：统一维护分类、标签、专题三个后台模块对应的真实后端地址。
 */
const TAXONOMY_ENDPOINT_MAP: Record<AdminTaxonomyKind, string> = {
  category: '/api/admin/categories',
  tag: '/api/admin/tags',
  topic: '/api/admin/topics'
}

/**
 * 转换后台内容编排条目。
 * 作用：为页面补齐 `kind` 字段并清洗可空值，保证前端三个模块可以共用同一套展示组件。
 *
 * @param kind 模块类型
 * @param item 后端返回条目
 * @returns 页面可直接消费的条目数据
 */
function toAdminTaxonomyItem(kind: AdminTaxonomyKind, item: AdminTaxonomyApiItem): AdminTaxonomyItem {
  return {
    id: item.id,
    kind,
    name: item.name,
    slug: item.slug,
    description: item.description || '',
    coverUrl: item.coverUrl || '',
    status: item.status,
    sortOrder: item.sortOrder ?? 0,
    relatedPostCount: item.relatedPostCount ?? 0,
    createdTime: formatChineseDateTime(item.createdTime, '-'),
    updatedTime: formatChineseDateTime(item.updatedTime, '-')
  }
}

/**
 * 后台内容编排组合式函数。
 * 作用：统一封装分类、标签、专题三类后台模块的真实 CRUD 接口，避免页面直接处理请求细节。
 */
export function useAdminTaxonomy() {
  const apiClient = useApiClient()

  /**
   * 获取模块对应接口地址。
   * 作用：集中处理分类、标签、专题的路径映射，避免页面和业务代码到处拼接字符串。
   *
   * @param kind 模块类型
   * @returns 对应后端接口地址
   */
  function resolveEndpoint(kind: AdminTaxonomyKind) {
    return TAXONOMY_ENDPOINT_MAP[kind]
  }

  /**
   * 查询指定类型的列表。
   * 作用：按模块、关键词、状态和分页参数请求真实接口，并转换为页面统一结构。
   *
   * @param kind 模块类型
   * @param query 查询参数
   * @returns 列表结果
   */
  async function listItems(kind: AdminTaxonomyKind, query: AdminTaxonomyQuery = {}): Promise<AdminTaxonomyListResponse> {
    const response = await apiClient.request<AdminTaxonomyApiListResponse>(resolveEndpoint(kind), {
      method: 'GET',
      query
    })

    return {
      ...response,
      list: response.list.map(item => toAdminTaxonomyItem(kind, item))
    }
  }

  /**
   * 创建内容编排条目。
   * 作用：调用真实接口创建分类、标签或专题，并返回转换后的条目结果。
   *
   * @param kind 模块类型
   * @param payload 表单数据
   * @returns 新创建的条目
   */
  async function createItem(kind: AdminTaxonomyKind, payload: AdminTaxonomyForm): Promise<AdminTaxonomyItem> {
    const response = await apiClient.request<AdminTaxonomyApiItem>(resolveEndpoint(kind), {
      method: 'POST',
      body: payload
    })

    return toAdminTaxonomyItem(kind, response)
  }

  /**
   * 更新内容编排条目。
   * 作用：调用真实接口更新分类、标签或专题信息，并返回转换后的条目结果。
   *
   * @param kind 模块类型
   * @param itemId 条目ID
   * @param payload 表单数据
   * @returns 更新后的条目
   */
  async function updateItem(kind: AdminTaxonomyKind, itemId: number, payload: AdminTaxonomyForm): Promise<AdminTaxonomyItem> {
    const response = await apiClient.request<AdminTaxonomyApiItem>(`${resolveEndpoint(kind)}/${itemId}`, {
      method: 'PUT',
      body: payload
    })

    return toAdminTaxonomyItem(kind, response)
  }

  /**
   * 删除内容编排条目。
   * 作用：调用真实接口删除指定分类、标签或专题。
   *
   * @param kind 模块类型
   * @param itemId 条目ID
   */
  async function deleteItem(kind: AdminTaxonomyKind, itemId: number) {
    await apiClient.request<void>(`${resolveEndpoint(kind)}/${itemId}`, {
      method: 'DELETE'
    })
  }

  return {
    listItems,
    createItem,
    updateItem,
    deleteItem
  }
}
