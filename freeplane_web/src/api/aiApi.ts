/**
 * AI 相关接口封装
 * 调用 freeplane_plugin_ai 插件的 REST API（端口 6299）
 */

import axios from 'axios'
import type {
  AIModel,
  TokenUsage,
  ExpandNodeResult,
  SummarizeResult,
  SearchResult,
  SmartResponse,
  ServiceType,
  SaveModelConfigPayload
} from '@/types/ai'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const isRetryableStatus = (status?: number) => status === 404 || status === 405

const getWithFallback = async <T>(primary: string, fallback: string) => {
  try {
    return await api.get<T>(primary)
  } catch (error: any) {
    if (!isRetryableStatus(error?.response?.status)) throw error
    return api.get<T>(fallback)
  }
}

const postWithFallback = async <T>(primary: string, fallback: string, data: unknown) => {
  try {
    return await api.post<T>(primary, data)
  } catch (error: any) {
    if (!isRetryableStatus(error?.response?.status)) throw error
    return api.post<T>(fallback, data)
  }
}

// 长超时 API 实例（120 秒，用于 AI 耗时操作）
const longTimeoutApi = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const postWithLongTimeout = async <T>(primary: string, fallback: string, data: unknown) => {
  try {
    return await longTimeoutApi.post<T>(primary, data)
  } catch (error: any) {
    if (!isRetryableStatus(error?.response?.status)) throw error
    return longTimeoutApi.post<T>(fallback, data)
  }
}

// ==================== AI 模型管理 ====================

/**
 * 获取可用的 AI 模型列表
 */
export function getAiModels() {
  return getWithFallback<{ models: AIModel[] }>('/ai/chat/models', '/ai/models')
}

// ==================== AI 对话 ====================

/**
 * AI 对话（同步响应）
 */
export function aiChat(data: {
  message: string
  modelSelection?: string
  mapId?: string
  selectedNodeId?: string
  serviceType?: ServiceType
}) {
  return postWithFallback<{
    reply: string
    tokenUsage: TokenUsage
  }>('/ai/chat/message', '/ai/chat', data)
}

/**
 * AI 对话（流式输出）- 待后端实现
 */
export function aiChatStream(data: {
  message: string
  modelSelection?: string
  onChunk: (chunk: string) => void
}) {
  // 兼容预留：若后端实现 SSE，可在这里启用真正的流式输出。
  // 当前项目的前端“逐字显示”由 store 的模拟打字效果实现，不依赖后端流式。
  void data
  throw new Error('后端未提供流式接口：请使用非流式 + 前端逐字显示')
}

// ==================== 节点扩展 ====================

/**
 * AI 扩展节点：基于目标节点生成子节点（耗时操作，超时 120 秒）
 */
export function expandNode(data: {
  nodeId: string
  mapId?: string
  count?: number
  depth?: number
  focus?: string
  modelSelection?: string
  serviceType?: ServiceType
}) {
  return postWithLongTimeout<ExpandNodeResult>('/ai/build/expand-node', '/ai/expand-node', data)
}

// ==================== 分支摘要 ====================

/**
 * AI 生成分支摘要（耗时操作，超时 120 秒）
 */
export function summarizeBranch(data: {
  nodeId: string
  mapId?: string
  maxWords?: number
  writeToNote?: boolean
  modelSelection?: string
  serviceType?: ServiceType
}) {
  return postWithLongTimeout<SummarizeResult>('/ai/build/summarize', '/ai/summarize', data)
}

// ==================== 节点搜索 ====================

/**
 * 关键词搜索节点
 */
export function searchNodes(data: {
  query: string
  caseSensitive?: boolean
  mapId?: string
}) {
  return api.post<{
    results: SearchResult[]
    totalCount: number
  }>('/nodes/search', data)
}

// ==================== 智能缓冲层 ====================

/**
 * 智能请求：自然语言理解，自动选择最佳工具
 */
export function smartRequest(data: {
  input: string
  modelSelection?: string
  mapId?: string
  selectedNodeId?: string
}) {
  return postWithLongTimeout<SmartResponse>('/ai/chat/smart', '/ai/smart', data)
}

// ==================== 思维导图生成 ====================

/**
 * AI 一键生成思维导图（耗时操作，超时 120 秒）
 */
export function generateMindMap(data: {
  topic: string
  modelSelection?: string
  maxDepth?: number
  serviceType?: ServiceType
}) {
  return postWithLongTimeout<{
    success: boolean
    topic: string
    nodeCount: number
    mapId: string
    result?: string
    tokenUsage?: TokenUsage
  }>('/ai/build/generate-mindmap', '/ai/generate-mindmap', data)
}

export function saveModelConfig(data: SaveModelConfigPayload) {
  return api.post<{ success: boolean }>('/ai/config/save', data)
}

export default api
