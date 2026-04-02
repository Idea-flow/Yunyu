<script setup lang="ts">
import type { CommentEmojiItem } from '../../constants/commentEmoji'
import { parseCommentRichContent } from '../../utils/commentEmoji'

/**
 * 评论富表情编辑器组件。
 * 作用：在保持评论正文仍以纯文本编码存储的前提下，
 * 为输入过程提供可视化表情 token 预览能力，避免输入框中直接暴露原始编码。
 */
const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
  disabled?: boolean
  compact?: boolean
}>(), {
  modelValue: '',
  placeholder: '写点什么吧',
  disabled: false,
  compact: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editorRef = ref<HTMLDivElement | null>(null)
const savedSelectionRange = ref<Range | null>(null)

/**
 * 计算编辑器最小高度。
 * 作用：根据普通评论和回复评论两种场景保持稳定的输入区高度。
 */
const minHeightStyle = computed(() => ({
  minHeight: props.compact ? '132px' : '164px'
}))

/**
 * 生成表情 token 节点。
 * 作用：把评论正文中的表情编码转换为编辑器内可视化展示的不可拆分节点。
 *
 * @param emoji 表情项
 * @returns 表情 token 元素
 */
function createEmojiTokenElement(emoji: CommentEmojiItem) {
  const tokenElement = document.createElement('span')
  tokenElement.className = 'comment-emoji-token'
  tokenElement.setAttribute('data-emoji-code', emoji.code)
  tokenElement.setAttribute('contenteditable', 'false')

  const imageElement = document.createElement('img')
  imageElement.src = emoji.src
  imageElement.alt = emoji.name
  imageElement.className = 'comment-emoji-token__image'

  const labelElement = document.createElement('span')
  labelElement.className = 'comment-emoji-token__label'
  labelElement.textContent = emoji.name

  tokenElement.append(imageElement, labelElement)
  return tokenElement
}

/**
 * 将评论正文渲染到编辑器中。
 * 作用：把原始文本编码转换为文本节点和表情 token，保证编辑态与展示态一致。
 *
 * @param value 原始评论内容
 */
function renderEditorContent(value: string) {
  if (!editorRef.value) {
    return
  }

  editorRef.value.innerHTML = ''

  const fragment = document.createDocumentFragment()
  const segments = parseCommentRichContent(value)

  segments.forEach((segment) => {
    if (segment.type === 'text') {
      fragment.append(document.createTextNode(segment.value))
      return
    }

    fragment.append(createEmojiTokenElement(segment.value))
  })

  editorRef.value.append(fragment)
}

/**
 * 序列化节点数组。
 * 作用：将编辑器 DOM 重新还原成评论真实存储的文本编码。
 *
 * @param nodes 节点数组
 * @returns 还原后的评论文本
 */
function serializeNodes(nodes: Node[]) {
  return nodes.map((node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      return node.textContent || ''
    }

    if (!(node instanceof HTMLElement)) {
      return ''
    }

    if (node.dataset.emojiCode) {
      return node.dataset.emojiCode
    }

    if (node.tagName === 'BR') {
      return '\n'
    }

    return serializeNodes(Array.from(node.childNodes))
  }).join('')
}

/**
 * 获取编辑器当前原始值。
 * 作用：统一从编辑器 DOM 中提取真实评论内容，供输入同步和表情插入后回写。
 *
 * @returns 原始评论值
 */
function getEditorRawValue() {
  if (!editorRef.value) {
    return ''
  }

  return serializeNodes(Array.from(editorRef.value.childNodes))
}

/**
 * 获取当前光标选区。
 * 作用：统一在键盘输入、表情插入和删除操作中复用当前选区。
 *
 * @returns 当前选区
 */
function getCurrentSelectionRange() {
  const selection = window.getSelection()

  if (!selection || selection.rangeCount === 0) {
    return null
  }

  const currentRange = selection.getRangeAt(0)

  if (!editorRef.value?.contains(currentRange.startContainer)) {
    return null
  }

  return currentRange
}

/**
 * 记录当前光标选区。
 * 作用：在点击表情按钮导致编辑器失焦后，仍然能够把表情插回用户刚才输入的位置。
 */
function rememberSelectionRange() {
  const currentRange = getCurrentSelectionRange()

  if (!currentRange || !editorRef.value?.contains(currentRange.startContainer)) {
    return
  }

  savedSelectionRange.value = currentRange.cloneRange()
}

/**
 * 将光标移动到指定节点后方。
 * 作用：在插入表情 token 后让用户能够继续自然输入文本。
 *
 * @param targetNode 目标节点
 */
function placeCaretAfterNode(targetNode: Node) {
  const selection = window.getSelection()

  if (!selection) {
    return
  }

  const range = document.createRange()
  range.setStartAfter(targetNode)
  range.collapse(true)
  selection.removeAllRanges()
  selection.addRange(range)
}

/**
 * 同步编辑器值到父组件。
 * 作用：在输入、删除和粘贴后把最新原始文本透传给评论表单。
 */
function syncEditorValue() {
  rememberSelectionRange()
  emit('update:modelValue', getEditorRawValue())
}

/**
 * 处理编辑器输入。
 * 作用：监听 contenteditable 的普通输入行为，并将其同步回评论草稿。
 */
function handleInput() {
  syncEditorValue()
}

/**
 * 处理纯文本换行。
 * 作用：拦截回车默认行为，避免浏览器插入段落标签破坏编辑器结构。
 *
 * @param event 键盘事件
 */
function handleEnter(event: KeyboardEvent) {
  event.preventDefault()
  document.execCommand('insertText', false, '\n')
  syncEditorValue()
}

/**
 * 处理纯文本粘贴。
 * 作用：将粘贴内容降级为纯文本，避免外部 HTML 结构注入编辑器。
 *
 * @param event 粘贴事件
 */
function handlePaste(event: ClipboardEvent) {
  event.preventDefault()

  const pastedText = event.clipboardData?.getData('text/plain') || ''
  document.execCommand('insertText', false, pastedText)
  syncEditorValue()
}

/**
 * 查找相邻表情 token。
 * 作用：在退格和删除时识别光标前后的表情节点，支持像普通字符一样移除表情。
 *
 * @param direction 查找方向
 * @returns 相邻表情节点
 */
function findAdjacentEmojiToken(direction: 'previous' | 'next') {
  const range = getCurrentSelectionRange()

  if (!range || !range.collapsed) {
    return null
  }

  const container = range.startContainer
  const offset = range.startOffset

  if (container.nodeType === Node.TEXT_NODE) {
    const textNode = container as Text

    if (direction === 'previous' && offset === 0) {
      const previousNode = textNode.previousSibling
      return previousNode instanceof HTMLElement && previousNode.dataset.emojiCode ? previousNode : null
    }

    if (direction === 'next' && offset === textNode.data.length) {
      const nextNode = textNode.nextSibling
      return nextNode instanceof HTMLElement && nextNode.dataset.emojiCode ? nextNode : null
    }
  }

  if (container instanceof HTMLElement) {
    const siblingIndex = direction === 'previous' ? offset - 1 : offset
    const targetNode = container.childNodes[siblingIndex]
    return targetNode instanceof HTMLElement && targetNode.dataset.emojiCode ? targetNode : null
  }

  return null
}

/**
 * 处理表情节点删除。
 * 作用：让用户在光标位于表情前后时，可以通过退格或删除键自然移除整个表情 token。
 *
 * @param event 键盘事件
 */
function handleEmojiDeletion(event: KeyboardEvent) {
  if (event.key !== 'Backspace' && event.key !== 'Delete') {
    return
  }

  const targetEmojiToken = findAdjacentEmojiToken(event.key === 'Backspace' ? 'previous' : 'next')

  if (!targetEmojiToken) {
    return
  }

  event.preventDefault()
  targetEmojiToken.remove()
  syncEditorValue()
}

/**
 * 聚焦编辑器。
 * 作用：供父组件在插入表情或切换交互状态后把输入焦点拉回编辑器。
 */
function focusEditor() {
  editorRef.value?.focus()
}

/**
 * 插入表情 token。
 * 作用：将选中的自定义表情插入当前光标位置，并同步回原始编码文本。
 *
 * @param emoji 当前表情项
 */
function insertEmoji(emoji: CommentEmojiItem) {
  if (props.disabled || !editorRef.value) {
    return
  }

  const selectionRange = getCurrentSelectionRange() || savedSelectionRange.value
  const tokenElement = createEmojiTokenElement(emoji)

  if (!selectionRange) {
    focusEditor()
    editorRef.value.append(tokenElement)
    placeCaretAfterNode(tokenElement)
    rememberSelectionRange()
    syncEditorValue()
    return
  }

  focusEditor()
  selectionRange.deleteContents()
  selectionRange.insertNode(tokenElement)
  placeCaretAfterNode(tokenElement)
  rememberSelectionRange()
  syncEditorValue()
}

watch(() => props.modelValue, (nextValue) => {
  if (nextValue !== getEditorRawValue()) {
    renderEditorContent(nextValue)
  }
})

onMounted(() => {
  renderEditorContent(props.modelValue)
})

defineExpose({
  insertEmoji,
  focusEditor
})
</script>

<template>
  <div
    ref="editorRef"
    :contenteditable="!props.disabled"
    :data-placeholder="props.placeholder"
    class="comment-rich-editor w-full whitespace-pre-wrap break-words rounded-[18px] border border-slate-200/65 bg-white/84 px-4 py-3 text-sm leading-7 text-slate-700 outline-none transition focus:border-sky-300 focus:ring-2 focus:ring-sky-200/50 dark:border-slate-700/80 dark:bg-slate-950/62 dark:text-slate-100 dark:focus:border-sky-600 dark:focus:ring-sky-500/16"
    :class="props.compact ? 'rounded-[16px]' : 'rounded-[18px]'"
    :style="minHeightStyle"
    @input="handleInput"
    @keydown.enter="handleEnter"
    @keydown="handleEmojiDeletion"
    @keyup="rememberSelectionRange"
    @mouseup="rememberSelectionRange"
    @focus="rememberSelectionRange"
    @paste="handlePaste"
  />
</template>

<style scoped>
.comment-rich-editor:empty::before {
  content: attr(data-placeholder);
  color: rgb(148 163 184);
  pointer-events: none;
}

.comment-rich-editor[contenteditable='false'] {
  cursor: not-allowed;
  opacity: 0.72;
}

.comment-rich-editor :deep(.comment-emoji-token) {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  margin: 0 0.15rem;
  padding: 0.15rem 0.5rem 0.15rem 0.35rem;
  border: 1px solid rgb(226 232 240 / 0.9);
  border-radius: 999px;
  background: rgb(248 250 252 / 0.92);
  vertical-align: -0.2em;
}

.comment-rich-editor :deep(.comment-emoji-token__image) {
  width: 1.15rem;
  height: 1.15rem;
  object-fit: contain;
}

.comment-rich-editor :deep(.comment-emoji-token__label) {
  font-size: 0.75rem;
  line-height: 1;
  color: rgb(100 116 139);
  white-space: nowrap;
}

:global(.dark) .comment-rich-editor :deep(.comment-emoji-token) {
  border-color: rgb(255 255 255 / 0.1);
  background: rgb(255 255 255 / 0.06);
}

:global(.dark) .comment-rich-editor :deep(.comment-emoji-token__label) {
  color: rgb(148 163 184);
}
</style>
