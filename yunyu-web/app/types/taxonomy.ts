/**
 * 后台内容编排模块类型。
 * 作用：统一约束分类、标签、专题三类后台内容编排模块的标识，便于页面和数据层复用。
 */
export type AdminTaxonomyKind = 'category' | 'tag' | 'topic'

/**
 * 后台内容编排条目类型。
 * 作用：统一描述分类、标签、专题在后台 CRUD 页面中的基础数据结构。
 */
export interface AdminTaxonomyItem {
  id: number
  kind: AdminTaxonomyKind
  name: string
  slug: string
  description: string
  coverUrl: string
  status: 'ACTIVE' | 'DISABLED'
  sortOrder: number
  relatedPostCount: number
  createdTime: string
  updatedTime: string
}

/**
 * 后台内容编排查询参数类型。
 * 作用：统一描述分类、标签、专题列表页的搜索、状态和分页参数。
 */
export interface AdminTaxonomyQuery {
  keyword?: string
  status?: '' | 'ACTIVE' | 'DISABLED'
  pageNo?: number
  pageSize?: number
}

/**
 * 后台内容编排列表响应类型。
 * 作用：承接分类、标签、专题列表页的分页结果。
 */
export interface AdminTaxonomyListResponse {
  list: AdminTaxonomyItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台内容编排表单类型。
 * 作用：承接分类、标签、专题新建和编辑时的通用表单字段。
 */
export interface AdminTaxonomyForm {
  name: string
  slug: string
  description: string
  coverUrl: string
  status: 'ACTIVE' | 'DISABLED'
  sortOrder: number
}
