package org.freeplane.plugin.ai.service;

import org.freeplane.plugin.ai.tools.AIToolSet;

import java.util.Map;

/**
 * 工具执行服务接口
 * 提供直接的工具调用能力，绕过 LLM 聊天环节
 */
public interface ToolExecutionService {

    /**
     * 执行工具调用
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 执行结果
     */
    Object executeTool(String toolName, Map<String, Object> parameters);

    /**
     * 获取支持的工具列表
     * @return 工具名称列表
     */
    String[] getSupportedTools();

    /**
     * 检查工具是否支持
     * @param toolName 工具名称
     * @return 是否支持
     */
    boolean isToolSupported(String toolName);

    /**
     * 设置 AIToolSet 实例
     * @param toolSet AIToolSet 实例
     */
    void setToolSet(AIToolSet toolSet);

    /**
     * 获取 AIToolSet 实例
     * @return AIToolSet 实例
     */
    AIToolSet getToolSet();
}