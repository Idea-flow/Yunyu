<script setup lang="ts">
/**
 * 云屿暗色粒子树组件。
 * 作用：保留首页暗黑模式原有的东方树影装饰效果，
 * 让暗色主题继续使用之前那套更克制的树形生长与呼吸动画。
 */
interface YunyuParticleTreeProps {
  treeAreaRatio?: number
  maxTreeWidth?: number
  minTreeWidth?: number
  anchorOffsetX?: number
}

interface TreeBranch {
  startX: number
  startY: number
  controlX: number
  controlY: number
  endX: number
  endY: number
  width: number
  depth: number
  growStart: number
  growDuration: number
  swayPhase: number
  swayAmplitude: number
}

interface TreeLeaf {
  x: number
  y: number
  radius: number
  revealAt: number
  driftPhase: number
  driftAmplitude: number
}

interface TreeParticle {
  x: number
  y: number
  radius: number
  alpha: number
  driftX: number
  driftY: number
  phase: number
  speed: number
}

interface TreeScene {
  branches: TreeBranch[]
  leaves: TreeLeaf[]
  particles: TreeParticle[]
  areaLeft: number
  areaWidth: number
}

interface TreePalette {
  branch: string
  branchGlow: string
  leaf: string
  leafSoft: string
  dust: string
}

interface TreeRenderProfile {
  branchGlowBaseAlpha: number
  branchGlowDepthAlpha: number
  branchShadowBlur: number
  branchShadowAlpha: number
  branchBaseAlpha: number
  branchDepthAlpha: number
  branchHighlightAlpha: number
  leafBaseAlpha: number
  leafDepthAlpha: number
  leafSoftBaseAlpha: number
  leafSoftDepthAlpha: number
  particleAlphaScale: number
}

interface TreeCanvasStage {
  stageWidth: number
  canvasWidth: number
  canvasLeft: number
  paddingLeft: number
  paddingRight: number
}

const rootRef = ref<HTMLDivElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const prefersReducedMotion = ref(false)
const props = withDefaults(defineProps<YunyuParticleTreeProps>(), {
  treeAreaRatio: 0.42,
  maxTreeWidth: 760,
  minTreeWidth: 320,
  anchorOffsetX: 0
})

const ENTRANCE_DURATION = 5600
const TREE_SEED = 20260412
const ENTRANCE_FRAME_INTERVAL = 1000 / 24
const STEADY_FRAME_INTERVAL = 1000 / 18
const TREE_DYNAMIC_PROFILE: TreeRenderProfile = {
  branchGlowBaseAlpha: 0.014,
  branchGlowDepthAlpha: 0.028,
  branchShadowBlur: 6,
  branchShadowAlpha: 0.06,
  branchBaseAlpha: 0.145,
  branchDepthAlpha: 0.105,
  branchHighlightAlpha: 0.008,
  leafBaseAlpha: 0.042,
  leafDepthAlpha: 0.06,
  leafSoftBaseAlpha: 0.024,
  leafSoftDepthAlpha: 0.036,
  particleAlphaScale: 0.42
}
const TREE_STATIC_PROFILE: TreeRenderProfile = {
  branchGlowBaseAlpha: 0.004,
  branchGlowDepthAlpha: 0.009,
  branchShadowBlur: 3,
  branchShadowAlpha: 0.018,
  branchBaseAlpha: 0.088,
  branchDepthAlpha: 0.065,
  branchHighlightAlpha: 0.003,
  leafBaseAlpha: 0.016,
  leafDepthAlpha: 0.026,
  leafSoftBaseAlpha: 0.011,
  leafSoftDepthAlpha: 0.02,
  particleAlphaScale: 0.22
}

let animationFrameId: number | null = null
let reducedMotionMediaQuery: MediaQueryList | null = null
let canvasContext: CanvasRenderingContext2D | null = null
let treeScene: TreeScene | null = null
let treePalette: TreePalette | null = null
let cachedTreeCanvas: HTMLCanvasElement | null = null
let sceneWidth = 0
let sceneHeight = 0
let animationStartedAt = 0
let hasPlayedEntrance = false
let lastFrameTimestamp = 0
let isTreeVisible = true
let intersectionObserver: IntersectionObserver | null = null

/**
 * 创建可复现的随机数生成器。
 * 作用：让树的枝干分布和粒子布局保持稳定，避免每次渲染都出现完全不同的形状。
 *
 * @param seed 随机种子
 * @returns 伪随机数函数
 */
function createSeededRandom(seed: number) {
  let currentSeed = seed >>> 0

  return () => {
    currentSeed = (currentSeed * 1664525 + 1013904223) >>> 0
    return currentSeed / 4294967296
  }
}

/**
 * 将数值限制在指定区间。
 * 作用：统一处理动画进度和局部插值，避免计算结果超出有效范围。
 *
 * @param value 当前值
 * @param min 最小值
 * @param max 最大值
 * @returns 限制后的结果
 */
function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max)
}

/**
 * 计算缓出曲线。
 * 作用：让树木生长前快后慢，呈现更自然的伸展感而不是机械匀速。
 *
 * @param value 当前线性进度
 * @returns 缓出后的进度
 */
function easeOutCubic(value: number) {
  return 1 - (1 - value) ** 3
}

/**
 * 同步“减少动态效果”系统偏好。
 * 作用：尊重用户系统设置，在偏好静态时自动切换为静态树形展示。
 */
function syncReducedMotionPreference() {
  prefersReducedMotion.value = Boolean(reducedMotionMediaQuery?.matches)
}

/**
 * 解析当前主题下的树形配色。
 * 作用：从组件根节点读取 CSS 变量，让当前暗色组件继续沿用之前的配色表现。
 *
 * @returns 当前主题对应的画布颜色配置
 */
function resolveTreePalette(): TreePalette {
  const styles = rootRef.value ? window.getComputedStyle(rootRef.value) : null

  return {
    branch: styles?.getPropertyValue('--yy-tree-branch-rgb').trim() || '138, 149, 164',
    branchGlow: styles?.getPropertyValue('--yy-tree-branch-glow-rgb').trim() || '77, 133, 190',
    leaf: styles?.getPropertyValue('--yy-tree-leaf-rgb').trim() || '110, 121, 136',
    leafSoft: styles?.getPropertyValue('--yy-tree-leaf-soft-rgb').trim() || '76, 90, 108',
    dust: styles?.getPropertyValue('--yy-tree-dust-rgb').trim() || '124, 137, 153'
  }
}

/**
 * 生成树形场景数据。
 * 作用：根据当前容器尺寸计算枝干、叶粒和漂浮粒子，
 * 让树可以在不同屏幕下保持类似的轮廓与比例。
 *
 * @param width 画布宽度
 * @param height 画布高度
 * @param paddingLeft 画布左侧留白
 * @param paddingRight 画布右侧留白
 * @returns 树形场景数据
 */
function createTreeScene(width: number, height: number, paddingLeft: number, paddingRight: number): TreeScene {
  const random = createSeededRandom(TREE_SEED + Math.round(width) * 3 + Math.round(height) * 7)
  const branches: TreeBranch[] = []
  const leaves: TreeLeaf[] = []
  const particles: TreeParticle[] = []
  const maxDepth = width < 520 ? 4 : 5
  const areaLeft = paddingLeft
  const areaWidth = Math.max(width - paddingLeft - paddingRight, 220)
  const originX = areaLeft + areaWidth * 0.04
  const originY = height * 0.62

  /**
   * 递归添加树枝。
   * 作用：沿横向主干逐层派生分支，形成更偏东方松树的舒展轮廓。
   *
   * @param startX 起点横坐标
   * @param startY 起点纵坐标
   * @param length 分支长度
   * @param angle 分支角度
   * @param lineWidth 分支宽度
   * @param depth 当前深度
   * @param startProgress 分支开始生长的全局进度
   */
  function appendBranch(
    startX: number,
    startY: number,
    length: number,
    angle: number,
    lineWidth: number,
    depth: number,
    startProgress: number
  ) {
    const endX = startX + Math.cos(angle) * length
    const endY = startY + Math.sin(angle) * length
    const growDuration = clamp(0.19 - depth * 0.018 + random() * 0.03, 0.08, 0.22)
    const midX = startX + (endX - startX) * 0.5
    const midY = startY + (endY - startY) * 0.5
    const normalX = -Math.sin(angle)
    const normalY = Math.cos(angle)
    const bendOffset = (depth === 0 ? -length * 0.08 : 0) + (random() - 0.5) * length * 0.18
    const controlX = midX + normalX * bendOffset
    const controlY = midY + normalY * bendOffset

    branches.push({
      startX,
      startY,
      controlX,
      controlY,
      endX,
      endY,
      width: lineWidth,
      depth,
      growStart: clamp(startProgress, 0, 0.92),
      growDuration,
      swayPhase: random() * Math.PI * 2,
      swayAmplitude: Math.max(0.12, (maxDepth - depth + 1) * 0.12)
    })

    if (depth >= maxDepth || length < Math.max(areaWidth * 0.08, 24)) {
      const clusterCount = width < 520 ? 3 : 5

      for (let index = 0; index < clusterCount; index += 1) {
        leaves.push({
          x: endX + (random() - 0.5) * length * 0.34,
          y: endY + (random() - 0.5) * length * 0.22,
          radius: 0.72 + random() * 1.28,
          revealAt: clamp(startProgress + growDuration * 0.72 + random() * 0.08, 0.12, 0.98),
          driftPhase: random() * Math.PI * 2,
          driftAmplitude: 0.2 + random() * 0.56
        })
      }

      return
    }

    const childCount = depth <= 1 ? 3 : (random() > 0.42 ? 3 : 2)

    for (let index = 0; index < childCount; index += 1) {
      const direction = childCount === 2
        ? (index === 0 ? -1 : 1)
        : (index === 0 ? -1 : (index === 1 ? 0 : 1))
      const branchBias = direction < 0
        ? -(0.36 + random() * 0.18)
        : (direction > 0 ? 0.22 + random() * 0.12 : -(0.08 + random() * 0.08))
      const childAngle = angle + branchBias + (random() - 0.5) * 0.14
      const childLength = length * (0.62 + random() * 0.12)
      const childWidth = Math.max(lineWidth * (0.64 + random() * 0.06), 0.9)
      const childStartRatio = depth === 0 ? 0.28 + random() * 0.26 : 0.36 + random() * 0.3
      const childStartX = startX + (endX - startX) * childStartRatio
      const childStartY = startY + (endY - startY) * childStartRatio

      appendBranch(
        childStartX,
        childStartY,
        childLength,
        childAngle,
        childWidth,
        depth + 1,
        startProgress + growDuration * (0.32 + random() * 0.18)
      )
    }
  }

  appendBranch(
    originX,
    originY,
    Math.min(areaWidth, height) * 0.58,
    -0.08,
    Math.max(areaWidth * 0.015, 5),
    0,
    0.02
  )

  const particleCount = width < 520 ? 4 : 7

  for (let index = 0; index < particleCount; index += 1) {
    particles.push({
      x: areaLeft + areaWidth * (0.22 + random() * 0.62),
      y: height * (0.22 + random() * 0.42),
      radius: 0.45 + random() * 0.92,
      alpha: 0.06 + random() * 0.09,
      driftX: 1.4 + random() * 4.2,
      driftY: 1.1 + random() * 3.4,
      phase: random() * Math.PI * 2,
      speed: 0.14 + random() * 0.22
    })
  }

  return {
    branches,
    leaves,
    particles,
    areaLeft,
    areaWidth
  }
}

/**
 * 构建静态树身缓存画布。
 * 作用：在入场动画结束后复用缓存的枝干与叶簇位图，
 * 避免每一帧都重新绘制整棵树，只保留少量粒子继续运动。
 *
 * @param pixelRatio 当前设备像素比
 */
function rebuildTreeCache(pixelRatio: number) {
  if (!import.meta.client || !treeScene || !treePalette) {
    return
  }

  const cacheCanvas = document.createElement('canvas')
  cacheCanvas.width = Math.round(sceneWidth * pixelRatio)
  cacheCanvas.height = Math.round(sceneHeight * pixelRatio)

  const cacheContext = cacheCanvas.getContext('2d')

  if (!cacheContext) {
    cachedTreeCanvas = null
    return
  }

  cacheContext.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
  cacheContext.lineCap = 'round'
  cacheContext.lineJoin = 'round'
  cacheContext.clearRect(0, 0, sceneWidth, sceneHeight)

  treeScene.branches.forEach(branch => {
    drawTreeBranch(cacheContext, branch, 1, 0, false, TREE_STATIC_PROFILE)
  })

  treeScene.leaves.forEach(leaf => {
    drawTreeLeaf(cacheContext, leaf, 1, 0, false, TREE_STATIC_PROFILE)
  })

  cachedTreeCanvas = cacheCanvas
}

/**
 * 绘制静态树身。
 * 作用：在树木入场结束后直接复用缓存位图，让后续帧只需要补充粒子层。
 */
function renderStaticTree() {
  if (!canvasContext) {
    return
  }

  canvasContext.clearRect(0, 0, sceneWidth, sceneHeight)

  if (cachedTreeCanvas) {
    canvasContext.drawImage(cachedTreeCanvas, 0, 0, sceneWidth, sceneHeight)
  }
}

/**
 * 计算粒子树实际绘制画布信息。
 * 作用：将暗色树影限制在首页左侧局部区域，避免整屏 canvas 持续重绘。
 *
 * @param width 首屏容器宽度
 * @returns 粒子树画布尺寸与偏移信息
 */
function resolveCanvasStage(width: number): TreeCanvasStage {
  const stageWidth = clamp(
    width * props.treeAreaRatio,
    Math.min(props.minTreeWidth, width * 0.8),
    Math.min(props.maxTreeWidth, width * 0.84)
  )
  const paddingLeft = 24
  const paddingRight = 84

  return {
    stageWidth,
    canvasWidth: stageWidth + paddingLeft + paddingRight,
    canvasLeft: width * 0.01 + props.anchorOffsetX - paddingLeft,
    paddingLeft,
    paddingRight
  }
}

/**
 * 同步画布尺寸并重建树形场景。
 * 作用：在首次挂载、窗口缩放或主题切换后重新适配当前容器尺寸与配色。
 */
function setupTreeScene() {
  if (!import.meta.client || !rootRef.value || !canvasRef.value) {
    return
  }

  const { width, height } = rootRef.value.getBoundingClientRect()

  if (!width || !height) {
    return
  }

  const { canvasWidth, canvasLeft, paddingLeft, paddingRight } = resolveCanvasStage(width)
  const pixelRatio = Math.min(window.devicePixelRatio || 1, 1.25)
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')

  if (!context) {
    return
  }

  canvas.width = Math.round(canvasWidth * pixelRatio)
  canvas.height = Math.round(height * pixelRatio)
  canvas.style.width = `${canvasWidth}px`
  canvas.style.height = `${height}px`
  canvas.style.left = `${canvasLeft}px`
  canvas.style.top = '0'

  context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
  context.lineCap = 'round'
  context.lineJoin = 'round'

  canvasContext = context
  sceneWidth = canvasWidth
  sceneHeight = height
  treePalette = resolveTreePalette()
  treeScene = createTreeScene(canvasWidth, height, paddingLeft, paddingRight)
  rebuildTreeCache(pixelRatio)
  lastFrameTimestamp = 0

  if (!hasPlayedEntrance) {
    animationStartedAt = 0
  }

  drawTreeFrame(performance.now())

  if (!prefersReducedMotion.value && animationFrameId === null) {
    animationFrameId = window.requestAnimationFrame(drawTreeFrame)
  }
}

/**
 * 停止树形动画循环。
 * 作用：在组件卸载或页面进入后台时释放动画帧资源。
 */
function stopTreeAnimation() {
  if (animationFrameId !== null) {
    window.cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
}

/**
 * 绘制树枝。
 * 作用：按生长进度逐步伸展分支，并通过轻微曲率让树干与枝条更精致自然。
 *
 * @param context 画布上下文
 * @param branch 当前树枝数据
 * @param growthProgress 全局生长进度
 * @param elapsedSeconds 当前已流逝秒数
 * @param enableOrganicMotion 是否启用持续摆动
 * @param renderProfile 当前绘制强度配置
 */
function drawTreeBranch(
  context: CanvasRenderingContext2D,
  branch: TreeBranch,
  growthProgress: number,
  elapsedSeconds: number,
  enableOrganicMotion = true,
  renderProfile: TreeRenderProfile = TREE_DYNAMIC_PROFILE
) {
  if (!treePalette) {
    return
  }

  const localProgress = clamp(
    (growthProgress - branch.growStart) / branch.growDuration,
    0,
    1
  )

  if (localProgress <= 0) {
    return
  }

  const easedProgress = easeOutCubic(localProgress)
  const swayRatio = enableOrganicMotion && growthProgress >= 1 && !prefersReducedMotion.value
    ? Math.sin(elapsedSeconds * 0.6 + branch.swayPhase) * branch.swayAmplitude * (branch.depth + 1)
    : 0
  const controlSwayRatio = swayRatio * 0.42
  const tipX = branch.startX + (branch.endX - branch.startX) * easedProgress + swayRatio
  const tipY = branch.startY + (branch.endY - branch.startY) * easedProgress - swayRatio * 0.12
  const controlX = branch.startX + (branch.controlX - branch.startX) * easedProgress + controlSwayRatio
  const controlY = branch.startY + (branch.controlY - branch.startY) * easedProgress - controlSwayRatio * 0.08

  context.beginPath()
  context.moveTo(branch.startX, branch.startY)
  context.quadraticCurveTo(controlX, controlY, tipX, tipY)
  context.lineWidth = branch.width + 0.36
  context.strokeStyle = `rgba(${treePalette.branchGlow}, ${renderProfile.branchGlowBaseAlpha + (maxDepthOpacity(branch.depth) * renderProfile.branchGlowDepthAlpha)})`
  context.shadowBlur = renderProfile.branchShadowBlur
  context.shadowColor = `rgba(${treePalette.branchGlow}, ${renderProfile.branchShadowAlpha})`
  context.stroke()
  context.shadowBlur = 0

  context.beginPath()
  context.moveTo(branch.startX, branch.startY)
  context.quadraticCurveTo(controlX, controlY, tipX, tipY)
  context.lineWidth = branch.width
  context.strokeStyle = `rgba(${treePalette.branch}, ${renderProfile.branchBaseAlpha + (maxDepthOpacity(branch.depth) * renderProfile.branchDepthAlpha)})`
  context.stroke()

  context.beginPath()
  context.moveTo(branch.startX, branch.startY)
  context.quadraticCurveTo(controlX, controlY, tipX, tipY)
  context.lineWidth = Math.max(branch.width * 0.08, 0.14)
  context.strokeStyle = `rgba(${treePalette.branch}, ${renderProfile.branchHighlightAlpha})`
  context.stroke()
}

/**
 * 根据树枝层级返回透明度补偿。
 * 作用：让主干更稳、更重，末梢分支更轻，整体轮廓层次更自然。
 *
 * @param depth 树枝深度
 * @returns 当前深度对应的透明度权重
 */
function maxDepthOpacity(depth: number) {
  return clamp(1 - depth * 0.12, 0.38, 1)
}

/**
 * 绘制叶粒。
 * 作用：在枝干末梢补充稀疏的叶粒和亮点，让整棵树更有“呼吸感”而不是纯线条骨架。
 *
 * @param context 画布上下文
 * @param leaf 当前叶粒数据
 * @param growthProgress 全局生长进度
 * @param elapsedSeconds 当前已流逝秒数
 * @param enableOrganicMotion 是否启用持续漂移
 * @param renderProfile 当前绘制强度配置
 */
function drawTreeLeaf(
  context: CanvasRenderingContext2D,
  leaf: TreeLeaf,
  growthProgress: number,
  elapsedSeconds: number,
  enableOrganicMotion = true,
  renderProfile: TreeRenderProfile = TREE_DYNAMIC_PROFILE
) {
  if (!treePalette) {
    return
  }

  const localProgress = clamp((growthProgress - leaf.revealAt) / 0.16, 0, 1)

  if (localProgress <= 0) {
    return
  }

  const easedProgress = easeOutCubic(localProgress)
  const driftX = prefersReducedMotion.value || !enableOrganicMotion
    ? 0
    : Math.sin(elapsedSeconds * 0.86 + leaf.driftPhase) * leaf.driftAmplitude
  const driftY = prefersReducedMotion.value || !enableOrganicMotion
    ? 0
    : Math.cos(elapsedSeconds * 0.72 + leaf.driftPhase) * leaf.driftAmplitude * 0.42
  const alpha = renderProfile.leafBaseAlpha + easedProgress * renderProfile.leafDepthAlpha

  context.beginPath()
  context.fillStyle = `rgba(${treePalette.leaf}, ${alpha})`
  context.ellipse(
    leaf.x + driftX,
    leaf.y + driftY,
    leaf.radius * (1 + easedProgress * 0.12),
    leaf.radius * 0.64,
    Math.PI * 0.18,
    0,
    Math.PI * 2
  )
  context.fill()

  context.beginPath()
  context.fillStyle = `rgba(${treePalette.leafSoft}, ${renderProfile.leafSoftBaseAlpha + easedProgress * renderProfile.leafSoftDepthAlpha})`
  context.arc(leaf.x + driftX * 0.7, leaf.y + driftY * 0.7, Math.max(leaf.radius * 0.34, 0.6), 0, Math.PI * 2)
  context.fill()
}

/**
 * 绘制漂浮微粒。
 * 作用：在树冠周围增加极少量缓慢漂浮的微粒，强化首页首屏的空气感和空间深度。
 *
 * @param context 画布上下文
 * @param particle 当前粒子数据
 * @param growthProgress 全局生长进度
 * @param elapsedSeconds 当前已流逝秒数
 * @param renderProfile 当前绘制强度配置
 */
function drawTreeParticle(
  context: CanvasRenderingContext2D,
  particle: TreeParticle,
  growthProgress: number,
  elapsedSeconds: number,
  renderProfile: TreeRenderProfile = TREE_DYNAMIC_PROFILE
) {
  if (!treePalette || growthProgress < 0.68) {
    return
  }

  const visibility = prefersReducedMotion.value
    ? particle.alpha * renderProfile.particleAlphaScale
    : particle.alpha * renderProfile.particleAlphaScale * (0.72 + Math.sin(elapsedSeconds * particle.speed + particle.phase) * 0.16)
  const x = particle.x + (prefersReducedMotion.value ? 0 : Math.sin(elapsedSeconds * particle.speed + particle.phase) * particle.driftX)
  const y = particle.y + (prefersReducedMotion.value ? 0 : Math.cos(elapsedSeconds * particle.speed * 0.8 + particle.phase) * particle.driftY)

  context.beginPath()
  context.fillStyle = `rgba(${treePalette.dust}, ${visibility})`
  context.arc(x, y, particle.radius, 0, Math.PI * 2)
  context.fill()
}

/**
 * 绘制当前帧。
 * 作用：统一清空画布、计算进度并依次渲染雾层、树枝、叶粒与漂浮粒子。
 *
 * @param timestamp 当前动画时间戳
 */
function drawTreeFrame(timestamp: number) {
  if (!canvasContext || !treeScene || !isTreeVisible) {
    return
  }

  if (!animationStartedAt) {
    animationStartedAt = timestamp
  }

  const elapsedSeconds = (timestamp - animationStartedAt) / 1000
  const growthProgress = prefersReducedMotion.value || hasPlayedEntrance
    ? 1
    : clamp((timestamp - animationStartedAt) / ENTRANCE_DURATION, 0, 1)
  const frameInterval = growthProgress >= 1 ? STEADY_FRAME_INTERVAL : ENTRANCE_FRAME_INTERVAL

  if (lastFrameTimestamp && timestamp - lastFrameTimestamp < frameInterval) {
    animationFrameId = window.requestAnimationFrame(drawTreeFrame)
    return
  }

  lastFrameTimestamp = timestamp

  if (growthProgress >= 1) {
    hasPlayedEntrance = true
  }

  if (growthProgress >= 1 && cachedTreeCanvas) {
    renderStaticTree()
  } else {
    canvasContext.clearRect(0, 0, sceneWidth, sceneHeight)

    treeScene.branches.forEach(branch => {
      drawTreeBranch(canvasContext!, branch, growthProgress, elapsedSeconds, true, TREE_DYNAMIC_PROFILE)
    })

    treeScene.leaves.forEach(leaf => {
      drawTreeLeaf(canvasContext!, leaf, growthProgress, elapsedSeconds, true, TREE_DYNAMIC_PROFILE)
    })
  }

  treeScene.particles.forEach(particle => {
    drawTreeParticle(
      canvasContext!,
      particle,
      growthProgress,
      elapsedSeconds,
      growthProgress >= 1 ? TREE_STATIC_PROFILE : TREE_DYNAMIC_PROFILE
    )
  })

  if (!prefersReducedMotion.value) {
    animationFrameId = window.requestAnimationFrame(drawTreeFrame)
  } else {
    stopTreeAnimation()
  }
}

/**
 * 处理窗口尺寸变化。
 * 作用：在首页首屏尺寸变化后同步重建树形场景，避免树木被压缩或模糊。
 */
function handleWindowResize() {
  stopTreeAnimation()
  setupTreeScene()
}

/**
 * 处理“减少动态效果”偏好变化。
 * 作用：在系统偏好改变后即时切换树形表现，并重新安排动画循环。
 */
function handleReducedMotionChange() {
  syncReducedMotionPreference()
  stopTreeAnimation()
  setupTreeScene()
}

/**
 * 处理页面可见性变化。
 * 作用：页面切到后台时暂停动画，回到前台后继续绘制，减少无意义的帧开销。
 */
function handleDocumentVisibilityChange() {
  if (document.hidden) {
    isTreeVisible = false
    stopTreeAnimation()
    return
  }

  isTreeVisible = true
  setupTreeScene()
}

/**
 * 处理首屏可见性变化。
 * 作用：当首页首屏离开视口时暂停暗色树影动画，返回首屏时再恢复。
 *
 * @param entries 可见性观察结果
 */
function handleTreeIntersection(entries: IntersectionObserverEntry[]) {
  const [entry] = entries

  if (!entry) {
    return
  }

  isTreeVisible = entry.isIntersecting

  if (!isTreeVisible) {
    stopTreeAnimation()
    return
  }

  setupTreeScene()
}

onMounted(() => {
  if (!import.meta.client) {
    return
  }

  reducedMotionMediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  syncReducedMotionPreference()
  reducedMotionMediaQuery.addEventListener('change', handleReducedMotionChange)
  window.addEventListener('resize', handleWindowResize, { passive: true })
  document.addEventListener('visibilitychange', handleDocumentVisibilityChange)
  intersectionObserver = new window.IntersectionObserver(handleTreeIntersection, {
    root: null,
    threshold: 0.04
  })
  intersectionObserver.observe(rootRef.value)
  setupTreeScene()
})

onBeforeUnmount(() => {
  stopTreeAnimation()
  reducedMotionMediaQuery?.removeEventListener('change', handleReducedMotionChange)
  window.removeEventListener('resize', handleWindowResize)
  document.removeEventListener('visibilitychange', handleDocumentVisibilityChange)
  intersectionObserver?.disconnect()
})
</script>

<template>
  <div ref="rootRef" class="yunyu-particle-tree" aria-hidden="true">
    <canvas ref="canvasRef" class="yunyu-particle-tree__canvas" />
  </div>
</template>

<style scoped>
/**
 * 云屿暗色粒子树组件样式。
 * 作用：保留之前首页树影的主题变量、边缘渐隐和画布承载关系，
 * 让暗黑模式效果与拆分前保持一致。
 */
.yunyu-particle-tree {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  --yy-tree-branch-rgb: 112, 124, 140;
  --yy-tree-branch-glow-rgb: 68, 120, 172;
  --yy-tree-leaf-rgb: 85, 98, 114;
  --yy-tree-leaf-soft-rgb: 68, 81, 98;
  --yy-tree-dust-rgb: 114, 128, 144;
  mask-image: linear-gradient(90deg, rgba(0, 0, 0, 0.98) 0%, rgba(0, 0, 0, 0.95) 56%, rgba(0, 0, 0, 0.36) 72%, transparent 100%);
}

:global(.dark) .yunyu-particle-tree {
  --yy-tree-branch-rgb: 126, 139, 155;
  --yy-tree-branch-glow-rgb: 72, 128, 182;
  --yy-tree-leaf-rgb: 93, 107, 123;
  --yy-tree-leaf-soft-rgb: 71, 86, 104;
  --yy-tree-dust-rgb: 118, 132, 148;
}

.yunyu-particle-tree__canvas {
  position: absolute;
  inset: 0 auto 0 0;
}

.yunyu-particle-tree__canvas {
  width: 100%;
  height: 100%;
  display: block;
  opacity: 0.74;
}
</style>
