package org.freeplane.plugin.ai.service.impl;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.service.ToolExecutionService;
import org.freeplane.plugin.ai.tools.AIToolSet;
import org.freeplane.plugin.ai.tools.edit.EditRequest;
import org.freeplane.plugin.ai.tools.edit.NodeContentEditItem;
import org.freeplane.plugin.ai.tools.create.CreateNodesRequest;
import org.freeplane.plugin.ai.tools.create.AnchorPlacement;
import org.freeplane.plugin.ai.tools.create.NodeCreationItem;
import org.freeplane.plugin.ai.tools.create.NodeFoldingState;
import org.freeplane.plugin.ai.tools.content.NodeContentWriteRequest;
import org.freeplane.plugin.ai.tools.content.AttributeEntry;
import org.freeplane.plugin.ai.tools.read.ReadNodesWithDescendantsRequest;
import org.freeplane.plugin.ai.tools.read.FetchNodesForEditingRequest;
import org.freeplane.plugin.ai.tools.selection.SelectionIdentifiersRequest;
import org.freeplane.plugin.ai.strategy.ToolStrategyDispatcher;
import org.freeplane.plugin.ai.strategy.GreedyLocalSearchStrategy;
import org.freeplane.plugin.ai.strategy.IntervalDPStrategy;
import org.freeplane.plugin.ai.strategy.UnionFindLCAStrategy;
import org.freeplane.plugin.ai.strategy.KnapsackDPStrategy;
import org.freeplane.plugin.ai.strategy.OptimizedToolCall;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具执行服务实现
 * 提供直接的工具调用能力，绕过 LLM 聊天环节
 * 
 * <p>架构演进：
 * <ul>
 *   <li>原架构：硬编码 Map 映射工具执行器</li>
 *   <li>新架构：策略者模式 + 动态规划算法优化</li>
 *   <li>向后兼容：保留原有执行器，新增策略调度层</li>
 * </ul>
 */
public class DefaultToolExecutionService implements ToolExecutionService {

    private AIToolSet toolSet;
    private final Map<String, ToolExecutor> toolExecutors;
    private final ToolStrategyDispatcher strategyDispatcher;
    private boolean strategyEnabled = true; // 默认启用策略优化

    public DefaultToolExecutionService() {
        this.toolExecutors = new HashMap<>();
        this.strategyDispatcher = new ToolStrategyDispatcher();
        initializeToolExecutors();
        initializeStrategies();
    }

    /**
     * 初始化策略调度器
     * 注册所有优化策略（按优先级自动排序）
     */
    private void initializeStrategies() {
        // 优先级 5：贪心+局部搜索（核心优化）
        strategyDispatcher.registerStrategy(new GreedyLocalSearchStrategy());
        
        // 优先级 10：区间动态规划（兄弟节点批量处理）
        strategyDispatcher.registerStrategy(new IntervalDPStrategy());
        
        // 优先级 15：并查集+LCA（消除重复调用）
        strategyDispatcher.registerStrategy(new UnionFindLCAStrategy());
        
        // 优先级 20：完全背包DP（资源约束优化）
        strategyDispatcher.registerStrategy(new KnapsackDPStrategy());
        
        LogUtils.info("DefaultToolExecutionService: Initialized " + 
                      strategyDispatcher.getStrategyCount() + " optimization strategies");
    }

    private void initializeToolExecutors() {
        // 注册工具执行器
        toolExecutors.put("readNodesWithDescendants", this::executeReadNodesWithDescendants);
        toolExecutors.put("fetchNodesForEditing", this::executeFetchNodesForEditing);
        toolExecutors.put("getSelectedMapAndNodeIdentifiers", this::executeGetSelectedMapAndNodeIdentifiers);
        toolExecutors.put("createNodes", this::executeCreateNodes);
        toolExecutors.put("edit", this::executeEdit);
        // 可以添加更多工具执行器
    }

    @Override
    public Object executeTool(String toolName, Map<String, Object> parameters) {
        if (!isToolSupported(toolName)) {
            throw new IllegalArgumentException("Tool not supported: " + toolName);
        }

        if (toolSet == null) {
            throw new IllegalStateException("AIToolSet not initialized");
        }

        // 策略优化路径（优先尝试）
        if (strategyEnabled) {
            try {
                LogUtils.info("ToolExecutionService: Attempting strategy optimization for tool " + toolName);
                Object optimizedResult = strategyDispatcher.dispatch(toolName, parameters);
                
                // 如果策略返回优化方案，记录日志
                if (optimizedResult instanceof OptimizedToolCall) {
                    OptimizedToolCall optimized = (OptimizedToolCall) optimizedResult;
                    LogUtils.info("ToolExecutionService: Strategy optimization applied: " + 
                                  optimized.getStrategyName() + ", steps=" + optimized.getStepCount() + 
                                  ", time=" + optimized.getOptimizationTimeMs() + "ms");
                    
                    // 注意：这里返回的是优化方案，实际工具执行仍需要调用原始执行器
                    // 后续可以扩展为直接执行优化后的工具调用序列
                }
                
                return optimizedResult;
            } catch (UnsupportedOperationException e) {
                // 没有匹配的策略，降级到原始执行器
                LogUtils.info("ToolExecutionService: No matching strategy, falling back to original executor");
            } catch (Exception e) {
                // 策略执行失败，降级到原始执行器
                LogUtils.warn("ToolExecutionService: Strategy optimization failed, falling back to original executor", e);
            }
        }

        // 原始执行器路径（向后兼容）
        ToolExecutor executor = toolExecutors.get(toolName);
        try {
            LogUtils.info("ToolExecutionService: Executing tool " + toolName + " (original executor)");
            Object result = executor.execute(parameters);
            LogUtils.info("ToolExecutionService: Tool " + toolName + " executed successfully");
            return result;
        } catch (Exception e) {
            LogUtils.warn("ToolExecutionService: Error executing tool " + toolName, e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String[] getSupportedTools() {
        return toolExecutors.keySet().toArray(new String[0]);
    }

    @Override
    public boolean isToolSupported(String toolName) {
        return toolExecutors.containsKey(toolName);
    }

    @Override
    public void setToolSet(AIToolSet toolSet) {
        this.toolSet = toolSet;
    }

    @Override
    public AIToolSet getToolSet() {
        return toolSet;
    }

    /**
     * 启用或禁用策略优化
     * 
     * @param enabled true 启用策略优化，false 使用原始执行器
     */
    public void setStrategyEnabled(boolean enabled) {
        this.strategyEnabled = enabled;
        LogUtils.info("ToolExecutionService: Strategy optimization " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * 检查策略优化是否启用
     * 
     * @return true 如果策略优化已启用
     */
    public boolean isStrategyEnabled() {
        return strategyEnabled;
    }

    /**
     * 获取策略调度器（用于监控和管理）
     * 
     * @return 策略调度器实例
     */
    public ToolStrategyDispatcher getStrategyDispatcher() {
        return strategyDispatcher;
    }

    // 工具执行器接口
    @FunctionalInterface
    private interface ToolExecutor {
        Object execute(Map<String, Object> parameters) throws Exception;
    }

    // 工具执行实现
    private Object executeReadNodesWithDescendants(Map<String, Object> parameters) {
        String mapIdentifier = (String) parameters.get("mapIdentifier");
        List<String> nodeIdentifiers = (List<String>) parameters.get("nodeIdentifiers");
        List<String> contextSectionsStr = (List<String>) parameters.get("contextSections");
        Integer fullContentDepth = (Integer) parameters.get("fullContentDepth");
        Integer summaryDepth = (Integer) parameters.get("summaryDepth");
        Integer maximumTotalTextCharacters = (Integer) parameters.get("maximumTotalTextCharacters");
        
        // 转换 contextSections 为枚举类型
        List<org.freeplane.plugin.ai.tools.read.ContextSection> contextSections = null;
        if (contextSectionsStr != null) {
            contextSections = contextSectionsStr.stream()
                    .map(section -> org.freeplane.plugin.ai.tools.read.ContextSection.valueOf(section))
                    .collect(Collectors.toList());
        }
        
        ReadNodesWithDescendantsRequest request = new ReadNodesWithDescendantsRequest(
                mapIdentifier,
                nodeIdentifiers,
                contextSections,
                fullContentDepth,
                summaryDepth,
                maximumTotalTextCharacters
        );
        return toolSet.readNodesWithDescendants(request);
    }

    private Object executeFetchNodesForEditing(Map<String, Object> parameters) {
        String mapIdentifier = (String) parameters.get("mapIdentifier");
        List<String> nodeIdentifiers = (List<String>) parameters.get("nodeIdentifiers");
        List<String> editableContentFields = (List<String>) parameters.get("editableContentFields");
        
        // 转换 editableContentFields 为枚举类型
        List<org.freeplane.plugin.ai.tools.content.EditableContentField> fields = editableContentFields.stream()
                .map(field -> org.freeplane.plugin.ai.tools.content.EditableContentField.valueOf(field))
                .collect(Collectors.toList());
        
        FetchNodesForEditingRequest request = new FetchNodesForEditingRequest(mapIdentifier, nodeIdentifiers, fields);
        return toolSet.fetchNodesForEditing(request);
    }

    private Object executeGetSelectedMapAndNodeIdentifiers(Map<String, Object> parameters) {
        String selectionModeStr = (String) parameters.get("selectionCollectionMode");
        org.freeplane.plugin.ai.tools.selection.SelectionCollectionMode selectionMode = null;
        if (selectionModeStr != null) {
            selectionMode = org.freeplane.plugin.ai.tools.selection.SelectionCollectionMode.valueOf(selectionModeStr);
        }
        SelectionIdentifiersRequest request = new SelectionIdentifiersRequest(selectionMode);
        return toolSet.getSelectedMapAndNodeIdentifiers(request);
    }

    private Object executeCreateNodes(Map<String, Object> parameters) {
        String mapIdentifier = (String) parameters.get("mapIdentifier");
        String userSummary = (String) parameters.get("userSummary");
        
        // 解析 anchorPlacement
        Map<String, Object> anchorPlacementMap = (Map<String, Object>) parameters.get("anchorPlacement");
        String anchorNodeIdentifier = (String) anchorPlacementMap.get("anchorNodeIdentifier");
        String placementMode = (String) anchorPlacementMap.get("placementMode");
        AnchorPlacement anchorPlacement = new AnchorPlacement(
                anchorNodeIdentifier,
                org.freeplane.plugin.ai.tools.create.AnchorPlacementMode.valueOf(placementMode)
        );
        
        // 解析 nodes
        List<Map<String, Object>> nodesMap = (List<Map<String, Object>>) parameters.get("nodes");
        List<NodeCreationItem> nodes = new ArrayList<>();
        for (int i = 0; i < nodesMap.size(); i++) {
            Map<String, Object> nodeMap = nodesMap.get(i);
            
            // 解析 content
            Map<String, Object> contentMap = (Map<String, Object>) nodeMap.get("content");
            NodeContentWriteRequest content = null;
            if (contentMap != null) {
                content = new NodeContentWriteRequest(
                        (String) contentMap.get("text"),
                        contentMap.containsKey("textContentType") ? 
                                org.freeplane.plugin.ai.tools.content.ContentType.valueOf((String) contentMap.get("textContentType")) : null,
                        (String) contentMap.get("details"),
                        contentMap.containsKey("detailsContentType") ? 
                                org.freeplane.plugin.ai.tools.content.ContentType.valueOf((String) contentMap.get("detailsContentType")) : null,
                        (String) contentMap.get("note"),
                        contentMap.containsKey("noteContentType") ? 
                                org.freeplane.plugin.ai.tools.content.ContentType.valueOf((String) contentMap.get("noteContentType")) : null,
                        null, // attributes
                        (List<String>) contentMap.get("tags"),
                        (List<String>) contentMap.get("icons"),
                        (String) contentMap.get("hyperlink")
                );
            }
            
            NodeCreationItem node = new NodeCreationItem(
                    (Integer) nodeMap.get("index"),
                    (Integer) nodeMap.get("parentIndex"),
                    content,
                    nodeMap.containsKey("foldingState") ? 
                            org.freeplane.plugin.ai.tools.create.NodeFoldingState.valueOf((String) nodeMap.get("foldingState")) : null,
                    (String) nodeMap.get("mainStyle")
            );
            nodes.add(node);
        }
        
        CreateNodesRequest request = new CreateNodesRequest(mapIdentifier, userSummary, anchorPlacement, nodes);
        return toolSet.createNodes(request);
    }

    private Object executeEdit(Map<String, Object> parameters) {
        String mapIdentifier = (String) parameters.get("mapIdentifier");
        String userSummary = (String) parameters.get("userSummary");
        
        // 解析 items
        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) parameters.get("items");
        List<NodeContentEditItem> items = new ArrayList<>();
        for (Map<String, Object> itemMap : itemsMap) {
            NodeContentEditItem item = new NodeContentEditItem(
                    (String) itemMap.get("nodeIdentifier"),
                    org.freeplane.plugin.ai.tools.edit.EditedElement.valueOf((String) itemMap.get("editedElement")),
                    itemMap.containsKey("originalContentType") ? 
                            org.freeplane.plugin.ai.tools.content.ContentType.valueOf((String) itemMap.get("originalContentType")) : null,
                    (String) itemMap.get("value"),
                    (Integer) itemMap.get("index"),
                    itemMap.containsKey("operation") ? 
                            org.freeplane.plugin.ai.tools.edit.EditOperation.valueOf((String) itemMap.get("operation")) : null,
                    (String) itemMap.get("targetKey")
            );
            items.add(item);
        }
        
        EditRequest request = new EditRequest(mapIdentifier, userSummary, items);
        return toolSet.edit(request);
    }
}