import type MarkdownIt from 'markdown-it'
import type katex from 'katex'

/**
 * markdown-it-texmath 模块声明。
 * 作用：为当前项目补齐数学公式插件的最小类型信息，
 * 避免第三方包未内置 TypeScript 声明时影响前端构建。
 */
declare module 'markdown-it-texmath' {
  /**
   * 数学公式插件配置类型。
   * 作用：描述 markdown-it-texmath 在项目中的可用配置项。
   */
  interface MarkdownItTexmathOptions {
    engine?: typeof katex
    delimiters?: string | string[]
    outerSpace?: boolean
    macros?: Record<string, string>
    katexOptions?: katex.KatexOptions
  }

  /**
   * markdown-it 数学公式插件。
   * 作用：为 Markdown 渲染器补充行内公式和块级公式解析能力。
   *
   * @param md MarkdownIt 实例
   * @param options 插件配置
   */
  const markdownItTexmath: (md: MarkdownIt, options?: MarkdownItTexmathOptions) => void

  export default markdownItTexmath
}
