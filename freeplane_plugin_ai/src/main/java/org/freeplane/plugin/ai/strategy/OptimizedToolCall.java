package org.freeplane.plugin.ai.strategy;

import java.util.List;
import java.util.Map;

/**
 * 优化工具调用方案
 * 
 * <p>封装策略优化后的工具调用序列和元数据。
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class OptimizedToolCall {
    
    private final String strategyName;
    private final List<ToolCallStep> steps;
    private final long optimizationTimeMs;
    private final double estimatedCost;
    
    public OptimizedToolCall(String strategyName, List<ToolCallStep> steps, 
                             long optimizationTimeMs, double estimatedCost) {
        this.strategyName = strategyName;
        this.steps = steps;
        this.optimizationTimeMs = optimizationTimeMs;
        this.estimatedCost = estimatedCost;
    }
    
    public String getStrategyName() {
        return strategyName;
    }
    
    public List<ToolCallStep> getSteps() {
        return steps;
    }
    
    public long getOptimizationTimeMs() {
        return optimizationTimeMs;
    }
    
    public double getEstimatedCost() {
        return estimatedCost;
    }
    
    public int getStepCount() {
        return steps.size();
    }
    
    @Override
    public String toString() {
        return "OptimizedToolCall{" +
               "strategy='" + strategyName + '\'' +
               ", steps=" + steps.size() +
               ", time=" + optimizationTimeMs + "ms" +
               ", cost=" + estimatedCost +
               '}';
    }
    
    /**
     * 工具调用步骤
     */
    public static class ToolCallStep {
        private final String toolName;
        private final Map<String, Object> parameters;
        private final int order;
        
        public ToolCallStep(String toolName, Map<String, Object> parameters, int order) {
            this.toolName = toolName;
            this.parameters = parameters;
            this.order = order;
        }
        
        public String getToolName() {
            return toolName;
        }
        
        public Map<String, Object> getParameters() {
            return parameters;
        }
        
        public int getOrder() {
            return order;
        }
    }
}
