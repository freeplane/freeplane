/**
 * 模型选择器组件
 * 显示可用的 AI 模型列表，支持切换模型
 */

<template>
  <div class="model-selector" :class="{ compact }">
    <label v-if="!compact" class="selector-label">AI 模型：</label>
    <select 
      v-model="selectedModel" 
      @change="handleModelChange"
      :disabled="aiStore.loading"
      class="model-select"
    >
      <option value="" disabled>
        {{ aiStore.loading ? '加载中...' : '请选择模型' }}
      </option>
      <option 
        v-for="model in aiStore.modelList" 
        :key="model.providerName + '|' + model.modelName"
        :value="model.providerName + '|' + model.modelName"
      >
        {{ model.displayName }} {{ model.isFree ? '(免费)' : '' }}
      </option>
    </select>

    <button class="config-btn" type="button" @click="emit('open-config')">⚙️</button>
    
    <span v-if="!aiStore.hasConfiguredModels" class="warning">
      ⚠️ 未检测到已配置的 AI Provider，请先在偏好设置中配置 API Key
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useAIStore } from '@/stores/aiStore'

const aiStore = useAIStore()
const props = withDefaults(defineProps<{ compact?: boolean }>(), {
  compact: false
})
const compact = props.compact
const emit = defineEmits<{
  (e: 'open-config'): void
}>()

const selectedModel = computed({
  get: () => aiStore.currentModel,
  set: (value: string) => aiStore.switchModel(value)
})

const handleModelChange = () => {
  console.log('切换模型:', selectedModel.value)
  // 可选：切换模型时清空对话
  // aiStore.clearChat()
}

// 监听模型列表加载
watch(() => aiStore.modelList.length, (newLength) => {
  if (newLength > 0 && !aiStore.currentModel) {
    const m = aiStore.modelList[0]
    aiStore.currentModel = m.providerName + '|' + m.modelName
  }
})
</script>

<style scoped>
.model-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-selector.compact .model-select {
  min-width: 160px;
}

.selector-label {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.model-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  min-width: 200px;
}

.model-select:hover {
  border-color: #2196F3;
}

.model-select:focus {
  outline: none;
  border-color: #2196F3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
}

.model-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.warning {
  font-size: 12px;
  color: #ff9800;
  white-space: nowrap;
}

.config-btn {
  border: 1px solid #d0d7de;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  height: 32px;
  width: 32px;
}
</style>
