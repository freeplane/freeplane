<template>
  <div v-if="visible" class="overlay" @click.self="emit('close')">
    <div class="dialog">
      <div class="header">
        <div class="title">配置 AI 模型</div>
        <button class="close-btn" @click="emit('close')">✕</button>
      </div>
      <div class="body">
        <label class="field">
          <span>Provider</span>
          <select v-model="providerName">
            <option value="openrouter">OpenRouter</option>
            <option value="gemini">Google Gemini</option>
            <option value="dashscope">DashScope (Qwen)</option>
            <option value="ernie">ERNIE (Baidu)</option>
            <option value="ollama">Ollama</option>
            <option value="custom">自定义</option>
          </select>
        </label>
        <label v-if="providerName === 'custom'" class="field">
          <span>Provider Name</span>
          <input v-model="customProviderName" placeholder="my_provider" />
        </label>
        <label class="field">
          <span>API Key</span>
          <input v-model="apiKey" :placeholder="providerName === 'ollama' ? '可选' : '必填'" />
        </label>
        <label class="field">
          <span>Base URL</span>
          <input v-model="baseUrl" placeholder="可选" />
        </label>
        <label class="field">
          <span>模型名</span>
          <input v-model="modelName" placeholder="如 qwen-max" />
        </label>
        <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
        <div class="footer">
          <button class="ghost" @click="emit('close')">取消</button>
          <button class="primary" @click="save" :disabled="busy">{{ busy ? '保存中...' : '保存并刷新' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useAIStore } from '@/stores/aiStore'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()
const aiStore = useAIStore()

const providerName = ref('openrouter')
const customProviderName = ref('')
const apiKey = ref('')
const baseUrl = ref('')
const modelName = ref('')
const busy = ref(false)
const errorMessage = ref('')

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    errorMessage.value = ''
  }
)

const save = async () => {
  errorMessage.value = ''
  const actualProviderName = providerName.value === 'custom' ? customProviderName.value.trim() : providerName.value
  if (!actualProviderName) {
    errorMessage.value = 'Provider Name 不能为空'
    return
  }
  if (providerName.value === 'custom' && !/^[a-zA-Z][a-zA-Z0-9_]*$/.test(actualProviderName)) {
    errorMessage.value = 'Provider Name 只能包含字母、数字、下划线，且必须字母开头'
    return
  }
  if (providerName.value !== 'ollama' && !apiKey.value.trim()) {
    errorMessage.value = 'API Key 不能为空'
    return
  }

  busy.value = true
  try {
    await aiStore.saveCustomModel({
      providerName: actualProviderName,
      apiKey: apiKey.value.trim(),
      baseUrl: baseUrl.value.trim() || undefined,
      modelName: modelName.value.trim() || undefined
    })
    emit('close')
  } catch (error: any) {
    errorMessage.value = error?.message || '保存失败，请在桌面端偏好设置中手动配置'
  } finally {
    busy.value = false
  }
}
</script>

<style scoped>
.overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.45); z-index: 4000; display: flex; align-items: center; justify-content: center; }
.dialog { width: 520px; max-width: calc(100vw - 24px); background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 12px 14px; border-bottom: 1px solid #e5e7eb; }
.title { font-size: 15px; font-weight: 700; }
.close-btn { border: 1px solid #d1d5db; background: #fff; border-radius: 8px; width: 30px; height: 30px; cursor: pointer; }
.body { padding: 14px; display: grid; gap: 10px; }
.field { display: grid; gap: 6px; font-size: 13px; }
.field input, .field select { border: 1px solid #d1d5db; border-radius: 8px; height: 36px; padding: 0 10px; }
.footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
.ghost, .primary { border-radius: 8px; padding: 8px 12px; font-weight: 600; cursor: pointer; }
.ghost { border: 1px solid #d1d5db; background: #fff; }
.primary { border: 1px solid #2563eb; background: #2563eb; color: #fff; }
.error { color: #dc2626; font-size: 12px; }
</style>
