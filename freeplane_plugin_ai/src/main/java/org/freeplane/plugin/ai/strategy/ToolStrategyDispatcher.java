package org.freeplane.plugin.ai.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.freeplane.core.util.LogUtils;

/**
 * 工具策略调度器
 * 
 * <p>根据工具名称和参数特征，按优先级遍历所有已注册策略，
 * 选择第一个支持该请求的策略执行。
 * 
 * <p>使用示例：
 * <pre>{@code
 * ToolStrategyDispatcher dispatcher = new ToolStrategyDispatcher();
 * dispatcher.registerStrategy(new GreedyLocalSearchStrategy());
 * dispatcher.registerStrategy(new IntervalDPStrategy());
 * 
 * Object result = dispatcher.dispatch("createNodes", parameters);
 * }</pre>
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class ToolStrategyDispatcher {
    
    private final List<ToolExecutionStrategy> strategies = new ArrayList<>();
    
    /**
     * 注册策略
     * 
     * @param strategy 要注册的策略
     */
    public void registerStrategy(ToolExecutionStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy");
        strategies.add(strategy);
        // 按优先级排序（数值越小优先级越高）
        Collections.sort(strategies, Comparator.comparingInt(ToolExecutionStrategy::getPriority));
        LogUtils.info("Registered strategy: " + strategy.getStrategyName() + 
                      " (priority=" + strategy.getPriority() + ")");
    }
    
    /**
     * 注销策略
     * 
     * @param strategyName 策略名称
     * @return true 如果成功注销
     */
    public boolean unregisterStrategy(String strategyName) {
        boolean removed = strategies.removeIf(s -> s.getStrategyName().equals(strategyName));
        if (removed) {
            LogUtils.info("Unregistered strategy: " + strategyName);
        }
        return removed;
    }
    
    /**
     * 分派工具调用请求到合适的策略
     * 
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 策略执行结果
     * @throws UnsupportedOperationException 如果没有策略支持该请求
     */
    public Object dispatch(String toolName, Map<String, Object> parameters) {
        for (ToolExecutionStrategy strategy : strategies) {
            if (strategy.supports(toolName, parameters)) {
                LogUtils.info("Selected strategy: " + strategy.getStrategyName() + 
                              " for tool: " + toolName);
                return strategy.execute(toolName, parameters);
            }
        }
        
        throw new UnsupportedOperationException(
            "No strategy supports tool: " + toolName + 
            " with parameters: " + parameters);
    }
    
    /**
     * 获取所有已注册的策略（只读）
     * 
     * @return 策略列表
     */
    public List<ToolExecutionStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }
    
    /**
     * 获取策略数量
     * 
     * @return 策略数量
     */
    public int getStrategyCount() {
        return strategies.size();
    }
}
