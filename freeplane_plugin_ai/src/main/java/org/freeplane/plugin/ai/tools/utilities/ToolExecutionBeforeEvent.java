package org.freeplane.plugin.ai.tools.utilities;

/**
 * 工具执行前事件。
 *
 * <p>观察者可在 {@code onBefore} 中对参数进行预校验、权限检查或日志打点。
 */
public record ToolExecutionBeforeEvent(
        String toolName,
        String rawArguments,
        ToolCaller toolCaller,
        long eventTimeMs
) implements ToolExecutionEvent {

    public ToolExecutionBeforeEvent {
        if (toolName == null) throw new NullPointerException("toolName");
        if (rawArguments == null) throw new NullPointerException("rawArguments");
        if (toolCaller == null) throw new NullPointerException("toolCaller");
    }

    public static ToolExecutionBeforeEvent create(String toolName, String rawArguments, ToolCaller toolCaller) {
        return new ToolExecutionBeforeEvent(toolName, rawArguments, toolCaller, System.currentTimeMillis());
    }
}
