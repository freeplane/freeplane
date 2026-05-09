<template>
  <div v-if="visible" class="overlay" @click.self="emit('cancel')">
    <div class="dialog">
      <div class="header">
        <div class="title">AI 展开节点</div>
        <button class="x" @click="emit('cancel')">×</button>
      </div>

      <div class="body">
        <div class="row">
          <div class="label">目标节点</div>
          <div class="value mono">{{ nodeId || '-' }}</div>
        </div>

        <div class="grid">
          <label class="field">
            <span>生成数量</span>
            <input v-model.number="count" type="number" min="1" max="20" />
          </label>
          <label class="field">
            <span>深度</span>
            <input v-model.number="depth" type="number" min="1" max="5" />
          </label>
        </div>

        <label class="field">
          <span>展开方向 / 关注点</span>
          <input v-model="focus" type="text" placeholder="例如：请从技术角度展开" />
        </label>

        <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
        <div v-if="resultSummary" class="result">{{ resultSummary }}</div>
      </div>

      <div class="footer">
        <button class="ghost" @click="emit('cancel')" :disabled="busy">取消</button>
        <button class="primary" @click="run" :disabled="busy || !nodeId">
          {{ busy ? '处理中...' : '开始展开' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useAIStore } from '@/stores/aiStore'

const props = defineProps<{
  visible: boolean
  mapId?: string
  nodeId?: string
}>()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'success'): void
}>()

const aiStore = useAIStore()

const count = ref(5)
const depth = ref(1)
const focus = ref('')

const busy = ref(false)
const errorMessage = ref('')
const resultSummary = ref('')

watch(
  () => props.visible,
  (v) => {
    if (!v) return
    errorMessage.value = ''
    resultSummary.value = ''
    count.value = 5
    depth.value = 1
    focus.value = ''
  }
)

const run = async () => {
  if (!props.nodeId) return
  busy.value = true
  errorMessage.value = ''
  resultSummary.value = ''
  try {
    const res = await aiStore.expandNode({
      mapId: props.mapId,
      nodeId: props.nodeId,
      count: count.value,
      depth: depth.value,
      focus: focus.value.trim() || undefined
    })
    resultSummary.value = res?.summary || '展开完成'
    emit('success')
  } catch (e: any) {
    errorMessage.value = e?.message || aiStore.lastError || '展开失败'
  } finally {
    busy.value = false
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
}

.dialog {
  width: 520px;
  max-width: 100%;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e7e7e7;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.22);
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid #eee;
}

.title {
  font-weight: 800;
  font-size: 14px;
}

.x {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 10px;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}

.body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #eee;
  border-radius: 12px;
  background: #fafafa;
}

.label {
  color: #666;
  font-size: 12px;
}

.value {
  color: #111;
  font-size: 12px;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #333;
}

input {
  height: 34px;
  border-radius: 10px;
  border: 1px solid #ddd;
  padding: 0 10px;
  outline: none;
  font-size: 13px;
}

input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.12);
}

.error {
  font-size: 12px;
  color: #d32f2f;
}

.result {
  font-size: 12px;
  color: #2e7d32;
}

.footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 14px;
  border-top: 1px solid #eee;
  background: #fff;
}

.ghost,
.primary {
  border-radius: 10px;
  padding: 8px 14px;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid #ddd;
  background: #fff;
}

.primary {
  border-color: #1890ff;
  background: #1890ff;
  color: #fff;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

