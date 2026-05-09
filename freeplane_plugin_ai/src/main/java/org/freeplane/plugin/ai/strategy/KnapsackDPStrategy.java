package org.freeplane.plugin.ai.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 完全背包动态规划策略 - 资源约束下的最优工具选择
 * 
 * <p>算法原理：
 * <p>在时间/空间预算约束下，选择最优工具组合以最大化价值（覆盖节点数）。
 * 
 * <p>状态定义：
 * <pre>
 * dp[t][s] = 使用时间t和空间s能获得的最大价值
 * </pre>
 * 
 * <p>状态转移：
 * <pre>
 * dp[t][s] = max{ dp[t - time[i]][s - space[i]] + value[i] }
 * </pre>
 * 
 * <p>复杂度分析：
 * <ul>
 *   <li>时间复杂度：O(|F|·T·S)，|F|为工具数，T为时间预算，S为空间预算</li>
 *   <li>空间复杂度：O(T·S)</li>
 *   <li>适用规模：T·S ≤ 10^6</li>
 * </ul>
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class KnapsackDPStrategy implements ToolExecutionStrategy {
    
    private static final int MAX_BUDGET_PRODUCT = 1_000_000;
    
    @Override
    public boolean supports(String toolName, Map<String, Object> parameters) {
        // 支持所有需要资源优化的工具
        if (!"createNodes".equals(toolName) && !"edit".equals(toolName) && 
            !"readNodesWithDescendants".equals(toolName)) {
            return false;
        }
        
        // 检查预算约束是否紧张
        long timeBudget = getTimeBudget(parameters);
        long spaceBudget = getSpaceBudget(parameters);
        long product = timeBudget * spaceBudget;
        
        return product <= MAX_BUDGET_PRODUCT && product > 0;
    }
    
    @Override
    public Object execute(String toolName, Map<String, Object> parameters) {
        long startTime = System.currentTimeMillis();
        
        List<KnapsackTool> tools = getKnapsackTools(parameters);
        int timeBudget = (int) Math.min(getTimeBudget(parameters), 1000);
        int spaceBudget = (int) Math.min(getSpaceBudget(parameters), 1000);
        
        // 二维背包DP
        long[][] dp = new long[timeBudget + 1][spaceBudget + 1];
        Choice[][] choice = new Choice[timeBudget + 1][spaceBudget + 1];
        
        // 初始化
        for (int t = 0; t <= timeBudget; t++) {
            for (int s = 0; s <= spaceBudget; s++) {
                dp[t][s] = 0;
                choice[t][s] = null;
            }
        }
        
        // 完全背包DP（可以重复选择同一工具）
        for (KnapsackTool tool : tools) {
            int timeCost = tool.getTimeCost();
            int spaceCost = tool.getSpaceCost();
            
            // 使用实际优先级作为价值分数（而不是简单的覆盖节点数）
            // 优先级越高，价值越大，越容易被选中
            int priorityWeight = ToolPerformanceProfile.getPriority(tool.getName());
            int value = tool.getValue() * priorityWeight / 100;  // 归一化价值
            
            // 完全背包：正向遍历
            for (int t = timeCost; t <= timeBudget; t++) {
                for (int s = spaceCost; s <= spaceBudget; s++) {
                    long newValue = dp[t - timeCost][s - spaceCost] + value;
                    
                    if (newValue > dp[t][s]) {
                        dp[t][s] = newValue;
                        choice[t][s] = new Choice(tool, t - timeCost, s - spaceCost);
                    }
                }
            }
        }
        
        // 回溯找到最优组合
        List<KnapsackTool> selectedTools = reconstructSolution(choice, timeBudget, spaceBudget);
        
        long elapsed = System.currentTimeMillis() - startTime;
        double totalCost = computeTotalCost(selectedTools);
        
        // 构建优化结果
        List<OptimizedToolCall.ToolCallStep> steps = buildToolCallSteps(selectedTools, parameters);
        
        return new OptimizedToolCall(
            "KnapsackDP",
            steps,
            elapsed,
            totalCost
        );
    }
    
    @Override
    public int getPriority() {
        return StrategyPriority.KNAPSACK_DP;
    }
    
    @Override
    public String getStrategyName() {
        return "KnapsackDP";
    }
    
    /**
     * 回溯构建最优解
     */
    private List<KnapsackTool> reconstructSolution(Choice[][] choice, int timeBudget, int spaceBudget) {
        List<KnapsackTool> selected = new ArrayList<>();
        
        int t = timeBudget;
        int s = spaceBudget;
        
        while (t > 0 && s > 0 && choice[t][s] != null) {
            Choice ch = choice[t][s];
            selected.add(ch.getTool());
            
            t = ch.getPrevTime();
            s = ch.getPrevSpace();
        }
        
        return selected;
    }
    
    /**
     * 计算总成本
     */
    private double computeTotalCost(List<KnapsackTool> tools) {
        double totalCost = 0;
        for (KnapsackTool tool : tools) {
            totalCost += tool.getTimeCost() + tool.getSpaceCost();
        }
        return totalCost;
    }
    
    /**
     * 构建工具调用步骤
     */
    private List<OptimizedToolCall.ToolCallStep> buildToolCallSteps(
            List<KnapsackTool> tools, Map<String, Object> parameters) {
        
        List<OptimizedToolCall.ToolCallStep> steps = new ArrayList<>();
        int order = 1;
        
        for (KnapsackTool tool : tools) {
            Map<String, Object> stepParams = Map.of(
                "toolName", tool.getName(),
                "timeCost", tool.getTimeCost(),
                "spaceCost", tool.getSpaceCost(),
                "value", tool.getValue()
            );
            
            steps.add(new OptimizedToolCall.ToolCallStep(
                "knapsackOptimizedCall",
                stepParams,
                order++
            ));
        }
        
        return steps;
    }
    
    // ========== 辅助方法 ==========
    
    private long getTimeBudget(Map<String, Object> parameters) {
        Object budget = parameters.get("timeBudget");
        return budget != null ? (Long) budget : 5000L; // 默认5秒
    }
    
    private long getSpaceBudget(Map<String, Object> parameters) {
        Object budget = parameters.get("spaceBudget");
        return budget != null ? (Long) budget : 256L; // 默认256MB
    }
    
    @SuppressWarnings("unchecked")
    private List<KnapsackTool> getKnapsackTools(Map<String, Object> parameters) {
        List<KnapsackTool> tools = (List<KnapsackTool>) parameters.get("knapsackTools");
        return tools != null ? tools : createMockKnapsackTools();
    }
    
    private List<KnapsackTool> createMockKnapsackTools() {
        List<KnapsackTool> tools = new ArrayList<>();
        
        // 创建14个Mock工具
        for (int i = 0; i < 14; i++) {
            int timeCost = 10 + i * 5;
            int spaceCost = 5 + i * 2;
            int value = 20 - i; // 工具0价值最高
            
            tools.add(new KnapsackTool(
                "tool_" + i,
                timeCost,
                spaceCost,
                value
            ));
        }
        
        return tools;
    }
    
    /**
     * 背包工具
     */
    public static class KnapsackTool {
        private final String name;
        private final int timeCost;
        private final int spaceCost;
        private final int value;
        
        public KnapsackTool(String name, int timeCost, int spaceCost, int value) {
            this.name = name;
            this.timeCost = timeCost;
            this.spaceCost = spaceCost;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getTimeCost() { return timeCost; }
        public int getSpaceCost() { return spaceCost; }
        public int getValue() { return value; }
    }
    
    /**
     * 选择记录（用于回溯）
     */
    private static class Choice {
        private final KnapsackTool tool;
        private final int prevTime;
        private final int prevSpace;
        
        public Choice(KnapsackTool tool, int prevTime, int prevSpace) {
            this.tool = tool;
            this.prevTime = prevTime;
            this.prevSpace = prevSpace;
        }
        
        public KnapsackTool getTool() { return tool; }
        public int getPrevTime() { return prevTime; }
        public int getPrevSpace() { return prevSpace; }
    }
}
