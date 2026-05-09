package org.freeplane.plugin.ai.strategy;

import java.util.Map;

/**
 * 工具执行策略接口
 * 
 * <p>策略者模式核心接口，定义不同工具执行策略的统一契约。
 * 每个策略根据参数特征决定是否支持该请求，并执行相应的优化算法。
 * 
 * <p>设计原则：
 * <ul>
 *   <li>开闭原则：对扩展开放（新增策略），对修改封闭（不改动现有代码）</li>
 *   <li>单一职责：每个策略只负责一种优化场景</li>
 *   <li>依赖倒置：高层模块依赖此接口，而非具体实现</li>
 * </ul>
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public interface ToolExecutionStrategy {
    
    /**
     * 判断该策略是否支持当前工具调用请求
     * 
     * @param toolName 工具名称（如 "createNodes", "edit"）
     * @param parameters 工具调用参数
     * @return true 表示该策略可以处理此请求
     */
    boolean supports(String toolName, Map<String, Object> parameters);
    
    /**
     * 执行策略优化逻辑
     * 
     * @param toolName 工具名称
     * @param parameters 工具调用参数
     * @return 优化后的工具调用方案
     */
    Object execute(String toolName, Map<String, Object> parameters);
    
    /**
     * 获取策略优先级（数值越小优先级越高）
     * 
     * <p>优先级建议：
     * <ul>
     *   <li>1-10: 核心业务策略（如贪心算法）</li>
     *   <li>11-20: 辅助业务策略（如区间DP）</li>
     *   <li>21-30: 优化策略（如并查集+LCA）</li>
     *   <li>31-40: 容错策略（如降级处理）</li>
     *   <li>90-100: 兜底策略</li>
     * </ul>
     * 
     * @return 优先级数值
     */
    int getPriority();
    
    /**
     * 获取策略名称（用于日志和监控）
     * 
     * @return 策略名称
     */
    String getStrategyName();
}
