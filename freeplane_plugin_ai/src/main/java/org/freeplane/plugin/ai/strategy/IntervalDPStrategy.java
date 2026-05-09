package org.freeplane.plugin.ai.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 区间动态规划策略 - 优化兄弟节点批量处理
 * 
 * <p>算法原理：
 * <p>对于同一父节点的兄弟节点序列，使用区间DP找到最优的批量处理方案。
 * 
 * <p>状态定义：
 * <pre>
 * dp[i][j] = 处理兄弟节点区间 [i, j] 的最小复杂度
 * </pre>
 * 
 * <p>状态转移：
 * <pre>
 * dp[i][j] = min{
 *   dp[i][k] + dp[k+1][j],           // 分割点 k
 *   cost(batchTool(i, j))            // 使用批量工具处理整个区间
 * }
 * 其中 i ≤ k < j
 * </pre>
 * 
 * <p>复杂度分析：
 * <ul>
 *   <li>时间复杂度：O(n³)，n为兄弟节点数</li>
 *   <li>空间复杂度：O(n²)</li>
 *   <li>适用规模：n ≤ 50</li>
 * </ul>
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class IntervalDPStrategy implements ToolExecutionStrategy {
    
    private static final int MAX_SIBLING_COUNT = 50;
    
    @Override
    public boolean supports(String toolName, Map<String, Object> parameters) {
        // 支持批量创建和批量编辑
        if (!"createNodes".equals(toolName) && !"edit".equals(toolName)) {
            return false;
        }
        
        // 检查兄弟节点数量
        int siblingCount = getSiblingCount(parameters);
        return siblingCount >= 3 && siblingCount <= MAX_SIBLING_COUNT;
    }
    
    @Override
    public Object execute(String toolName, Map<String, Object> parameters) {
        long startTime = System.currentTimeMillis();
        
        List<SiblingNode> siblings = getSiblingNodes(parameters);
        int n = siblings.size();
        
        // DP表：dp[i][j] 表示处理区间 [i, j] 的最小成本
        long[][] dp = new long[n][n];
        int[][] splitPoint = new int[n][n];
        
        // 初始化：单个节点
        for (int i = 0; i < n; i++) {
            dp[i][i] = siblings.get(i).getIndividualCost();
            splitPoint[i][i] = -1; // -1 表示不分割
        }
        
        // 区间DP
        for (int len = 2; len <= n; len++) {          // 区间长度
            for (int i = 0; i <= n - len; i++) {      // 左端点
                int j = i + len - 1;                   // 右端点
                dp[i][j] = Long.MAX_VALUE;
                
                // 枚举分割点
                for (int k = i; k < j; k++) {
                    long cost = dp[i][k] + dp[k+1][j];
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                        splitPoint[i][j] = k;
                    }
                }
                
                // 尝试批量工具处理
                long batchCost = computeBatchCost(siblings.subList(i, j+1));
                if (batchCost < dp[i][j]) {
                    dp[i][j] = batchCost;
                    splitPoint[i][j] = -2; // -2 表示批量处理
                }
            }
        }
        
        // 回溯构建方案
        List<BatchOperation> operations = reconstructSolution(siblings, splitPoint, 0, n-1);
        
        long elapsed = System.currentTimeMillis() - startTime;
        double totalCost = dp[0][n-1];
        
        // 构建优化结果
        List<OptimizedToolCall.ToolCallStep> steps = buildToolCallSteps(operations, parameters);
        
        return new OptimizedToolCall(
            "IntervalDP",
            steps,
            elapsed,
            totalCost
        );
    }
    
    @Override
    public int getPriority() {
        return StrategyPriority.INTERVAL_DP;
    }
    
    @Override
    public String getStrategyName() {
        return "IntervalDP";
    }
    
    /**
     * 计算批量处理成本
     * <p>批量工具通常有折扣：cost = base_cost * log(n)
     */
    private long computeBatchCost(List<SiblingNode> nodes) {
        if (nodes.isEmpty()) {
            return 0;
        }
        
        // 假设批量工具的基础成本是单个节点平均成本的 1.5 倍
        long sumCost = 0;
        for (SiblingNode node : nodes) {
            sumCost += node.getIndividualCost();
        }
        long avgCost = sumCost / nodes.size();
        
        // 批量折扣：log2(n)
        double discount = Math.log(nodes.size()) / Math.log(2);
        return (long) (avgCost * 1.5 * discount);
    }
    
    /**
     * 回溯构建最优方案
     */
    private List<BatchOperation> reconstructSolution(
            List<SiblingNode> siblings, int[][] splitPoint, int i, int j) {
        
        List<BatchOperation> operations = new ArrayList<>();
        
        if (i > j) {
            return operations;
        }
        
        int split = splitPoint[i][j];
        
        if (split == -1) {
            // 单个节点
            operations.add(new BatchOperation(
                BatchOperation.Type.SINGLE,
                siblings.subList(i, i+1)
            ));
        } else if (split == -2) {
            // 批量处理
            operations.add(new BatchOperation(
                BatchOperation.Type.BATCH,
                siblings.subList(i, j+1)
            ));
        } else {
            // 分割点：递归处理左右子区间
            operations.addAll(reconstructSolution(siblings, splitPoint, i, split));
            operations.addAll(reconstructSolution(siblings, splitPoint, split+1, j));
        }
        
        return operations;
    }
    
    /**
     * 构建工具调用步骤
     */
    private List<OptimizedToolCall.ToolCallStep> buildToolCallSteps(
            List<BatchOperation> operations, Map<String, Object> parameters) {
        
        List<OptimizedToolCall.ToolCallStep> steps = new ArrayList<>();
        int order = 1;
        
        for (BatchOperation op : operations) {
            Map<String, Object> stepParams = Map.of(
                "operationType", op.getType().name(),
                "nodeCount", op.getNodes().size(),
                "nodeIds", op.getNodeIds()
            );
            
            steps.add(new OptimizedToolCall.ToolCallStep(
                "batchProcess",
                stepParams,
                order++
            ));
        }
        
        return steps;
    }
    
    // ========== 辅助方法 ==========
    
    private int getSiblingCount(Map<String, Object> parameters) {
        Object count = parameters.get("siblingCount");
        return count != null ? (Integer) count : 0;
    }
    
    @SuppressWarnings("unchecked")
    private List<SiblingNode> getSiblingNodes(Map<String, Object> parameters) {
        List<SiblingNode> nodes = (List<SiblingNode>) parameters.get("siblingNodes");
        return nodes != null ? nodes : createMockSiblingNodes(10);
    }
    
    private List<SiblingNode> createMockSiblingNodes(int count) {
        List<SiblingNode> nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nodes.add(new SiblingNode(
                "sibling_" + i,
                10 + i * 2  // individual cost
            ));
        }
        return nodes;
    }
    
    /**
     * 兄弟节点
     */
    public static class SiblingNode {
        private final String nodeId;
        private final long individualCost;
        
        public SiblingNode(String nodeId, long individualCost) {
            this.nodeId = nodeId;
            this.individualCost = individualCost;
        }
        
        public String getNodeId() { return nodeId; }
        public long getIndividualCost() { return individualCost; }
    }
    
    /**
     * 批量操作
     */
    public static class BatchOperation {
        public enum Type { SINGLE, BATCH }
        
        private final Type type;
        private final List<SiblingNode> nodes;
        
        public BatchOperation(Type type, List<SiblingNode> nodes) {
            this.type = type;
            this.nodes = nodes;
        }
        
        public Type getType() { return type; }
        public List<SiblingNode> getNodes() { return nodes; }
        
        public List<String> getNodeIds() {
            List<String> ids = new ArrayList<>();
            for (SiblingNode node : nodes) {
                ids.add(node.getNodeId());
            }
            return ids;
        }
    }
}
