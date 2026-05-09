<template>
	<div
	  v-if="visible"
	  class="context-menu"
	  :style="{ left: x + 'px', top: y + 'px' }"
	>
	  <div class="menu-item" @click="handleEdit">
		✏️ 编辑节点
	  </div>
	  <div class="menu-item" @click="handleCreateChild">
		➕ 新建子节点
	  </div>
	  <div class="menu-item" @click="handleFold">
		▼ 折叠节点
	  </div>
	  <div class="menu-item" @click="handleUnfold">
		▶ 展开节点
	  </div>
	  <div class="menu-item" @click="handleDelete">
		🗑️ 删除节点
	  </div>
	  <div class="menu-item ai-item" @click="handleAIExpand">
		🤖 AI 展开节点
	  </div>
	  <div class="menu-item ai-item" @click="handleAISummarize">
		📝 AI 分支摘要
	  </div>
	  <div class="divider"></div>
	  <div class="menu-item" @click="emit('close')">
		❌ 关闭
	  </div>
	</div>
  </template>
  
  <script setup lang="ts">
  const props = defineProps<{
	visible: boolean
	x: number
	y: number
	nodeId: string
  }>()
  
  const emit = defineEmits<{
	(e: 'edit', nodeId: string): void
	(e: 'create-child', nodeId: string): void
	(e: 'fold', nodeId: string): void
	(e: 'unfold', nodeId: string): void
	(e: 'delete', nodeId: string): void
	(e: 'ai-expand', nodeId: string): void
	(e: 'ai-summarize', nodeId: string): void
	(e: 'close'): void
  }>()
  
  const handleEdit = () => {
	emit('edit', props.nodeId)
  }
  
  const handleCreateChild = () => {
	emit('create-child', props.nodeId)
  }
  
  const handleFold = () => {
	emit('fold', props.nodeId)
	emit('close')
  }
  
  const handleUnfold = () => {
	emit('unfold', props.nodeId)
	emit('close')
  }
  
  const handleDelete = () => {
	emit('delete', props.nodeId)
  }
  
  const handleAIExpand = () => {
	emit('ai-expand', props.nodeId)
	emit('close')
  }

  const handleAISummarize = () => {
	emit('ai-summarize', props.nodeId)
	emit('close')
  }
  </script>
  
  <style scoped>
  .context-menu {
	position: fixed;
	background: #fff;
	border: 1px solid #ccc;
	border-radius: 6px;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
	padding: 4px 0;
	z-index: 9999;
	min-width: 160px;
	font-size: 14px;
  }
  
  .menu-item {
	padding: 8px 16px;
	cursor: pointer;
	display: flex;
	align-items: center;
	gap: 8px;
  }
  
  .menu-item:hover {
	background: #f0f0f0;
  }
  
  .ai-item {
	color: #1890ff;
  }
  
  .divider {
	height: 1px;
	background: #eee;
	margin: 4px 8px;
  }
  </style>