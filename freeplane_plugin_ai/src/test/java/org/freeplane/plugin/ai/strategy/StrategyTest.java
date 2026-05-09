package org.freeplane.plugin.ai.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * 策略者模式单元测试
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class StrategyTest {
    
    private ToolStrategyDispatcher dispatcher;
    
    @Before
    public void setUp() {
        dispatcher = new ToolStrategyDispatcher();
    }
    
    // ========== 贪心+局部搜索策略测试 ==========
    
    @Test
    public void greedyStrategy_supports_createNodes() {
        GreedyLocalSearchStrategy strategy = new GreedyLocalSearchStrategy();
        
        Map<String, Object> params = Map.of(
            "availableToolCount", 14,
            "requiredNodes", Set.of("node1", "node2", "node3")
        );
        
        assertThat(strategy.supports("createNodes", params)).isTrue();
    }
    
    @Test
    public void greedyStrategy_notSupports_tooManyTools() {
        GreedyLocalSearchStrategy strategy = new GreedyLocalSearchStrategy();
        
        Map<String, Object> params = Map.of(
            "availableToolCount", 60,
            "requiredNodes", Set.of("node1")
        );
        
        assertThat(strategy.supports("createNodes", params)).isFalse();
    }
    
    @Test
    public void greedyStrategy_execute_returnsOptimizedCall() {
        GreedyLocalSearchStrategy strategy = new GreedyLocalSearchStrategy();
        
        List<GreedyLocalSearchStrategy.ToolProfile> tools = new ArrayList<>();
        tools.add(new GreedyLocalSearchStrategy.ToolProfile(
            "tool_0", 10, 5, Set.of("node1", "node2")
        ));
        tools.add(new GreedyLocalSearchStrategy.ToolProfile(
            "tool_1", 15, 8, Set.of("node3", "node4")
        ));
        
        Map<String, Object> params = Map.of(
            "availableToolCount", 2,
            "availableTools", tools,
            "requiredNodes", Set.of("node1", "node2", "node3", "node4")
        );
        
        Object result = strategy.execute("createNodes", params);
        
        assertThat(result).isInstanceOf(OptimizedToolCall.class);
        OptimizedToolCall optimized = (OptimizedToolCall) result;
        assertThat(optimized.getStrategyName()).isEqualTo("Greedy+LocalSearch");
        assertThat(optimized.getStepCount()).isGreaterThan(0);
        assertThat(optimized.getOptimizationTimeMs()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    public void greedyStrategy_priority() {
        GreedyLocalSearchStrategy strategy = new GreedyLocalSearchStrategy();
        assertThat(strategy.getPriority()).isEqualTo(StrategyPriority.GREEDY_OPTIMIZATION);
    }
    
    // ========== 区间DP策略测试 ==========
    
    @Test
    public void intervalDPStrategy_supports_validSiblingCount() {
        IntervalDPStrategy strategy = new IntervalDPStrategy();
        
        Map<String, Object> params = Map.of(
            "siblingCount", 10
        );
        
        assertThat(strategy.supports("createNodes", params)).isTrue();
    }
    
    @Test
    public void intervalDPStrategy_notSupports_tooFewSiblings() {
        IntervalDPStrategy strategy = new IntervalDPStrategy();
        
        Map<String, Object> params = Map.of(
            "siblingCount", 2
        );
        
        assertThat(strategy.supports("createNodes", params)).isFalse();
    }
    
    @Test
    public void intervalDPStrategy_execute_returnsBatchOperations() {
        IntervalDPStrategy strategy = new IntervalDPStrategy();
        
        List<IntervalDPStrategy.SiblingNode> siblings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            siblings.add(new IntervalDPStrategy.SiblingNode(
                "sibling_" + i,
                10 + i * 2
            ));
        }
        
        Map<String, Object> params = Map.of(
            "siblingCount", 10,
            "siblingNodes", siblings
        );
        
        Object result = strategy.execute("createNodes", params);
        
        assertThat(result).isInstanceOf(OptimizedToolCall.class);
        OptimizedToolCall optimized = (OptimizedToolCall) result;
        assertThat(optimized.getStrategyName()).isEqualTo("IntervalDP");
        assertThat(optimized.getStepCount()).isGreaterThan(0);
    }
    
    @Test
    public void intervalDPStrategy_priority() {
        IntervalDPStrategy strategy = new IntervalDPStrategy();
        assertThat(strategy.getPriority()).isEqualTo(StrategyPriority.INTERVAL_DP);
    }
    
    // ========== 并查集+LCA策略测试 ==========
    
    @Test
    public void unionFindLCAStrategy_supports_highOverlap() {
        UnionFindLCAStrategy strategy = new UnionFindLCAStrategy();
        
        Map<String, Object> params = Map.of(
            "toolOverlap", 0.5
        );
        
        assertThat(strategy.supports("readNodesWithDescendants", params)).isTrue();
    }
    
    @Test
    public void unionFindLCAStrategy_notSupports_lowOverlap() {
        UnionFindLCAStrategy strategy = new UnionFindLCAStrategy();
        
        Map<String, Object> params = Map.of(
            "toolOverlap", 0.2
        );
        
        assertThat(strategy.supports("readNodesWithDescendants", params)).isFalse();
    }
    
    @Test
    public void unionFindLCAStrategy_execute_returnsLCAOperations() {
        UnionFindLCAStrategy strategy = new UnionFindLCAStrategy();
        
        List<UnionFindLCAStrategy.TreeNode> nodes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            nodes.add(new UnionFindLCAStrategy.TreeNode(
                "node_" + i,
                i / 5
            ));
        }
        
        List<UnionFindLCAStrategy.ToolProfile> tools = new ArrayList<>();
        tools.add(new UnionFindLCAStrategy.ToolProfile(
            "tool_0", 10, 5, List.of("node_0", "node_1", "node_2")
        ));
        
        Map<String, Object> params = Map.of(
            "toolOverlap", 0.5,
            "treeNodes", nodes,
            "availableTools", tools
        );
        
        Object result = strategy.execute("readNodesWithDescendants", params);
        
        assertThat(result).isInstanceOf(OptimizedToolCall.class);
        OptimizedToolCall optimized = (OptimizedToolCall) result;
        assertThat(optimized.getStrategyName()).isEqualTo("UnionFind+LCA");
    }
    
    @Test
    public void unionFindLCAStrategy_priority() {
        UnionFindLCAStrategy strategy = new UnionFindLCAStrategy();
        assertThat(strategy.getPriority()).isEqualTo(StrategyPriority.UNION_FIND_LCA);
    }
    
    // ========== 完全背包DP策略测试 ==========
    
    @Test
    public void knapsackDPStrategy_supports_validBudget() {
        KnapsackDPStrategy strategy = new KnapsackDPStrategy();
        
        Map<String, Object> params = Map.of(
            "timeBudget", 1000L,
            "spaceBudget", 256L
        );
        
        assertThat(strategy.supports("createNodes", params)).isTrue();
    }
    
    @Test
    public void knapsackDPStrategy_notSupports_excessiveBudget() {
        KnapsackDPStrategy strategy = new KnapsackDPStrategy();
        
        Map<String, Object> params = Map.of(
            "timeBudget", 10000L,
            "spaceBudget", 10000L
        );
        
        assertThat(strategy.supports("createNodes", params)).isFalse();
    }
    
    @Test
    public void knapsackDPStrategy_execute_returnsOptimalCombination() {
        KnapsackDPStrategy strategy = new KnapsackDPStrategy();
        
        List<KnapsackDPStrategy.KnapsackTool> tools = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tools.add(new KnapsackDPStrategy.KnapsackTool(
                "tool_" + i,
                10 + i * 5,
                5 + i * 2,
                20 - i
            ));
        }
        
        Map<String, Object> params = Map.of(
            "timeBudget", 500L,
            "spaceBudget", 256L,
            "knapsackTools", tools
        );
        
        Object result = strategy.execute("createNodes", params);
        
        assertThat(result).isInstanceOf(OptimizedToolCall.class);
        OptimizedToolCall optimized = (OptimizedToolCall) result;
        assertThat(optimized.getStrategyName()).isEqualTo("KnapsackDP");
        assertThat(optimized.getStepCount()).isGreaterThan(0);
    }
    
    @Test
    public void knapsackDPStrategy_priority() {
        KnapsackDPStrategy strategy = new KnapsackDPStrategy();
        assertThat(strategy.getPriority()).isEqualTo(StrategyPriority.KNAPSACK_DP);
    }
    
    // ========== 策略调度器测试 ==========
    
    @Test
    public void dispatcher_selectsStrategyByPriority() {
        dispatcher.registerStrategy(new KnapsackDPStrategy());  // 优先级20
        dispatcher.registerStrategy(new GreedyLocalSearchStrategy());  // 优先级5
        
        Map<String, Object> params = Map.of(
            "availableToolCount", 14,
            "requiredNodes", Set.of("node1")
        );
        
        Object result = dispatcher.dispatch("createNodes", params);
        
        // 应该选择优先级更高的贪心策略
        assertThat(result).isInstanceOf(OptimizedToolCall.class);
        OptimizedToolCall optimized = (OptimizedToolCall) result;
        assertThat(optimized.getStrategyName()).isEqualTo("Greedy+LocalSearch");
    }
    
    @Test
    public void dispatcher_throwsException_whenNoStrategyMatches() {
        dispatcher.registerStrategy(new GreedyLocalSearchStrategy());
        
        Map<String, Object> params = Map.of(
            "availableToolCount", 100  // 超过贪心策略支持范围
        );
        
        assertThatThrownBy(() -> dispatcher.dispatch("createNodes", params))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("No strategy supports tool");
    }
    
    @Test
    public void dispatcher_registerAndUnregister() {
        dispatcher.registerStrategy(new GreedyLocalSearchStrategy());
        assertThat(dispatcher.getStrategyCount()).isEqualTo(1);
        
        dispatcher.unregisterStrategy("GreedyLocalSearch");
        assertThat(dispatcher.getStrategyCount()).isEqualTo(0);
    }
    
    @Test
    public void dispatcher_maintainsPriorityOrder() {
        dispatcher.registerStrategy(new KnapsackDPStrategy());  // 20
        dispatcher.registerStrategy(new GreedyLocalSearchStrategy());  // 5
        dispatcher.registerStrategy(new IntervalDPStrategy());  // 10
        
        List<ToolExecutionStrategy> strategies = dispatcher.getStrategies();
        
        assertThat(strategies).hasSize(3);
        assertThat(strategies.get(0).getPriority()).isEqualTo(5);   // Greedy
        assertThat(strategies.get(1).getPriority()).isEqualTo(10);  // Interval
        assertThat(strategies.get(2).getPriority()).isEqualTo(20);  // Knapsack
    }
}
