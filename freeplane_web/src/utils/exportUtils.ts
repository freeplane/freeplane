/**
 * 思维导图导出工具
 * 支持导出为 Freeplane .mm 格式和 JSON 格式
 */

import type { MindMapData, MindMapNode } from '@/types/mindmap'

/**
 * 导出为 Freeplane .mm 格式（XML）
 */
export function exportToMM(mindmap: MindMapData, filename?: string): void {
  const xmlContent = buildMMXML(mindmap)
  downloadFile(xmlContent, filename || `${mindmap.title}.mm`, 'application/xml')
}

/**
 * 导出为 JSON 格式
 */
export function exportToJSON(mindmap: MindMapData, filename?: string): void {
  const jsonContent = JSON.stringify(mindmap, null, 2)
  downloadFile(jsonContent, filename || `${mindmap.title}.json`, 'application/json')
}

/**
 * 构建 Freeplane .mm 格式的 XML 内容
 */
function buildMMXML(mindmap: MindMapData): string {
  const xmlDeclaration = '<?xml version="1.0" encoding="UTF-8"?>'
  const mapOpen = `<map version="1.0.1">`
  const mapClose = `</map>`
  
  const rootXML = nodeToXML(mindmap.root)
  
  return `${xmlDeclaration}
${mapOpen}
${rootXML}
${mapClose}`
}

/**
 * 递归将节点转换为 XML
 */
function nodeToXML(node: MindMapNode, indent: string = '  '): string {
  const attributes = [
    `ID="${node.id}"`,
    `TEXT="${escapeXML(node.text)}"`
  ].join(' ')
  
  let xml = `${indent}<node ${attributes}>`
  
  // 如果有备注
  if (node.note) {
    xml += `
${indent}  <richcontent TYPE="NOTE">
${indent}    <html>
${indent}      <body>
${indent}        <p>${escapeXML(node.note)}</p>
${indent}      </body>
${indent}    </html>
${indent}  </richcontent>`
  }
  
  // 如果有属性
  if (node.attributes && node.attributes.length > 0) {
    xml += `
${indent}  <attribute_registry>`
    for (const attr of node.attributes) {
      xml += `
${indent}    <attribute NAME="${escapeXML(attr.key)}" VALUE="${escapeXML(attr.value)}"/>`
    }
    xml += `
${indent}  </attribute_registry>`
  }
  
  // 递归处理子节点
  if (node.children && node.children.length > 0) {
    for (const child of node.children) {
      xml += '\n' + nodeToXML(child, indent + '  ')
    }
  }
  
  xml += `
${indent}</node>`
  
  return xml
}

/**
 * 转义 XML 特殊字符
 */
function escapeXML(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;')
}

/**
 * 下载文件到本地
 */
function downloadFile(content: string, filename: string, mimeType: string): void {
  const blob = new Blob([content], { type: `${mimeType};charset=utf-8` })
  const url = URL.createObjectURL(blob)
  
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'
  
  document.body.appendChild(link)
  link.click()
  
  // 清理
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 显示导出对话框（让用户选择格式）
 */
export function showExportDialog(mindmap: MindMapData): void {
  const format = prompt(
    '选择导出格式：\n\n1 - Freeplane .mm 格式（推荐）\n2 - JSON 备份格式\n\n请输入数字（1 或 2）：',
    '1'
  )
  
  if (format === '1') {
    exportToMM(mindmap)
  } else if (format === '2') {
    exportToJSON(mindmap)
  }
}

/**
 * 导入思维导图文件（支持 .json / .mm / .xml）
 * .json 在前端本地解析；.mm/.xml 由前端 DOMParser 解析
 */
export async function importMindMapFile(file: File): Promise<MindMapData> {
  const fileName = file.name.toLowerCase()
  const content = await file.text()
  if (fileName.endsWith('.json')) {
    return parseJSONMap(content)
  }
  if (fileName.endsWith('.mm') || fileName.endsWith('.xml')) {
    return parseMMMap(content, file.name)
  }
  throw new Error('仅支持 .json / .mm / .xml 文件')
}

function parseJSONMap(content: string): MindMapData {
  let parsed: unknown
  try {
    parsed = JSON.parse(content)
  } catch {
    throw new Error('JSON 文件格式错误')
  }
  if (!isMindMapData(parsed)) {
    throw new Error('JSON 结构不符合导图格式')
  }
  return parsed
}

function parseMMMap(content: string, filename: string): MindMapData {
  const parser = new DOMParser()
  const doc = parser.parseFromString(content, 'application/xml')
  const parseError = doc.querySelector('parsererror')
  if (parseError) {
    throw new Error('MM/XML 文件解析失败')
  }
  const rootElement = doc.querySelector('map > node')
  if (!rootElement) {
    throw new Error('未找到根节点')
  }
  const root = parseMMNode(rootElement, null)
  return {
    mapId: `imported-${Date.now()}`,
    title: filename.replace(/\.(mm|xml)$/i, '') || root.text || '导入导图',
    root,
  }
}

function parseMMNode(element: Element, parentId: string | null): MindMapNode {
  const id = element.getAttribute('ID') || `node-${Math.random().toString(36).slice(2, 9)}`
  const text = element.getAttribute('TEXT') || ''
  const noteContainer = Array.from(element.children).find(
    (child) => child.tagName.toLowerCase() === 'richcontent' && child.getAttribute('TYPE') === 'NOTE'
  )
  const noteNode = noteContainer?.querySelector('body')
  const note = noteNode?.textContent?.trim() || ''
  const children = Array.from(element.children)
    .filter((child) => child.tagName.toLowerCase() === 'node')
    .map((child) => parseMMNode(child, id))
  return {
    id,
    text,
    parentId,
    folded: false,
    note,
    children,
  }
}

function isMindMapData(value: unknown): value is MindMapData {
  if (!value || typeof value !== 'object') return false
  const data = value as Record<string, unknown>
  return typeof data.mapId === 'string'
    && typeof data.title === 'string'
    && isMindMapNode(data.root)
}

function isMindMapNode(value: unknown): value is MindMapNode {
  if (!value || typeof value !== 'object') return false
  const node = value as Record<string, unknown>
  const children = node.children
  return typeof node.id === 'string'
    && typeof node.text === 'string'
    && (typeof node.parentId === 'string' || node.parentId === null)
    && typeof node.folded === 'boolean'
    && typeof node.note === 'string'
    && Array.isArray(children)
    && children.every(isMindMapNode)
}
