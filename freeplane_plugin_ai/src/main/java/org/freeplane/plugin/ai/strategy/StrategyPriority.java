package org.freeplane.plugin.ai.strategy;

/**
 * 策略优先级常量
 * 
 * <p>定义各类策略的优先级范围，确保策略选择的确定性。
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public final class StrategyPriority {
    
    /**
     * 核心优化策略（贪心+局部搜索）
     * <p>优先级最高，适用于大多数场景
     */
    public static final int GREEDY_OPTIMIZATION = 5;
    
    /**
     * 区间动态规划策略
     * <p>适用于兄弟节点批量处理优化
     */
    public static final int INTERVAL_DP = 10;
    
    /**
     * 并查集+LCA优化策略
     * <p>适用于消除重复工具调用
     */
    public static final int UNION_FIND_LCA = 15;
    
    /**
     * 完全背包动态规划策略
     * <p>适用于资源约束下的最优工具选择
     */
    public static final int KNAPSACK_DP = 20;
    
    /**
     * 容错降级策略
     * <p>失败时触发
     */
    public static final int FALLBACK = 40;
    
    /**
     * 兜底策略
     * <p>优先级最低，当其他策略都不匹配时使用
     */
    public static final int DEFAULT = 100;
    
    private StrategyPriority() {
        // 防止实例化
    }
}
