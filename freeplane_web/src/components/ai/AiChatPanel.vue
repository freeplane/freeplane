<template>
  <aside v-if="aiStore.panelVisible && aiStore.aiMode === 'chat'" class="chat-panel" :style="{ width: `${aiStore.panelWidth}px` }">
    <div class="resize-handle" @mousedown="onResizeStart"></div>
    <div class="chat-header">
      <div class="title">🤖 AI Chat</div>
      <ModelSelector compact @open-config="configVisible = true" />
      <button class="ghost" @click="aiStore.clearChat" :disabled="aiStore.streaming">清空</button>
    </div>

    <div ref="scrollEl" class="chat-history">
      <div v-if="aiStore.chatHistory.length === 0" class="empty">
        请选择模型，然后开始对话。支持 Markdown 文本输出（当前以纯文本方式显示）。
      </div>

      <div v-for="(m, idx) in aiStore.chatHistory" :key="idx" class="msg" :class="m.role">
        <div class="meta">
          <span class="role">{{ roleLabel(m.role) }}</span>
          <span v-if="m.nodeId" class="node">节点：{{ m.nodeId }}</span>
        </div>
        <pre class="content">{{ m.content }}</pre>
        <div v-if="aiStore.streaming && m.role === 'assistant' && !m.content" class="typing">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>

    <div class="current-node">当前节点: {{ selectedNodeId || '-' }}</div>
    <div class="composer">
      <textarea
        v-model="input"
        class="input"
        placeholder="输入消息（Ctrl+Enter 发送）"
        :disabled="aiStore.streaming"
        @keydown="onKeydown"
      />
      <button class="primary" @click="send" :disabled="aiStore.streaming || !canSend">
        {{ aiStore.streaming ? '输出中...' : '发送' }}
      </button>
    </div>
    <div v-if="aiStore.lastChatFailed" class="retry-row">
      <button class="retry-btn" @click="retry" :disabled="aiStore.streaming">重试上一条</button>
    </div>
    <ModelConfigPanel :visible="configVisible" @close="configVisible = false" />
  </aside>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useAIStore } from '@/stores/aiStore'
import type { ChatMessage } from '@/types/ai'
import ModelSelector from '@/components/ai/ModelSelector.vue'
import ModelConfigPanel from '@/components/ai/ModelConfigPanel.vue'

const props = defineProps<{
  mapId?: string
  selectedNodeId?: string
}>()

const aiStore = useAIStore()
const input = ref('')
const scrollEl = ref<HTMLElement | null>(null)
const configVisible = ref(false)
let startX = 0
let startWidth = 320

const canSend = computed(() => input.value.trim().length > 0 && !!aiStore.currentModel)

const roleLabel = (role: ChatMessage['role']) => {
  if (role === 'user') return '我'
  if (role === 'assistant') return 'AI'
  return '系统'
}

const send = async () => {
  const msg = input.value.trim()
  if (!msg) return
  input.value = ''

  await aiStore.sendChatStreaming(msg, {
    mapId: props.mapId,
    selectedNodeId: props.selectedNodeId
  })

  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (!scrollEl.value) return
  scrollEl.value.scrollTop = scrollEl.value.scrollHeight
}

watch(
  () => aiStore.chatHistory.length,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

const onKeydown = (e: KeyboardEvent) => {
  if (e.key !== 'Enter') return
  if (!e.ctrlKey) return
  e.preventDefault()
  void send()
}

const onResizeStart = (event: MouseEvent) => {
  startX = event.clientX
  startWidth = aiStore.panelWidth
  window.addEventListener('mousemove', onResizeMove)
  window.addEventListener('mouseup', onResizeEnd)
}

const onResizeMove = (event: MouseEvent) => {
  const delta = startX - event.clientX
  aiStore.setPanelWidth(startWidth + delta)
}

const onResizeEnd = () => {
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', onResizeEnd)
}

const retry = async () => {
  await aiStore.retryLastChat()
}
</script>

<style scoped>
.chat-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 320px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  z-index: 1500;
}

.resize-handle { position: absolute; top: 0; left: 0; width: 6px; height: 100%; cursor: ew-resize; background: transparent; z-index: 5; }
.resize-handle:hover { background: rgba(37, 99, 235, 0.12); }

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid #e7e7e7;
  background: #fff;
}

.title {
  font-weight: 700;
  font-size: 14px;
}

.ghost {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 12px;
  cursor: pointer;
}

.ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chat-history {
  flex: 1;
  overflow: auto;
  padding: 12px;
  background: #fafafa;
}

.current-node {
  font-size: 12px;
  color: #4b5563;
  padding: 8px 12px;
  border-top: 1px solid #e7e7e7;
}

.empty {
  color: #666;
  font-size: 13px;
  padding: 8px 0;
}

.msg {
  padding: 10px 10px;
  border-radius: 10px;
  border: 1px solid #e7e7e7;
  background: #fff;
  margin-bottom: 10px;
}

.msg.user {
  border-color: #d7ebff;
  background: #f3f9ff;
}

.meta {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 6px;
  color: #666;
  font-size: 12px;
}

.role {
  font-weight: 700;
  color: #333;
}

.node {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 11px;
}

.content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.5;
  color: #111;
}

.composer {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
  border-top: 1px solid #e7e7e7;
  background: #fff;
}

.input {
  flex: 1;
  height: 64px;
  resize: none;
  border-radius: 10px;
  border: 1px solid #ddd;
  padding: 10px;
  font-size: 13px;
  outline: none;
}

.input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.12);
}

.primary {
  width: 90px;
  border-radius: 10px;
  border: 1px solid #1890ff;
  background: #1890ff;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.typing {
  display: inline-flex;
  gap: 4px;
  margin-top: 4px;
}

.typing span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3b82f6;
  animation: blink 1.2s infinite ease-in-out;
}

.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0%, 80%, 100% { opacity: 0.2; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-2px); }
}

.retry-row {
  padding: 0 12px 10px;
}

.retry-btn {
  width: 100%;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #f59e0b;
  background: #fff7ed;
  color: #b45309;
  font-weight: 700;
  cursor: pointer;
}

@media (max-width: 768px) {
  .chat-panel { width: 100vw !important; max-width: 100vw; }
  .resize-handle { display: none; }
}
</style>

