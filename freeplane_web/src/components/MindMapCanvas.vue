<template>
	<div class="mindmap-container">
	  <VueFlow
		v-model:nodes="nodes"
		v-model:edges="edges"
		fit-view
		:min-zoom="0.2"
		:max-zoom="2"
		@node-double-click="handleDoubleClick"
		@node-click="handleNodeClick"
		@node-drag-stop="handleNodeDragStop"
		:nodes-draggable="true"
		:edges-updatable="false"
		:connection-line-style="{ stroke: '#94a3b8', strokeWidth: 1.5 }"
		:default-edge-options="{ type: 'smoothstep' }"
		@node-context-menu="handleNodeContextMenu"
		@pane-click="hideContextMenu"
	  >
		<Background variant="dots" />
		<Controls />
	  </VueFlow>
  
	  <NodeEditPanel
		:visible="editPanel.visible"
		:node-id="editPanel.nodeId"
		:initial-text="editPanel.text"
		@save="handleSaveEdit"
		@cancel="editPanel.visible = false"
	  />
  
	  <ActionModal
		:visible="actionModal.visible"
		:title="actionModal.title"
		:mode="actionModal.mode"
		@new-child="handleNewChildConfirm"
		@fold="handleFoldConfirm"
		@delete="handleDelete"
		@confirm-input="handleInputConfirm"
		@cancel="closeActionModal"
	  />
  
	  <Toolbar :vue-flow="vueFlow" />

	  <AiAutoPanel v-if="aiStore.panelVisible" :map-id="store.currentMap?.mapId" :selected-node-id="selectedNodeId" />
	  <AiChatPanel v-if="aiStore.panelVisible" :map-id="store.currentMap?.mapId" :selected-node-id="selectedNodeId" />
	  <AiBuildPanel v-if="aiStore.panelVisible" :map-id="store.currentMap?.mapId" :selected-node-id="selectedNodeId" />
	  <NodeContextMenu
		:visible="contextMenu.visible"
		:x="contextMenu.x"
		:y="contextMenu.y"
		:node-id="contextMenu.nodeId"
		@close="hideContextMenu"
		@ai-expand="handleAIExpand"
		@ai-summarize="handleAISummarize"
		@edit="openEditFromContext"
		@create-child="openCreateChildFromContext"
		@fold="handleFoldFromContext"
		@unfold="handleUnfoldFromContext"
		@delete="deleteFromContext"
	  />
	</div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, onUnmounted, watch, nextTick, provide } from 'vue'
  import { VueFlow, useVueFlow, type Node, type Edge } from '@vue-flow/core'
  import { Background } from '@vue-flow/background'
  import { Controls } from '@vue-flow/controls'
  
  import { useMapStore } from '@/stores/mapStore'
  import { useAIStore } from '@/stores/aiStore'
  import NodeEditPanel from './NodeEditPanel.vue'
  import Toolbar from './Toolbar.vue'
  import ActionModal from './ActionModal.vue'
  import AiAutoPanel from './ai/AiAutoPanel.vue'
  import AiChatPanel from './ai/AiChatPanel.vue'
  import AiBuildPanel from './ai/AiBuildPanel.vue'
  import NodeContextMenu from './NodeContextMenu.vue'
  import { treeToFlow } from '@/utils/treeToFlow'
  
  const store = useMapStore()
  const aiStore = useAIStore()
  const vueFlow = useVueFlow()
  
  const nodes = ref<Node[]>([])
  const edges = ref<Edge[]>([])
  const selectedNodeId = ref<string>('')
  
  // 拖拽位置缓存：以 mapId + nodeId 为 key，轮询刷新时优先恢复用户拖拽位置
  const dragPositionCache = ref<Record<string, { x: number; y: number }>>({})
  
  const getCacheKey = (nodeId: string) => {
	const mapId = store.currentMap?.mapId || 'default'
	return `${mapId}::${nodeId}`
  }
  
  const editPanel = ref({ visible: false, nodeId: '', text: '' })
  
  const actionModal = ref({
	visible: false,
	title: '',
	mode: 'choose' as 'choose' | 'input' | 'delete',
	targetNodeId: ''
  })

  const contextMenu = ref({
	visible: false,
	x: 0,
	y: 0,
	nodeId: ''
  })
  
  // **加强版更新**：每次都生成全新 nodes/edges 数组 + 拖拽位置缓存恢复
  const updateFlow = async () => {
	if (!store.currentMap?.root) return

	const { nodes: newNodes, edges: newEdges } = treeToFlow(store.currentMap.root)

	const prevCount = nodes.value.length

	// 轮询刷新时优先恢复用户拖拽位置；首次加载或节点数变化时使用布局坐标
	nodes.value = newNodes.map((node) => {
	  const isSelected = node.id === selectedNodeId.value
	  const cacheKey = getCacheKey(node.id)
	  const cachedPos = dragPositionCache.value[cacheKey]
	  const position = cachedPos ?? node.position
	  const baseStyle = typeof node.style === 'object' && node.style !== null ? node.style as Record<string, unknown> : {}
	  const style = isSelected
		? { ...(baseStyle as any), border: '3px solid #1e3a8a', boxShadow: '0 0 0 2px rgba(30,58,138,0.3)' }
		: (baseStyle as any)
	  return { ...node, position, selected: isSelected, style }
	})

	if (selectedNodeId.value && !nodes.value.some((node) => node.id === selectedNodeId.value)) {
	  selectedNodeId.value = ''
	}
	edges.value = [...newEdges]

	await nextTick()
	// 首次加载或节点数量变化时才居中，否则保持当前视图
	if (prevCount === 0 || nodes.value.length !== prevCount) {
	  vueFlow.fitView({ padding: 0.15, duration: 200 })
	}
  }
  
  const updateSelectedNodeId = () => {
	const allNodes: any[] = (vueFlow.nodes as any)
	const selected = Array.isArray(allNodes) ? allNodes.find((n: any) => n.selected) : undefined
	selectedNodeId.value = selected?.id || ''
  }

  const handleNodeClick = (event: any) => {
	if (!event?.node?.id) return
	selectedNodeId.value = event.node.id
  }

  // 节点拖拽结束时，将最新位置写入缓存以防止轮询刷新时回弹
  const handleNodeDragStop = (event: any) => {
	const node = event?.node
	if (!node?.id || !node?.position) return
	const cacheKey = getCacheKey(node.id)
	dragPositionCache.value[cacheKey] = { x: node.position.x, y: node.position.y }
  }

  const isEditableTarget = (target: EventTarget | null) => {
	if (!(target instanceof HTMLElement)) return false
	const tagName = target.tagName
	return (
	  tagName === 'INPUT' ||
	  tagName === 'TEXTAREA' ||
	  tagName === 'SELECT' ||
	  target.isContentEditable ||
	  !!target.closest('[contenteditable="true"]')
	)
  }

  const findNodeInMap = (nodeId: string) => {
	if (!store.currentMap?.root) return null
	const stack = [store.currentMap.root]
	while (stack.length) {
	  const current = stack.pop()!
	  if (current.id === nodeId) return current
	  if (current.children?.length) stack.push(...current.children)
	}
	return null
  }

  const getActiveNodeId = () => {
	const selected = vueFlow.nodes.value.find((n: any) => n.selected)
	if (selected?.id) return selected.id
	if (selectedNodeId.value) return selectedNodeId.value
	return ''
  }
  
  const handleKeyDown = (e: KeyboardEvent) => {
	if (e.key !== 'Tab') return
	if (e.repeat) return
	if (editPanel.value.visible || actionModal.value.visible) return
	if (isEditableTarget(e.target)) return
	e.preventDefault()
  
	const activeNodeId = getActiveNodeId()
	if (!activeNodeId) {
	  if (nodes.value.length > 0) {
		actionModal.value = { visible: true, title: '新建子节点', mode: 'input', targetNodeId: nodes.value[0].id }
	  }
	  return
	}
  
	const mapNode = findNodeInMap(activeNodeId)
	if (!mapNode) return
	const isFolded = !!mapNode.folded
  
	if (isFolded) {
	  void store.toggleNodeFold(activeNodeId)
	} else {
	  actionModal.value = { visible: true, title: '请选择操作', mode: 'choose', targetNodeId: activeNodeId }
	}
  }
  
  const closeActionModal = () => { actionModal.value.visible = false }
  
  const handleNewChildConfirm = () => {
	closeActionModal()
	actionModal.value = { visible: true, title: '新建子节点', mode: 'input', targetNodeId: actionModal.value.targetNodeId }
  }
  
  const handleFoldConfirm = () => {
	closeActionModal()
	if (actionModal.value.targetNodeId) {
	  store.toggleNodeFold(actionModal.value.targetNodeId)
	}
  }
  
  const handleDelete = () => {
	const currentMode = actionModal.value.mode
	const currentTargetId = actionModal.value.targetNodeId
	if (currentMode === 'choose') {
	  closeActionModal()
	  actionModal.value = { visible: true, title: '删除节点', mode: 'delete', targetNodeId: currentTargetId }
	} else if (currentMode === 'delete') {
	  if (currentTargetId) {
		store.deleteNode(currentTargetId)
	  }
	  closeActionModal()
	}
  }
  
  const handleInputConfirm = (text: string) => {
	closeActionModal()
	if (actionModal.value.targetNodeId && text) {
	  store.createNode(actionModal.value.targetNodeId, text)
	}
  }
  
  const handleDoubleClick = (event: any) => {
	const node = event.node
	selectedNodeId.value = node.id
	editPanel.value = { visible: true, nodeId: node.id, text: (node.data as any)?.label || '' }
  }
  
  const handleSaveEdit = (nodeId: string, text: string) => {
	store.editNode(nodeId, text)
	editPanel.value.visible = false
  }
  
  onMounted(() => {
	store.loadMap()
	store.startPolling()
	aiStore.init()
	window.addEventListener('keydown', handleKeyDown)
	window.addEventListener('click', hideContextMenu)
  })
  
  onUnmounted(() => {
	store.stopPolling()
	window.removeEventListener('keydown', handleKeyDown)
	window.removeEventListener('click', hideContextMenu)
  })
  
  watch(() => store.currentMap, updateFlow, { deep: true })
  watch(() => vueFlow.nodes.value.map((n: any) => ({ id: n.id, selected: n.selected })), updateSelectedNodeId, { deep: true })
  
  const focusNode = (nodeId: string) => {
	const target = vueFlow.findNode(nodeId)
	if (!target) return
	vueFlow.setCenter(target.position.x, target.position.y, { zoom: 1.2, duration: 300 })
  }

  provide('selectedNodeId', selectedNodeId)

  const handleNodeContextMenu = (event: any) => {
	if (!event?.event || !event?.node) return
	event.event.preventDefault()
	contextMenu.value = {
	  visible: true,
	  x: event.event.clientX,
	  y: event.event.clientY,
	  nodeId: event.node.id
	}
	selectedNodeId.value = event.node.id
  }

  const hideContextMenu = () => {
	contextMenu.value.visible = false
  }

  const handleAIExpand = async (nodeId: string) => {
	await aiStore.expandNode({ mapId: store.currentMap?.mapId, nodeId })
  }

  const handleAISummarize = async (nodeId: string) => {
	await aiStore.summarize({ mapId: store.currentMap?.mapId, nodeId, writeToNote: true })
  }

  const openEditFromContext = (nodeId: string) => {
	const node = vueFlow.findNode(nodeId)
	editPanel.value = { visible: true, nodeId, text: (node?.data as any)?.label || '' }
	hideContextMenu()
  }

  const openCreateChildFromContext = (nodeId: string) => {
	actionModal.value = { visible: true, title: '新建子节点', mode: 'input', targetNodeId: nodeId }
	hideContextMenu()
  }

  const handleFoldFromContext = (nodeId: string) => {
    void store.toggleNodeFold(nodeId)
    hideContextMenu()
  }

  const handleUnfoldFromContext = (nodeId: string) => {
    void store.toggleNodeFold(nodeId)
    hideContextMenu()
  }

  const deleteFromContext = (nodeId: string) => {
    store.deleteNode(nodeId)
    hideContextMenu()
  }

  // 组件挂载时加载当前地图
  onMounted(async () => {
    console.log('[MindMapCanvas] 组件挂载，开始加载地图...')
    try {
      await store.loadMap()
      console.log('[MindMapCanvas] 地图加载成功:', store.currentMap)
    } catch (error) {
      console.error('[MindMapCanvas] 地图加载失败:', error)
    }
    
    // 启动轮询，保持与 Freeplane 同步
    const pollInterval = setInterval(() => {
      console.log('[MindMapCanvas] 轮询刷新地图...')
      store.loadMap()
    }, 3000) // 每3秒刷新一次
    
    // 组件卸载时清理定时器
    onUnmounted(() => {
      clearInterval(pollInterval)
    })
  })
  </script>
  
  <style scoped>
  .mindmap-container {
	width: 100%;
	height: 100vh;
	background: #f8fafc;
  }
  </style>