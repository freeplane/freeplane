// AI 类型定义

/**
 * AI 模型信息
 */
export interface AIModel {
  providerName: string
  providerDisplayName: string
  modelName: string
  displayName: string
  isFree: boolean
}

/**
 * 对话消息
 */
export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
  nodeId?: string
}

/**
 * Token 使用统计
 */
export interface TokenUsage {
  inputTokens: number
  outputTokens: number
}

export type AiMode = 'auto' | 'chat' | 'build'
export type ServiceType = 'auto' | 'chat' | 'agent'

/**
 * 节点扩展结果（兼容新老后端结构）
 */
export interface ExpandNodeResult {
  nodeId: string
  result?: string
  summary?: string
  createdNodes?: Array<{
    nodeId: string
    text: string
  }>
  tokenUsage?: TokenUsage
}

/**
 * 分支摘要结果（兼容新老后端结构）
 */
export interface SummarizeResult {
  nodeId: string
  summary: string
  wordCount?: number
  writtenToNote?: boolean
  tokenUsage?: TokenUsage
}

/**
 * 搜索结果
 */
export interface SearchResult {
  nodeId: string
  text: string
  path: string
}

/**
 * 智能缓冲层响应
 */
export interface SmartResponse {
  success: boolean
  usedModel: string
  qualityScore: number
  bufferLayer: string
  processingTime: number
  logs: string[]
  data?: any
  errorMessage?: string
}

export interface SaveModelConfigPayload {
  providerName: string
  apiKey: string
  baseUrl?: string
  modelName?: string
}
