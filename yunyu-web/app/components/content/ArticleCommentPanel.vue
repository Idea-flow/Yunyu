<script setup lang="ts">
import type { SiteCommentItem } from '../../types/comment'
import CommentComposer from './CommentComposer.vue'
import FrontPaginationBar from './FrontPaginationBar.vue'

/**
 * 文章评论区组件。
 * 作用：承载前台文章详情页的评论展示、回复、登录态引导和评论发布交互。
 */
const props = defineProps<{
  postSlug: string
  allowComment: boolean
}>()

const route = useRoute()
const toast = useToast()
const auth = useAuth()
const siteContent = useSiteContent()

await auth.fetchCurrentUser()

const currentPage = ref(1)
const pageSize = 10
const isLoading = ref(false)
const isRootSubmitting = ref(false)
const isReplySubmitting = ref(false)
const comments = ref<SiteCommentItem[]>([])
const total = ref(0)
const totalPages = ref(1)
const commentCount = ref(0)
const replyTarget = ref<SiteCommentItem | null>(null)
const rootContent = ref('')
const replyContent = ref('')

/**
 * 判断当前是否已登录。
 * 作用：统一给评论发布、回复按钮和登录引导区域提供状态来源。
 */
const isLoggedIn = computed(() => Boolean(auth.currentUser.value))

/**
 * 计算评论区顶部统计文案。
 * 作用：让评论区始终显示当前文章已公开评论总数，而不是仅显示当前页楼层数量。
 */
const commentSummaryText = computed(() => `${commentCount.value} 条评论`)

/**
 * 计算主评论输入框占位提示。
 * 作用：根据登录态和文章评论开关调整根评论发布区的输入引导文案。
 */
const rootCommentPlaceholder = computed(() => {
  if (!props.allowComment) {
    return '当前文章暂未开放评论'
  }

  if (!isLoggedIn.value) {
    return '登录后即可参与评论交流'
  }

  return '写下你对这篇文章的看法、补充或想继续聊的话题'
})

/**
 * 计算楼层回复输入框占位提示。
 * 作用：根据当前回复对象和登录态为就地回复框提供更明确的输入提示。
 */
const replyCommentPlaceholder = computed(() => {
  if (!props.allowComment) {
    return '当前文章暂未开放回复'
  }

  if (!isLoggedIn.value) {
    return '登录后即可在当前楼层继续回复'
  }

  if (replyTarget.value) {
    return `回复 @${replyTarget.value.author.userName}`
  }

  return '写下你的回复内容'
})

/**
 * 计算主评论帮助文案。
 * 作用：在主评论输入区向用户说明评论开放状态与审核机制。
 */
const rootHelperText = computed(() => {
  if (!props.allowComment) {
    return '当前文章暂不接受新的评论内容。'
  }

  if (!isLoggedIn.value) {
    return '登录后即可发表评论，普通用户的新评论会先进入审核队列。'
  }

  return '请保持友善交流，避免发布无关或攻击性内容。'
})

/**
 * 计算楼层回复帮助文案。
 * 作用：在当前评论下方说明就地回复的规则，减少用户的操作疑惑。
 */
const replyHelperText = computed(() => {
  if (!props.allowComment) {
    return '当前文章暂不接受新的回复内容。'
  }

  if (!isLoggedIn.value) {
    return '登录后即可在这里直接回复，不需要回到顶部。'
  }

  return '回复提交后会进入审核流程，通过后展示在当前楼层。'
})

/**
 * 加载文章评论列表。
 * 作用：根据当前文章与页码拉取评论区数据，并同步统计信息。
 */
async function loadComments() {
  isLoading.value = true

  try {
    const response = await siteContent.listPostComments(props.postSlug, {
      pageNo: currentPage.value,
      pageSize
    })
    comments.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
    commentCount.value = response.commentCount
  } catch (error: any) {
    toast.add({
      title: '评论加载失败',
      description: error?.message || '暂时无法获取评论列表，请稍后重试。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 处理评论分页切换。
 * 作用：在切换评论页码时刷新评论列表，并保持当前回复状态清晰可控。
 *
 * @param pageNo 目标页码
 */
async function handlePageChange(pageNo: number) {
  if (pageNo === currentPage.value) {
    return
  }

  currentPage.value = pageNo
  replyTarget.value = null
  await loadComments()
}

/**
 * 激活回复模式。
 * 作用：让评论表单知道当前是在回复哪条评论，并把输入焦点引导到表单区。
 *
 * @param comment 评论条目
 */
function startReply(comment: SiteCommentItem) {
  if (!props.allowComment) {
    toast.add({
      title: '当前文章未开放评论',
      color: 'warning'
    })
    return
  }

  if (replyTarget.value?.id === comment.id) {
    cancelReply()
    return
  }

  replyTarget.value = comment
  replyContent.value = ''
}

/**
 * 取消当前回复状态。
 * 作用：允许用户从回复模式退回到普通评论模式。
 */
function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

/**
 * 跳转到登录页以继续评论。
 * 作用：未登录用户点击评论操作时，保留当前文章地址作为回跳目标。
 */
async function goToLogin() {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

/**
 * 发布评论。
 * 作用：统一封装根评论和楼层回复的真实发送逻辑，避免两套提交流程重复实现。
 *
 * @param payload 评论请求参数
 * @returns 发布结果
 */
async function submitComment(payload: { content: string, replyCommentId?: number | null }) {
  if (!props.allowComment) {
    toast.add({
      title: '当前文章未开放评论',
      color: 'warning'
    })
    return
  }

  if (!isLoggedIn.value) {
    toast.add({
      title: '请先登录后再发表评论',
      color: 'warning'
    })
    await goToLogin()
    return null
  }

  const content = payload.content.trim()

  if (!content) {
    toast.add({
      title: '请输入评论内容',
      color: 'warning'
    })
    return null
  }

  try {
    const response = await siteContent.createPostComment(props.postSlug, {
      content,
      replyCommentId: payload.replyCommentId || null
    })

    toast.add({
      title: response.visible ? '评论已发布' : '评论已提交',
      description: response.message,
      color: 'success'
    })

    return response
  } catch (error: any) {
    toast.add({
      title: '评论发送失败',
      description: error?.message || '评论暂未发送成功，请稍后重试。',
      color: 'error'
    })
    return null
  }
}

/**
 * 提交根评论。
 * 作用：处理顶部主评论输入区的发布动作，并在成功后刷新到最新评论页。
 */
async function handleRootSubmit() {
  isRootSubmitting.value = true

  try {
    const response = await submitComment({
      content: rootContent.value
    })

    if (!response) {
      return
    }

    rootContent.value = ''

    if (response.visible) {
      if (currentPage.value !== 1) {
        currentPage.value = 1
      }
      await loadComments()
    }
  } finally {
    isRootSubmitting.value = false
  }
}

/**
 * 提交楼层回复。
 * 作用：在当前评论下方直接发送回复，成功后保留当前上下文并刷新当前页数据。
 */
async function handleReplySubmit() {
  if (!replyTarget.value) {
    return
  }

  isReplySubmitting.value = true

  try {
    const response = await submitComment({
      content: replyContent.value,
      replyCommentId: replyTarget.value.id
    })

    if (!response) {
      return
    }

    replyContent.value = ''
    replyTarget.value = null

    if (response.visible) {
      await loadComments()
    }
  } finally {
    isReplySubmitting.value = false
  }
}

/**
 * 格式化评论时间。
 * 作用：将接口返回的时间统一转换为更适合前台阅读的中文时间格式。
 *
 * @param value 原始时间值
 * @returns 格式化后的时间文案
 */
function formatCommentTime(value: string) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 生成评论头像占位字符。
 * 作用：在没有头像图片时，用用户名首字符保持评论区的识别感。
 *
 * @param userName 用户名
 * @returns 首字符占位
 */
function getAvatarFallback(userName: string) {
  return (userName || '云').trim().slice(0, 1).toUpperCase()
}

watch(() => props.postSlug, async () => {
  currentPage.value = 1
  replyTarget.value = null
  rootContent.value = ''
  replyContent.value = ''
  await loadComments()
}, { immediate: true })
</script>

<template>
  <section class="rounded-[36px] border border-white/60 bg-white/86 p-5 shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-slate-950/74 sm:p-6">
    <div class="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
      <div>
        <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">评论</p>
        <h2 class="mt-3 text-[clamp(1.55rem,1.35rem+0.56vw,1.95rem)] font-semibold tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
          在这篇文章下继续聊
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-500 dark:text-slate-400">
          {{ commentSummaryText }}。普通用户的新评论会先进入审核队列，通过后才会展示在这里。
        </p>
      </div>

      <div class="rounded-[22px] border border-slate-200/80 bg-white/78 px-4 py-3 text-sm text-slate-500 shadow-[0_16px_40px_-36px_rgba(15,23,42,0.24)] dark:border-white/10 dark:bg-slate-900/70 dark:text-slate-300">
        <p class="font-medium text-slate-900 dark:text-slate-50">
          {{ isLoggedIn ? `当前登录：${auth.currentUser?.userName || auth.currentUser?.email}` : '未登录用户' }}
        </p>
        <p class="mt-1 text-xs leading-6 text-slate-500 dark:text-slate-400">
          {{ props.allowComment ? '登录后即可评论或回复他人。' : '作者当前关闭了这篇文章的评论功能。' }}
        </p>
      </div>
    </div>

    <div class="mt-8">
      <CommentComposer
        v-model="rootContent"
        title="发表一条评论"
        :placeholder="rootCommentPlaceholder"
        submit-label="发布评论"
        :helper-text="rootHelperText"
        :loading="isRootSubmitting"
        :disabled="!props.allowComment"
        :show-login-button="!isLoggedIn"
        @submit="handleRootSubmit"
        @login="goToLogin"
      />
    </div>

    <div class="mt-8">
      <div v-if="isLoading" class="space-y-4">
        <USkeleton
          v-for="index in 3"
          :key="index"
          class="h-32 rounded-[24px]"
        />
      </div>

      <div v-else-if="comments.length" class="space-y-4">
        <article
          v-for="comment in comments"
          :key="comment.id"
          class="rounded-[28px] border border-slate-200/80 bg-white/88 p-4 shadow-[0_20px_54px_-42px_rgba(15,23,42,0.16)] dark:border-white/10 dark:bg-slate-900/72 sm:p-5"
        >
          <div class="flex items-start gap-3">
            <div class="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#38bdf8,#fb923c)] text-sm font-semibold text-white shadow-[0_16px_30px_-18px_rgba(14,165,233,0.42)]">
              {{ getAvatarFallback(comment.author.userName) }}
            </div>

            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ comment.author.userName }}</p>
                <p class="text-xs text-slate-400 dark:text-slate-500">{{ formatCommentTime(comment.createdTime) }}</p>
              </div>

              <p class="mt-3 whitespace-pre-wrap text-sm leading-7 text-slate-600 dark:text-slate-300">
                {{ comment.content }}
              </p>

              <div class="mt-3 flex items-center gap-3">
                <button
                  v-if="props.allowComment"
                  type="button"
                  class="inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium text-sky-600 transition hover:bg-sky-50 hover:text-sky-700 dark:text-sky-300 dark:hover:bg-sky-400/10 dark:hover:text-sky-200"
                  @click="startReply(comment)"
                >
                  <UIcon name="i-lucide-reply" class="size-3.5" />
                  回复
                </button>
              </div>
            </div>
          </div>

          <div v-if="replyTarget?.id === comment.id" class="mt-4 pl-14">
            <CommentComposer
              v-model="replyContent"
              :title="`回复 @${comment.author.userName}`"
              :placeholder="replyCommentPlaceholder"
              submit-label="发送回复"
              :helper-text="replyHelperText"
              :loading="isReplySubmitting"
              :disabled="!props.allowComment"
              compact
              show-cancel
              :show-login-button="!isLoggedIn"
              @submit="handleReplySubmit"
              @cancel="cancelReply"
              @login="goToLogin"
            />
          </div>

          <div v-if="comment.replies.length" class="mt-5 space-y-3 border-t border-slate-200/75 pt-4 dark:border-white/10">
            <article
              v-for="reply in comment.replies"
              :key="reply.id"
              class="rounded-[22px] border border-slate-200/75 bg-slate-50/88 p-4 dark:border-slate-800 dark:bg-slate-950/68"
            >
              <div class="flex items-start gap-3">
                <div class="flex size-9 shrink-0 items-center justify-center rounded-xl bg-[linear-gradient(135deg,#0ea5e9,#14b8a6)] text-xs font-semibold text-white">
                  {{ getAvatarFallback(reply.author.userName) }}
                </div>

                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ reply.author.userName }}</p>
                    <p v-if="reply.replyToUserName" class="text-xs text-sky-600 dark:text-sky-300">
                      回复 @{{ reply.replyToUserName }}
                    </p>
                    <p class="text-xs text-slate-400 dark:text-slate-500">{{ formatCommentTime(reply.createdTime) }}</p>
                  </div>

                  <p class="mt-2 whitespace-pre-wrap text-sm leading-7 text-slate-600 dark:text-slate-300">
                    {{ reply.content }}
                  </p>

                  <div class="mt-3">
                    <button
                      v-if="props.allowComment"
                      type="button"
                      class="inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium text-sky-600 transition hover:bg-sky-50 hover:text-sky-700 dark:text-sky-300 dark:hover:bg-sky-400/10 dark:hover:text-sky-200"
                      @click="startReply(reply)"
                    >
                      <UIcon name="i-lucide-reply" class="size-3.5" />
                      回复
                    </button>
                  </div>
                </div>
              </div>

              <div v-if="replyTarget?.id === reply.id" class="mt-4 pl-12">
                <CommentComposer
                  v-model="replyContent"
                  :title="`回复 @${reply.author.userName}`"
                  :placeholder="replyCommentPlaceholder"
                  submit-label="发送回复"
                  :helper-text="replyHelperText"
                  :loading="isReplySubmitting"
                  :disabled="!props.allowComment"
                  compact
                  show-cancel
                  :show-login-button="!isLoggedIn"
                  @submit="handleReplySubmit"
                  @cancel="cancelReply"
                  @login="goToLogin"
                />
              </div>
            </article>
          </div>
        </article>

        <FrontPaginationBar
          :page-no="currentPage"
          :total-pages="totalPages"
          @change="handlePageChange"
        />
      </div>

      <div
        v-else
        class="rounded-[28px] border border-dashed border-slate-200/85 bg-slate-50/72 px-6 py-10 text-center dark:border-white/10 dark:bg-slate-900/52"
      >
        <div class="mx-auto flex size-14 items-center justify-center rounded-[18px] bg-white text-sky-600 shadow-[0_18px_32px_-24px_rgba(14,165,233,0.35)] dark:bg-white/6 dark:text-sky-300">
          <UIcon name="i-lucide-messages-square" class="size-6" />
        </div>
        <p class="mt-4 text-base font-semibold text-slate-900 dark:text-slate-50">还没有公开评论</p>
        <p class="mt-2 text-sm leading-7 text-slate-500 dark:text-slate-400">
          {{ props.allowComment ? '如果你刚提交了评论，它可能正在等待审核。' : '这篇文章当前未开放评论互动。' }}
        </p>
      </div>
    </div>
  </section>
</template>
