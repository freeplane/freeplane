<template>
  <aside v-if="aiStore.panelVisible && aiStore.aiMode === 'auto'" class="panel" :style="{ width: `${aiStore.panelWidth}px` }">
    <div class="resize-handle" @mousedown="onResizeStart"></div>
    <div class="panel-header">
      <div class="title">✨ Auto 智能模式</div>
      <ModelSelector compact @open-config="configVisible = true" />
    </div>
    <div class="desc">描述你的需求，系统会自动选择对话或导图操作。</div>
    <div class="current-node">当前节点: {{ selectedNodeId || '-' }}</div>
    <textarea
      v-model="input"
      class="input"
      placeholder="输入自然语言指令..."
      :disabled="aiStore.loading"
    />
    <button class="primary" :disabled="aiStore.loading || !input.trim()" @click="send">
      {{ aiStore.loading ? '发送中...' : '发送' }}
    </button>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <div class="examples">
      <div>示例指令：</div>
      <div>· 展开这个节点，生成 5 个子节点</div>
      <div>· 帮我总结这个分支内容</div>
      <div>· 这个节点讲的是什么</div>
    </div>
    <div v-if="aiStore.buildResult" class="result">
      <div class="result-title">结果预览</div>
      <pre>{{ aiStore.buildResult }}</pre>
    </div>
    <ModelConfigPanel :visible="configVisible" @close="configVisible = false" />
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAIStore } from '@/stores/aiStore'
import ModelSelector from '@/components/ai/ModelSelector.vue'
import ModelConfigPanel from '@/components/ai/ModelConfigPanel.vue'

const props = defineProps<{
  mapId?: string
  selectedNodeId?: string
}>()

const aiStore = useAIStore()
const input = ref('')
const configVisible = ref(false)
const errorMessage = ref('')
let startX = 0
let startWidth = 320

const send = async () => {
  const message = input.value.trim()
  if (!message) return
  errorMessage.value = ''
  try {
    await aiStore.sendSmart(message, {
      mapId: props.mapId,
      selectedNodeId: props.selectedNodeId
    })
    input.value = ''
  } catch (e: any) {
    errorMessage.value = e?.message || aiStore.lastError || '请求失败，请检查后端是否已启动'
  }
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
</script>

<style scoped>
.panel { position: fixed; top: 0; right: 0; width: 320px; height: 100vh; background: #fff; border-left: 1px solid #e5e7eb; z-index: 1500; padding: 12px; display: grid; grid-template-rows: auto auto auto 96px auto auto 1fr; gap: 10px; }
.resize-handle { position: absolute; top: 0; left: 0; width: 6px; height: 100%; cursor: ew-resize; background: transparent; }
.resize-handle:hover { background: rgba(37, 99, 235, 0.12); }
.panel-header { display: grid; gap: 8px; }
.title { font-weight: 700; font-size: 15px; }
.desc { color: #4b5563; font-size: 12px; }
.current-node { font-size: 12px; color: #1f2937; padding: 8px; background: #f9fafb; border-radius: 8px; }
.input { resize: none; border: 1px solid #d1d5db; border-radius: 8px; padding: 8px; font-size: 13px; }
.primary { border: 1px solid #2563eb; background: #2563eb; color: #fff; border-radius: 8px; height: 36px; cursor: pointer; font-weight: 600; }
.examples { font-size: 12px; color: #4b5563; display: grid; gap: 4px; }
.error { font-size: 12px; color: #dc2626; padding: 4px 0; }
.result { border-top: 1px solid #e5e7eb; padding-top: 8px; min-height: 0; overflow: auto; }
.result-title { font-size: 12px; font-weight: 700; margin-bottom: 6px; }
.result pre { margin: 0; white-space: pre-wrap; font-size: 12px; color: #111827; }

@media (max-width: 768px) {
  .panel { width: 100vw !important; max-width: 100vw; }
  .resize-handle { display: none; }
}
</style>
