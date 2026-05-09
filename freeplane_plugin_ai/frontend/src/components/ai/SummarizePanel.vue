<template>
  <div class="summary-panel">
    <h3>分支摘要</h3>
    <div class="node-info" v-if="aiStore.selectedNode">
      当前分支根节点：{{ aiStore.selectedNode.text }}
    </div>

    <button @click="runSummary" :disabled="!aiStore.hasModel || loading">
      {{ loading ? '生成中...' : '生成分支摘要' }}
    </button>

    <!-- 流式输出区：实时显示逐 token 累积的素文 -->
    <div v-if="streamText" class="summary streaming">
      <span class="stream-content">{{ streamText }}</span>
      <span v-if="loading" class="cursor">|</span>
    </div>

    <!-- 完成后渲染 Markdown HTML -->
    <div v-else-if="summaryHtml" class="summary" v-html="summaryHtml"></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAiStore } from '../../stores/aiStore'
import { summarizeBranchStream } from '../../api/aiApi'
import { marked } from 'marked'

const aiStore = useAiStore()
const loading = ref(false)
const streamText = ref('') // 流式列历中的原始文本
const summaryHtml = ref('') // 完成后的 Markdown HTML

const runSummary = async () => {
  if (!aiStore.selectedNode) return
  loading.value = true
  streamText.value = ''
  summaryHtml.value = ''

  await summarizeBranchStream(
    { nodeId: aiStore.selectedNode.id },
    // onToken: 逐 token 累加显示
    (token) => { streamText.value += token },
    // onDone: 流式完成，将素文转为 Markdown HTML
    async () => {
      const raw = streamText.value
      streamText.value = ''
      summaryHtml.value = await marked.parse(raw)
      loading.value = false

      // 画布联动：高亮节点
      window.postMessage({
        type: 'ai:showSummary',
        nodeId: aiStore.selectedNode!.id,
        summary: raw
      }, '*')
    },
    // onError
    (err) => {
      console.error('摘要失败', err)
      streamText.value = ''
      loading.value = false
    }
  )
}
</script>

<style scoped>
.summary-panel { padding: 16px; }
.summary { margin-top: 10px; padding: 10px; background: #f9f9f9; white-space: pre-wrap; word-break: break-word; }
.streaming { min-height: 40px; }
.cursor {
  display: inline-block;
  width: 2px;
  background: #333;
  animation: blink 0.8s step-end infinite;
  margin-left: 1px;
  vertical-align: text-bottom;
  height: 1em;
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
</style>