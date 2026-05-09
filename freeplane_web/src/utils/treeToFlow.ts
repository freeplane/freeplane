import { Position, type Edge, type Node } from '@vue-flow/core'
import type { MindMapNode } from '@/types/mindmap'

export const treeToFlow = (root: MindMapNode) => {
  const nodes: Node[] = []
  const edges: Edge[] = []

  const LEVEL_GAP = 280
  const SIBLING_GAP = 40
  const NODE_WIDTH = 220
  const NODE_HEIGHT = 56
  const ROOT_X = 80
  const ROOT_Y = 80
  const BRANCH_COLORS = [
    { line: '#5B8FF9', border: '#7DA4FA', bg: '#F4F8FF' },
    { line: '#5AD8A6', border: '#7FDFB8', bg: '#F3FCF8' },
    { line: '#F6BD16', border: '#F7CA4A', bg: '#FFFBEF' },
    { line: '#E8684A', border: '#EE8A74', bg: '#FFF5F2' },
    { line: '#6DC8EC', border: '#8DD5F0', bg: '#F1FBFF' },
    { line: '#9270CA', border: '#A78AD4', bg: '#F7F3FC' }
  ] as const

  const getVisibleChildren = (node: MindMapNode) =>
    node.folded ? [] : (node.children || [])

  // 将缓存移到函数内部，确保每次调用都重新计算
  const subtreeHeightCache = new Map<string, number>()
  const getSubtreeHeight = (node: MindMapNode): number => {
    const cached = subtreeHeightCache.get(node.id)
    if (cached !== undefined) return cached

    const children = getVisibleChildren(node)
    if (children.length === 0) {
      subtreeHeightCache.set(node.id, NODE_HEIGHT)
      return NODE_HEIGHT
    }

    let totalChildrenHeight = 0
    children.forEach((child) => {
      totalChildrenHeight += getSubtreeHeight(child)
    })
    totalChildrenHeight += SIBLING_GAP * (children.length - 1)

    const height = Math.max(NODE_HEIGHT, totalChildrenHeight)
    subtreeHeightCache.set(node.id, height)
    return height
  }

  const getBranchColor = (branchIndex: number) =>
    BRANCH_COLORS[((branchIndex % BRANCH_COLORS.length) + BRANCH_COLORS.length) % BRANCH_COLORS.length]

  const pushNode = (node: MindMapNode, level: number, centerY: number, branchIndex: number) => {
    const isFolded = !!node.folded
    const hasChildren = (node.children?.length || 0) > 0
    const isRoot = level === 0
    const color = getBranchColor(branchIndex)

    nodes.push({
      id: node.id,
      type: 'default',
      position: { x: ROOT_X + level * LEVEL_GAP, y: centerY - NODE_HEIGHT / 2 },
      sourcePosition: Position.Right,
      targetPosition: Position.Left,
      data: {
        label: node.text + (isFolded && hasChildren ? ' ▼' : ''),
        text: node.text,
        note: node.note,
        originalNode: node,
        folded: isFolded,
        branchIndex
      },
      style: {
        width: `${NODE_WIDTH}px`,
        minHeight: `${NODE_HEIGHT}px`,
        padding: isRoot ? '14px 22px' : '10px 14px',
        borderRadius: isRoot ? '24px' : '10px',
        border: isRoot
          ? '2px solid #9ca3af'
          : (isFolded ? `1.8px dashed ${color.border}` : `1.8px solid ${color.border}`),
        background: isRoot ? '#ffffff' : (isFolded ? '#f8fafc' : color.bg),
        color: '#334155',
        fontFamily: '"SimSun", "Songti SC", "STSong", "宋体", "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "Noto Sans", "PingFang SC", "Microsoft YaHei", sans-serif',
        fontSize: isRoot ? '26px' : (level === 1 ? '22px' : '20px'),
        fontWeight: isRoot ? 600 : (level === 1 ? 500 : 400),
        lineHeight: isRoot ? '34px' : (level === 1 ? '30px' : '26px'),
        textAlign: 'center' as const,
        whiteSpace: 'normal' as const,
        wordBreak: 'break-word' as const,
        boxShadow: isRoot ? '0 2px 6px rgba(15, 23, 42, 0.08)' : '0 1px 3px rgba(15, 23, 42, 0.06)',
        opacity: isFolded ? 0.95 : 1
      },
      connectable: true
    })
  }

  const pushEdge = (parentId: string, nodeId: string, branchIndex: number, depth: number) => {
    const color = getBranchColor(branchIndex)
    edges.push({
      id: `${parentId}-${nodeId}`,
      source: parentId,
      target: nodeId,
      type: 'smoothstep',
      style: {
        stroke: color.line,
        strokeWidth: depth <= 1 ? 2.2 : 1.8,
        opacity: depth <= 1 ? 0.8 : 0.65
      }
    })
  }

  const placeSubtree = (
    node: MindMapNode,
    level: number,
    topY: number,
    parentId: string | null,
    branchIndex: number
  ) => {
    const subtreeHeight = getSubtreeHeight(node)
    const centerY = topY + subtreeHeight / 2
    pushNode(node, level, centerY, branchIndex)

    if (parentId) {
      pushEdge(parentId, node.id, branchIndex, level)
    }

    const children = getVisibleChildren(node)
    let childTop = topY
    children.forEach((child, index) => {
      const childHeight = getSubtreeHeight(child)
      const nextBranchIndex = level === 0 ? index : branchIndex
      placeSubtree(child, level + 1, childTop, node.id, nextBranchIndex)
      childTop += childHeight + SIBLING_GAP
    })
  }

  placeSubtree(root, 0, ROOT_Y, null, 0)
  return { nodes, edges }
}