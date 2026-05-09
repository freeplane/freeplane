<template>
  <div class="summarize">
    <div class="header">
      <div class="title">分支摘要</div>
      <div class="hint">对当前选中节点的分支做摘要，可选写入备注</div>
    </div>

    <div class="form">
      <div class="row">
        <div class="label">目标节点</div>
        <div class="value mono">{{ selectedNodeId || '-' }}</div>
      </div>

      <label class="field">
        <span>摘要字数上限</span>
        <input v-model.number="maxWords" type="number" min="20" max="500" />
      </label>

      <label class="checkbox">
        <input v-model="writeToNote" type="checkbox" />
        <span>写入节点备注（writeToNote）</span>
      </label>

      <button class="primary" @click="run" :disabled="!canRun">
        {{ aiStore.buildLoading ? '生成中...' : '生成摘要' }}
      </button>

      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <div v-if="summary" class="output">
        <div class="out-title">摘要结果</div>
        <pre class="out-text">{{ summary }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAIStore } from '@/stores/aiStore'

const props = defineProps<{
  mapId?: string
  selectedNodeId?: string
}>()

const emit = defineEmits<{
  (e: 'updated-map'): void
}>()

const aiStore = useAIStore()

const maxWords = ref(100)
const writeToNote = ref(true)
const summary = ref('')
const errorMessage = ref('')

watch(
  () => props.selectedNodeId,
  () => {
    summary.value = ''
    errorMessage.value = ''
  }
)

const canRun = computed(() => !!props.selectedNodeId && !aiStore.buildLoading)

const run = async () => {
  if (!props.selectedNodeId) return
  summary.value = ''
  errorMessage.value = ''
  try {
    const res = await aiStore.summarize({
      mapId: props.mapId,
      nodeId: props.selectedNodeId,
      maxWords: maxWords.value,
      writeToNote: writeToNote.value
    })
    summary.value = res?.summary || ''
    emit('updated-map')
  } catch (e: any) {
    errorMessage.value = e?.message || aiStore.lastError || '摘要失败'
  }
}
</script>

<style scoped>
.summarize {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header .title {
  font-weight: 800;
  font-size: 14px;
}

.hint {
  color: #666;
  font-size: 12px;
  margin-top: 4px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #333;
}

input[type="number"] {
  height: 34px;
  border-radius: 10px;
  border: 1px solid #ddd;
  padding: 0 10px;
  outline: none;
  font-size: 13px;
}

input[type="number"]:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.12);
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #333;
}

.primary {
  height: 36px;
  border-radius: 10px;
  border: 1px solid #1890ff;
  background: #1890ff;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  font-size: 12px;
  color: #d32f2f;
}

.output {
  border: 1px solid #eee;
  border-radius: 12px;
  background: #fff;
  padding: 10px;
}

.out-title {
  font-weight: 800;
  font-size: 12px;
  margin-bottom: 6px;
}

.out-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.5;
}
</style>

