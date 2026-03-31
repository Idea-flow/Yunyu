/**
 * 应用级 UI 配置。
 * 负责定义 Yunyu 当前阶段的语义色方案，
 * 让 Nuxt UI 组件和整体主题表达保持一致。
 */
export default defineAppConfig({
  ui: {
    colors: {
      primary: 'sky',
      neutral: 'slate'
    }
  }
})
