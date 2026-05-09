// frontend/src/api/aiApi.ts
import axios from 'axios'

// 创建 axios 实例（适配后端接口）
const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// ---------- 内部工具：SSE 流读取 ----------
// 逐 token 解析 text/event-stream 响应流。
// 每个事件格式为 "data: <token>\n\n"，[DONE] 表示完成，[ERROR] 表示错误。
async function consumeSseStream(
  url: string,
  requestBody: object,
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (err: string) => void
): Promise<void> {
  let response: Response
  try {
    response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
  } catch (e) {
    onError(String(e))
    return
  }
  if (!response.ok) {
    onError(`HTTP ${response.status}`)
    return
  }
  const reader = response.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n\n')
    buffer = parts.pop() ?? ''
    for (const part of parts) {
      for (const line of part.split('\n')) {
        if (!line.startsWith('data: ')) continue
        const payload = line.slice(6)
        if (payload === '[DONE]') { onDone(); return }
        if (payload.startsWith('[ERROR]')) { onError(payload.slice(8)); return }
        // Unescape server-encoded newlines
        const text = payload.replace(/\\n/g, '\n').replace(/\\r/g, '\r').replace(/\\\\/g, '\\')
        onToken(text)
      }
    }
  }
  onDone()
}

// 1. 获取模型列表ï¼动态加载通义千问、文心一言等）
export function getAiModels() {
  return api.get('/ai/chat/models')
}

// 2. AI 流式对话
export async function aiChatStream(
  data: { model: string; message: string; nodeId?: string },
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (err: string) => void
): Promise<void> {
  return consumeSseStream('/api/ai/chat/stream', data, onToken, onDone, onError)
}

// 3. 节点展开
export function expandNode(data: {
  nodeId: string
  model: string
}) {
  return api.post('/ai/build/expand-node', data)
}

// 4. 分支摘要（同步）
export function summarizeBranch(data: {
  nodeId: string
  model: string
}) {
  return api.post('/ai/build/summarize', data)
}

// 4b. 分支摘要（流式 SSE，打字机输出）
export async function summarizeBranchStream(
  data: { nodeId: string; mapId?: string; maxWords?: number },
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (err: string) => void
): Promise<void> {
  return consumeSseStream('/api/ai/build/summarize-stream', data, onToken, onDone, onError)
}

// 5. 语义搜索
export function semanticSearch(data: {
  keyword: string
  model: string
}) {
  return api.post('/ai/build/search', data)
}

export default api