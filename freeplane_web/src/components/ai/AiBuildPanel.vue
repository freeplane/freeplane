<template>
  <aside v-if="aiStore.panelVisible && aiStore.aiMode === 'build'" class="panel" :style="{ width: `${aiStore.panelWidth}px` }">
    <div class="resize-handle" @mousedown="onResizeStart"></div>
    <div class="top">
      <div class="title">🔧 AI Build</div>
      <ModelSelector compact @open-config="configVisible = true" />
    </div>

    <section class="card">
      <div class="card-title">生成思维导图</div>
      <input v-model="topic" placeholder="输入主题" @input="showCacheDuplicateWarning = false" />
      <div class="cache-hint">
        <span class="cache-hint-icon">💡</span>
        系统会缓存相同请求的结果。如需生成不同内容，请修改主题或调整深度。
      </div>
      <div class="row">
        <label>深度</label>
        <select v-model.number="maxDepth" @change="showCacheDuplicateWarning = false">
          <option :value="1">1 层</option>
          <option :value="2">2 层</option>
          <option :value="3">3 层</option>
          <option :value="4">4 层</option>
          <option :value="5">5 层</option>
          <option :value="6">6 层</option>
        </select>
        <button :disabled="aiStore.buildLoading || !topic.trim()" @click="runGenerate">一键生成</button>
      </div>
      <div v-if="showCacheDuplicateWarning" class="cache-warning">
        ⚠️ 主题与深度与上次完全相同，将直接返回缓存结果。若需重新生成，请修改主题内容或调整深度。
      </div>
      <div v-if="generateError" class="error-msg">{{ generateError }}</div>
    </section>

    <section class="card">
      <div class="card-title">展开节点</div>
      <div class="node">当前节点: {{ selectedNodeId || '请先选中节点' }}</div>
      <div class="row">
        <label>数量</label>
        <select v-model.number="expandCount">
          <option :value="3">3</option>
          <option :value="5">5</option>
          <option :value="8">8</option>
        </select>
        <button :disabled="aiStore.buildLoading || !selectedNodeId" @click="runExpand">
          {{ aiStore.buildLoading ? '处理中...' : 'AI 展开' }}
        </button>
      </div>
    </section>

    <section class="card">
      <div class="card-title">分支摘要</div>
      <div class="node">当前节点: {{ selectedNodeId || '请先选中节点' }}</div>
      <button :disabled="aiStore.buildLoading || !selectedNodeId" @click="runSummarize">
        {{ aiStore.buildLoading ? '处理中...' : '生成摘要' }}
      </button>
    </section>

    <section class="result">
      <div class="result-title">结果预览</div>
      <div class="result-status">
        <span v-if="aiStore.buildResultStatus === 'loading'">处理中...</span>
        <span v-else-if="aiStore.buildResultStatus === 'ready'">AI 建议已生成，是否应用？</span>
        <span v-else>等待操作</span>
      </div>
      <pre>{{ aiStore.buildResult || (aiStore.buildResultStatus === 'loading' ? '' : '暂无结果') }}</pre>
      <div class="result-actions">
        <button class="ghost-btn" :disabled="!aiStore.buildResult" @click="copyResult">复制结果</button>
        <button
          class="apply-btn"
          :disabled="!aiStore.buildResult || aiStore.buildLoading"
          @click="applyResult"
        >
          应用到导图
        </button>
      </div>
    </section>
    <ModelConfigPanel :visible="configVisible" @close="configVisible = false" />
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAIStore } from '@/stores/aiStore'
import ModelSelector from '@/components/ai/ModelSelector.vue'
import ModelConfigPanel from '@/components/ai/ModelConfigPanel.vue'

const props = defineProps<{
  mapId?: string
  selectedNodeId?: string
}>()

const aiStore = useAIStore()
const configVisible = ref(false)
const topic = ref('')
const maxDepth = ref(3)
const expandCount = ref(5)
const generateError = ref('')

// 缓存提醒：记录上一次提交的 topic+depth 组合，用于判断是否重复输入
const lastSubmittedKey = ref('')
const showCacheDuplicateWarning = ref(false)

const currentKey = computed(() => `${topic.value.trim()}|${maxDepth.value}`)

let startX = 0
let startWidth = 320

const runGenerate = async () => {
  generateError.value = ''

  // 检测是否与上次提交完全相同（触发缓存警告）
  if (lastSubmittedKey.value && lastSubmittedKey.value === currentKey.value) {
    showCacheDuplicateWarning.value = true
  } else {
    showCacheDuplicateWarning.value = false
  }

  lastSubmittedKey.value = currentKey.value

  try {
    await aiStore.generateMindMap(topic.value.trim(), { maxDepth: maxDepth.value })
  } catch (e: any) {
    generateError.value = e?.message || aiStore.lastError || '生成思维导图失败'
  }
}

const runExpand = async () => {
  if (!props.selectedNodeId) return
  try {
    await aiStore.expandNode({
      mapId: props.mapId,
      nodeId: props.selectedNodeId,
      count: expandCount.value
    })
  } catch (e: any) {
    generateError.value = e?.message || aiStore.lastError || '展开节点失败'
  }
}

const runSummarize = async () => {
  if (!props.selectedNodeId) return
  try {
    await aiStore.summarize({
      mapId: props.mapId,
      nodeId: props.selectedNodeId,
      writeToNote: true
    })
  } catch (e: any) {
    generateError.value = e?.message || aiStore.lastError || '生成摘要失败'
  }
}

const copyResult = async () => {
  if (!aiStore.buildResult) return
  await navigator.clipboard.writeText(aiStore.buildResult)
}

const applyResult = async () => {
  const confirmed = window.confirm('AI 建议已生成，是否应用到导图？')
  if (!confirmed) return
  try {
    // 如果没有选中节点，则应用到根节点（生成思维导图场景）
    const targetNodeId = props.selectedNodeId || 'ID_1'
    await aiStore.applyBuildResultToMap(targetNodeId, props.mapId)
    window.alert('已应用到导图')
  } catch (error: any) {
    window.alert(error?.message || '应用失败')
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
.panel { position: fixed; top: 0; right: 0; width: 320px; height: 100vh; background: #fff; border-left: 1px solid #e5e7eb; z-index: 1500; padding: 12px; display: grid; gap: 10px; overflow: auto; }
.resize-handle { position: absolute; top: 0; left: 0; width: 6px; height: 100%; cursor: ew-resize; background: transparent; }
.resize-handle:hover { background: rgba(37, 99, 235, 0.12); }
.top { display: grid; gap: 8px; }
.title { font-size: 15px; font-weight: 700; }
.card { border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px; display: grid; gap: 8px; }
.card-title { font-size: 13px; font-weight: 700; }
.row { display: flex; align-items: center; gap: 8px; }
.row select, input { flex: 1; border: 1px solid #d1d5db; border-radius: 8px; height: 34px; padding: 0 8px; }
button { border: 1px solid #2563eb; background: #2563eb; color: #fff; border-radius: 8px; height: 34px; padding: 0 10px; font-size: 12px; font-weight: 600; cursor: pointer; }
button:disabled { opacity: 0.6; cursor: not-allowed; }
.node { font-size: 12px; color: #4b5563; }
.error-msg { font-size: 12px; color: #dc2626; margin-top: 4px; }
.cache-hint { font-size: 11px; color: #6b7280; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 6px 8px; display: flex; align-items: flex-start; gap: 4px; line-height: 1.5; }
.cache-hint-icon { flex-shrink: 0; }
.cache-warning { font-size: 12px; color: #92400e; background: #fffbeb; border: 1px solid #fcd34d; border-radius: 6px; padding: 7px 9px; line-height: 1.5; margin-top: 2px; }
.result { border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px; min-height: 120px; }
.result-title { font-size: 12px; font-weight: 700; margin-bottom: 6px; }
.result-status { font-size: 12px; color: #4b5563; margin-bottom: 6px; }
.result pre { margin: 0; font-size: 12px; white-space: pre-wrap; color: #111827; }
.result-actions { display: flex; gap: 8px; margin-top: 8px; }
.ghost-btn { border: 1px solid #d1d5db; background: #fff; color: #1f2937; }
.apply-btn { border: 1px solid #2563eb; background: #2563eb; color: #fff; }

@media (max-width: 768px) {
  .panel { width: 100vw !important; max-width: 100vw; }
  .resize-handle { display: none; }
}
</style>
