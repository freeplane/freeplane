<template>
  <div class="ai-panel" :class="{ open: aiStore.panelVisible }">
    <div class="topbar">
      <div class="left">
        <div class="brand">AI</div>
        <ModelSelector />
      </div>
      <div class="right">
        <button class="ghost" @click="aiStore.fetchModelList" :disabled="aiStore.loading">刷新模型</button>
        <button class="ghost" @click="aiStore.hidePanel">收起</button>
      </div>
    </div>

    <div class="tabs">
      <button class="tab" :class="{ active: tab === 'chat' }" @click="tab = 'chat'">对话</button>
      <button class="tab" :class="{ active: tab === 'expand' }" @click="openExpand()">展开</button>
      <button class="tab" :class="{ active: tab === 'summarize' }" @click="tab = 'summarize'">摘要</button>
      <button class="tab" :class="{ active: tab === 'search' }" @click="tab = 'search'">搜索</button>
    </div>

    <div class="body">
      <AiChatPanel v-if="tab === 'chat'" :map-id="mapId" :selected-node-id="selectedNodeId" />

      <div v-else-if="tab === 'expand'" class="placeholder">
        点击“展开”会弹窗配置参数并执行。
      </div>

      <SummarizePanel
        v-else-if="tab === 'summarize'"
        :map-id="mapId"
        :selected-node-id="selectedNodeId"
        @updated-map="emit('refresh-map')"
      />

      <SemanticSearchBar v-else-if="tab === 'search'" :map-id="mapId" @pick-node="emit('focus-node', $event)" />
    </div>

    <ExpandNodeDialog
      :visible="expandVisible"
      :map-id="mapId"
      :node-id="selectedNodeId"
      @cancel="expandVisible = false"
      @success="handleExpanded"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAIStore } from '@/stores/aiStore'
import ModelSelector from '@/components/ai/ModelSelector.vue'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'
import ExpandNodeDialog from '@/components/ai/ExpandNodeDialog.vue'
import SummarizePanel from '@/components/ai/SummarizePanel.vue'
import SemanticSearchBar from '@/components/ai/SemanticSearchBar.vue'

defineProps<{
  mapId?: string
  selectedNodeId?: string
}>()

const emit = defineEmits<{
  (e: 'refresh-map'): void
  (e: 'focus-node', nodeId: string): void
}>()

const aiStore = useAIStore()
const tab = ref<'chat' | 'expand' | 'summarize' | 'search'>('chat')
const expandVisible = ref(false)

onMounted(() => {
  void aiStore.init()
})

const openExpand = () => {
  tab.value = 'expand'
  expandVisible.value = true
}

const handleExpanded = () => {
  expandVisible.value = false
  emit('refresh-map')
}
</script>

<style scoped>
.ai-panel {
  position: fixed;
  top: 0;
  right: 0;
  height: 100vh;
  width: 420px;
  max-width: 92vw;
  transform: translateX(100%);
  transition: transform 160ms ease;
  background: #fff;
  border-left: 1px solid #e7e7e7;
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.10);
  z-index: 1500;
  display: flex;
  flex-direction: column;
}

.ai-panel.open {
  transform: translateX(0);
}

.topbar {
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.brand {
  font-weight: 900;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 10px;
  background: #111;
  color: #fff;
}

.right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ghost {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 10px;
  padding: 6px 10px;
  font-size: 12px;
  cursor: pointer;
}

.ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-bottom: 1px solid #eee;
}

.tab {
  padding: 10px 6px;
  background: #fff;
  border: none;
  border-right: 1px solid #eee;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
}

.tab:last-child {
  border-right: none;
}

.tab.active {
  background: #f3f9ff;
  color: #0b61c2;
}

.body {
  flex: 1;
  min-height: 0;
}

.placeholder {
  padding: 12px;
  color: #666;
  font-size: 12px;
}
</style>

