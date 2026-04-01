import type {
  SiteCategoryDetail,
  SiteCategoryItem,
  SiteHomeResponse,
  SitePostDetail,
  SitePostListResponse,
  SitePostQuery,
  SiteTopicDetail,
  SiteTopicItem
} from '../types/site'

/**
 * 前台内容接口组合式函数。
 * 作用：统一封装首页、文章、分类和专题相关的公开接口请求，避免页面层直接拼接后端地址。
 */
export function useSiteContent() {
  const apiClient = useApiClient()

  /**
   * 获取首页聚合数据。
   *
   * @returns 首页聚合数据
   */
  async function getHome() {
    return await apiClient.request<SiteHomeResponse>('/api/site/home')
  }

  /**
   * 查询前台文章列表。
   *
   * @param query 查询参数
   * @returns 文章分页结果
   */
  async function listPosts(query: SitePostQuery = {}) {
    return await apiClient.request<SitePostListResponse>('/api/site/posts', {
      params: query
    })
  }

  /**
   * 查询前台文章详情。
   *
   * @param slug 文章 slug
   * @returns 文章详情
   */
  async function getPostDetail(slug: string) {
    return await apiClient.request<SitePostDetail>(`/api/site/posts/${slug}`)
  }

  /**
   * 查询前台分类列表。
   *
   * @returns 分类列表
   */
  async function listCategories() {
    return await apiClient.request<SiteCategoryItem[]>('/api/site/categories')
  }

  /**
   * 查询前台分类详情。
   *
   * @param slug 分类 slug
   * @param query 查询参数
   * @returns 分类详情
   */
  async function getCategoryDetail(slug: string, query: SitePostQuery = {}) {
    return await apiClient.request<SiteCategoryDetail>(`/api/site/categories/${slug}`, {
      params: query
    })
  }

  /**
   * 查询前台专题列表。
   *
   * @returns 专题列表
   */
  async function listTopics() {
    return await apiClient.request<SiteTopicItem[]>('/api/site/topics')
  }

  /**
   * 查询前台专题详情。
   *
   * @param slug 专题 slug
   * @param query 查询参数
   * @returns 专题详情
   */
  async function getTopicDetail(slug: string, query: SitePostQuery = {}) {
    return await apiClient.request<SiteTopicDetail>(`/api/site/topics/${slug}`, {
      params: query
    })
  }

  return {
    getHome,
    listPosts,
    getPostDetail,
    listCategories,
    getCategoryDetail,
    listTopics,
    getTopicDetail
  }
}
