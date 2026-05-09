package org.freeplane.plugin.ai.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 贪心+局部搜索优化工具调用策略
 * 
 * <p>算法原理：
 * <ol>
 *   <li>贪心阶段：按价值密度（覆盖节点数/成本）排序，依次选择工具</li>
 *   <li>局部搜索阶段：通过 Swap 邻域搜索优化解，尝试替换工具降低成本</li>
 * </ol>
 * 
 * <p>复杂度分析：
 * <ul>
 *   <li>时间复杂度：O(n·log n + k·n²)，n为工具数，k为局部搜索迭代次数</li>
 *   <li>空间复杂度：O(n)</li>
 *   <li>近似比：O(log n)</li>
 * </ul>
 * 
 * <p>适用场景：
 * <ul>
 *   <li>工具数量 ≤ 50（贪心近似效果好）</li>
 *   <li>需要快速响应（< 10ms）</li>
 *   <li>导图深度 ≤ 10层</li>
 * </ul>
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class GreedyLocalSearchStrategy implements ToolExecutionStrategy {
    
    private static final int MAX_LOCAL_SEARCH_ITERATIONS = 10;
    
    @Override
    public boolean supports(String toolName, Map<String, Object> parameters) {
        // 支持思维导图生成和节点展开场景
        if (!"createNodes".equals(toolName) && !"readNodesWithDescendants".equals(toolName)) {
            return false;
        }
        
        // 工具数量适中时使用贪心（≤ 50个工具）
        int toolCount = getToolCount(parameters);
        return toolCount <= 50;
    }
    
    @Override
    public Object execute(String toolName, Map<String, Object> parameters) {
        long startTime = System.currentTimeMillis();
        
        // 1. 贪心选择
        List<ToolProfile> selectedTools = greedySelect(parameters);
        
        // 2. 局部搜索优化
        selectedTools = localSearchOptimize(selectedTools, parameters);
        
        long elapsed = System.currentTimeMillis() - startTime;
        double cost = computeTotalCost(selectedTools);
        
        // 3. 构建优化结果
        List<OptimizedToolCall.ToolCallStep> steps = buildToolCallSteps(selectedTools, parameters);
        
        return new OptimizedToolCall(
            "Greedy+LocalSearch",
            steps,
            elapsed,
            cost
        );
    }
    
    @Override
    public int getPriority() {
        return StrategyPriority.GREEDY_OPTIMIZATION;
    }
    
    @Override
    public String getStrategyName() {
        return "GreedyLocalSearch";
    }
    
    /**
     * 贪心选择：按价值密度排序
     */
    private List<ToolProfile> greedySelect(Map<String, Object> parameters) {
        List<ToolProfile> availableTools = getAvailableTools(parameters);
        Set<String> requiredNodes = getRequiredNodes(parameters);
        
        List<ToolProfile> selected = new ArrayList<>();
        Set<String> coveredNodes = new HashSet<>();
        
        // 按价值密度降序排序
        availableTools.sort((t1, t2) -> {
            double density1 = computeValueDensity(t1, requiredNodes);
            double density2 = computeValueDensity(t2, requiredNodes);
            return Double.compare(density2, density1); // 降序
        });
        
        // 贪心选择
        for (ToolProfile tool : availableTools) {
            if (coveredNodes.containsAll(requiredNodes)) {
                break; // 已覆盖所有节点
            }
            
            // 检查该工具是否覆盖未覆盖的节点
            Set<String> toolCoverage = tool.getCoveredNodes();
            boolean hasNewCoverage = false;
            for (String node : toolCoverage) {
                if (requiredNodes.contains(node) && !coveredNodes.contains(node)) {
                    hasNewCoverage = true;
                    break;
                }
            }
            
            if (hasNewCoverage) {
                selected.add(tool);
                coveredNodes.addAll(toolCoverage);
            }
        }
        
        return selected;
    }
    
    /**
     * 局部搜索优化：Swap邻域
     */
    private List<ToolProfile> localSearchOptimize(List<ToolProfile> currentSolution, 
                                                    Map<String, Object> parameters) {
        List<ToolProfile> availableTools = getAvailableTools(parameters);
        Set<String> requiredNodes = getRequiredNodes(parameters);
        
        List<ToolProfile> bestSolution = new ArrayList<>(currentSolution);
        double bestCost = computeTotalCost(bestSolution);
        
        // 局部搜索迭代
        for (int iter = 0; iter < MAX_LOCAL_SEARCH_ITERATIONS; iter++) {
            boolean improved = false;
            
            // 尝试 Swap：移除一个工具，添加另一个工具
            for (int i = 0; i < bestSolution.size(); i++) {
                ToolProfile removeTool = bestSolution.get(i);
                
                for (ToolProfile addTool : availableTools) {
                    if (bestSolution.contains(addTool)) {
                        continue; // 已存在
                    }
                    
                    // 生成候选解
                    List<ToolProfile> candidate = new ArrayList<>(bestSolution);
                    candidate.remove(i);
                    candidate.add(addTool);
                    
                    // 检查是否仍然覆盖所有节点
                    if (!coversAllNodes(candidate, requiredNodes)) {
                        continue;
                    }
                    
                    // 计算成本
                    double candidateCost = computeTotalCost(candidate);
                    
                    // 如果改进，接受
                    if (candidateCost < bestCost) {
                        bestSolution = candidate;
                        bestCost = candidateCost;
                        improved = true;
                        break;
                    }
                }
                
                if (improved) {
                    break;
                }
            }
            
            // 如果没有改进，提前终止
            if (!improved) {
                break;
            }
        }
        
        return bestSolution;
    }
    
    /**
     * 计算价值密度 = (覆盖节点数 × 优先级权重) / 成本
     * 
     * <p>优先级权重基于实际性能数据（见 ToolPerformanceProfile）：
     * <ul>
     *   <li>树结构操作：权重1.0（95-100分，推荐优先调用）</li>
     *   <li>样式操作：权重0.94（88-94分）</li>
     *   <li>选择导航：权重0.90（85-90分）</li>
     *   <li>搜索操作：权重0.87（80-88分）</li>
     *   <li>过滤操作：权重0.81（75-82分）</li>
     *   <li>公式计算：权重0.72（65-75分，避免高频调用）</li>
     *   <li>导出操作：权重0.67（60-70分）</li>
     *   <li>批量操作：权重0.62（55-65分，最慢）</li>
     * </ul>
     */
    private double computeValueDensity(ToolProfile tool, Set<String> requiredNodes) {
        int coveredCount = 0;
        for (String node : tool.getCoveredNodes()) {
            if (requiredNodes.contains(node)) {
                coveredCount++;
            }
        }
        
        if (coveredCount == 0) {
            return 0;
        }
        
        // 获取工具的优先级权重（归一化到0-1范围）
        double priorityWeight = ToolPerformanceProfile.getPriority(tool.getName()) / 100.0;
        
        double cost = tool.getTimeCost() + tool.getSpaceCost();
        
        // 价值密度 = 覆盖节点数 × 优先级权重 / 成本
        return (coveredCount * priorityWeight) / cost;
    }
    
    /**
     * 检查是否覆盖所有必需节点
     */
    private boolean coversAllNodes(List<ToolProfile> tools, Set<String> requiredNodes) {
        Set<String> covered = new HashSet<>();
        for (ToolProfile tool : tools) {
            for (String node : tool.getCoveredNodes()) {
                if (requiredNodes.contains(node)) {
                    covered.add(node);
                }
            }
        }
        return covered.containsAll(requiredNodes);
    }
    
    /**
     * 计算总成本
     */
    private double computeTotalCost(List<ToolProfile> tools) {
        double totalCost = 0;
        for (ToolProfile tool : tools) {
            totalCost += tool.getTimeCost() + tool.getSpaceCost();
        }
        return totalCost;
    }
    
    /**
     * 构建工具调用步骤
     */
    private List<OptimizedToolCall.ToolCallStep> buildToolCallSteps(
            List<ToolProfile> tools, Map<String, Object> parameters) {
        List<OptimizedToolCall.ToolCallStep> steps = new ArrayList<>();
        
        for (int i = 0; i < tools.size(); i++) {
            ToolProfile tool = tools.get(i);
            steps.add(new OptimizedToolCall.ToolCallStep(
                tool.getName(),
                parameters,
                i + 1
            ));
        }
        
        return steps;
    }
    
    // ========== 辅助方法（从参数提取） ==========
    
    private int getToolCount(Map<String, Object> parameters) {
        // 简化实现：从参数中获取工具数量
        Object count = parameters.get("availableToolCount");
        return count != null ? (Integer) count : 14; // 默认14个工具
    }
    
    private List<ToolProfile> getAvailableTools(Map<String, Object> parameters) {
        // 简化实现：返回 Mock 工具列表
        @SuppressWarnings("unchecked")
        List<ToolProfile> tools = (List<ToolProfile>) parameters.get("availableTools");
        return tools != null ? tools : createMockTools();
    }
    
    private Set<String> getRequiredNodes(Map<String, Object> parameters) {
        @SuppressWarnings("unchecked")
        Set<String> nodes = (Set<String>) parameters.get("requiredNodes");
        return nodes != null ? nodes : Set.of("node1", "node2", "node3");
    }
    
    private List<ToolProfile> createMockTools() {
        List<ToolProfile> tools = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            tools.add(new ToolProfile(
                "tool_" + i,
                10 + i * 5,  // time cost
                5 + i * 2,   // space cost
                Set.of("node" + (i + 1))
            ));
        }
        return tools;
    }
    
    /**
     * 工具画像（简化版）
     */
    public static class ToolProfile {
        private final String name;
        private final long timeCost;
        private final long spaceCost;
        private final Set<String> coveredNodes;
        
        public ToolProfile(String name, long timeCost, long spaceCost, Set<String> coveredNodes) {
            this.name = name;
            this.timeCost = timeCost;
            this.spaceCost = spaceCost;
            this.coveredNodes = coveredNodes;
        }
        
        public String getName() { return name; }
        public long getTimeCost() { return timeCost; }
        public long getSpaceCost() { return spaceCost; }
        public Set<String> getCoveredNodes() { return coveredNodes; }
    }
}
