<script setup lang="ts">
/**
 * 后台 AI 操作场协议类型。
 * 作用：区分 Chat 与 Responses 两套 OpenAI 协议入口。
 */
type PlaygroundProtocol = 'chat' | 'responses'

/**
 * 后台 AI 操作场页面。
 * 作用：提供 OpenAI 兼容请求调试入口，支持流式与非流式结果验证。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const apiClient = useApiClient()
const runtimeConfig = useRuntimeConfig()

const protocol = ref<PlaygroundProtocol>('chat')
const streamEnabled = ref(false)
const requestJson = ref('')
const responseJson = ref('')
const streamRawOutput = ref('')
const streamMergedText = ref('')
const lastRequestPath = ref('')
const isRunning = ref(false)
const abortController = ref<AbortController | null>(null)

const protocolOptions = [
  { label: 'Chat Completions', value: 'chat' },
  { label: 'Responses', value: 'responses' }
] as const

/**
 * 当前协议对应的请求路径。
 * 作用：统一维护协议切换时的 API 端点映射。
 */
const endpointPath = computed(() =>
  protocol.value === 'chat' ? '/v1/chat/completions' : '/v1/response'
)

/**
 * 构建协议示例请求体。
 * 作用：提供可直接发送的最小 OpenAI 兼容示例，降低调试门槛。
 *
 * @param targetProtocol 协议类型
 * @returns 示例请求体
 */
function buildSamplePayload(targetProtocol: PlaygroundProtocol) {
  if (targetProtocol === 'chat') {
    return {
      model: 'gpt-4.1-mini',
      messages: [
        {
          role: 'user',
          content: '请输出一段简短的站点欢迎语。'
        }
      ],
      temperature: 0.4,
      stream: false
    }
  }

  return {
    model: 'gpt-4.1-mini',
    input: '请输出一段简短的站点欢迎语。',
    temperature: 0.4,
    stream: false
  }
}

/**
 * 生成并回填示例请求 JSON。
 *
 * @param targetProtocol 协议类型
 */
function fillSampleRequest(targetProtocol: PlaygroundProtocol) {
  requestJson.value = JSON.stringify(buildSamplePayload(targetProtocol), null, 2)
}

/**
 * 清理输出面板内容。
 * 作用：每次新请求前重置结果区域，避免旧数据干扰判断。
 */
function resetOutputs() {
  responseJson.value = ''
  streamRawOutput.value = ''
  streamMergedText.value = ''
}

/**
 * 解析编辑器中的请求 JSON。
 * 作用：把文本编辑器输入转换为对象，并统一校验 JSON 格式。
 *
 * @returns 请求对象
 */
function parseRequestPayload() {
  try {
    return JSON.parse(requestJson.value || '{}') as Record<string, any>
  } catch {
    throw new Error('请求体 JSON 格式不正确，请先修复后再发送。')
  }
}

/**
 * 提取 Chat 流式分片文本。
 * 作用：兼容 `delta.content` 为字符串或数组两种结构。
 *
 * @param chunk 流式分片
 * @returns 文本增量
 */
function extractChatDeltaText(chunk: any) {
  const firstChoice = Array.isArray(chunk?.choices) ? chunk.choices[0] : null
  const deltaContent = firstChoice?.delta?.content

  if (typeof deltaContent === 'string') {
    return deltaContent
  }

  if (Array.isArray(deltaContent)) {
    return deltaContent
      .map(item => {
        if (typeof item === 'string') {
          return item
        }
        return typeof item?.text === 'string' ? item.text : ''
      })
      .join('')
  }

  return ''
}

/**
 * 提取 Responses 流式分片文本。
 * 作用：兼容常见 delta 事件和输出片段结构，汇总可读文本。
 *
 * @param chunk 流式分片
 * @returns 文本增量
 */
function extractResponsesDeltaText(chunk: any) {
  if (typeof chunk?.delta === 'string') {
    return chunk.delta
  }

  if (typeof chunk?.output_text === 'string') {
    return chunk.output_text
  }

  const outputItems = Array.isArray(chunk?.output) ? chunk.output : []
  const textParts: string[] = []

  for (const outputItem of outputItems) {
    const contentList = Array.isArray(outputItem?.content) ? outputItem.content : []

    for (const contentItem of contentList) {
      if (typeof contentItem?.text === 'string') {
        textParts.push(contentItem.text)
        continue
      }
      if (typeof contentItem?.value === 'string') {
        textParts.push(contentItem.value)
      }
    }
  }

  if (textParts.length) {
    return textParts.join('')
  }

  return ''
}

/**
 * 处理单行 SSE 数据。
 * 作用：记录原始流事件并抽取增量文本到展示区。
 *
 * @param line 原始行
 */
function handleSseLine(line: string) {
  const normalizedLine = line.trimEnd()
  if (!normalizedLine) {
    return
  }

  streamRawOutput.value += `${normalizedLine}\n`

  const dataLine = normalizedLine.startsWith('data:')
    ? normalizedLine.slice(5).trim()
    : normalizedLine

  if (!dataLine || dataLine === '[DONE]') {
    return
  }

  let chunk: any
  try {
    chunk = JSON.parse(dataLine)
  } catch {
    return
  }

  const errorMessage = chunk?.error?.message
  if (typeof errorMessage === 'string' && errorMessage.trim()) {
    throw new Error(errorMessage)
  }

  const deltaText = protocol.value === 'chat'
    ? extractChatDeltaText(chunk)
    : extractResponsesDeltaText(chunk)

  if (deltaText) {
    streamMergedText.value += deltaText
  }
}

/**
 * 执行非流式请求。
 * 作用：调用 OpenAI 兼容接口并展示完整 JSON 响应。
 *
 * @param payload 请求体
 */
async function runNonStream(payload: Record<string, any>) {
  const response = await apiClient.rawRequest<Record<string, any>>(endpointPath.value, {
    method: 'POST',
    body: payload
  })
  responseJson.value = JSON.stringify(response, null, 2)
}

/**
 * 执行流式请求。
 * 作用：通过 Fetch 读取 SSE 字节流，实时展示原始事件与增量文本。
 *
 * @param payload 请求体
 */
async function runStream(payload: Record<string, any>) {
  const authToken = apiClient.hydratePersistedAccessToken() || apiClient.accessToken.value || ''
  const controller = new AbortController()
  abortController.value = controller

  const response = await fetch(`${runtimeConfig.public.apiBase}${endpointPath.value}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {})
    },
    body: JSON.stringify(payload),
    signal: controller.signal
  })

  if (!response.ok) {
    let message = `流式请求失败，HTTP 状态码：${response.status}`
    try {
      const errorBody = await response.json() as any
      message = errorBody?.error?.message || errorBody?.message || message
    } catch {
      // 忽略解析失败，保留默认提示。
    }
    throw new Error(message)
  }

  if (!response.body) {
    throw new Error('流式响应体为空，无法读取。')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()

    if (done) {
      break
    }

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split(/\r?\n/)
    buffer = lines.pop() || ''

    for (const line of lines) {
      handleSseLine(line)
    }
  }

  buffer += decoder.decode()
  if (buffer.trim()) {
    handleSseLine(buffer)
  }
}

/**
 * 提交请求。
 * 作用：统一处理请求体解析、stream 覆盖和协议发送流程。
 */
async function runRequest() {
  if (isRunning.value) {
    return
  }

  isRunning.value = true
  resetOutputs()

  try {
    const payload = parseRequestPayload()
    payload.stream = streamEnabled.value

    lastRequestPath.value = endpointPath.value

    if (streamEnabled.value) {
      await runStream(payload)
    } else {
      await runNonStream(payload)
    }

    toast.add({
      title: '请求完成',
      color: 'success'
    })
  } catch (error: any) {
    const isAbortError = error?.name === 'AbortError'

    if (isAbortError) {
      toast.add({
        title: '流式请求已停止',
        color: 'warning'
      })
      return
    }

    toast.add({
      title: '请求失败',
      description: error?.message || 'AI 请求执行失败，请稍后重试。',
      color: 'error'
    })
  } finally {
    abortController.value = null
    isRunning.value = false
  }
}

/**
 * 停止流式请求。
 * 作用：中断当前 SSE 读取，便于快速结束长响应。
 */
function stopStreaming() {
  abortController.value?.abort()
}

/**
 * 清空请求与输出。
 * 作用：快速恢复到当前协议的初始调试状态。
 */
function resetPlayground() {
  fillSampleRequest(protocol.value)
  resetOutputs()
  lastRequestPath.value = ''
}

watch(protocol, (value) => {
  fillSampleRequest(value)
  resetOutputs()
  lastRequestPath.value = ''
})

onMounted(() => {
  fillSampleRequest(protocol.value)
})
</script>

<template>
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">AI 操作场</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
            调试 `/v1/chat/completions` 与 `/v1/response` 的流式和非流式请求。
          </p>
        </div>

        <div class="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto sm:flex-nowrap">
          <AdminButton
            icon="i-lucide-sparkles"
            label="填充示例"
            tone="neutral"
            variant="outline"
            size="sm"
            @click="fillSampleRequest(protocol)"
          />
          <AdminButton
            icon="i-lucide-rotate-ccw"
            label="重置"
            tone="neutral"
            variant="outline"
            size="sm"
            @click="resetPlayground"
          />
          <AdminPrimaryButton
            :loading="isRunning"
            icon="i-lucide-send"
            label="发送请求"
            loading-label="请求中..."
            @click="runRequest"
          />
          <AdminButton
            v-if="streamEnabled && isRunning"
            icon="i-lucide-square"
            label="停止"
            tone="neutral"
            variant="outline"
            size="sm"
            @click="stopStreaming"
          />
        </div>
      </div>
    </section>

    <section class="grid gap-4 rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)] lg:p-5">
      <div class="grid gap-4 md:grid-cols-3">
        <div class="space-y-2">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300">协议</p>
          <AdminSelect
            v-model="protocol"
            :items="protocolOptions"
            placeholder="请选择协议"
          />
        </div>

        <div class="space-y-2">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300">返回模式</p>
          <div class="rounded-[12px] border border-slate-200/80 bg-white/80 p-3 dark:border-white/10 dark:bg-white/[0.03]">
            <AdminToggleButton
              v-model="streamEnabled"
              tone="info"
              active-label="流式"
              inactive-label="非流式"
            />
          </div>
        </div>

        <div class="space-y-2">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300">请求端点</p>
          <AdminInput :model-value="endpointPath" readonly />
        </div>
      </div>

      <div class="mt-4 space-y-2">
        <div class="flex items-center justify-between gap-2">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300">请求 JSON</p>
          <p class="text-xs text-slate-400 dark:text-slate-500">
            实际发送时会自动覆盖 `stream = {{ streamEnabled }}`
          </p>
        </div>
        <AdminTextarea
          v-model="requestJson"
          :rows="16"
          placeholder="请输入 OpenAI 协议请求 JSON"
          class="font-mono text-xs leading-5"
        />
      </div>
    </section>

    <section class="space-y-4 rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)] lg:p-5">
      <div class="flex flex-wrap items-center justify-between gap-2">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">响应结果</h2>
        <p class="text-xs text-slate-500 dark:text-slate-400">
          {{ lastRequestPath ? `最近请求：${lastRequestPath}` : '尚未发送请求' }}
        </p>
      </div>

      <div v-if="!streamEnabled" class="space-y-2">
        <p class="text-sm font-medium text-slate-700 dark:text-slate-300">非流式 JSON</p>
        <pre class="max-h-[560px] overflow-auto rounded-[14px] border border-slate-200/80 bg-slate-950/95 p-3 text-xs leading-5 text-slate-100 dark:border-white/10">{{ responseJson || '等待请求结果...' }}</pre>
      </div>

      <div v-else class="grid gap-4 xl:grid-cols-2">
        <div class="space-y-2">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300">流式拼接文本</p>
          <pre class="max-h-[560px] overflow-auto whitespace-pre-wrap break-words rounded-[14px] border border-slate-200/80 bg-slate-950/95 p-3 text-xs leading-5 text-slate-100 dark:border-white/10">{{ streamMergedText || '等待流式增量...' }}</pre>
        </div>

        <div class="space-y-2">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300">原始 SSE 事件</p>
          <pre class="max-h-[560px] overflow-auto whitespace-pre-wrap break-words rounded-[14px] border border-slate-200/80 bg-slate-950/95 p-3 text-xs leading-5 text-slate-100 dark:border-white/10">{{ streamRawOutput || '等待流式事件...' }}</pre>
        </div>
      </div>
    </section>
  </div>
</template>
