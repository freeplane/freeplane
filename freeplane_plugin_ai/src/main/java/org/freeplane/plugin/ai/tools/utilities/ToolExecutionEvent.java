package org.freeplane.plugin.ai.tools.utilities;

/**
 * 工具执行事件的不可变载体。
 *
 * <p>所有实现类均为 record（Java 16+），保证不可变语义和值比较。
 */
public sealed interface ToolExecutionEvent
        permits ToolExecutionBeforeEvent, ToolExecutionAfterEvent, ToolExecutionErrorEvent {

    /** 工具名称 */
    String toolName();

    /** 原始参数字符串（JSON），执行前可用于预校验 */
    String rawArguments();

    /** 调用来源（CHAT / MCP / etc.） */
    ToolCaller toolCaller();

    /** 事件时间戳（毫秒） */
    long eventTimeMs();
}
