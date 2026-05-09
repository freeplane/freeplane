/**
 * AI 状态管理
 * 管理 AI 相关的全局状态和操作
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useMapStore } from '@/stores/mapStore'
import type { AIModel, ChatMessage, AiMode, ServiceType, SaveModelConfigPayload } from '@/types/ai'
import * as aiApi from '@/api/aiApi'

export const useAIStore = defineStore('ai', () => {
  // ==================== 状态 ====================
  
  /** 可用的 AI 模型列表 */
  const modelList = ref<AIModel[]>([])
  
  /** 当前选中的模型（格式：providerName:modelName） */
  const currentModel = ref<string>('')
  
  /** 对话历史记录 */
  const chatHistory = ref<ChatMessage[]>([])
  
  /** 是否正在流式输出 */
  const streaming = ref(false)
  
  /** 是否正在加载 */
  const loading = ref(false)
  
  /** AI 面板是否可见 */
  const panelVisible = ref(false)

  /** 当前 AI 模式：auto / chat / build */
  const aiMode = ref<AiMode>('chat')

  /** 服务路由类型 */
  const serviceType = ref<ServiceType>('chat')

  /** Build 操作独立加载状态 */
  const buildLoading = ref(false)

  /** Build 操作结果预览 */
  const buildResult = ref('')
  const buildResultStatus = ref<'idle' | 'loading' | 'ready'>('idle')

  const panelWidth = ref(320)
  
  /** 最近一次错误信息（用于 UI 反馈） */
  const lastError = ref<string>('')
  const lastChatFailed = ref(false)
  const lastChatRequest = ref<{ message: string; ctx?: { mapId?: string; selectedNodeId?: string } } | null>(null)

  // ==================== 计算属性 ====================
  
  /** 是否有已配置的模型 */
  const hasConfiguredModels = computed(() => modelList.value.length > 0)
  
  /** 当前选中的模型对象 */
  const currentModelObj = computed(() => {
    const [providerName, modelName] = splitModelSelection(currentModel.value)
    return modelList.value.find(m => m.providerName === providerName && m.modelName === modelName)
  })

  // ==================== 方法 ====================
  
  const splitModelSelection = (selection: string): [string, string] => {
    const idx = selection.indexOf('|')
    if (idx <= 0 || idx === selection.length - 1) return ['', selection]
    return [selection.slice(0, idx), selection.slice(idx + 1)]
  }
  
  const toModelSelection = (model: AIModel) => `${model.providerName}|${model.modelName}`

  const setMode = (mode: AiMode) => {
    aiMode.value = mode
    if (mode === 'auto') serviceType.value = 'auto'
    if (mode === 'chat') serviceType.value = 'chat'
    if (mode === 'build') serviceType.value = 'agent'
  }

  const setPanelWidth = (width: number) => {
    const normalized = Math.max(280, Math.min(680, Math.round(width)))
    panelWidth.value = normalized
  }
  
  /**
   * 获取模型列表
   */
  const fetchModelList = async () => {
    try {
      loading.value = true
      lastError.value = ''
      const response = await aiApi.getAiModels()
      modelList.value = response.data.models
      
      // 如果没有选中模型，自动选择第一个
      if (modelList.value.length > 0 && !currentModel.value) {
        currentModel.value = toModelSelection(modelList.value[0])
      }
    } catch (error: any) {
      console.error('获取模型列表失败:', error)
      lastError.value = error?.message || '获取模型列表失败'
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 切换模型
   */
  const switchModel = (modelSelection: string) => {
    currentModel.value = modelSelection
    // 可选：清空对话历史
    // chatHistory.value = []
  }
  
  /**
   * 发送 AI 对话
   */
  const sendChat = async (message: string, ctx?: { mapId?: string; selectedNodeId?: string }) => {
    if (!message.trim()) return
    lastChatRequest.value = { message, ctx }
    lastChatFailed.value = false
    
    // 添加用户消息
    chatHistory.value.push({
      role: 'user',
      content: message,
      timestamp: Date.now(),
      nodeId: ctx?.selectedNodeId
    })
    
    // 添加助手占位消息
    const assistantIndex = chatHistory.value.push({
      role: 'assistant',
      content: '',
      timestamp: Date.now()
    }) - 1
    
    streaming.value = true
    try {
      const response = await aiApi.aiChat({
        message,
        modelSelection: currentModel.value,
        mapId: ctx?.mapId,
        selectedNodeId: ctx?.selectedNodeId,
        serviceType: serviceType.value
      })
      
      // 更新助手回复
      chatHistory.value[assistantIndex].content = response.data.reply
    } catch (error: any) {
      const msg = error?.message || '对话失败'
      lastError.value = msg
      lastChatFailed.value = true
      chatHistory.value[assistantIndex].content = `❌ 对话失败：${msg}`
      console.error('AI 对话失败:', error)
    } finally {
      streaming.value = false
    }
  }
  
  /**
   * 发送 AI 对话（逐字显示）
   * 当前实现：先请求完整回复，再模拟逐字输出（后端 SSE 上线后可替换为真正流式）。
   */
  const sendChatStreaming = async (message: string, ctx?: { mapId?: string; selectedNodeId?: string }) => {
    if (!message.trim()) return
    lastChatRequest.value = { message, ctx }
    lastChatFailed.value = false
    
    chatHistory.value.push({
      role: 'user',
      content: message,
      timestamp: Date.now(),
      nodeId: ctx?.selectedNodeId
    })
    
    const assistantIndex = chatHistory.value.push({
      role: 'assistant',
      content: '',
      timestamp: Date.now()
    }) - 1
    
    streaming.value = true
    try {
      const response = await aiApi.aiChat({
        message,
        modelSelection: currentModel.value,
        mapId: ctx?.mapId,
        selectedNodeId: ctx?.selectedNodeId,
        serviceType: serviceType.value
      })
      
      const text = response.data.reply || ''
      await typeOut(chatHistory.value[assistantIndex], text, 10)
    } catch (error: any) {
      const msg = error?.message || '对话失败'
      lastError.value = msg
      lastChatFailed.value = true
      chatHistory.value[assistantIndex].content = `❌ 对话失败：${msg}`
      console.error('AI 对话失败:', error)
    } finally {
      streaming.value = false
    }
  }
  
  const typeOut = (message: ChatMessage, fullText: string, delayMs: number) => {
    message.content = ''
    return new Promise<void>((resolve) => {
      let i = 0
      const timer = window.setInterval(() => {
        i += 1
        message.content = fullText.slice(0, i)
        if (i >= fullText.length) {
          window.clearInterval(timer)
          resolve()
        }
      }, Math.max(0, delayMs))
    })
  }
  
  const normalizeResultText = (text: string) => {
    const trimmed = text.trim()
    if (trimmed.startsWith('```')) {
      return trimmed.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/, '')
    }
    return trimmed
  }

  interface TreeNode {
    text: string
    children?: TreeNode[]
  }

  const expandNode = async (data: { mapId?: string; nodeId: string; depth?: number; count?: number; focus?: string }) => {
    try {
      buildLoading.value = true
      buildResultStatus.value = 'loading'
      lastError.value = ''
      const payload = {
        ...data,
        modelSelection: currentModel.value,
        serviceType: serviceType.value
      }
      const res = await aiApi.expandNode(payload)
      const resultText = res.data.result || res.data.summary || ''
      buildResult.value = resultText
      buildResultStatus.value = 'ready'

      // 自动解析 JSON 并递归创建所有子节点（含嵌套子节点）
      if (resultText && data.nodeId) {
        const mapStore = useMapStore()
        const children = parseExpandResult(resultText)
        if (children.length > 0) {
          await createNodesRecursive(data.nodeId, children, mapStore)
        }
        await mapStore.loadMap()
      }

      return res.data
    } catch (error: any) {
      lastError.value = error?.message || '展开节点失败'
      buildResultStatus.value = 'idle'
      throw error
    } finally {
      buildLoading.value = false
    }
  }

  const parseExpandResult = (text: string): TreeNode[] => {
    const normalized = normalizeResultText(text)
    if (!normalized) return []
    try {
      const parsed = JSON.parse(normalized)
      // 支持 { children: [...] } 格式
      if (parsed?.children && Array.isArray(parsed.children)) {
        return parsed.children.filter((item: any) => item?.text)
      }
      // 支持直接数组格式
      if (Array.isArray(parsed)) {
        return parsed.filter((item: any) => item?.text || typeof item === 'string')
          .map((item: any) => typeof item === 'string' ? { text: item } : item)
      }
      // 支持有 text 的单个节点
      if (parsed?.text) {
        return parsed.children ? [parsed] : [parsed]
      }
    } catch (e) {
      console.warn('解析 expandNode 返回的 JSON 失败:', e)
    }
    return []
  }

  const createNodesRecursive = async (parentId: string, nodes: TreeNode[], mapStore: ReturnType<typeof useMapStore>) => {
    for (const node of nodes) {
      const newId = await mapStore.createNode(parentId, node.text)
      // 递归创建子节点：利用 createNode 返回的新节点 ID 作为下一层的 parentId
      if (node.children && node.children.length > 0 && newId) {
        await createNodesRecursive(newId, node.children, mapStore)
      }
    }
  }

  const summarize = async (data: { mapId?: string; nodeId: string; maxWords?: number; writeToNote?: boolean }) => {
    try {
      buildLoading.value = true
      buildResultStatus.value = 'loading'
      lastError.value = ''
      const res = await aiApi.summarizeBranch({
        ...data,
        modelSelection: currentModel.value,
        serviceType: serviceType.value
      })
      buildResult.value = res.data.summary || ''
      buildResultStatus.value = 'ready'
      const mapStore = useMapStore()
      await mapStore.loadMap()
      return res.data
    } catch (error: any) {
      lastError.value = error?.message || '分支摘要失败'
      throw error
    } finally {
      buildLoading.value = false
    }
  }
  
  const generateMindMap = async (topic: string, options?: { maxDepth?: number }) => {
    try {
      buildLoading.value = true
      buildResultStatus.value = 'loading'
      lastError.value = ''
      const res = await aiApi.generateMindMap({
        topic,
        maxDepth: options?.maxDepth,
        modelSelection: currentModel.value,
        serviceType: serviceType.value
      })
      buildResult.value = res.data.result || `已生成 ${res.data.nodeCount || 0} 个节点`
      buildResultStatus.value = 'ready'
      
      // 后端已通过 MindMapBufferLayer 直接创建节点，前端只需重新加载地图
      const mapStore = useMapStore()
      await mapStore.loadMap()
      
      return res.data
    } catch (error: any) {
      lastError.value = error?.message || '生成导图失败'
      throw error
    } finally {
      buildLoading.value = false
    }
  }

  const parseTreeNodes = (text: string, isRootLevel: boolean = false): TreeNode[] => {
    const normalized = normalizeResultText(text)
    if (!normalized) return []
    try {
      const parsed = JSON.parse(normalized)
      if (Array.isArray(parsed)) {
        return parsed.filter((item: any) => item?.text || typeof item === 'string')
          .map((item: any) => typeof item === 'string' ? { text: item } : item)
      }
      if (parsed?.children && Array.isArray(parsed.children)) {
        // 如果是根节点级别，直接返回 children（避免嵌套）
        // 否则返回包含根节点的数组
        return isRootLevel 
          ? parsed.children.filter((item: any) => item?.text || typeof item === 'string')
              .map((item: any) => typeof item === 'string' ? { text: item } : item)
          : [parsed]
      }
      if (parsed?.text) {
        return isRootLevel && parsed?.children 
          ? parsed.children.filter((item: any) => item?.text || typeof item === 'string')
              .map((item: any) => typeof item === 'string' ? { text: item } : item)
          : [parsed]
      }
    } catch (e) {
      console.warn('解析 AI 返回的 JSON 失败:', e)
    }
    return []
  }

  const applyBuildResultToMap = async (targetNodeId: string, mapId?: string) => {
    const mapStore = useMapStore()
    // 如果是根节点 ID_1，使用 isRootLevel=true 提取 children
    // 否则（展开节点等场景）使用 isRootLevel=false
    const isRootLevel = targetNodeId === 'ID_1'
    const treeNodes = parseTreeNodes(buildResult.value, isRootLevel)
    if (!treeNodes.length) {
      throw new Error('结果中未解析到可应用的节点，请确保 AI 返回 JSON 数组或 children 结构')
    }

    // 递归创建所有层级的节点
    await createNodesRecursive(targetNodeId, treeNodes, mapStore)
    if (mapId) {
      await mapStore.loadMap()
    }
  }

  const retryLastChat = async () => {
    if (!lastChatRequest.value) return
    const { message, ctx } = lastChatRequest.value
    await sendChatStreaming(message, ctx)
  }

  const sendSmart = async (input: string, ctx?: { mapId?: string; selectedNodeId?: string }) => {
    if (!input.trim()) return
    loading.value = true
    lastError.value = ''
    try {
      const response = await aiApi.smartRequest({
        input,
        mapId: ctx?.mapId,
        selectedNodeId: ctx?.selectedNodeId,
        modelSelection: currentModel.value
      })
      const output = typeof response.data.data === 'string' ? response.data.data : JSON.stringify(response.data.data || {}, null, 2)
      buildResult.value = output
      return response.data
    } catch (error: any) {
      lastError.value = error?.message || 'Auto 模式请求失败'
      throw error
    } finally {
      loading.value = false
    }
  }
  
  const keywordSearch = async (data: { mapId?: string; query: string; caseSensitive?: boolean }) => {
    try {
      loading.value = true
      lastError.value = ''
      const res = await aiApi.searchNodes(data)
      return res.data
    } catch (error: any) {
      lastError.value = error?.message || '搜索失败'
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 清空对话历史
   */
  const clearChat = () => {
    chatHistory.value = []
    lastChatFailed.value = false
    lastChatRequest.value = null
  }
  
  /**
   * 切换 AI 面板显示状态
   */
  const togglePanel = () => {
    panelVisible.value = !panelVisible.value
  }
  
  /**
   * 显示 AI 面板
   */
  const showPanel = () => {
    panelVisible.value = true
  }
  
  /**
   * 隐藏 AI 面板
   */
  const hidePanel = () => {
    panelVisible.value = false
  }
  
  /**
   * 初始化 AI Store
   */
  const init = async () => {
    try {
      await fetchModelList()
      setMode(aiMode.value)
    } catch (error) {
      console.warn('AI Store 初始化失败:', error)
    }
  }

  const saveCustomModel = async (payload: SaveModelConfigPayload) => {
    await aiApi.saveModelConfig(payload)
    await fetchModelList()
  }

  // ==================== 返回 ====================
  
  return {
    // 状态
    modelList,
    currentModel,
    chatHistory,
    streaming,
    loading,
    panelVisible,
    aiMode,
    serviceType,
    buildLoading,
    buildResult,
    buildResultStatus,
    panelWidth,
    lastError,
    lastChatFailed,
    
    // 计算属性
    hasConfiguredModels,
    currentModelObj,
    
    // 方法
    fetchModelList,
    switchModel,
    sendChat,
    sendChatStreaming,
    clearChat,
    togglePanel,
    showPanel,
    hidePanel,
    setMode,
    setPanelWidth,
    init,
    expandNode,
    summarize,
    generateMindMap,
    keywordSearch,
    sendSmart,
    saveCustomModel,
    applyBuildResultToMap,
    retryLastChat
  }
})
