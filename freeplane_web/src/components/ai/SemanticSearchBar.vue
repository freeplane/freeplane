<template>
  <div class="search">
    <div class="header">
      <div class="title">搜索</div>
      <div class="hint">当前实现为关键词搜索（后续可替换为语义检索）</div>
    </div>

    <div class="row">
      <input
        v-model="query"
        class="input"
        type="text"
        placeholder="输入关键词，回车搜索"
        :disabled="aiStore.loading"
        @keyup.enter="run"
      />
      <button class="primary" @click="run" :disabled="aiStore.loading || !query.trim()">
        {{ aiStore.loading ? '搜索中...' : '搜索' }}
      </button>
    </div>

    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>

    <div v-if="results.length" class="results">
      <div class="result-title">结果（{{ results.length }}）</div>
      <button v-for="r in results" :key="r.nodeId" class="result" @click="emit('pick-node', r.nodeId)">
        <div class="line1">{{ r.text }}</div>
        <div class="line2">{{ r.path }}</div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAIStore } from '@/stores/aiStore'
import type { SearchResult } from '@/types/ai'

const props = defineProps<{
  mapId?: string
}>()

const emit = defineEmits<{
  (e: 'pick-node', nodeId: string): void
}>()

const aiStore = useAIStore()
const query = ref('')
const results = ref<SearchResult[]>([])
const errorMessage = ref('')

const run = async () => {
  const q = query.value.trim()
  if (!q) return
  errorMessage.value = ''
  results.value = []
  try {
    const res = await aiStore.keywordSearch({
      mapId: props.mapId,
      query: q,
      caseSensitive: false
    })
    results.value = res?.results || []
  } catch (e: any) {
    errorMessage.value = e?.message || aiStore.lastError || '搜索失败'
  }
}
</script>

<style scoped>
.search {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.row {
  display: flex;
  gap: 10px;
}

.input {
  flex: 1;
  height: 34px;
  border-radius: 10px;
  border: 1px solid #ddd;
  padding: 0 10px;
  outline: none;
  font-size: 13px;
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

.results {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-title {
  font-weight: 800;
  font-size: 12px;
}

.result {
  text-align: left;
  border: 1px solid #eee;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
}

.result:hover {
  border-color: #1890ff;
}

.line1 {
  font-weight: 800;
  font-size: 12px;
  color: #111;
  margin-bottom: 4px;
}

.line2 {
  font-size: 12px;
  color: #666;
}
</style>

