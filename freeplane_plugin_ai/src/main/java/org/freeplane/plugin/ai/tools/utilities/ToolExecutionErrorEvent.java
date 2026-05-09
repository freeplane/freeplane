package org.freeplane.plugin.ai.tools.utilities;

/**
 * 工具执行失败事件。
 */
public record ToolExecutionErrorEvent(
        String toolName,
        String rawArguments,
        ToolCaller toolCaller,
        long eventTimeMs,
        Throwable error,
        long elapsedMs
) implements ToolExecutionEvent {

    public ToolExecutionErrorEvent {
        if (toolName == null) throw new NullPointerException("toolName");
        if (rawArguments == null) throw new NullPointerException("rawArguments");
        if (toolCaller == null) throw new NullPointerException("toolCaller");
        if (error == null) throw new NullPointerException("error");
    }

    public static ToolExecutionErrorEvent create(
            String toolName, String rawArguments, ToolCaller toolCaller,
            long startTime, Throwable error) {
        long now = System.currentTimeMillis();
        return new ToolExecutionErrorEvent(toolName, rawArguments, toolCaller, now, error, now - startTime);
    }
}
