package org.freeplane.plugin.ai.tools.utilities;

/**
 * 工具执行成功事件。
 */
public record ToolExecutionAfterEvent(
        String toolName,
        String rawArguments,
        ToolCaller toolCaller,
        long eventTimeMs,
        String resultText,
        long elapsedMs
) implements ToolExecutionEvent {

    public ToolExecutionAfterEvent {
        if (toolName == null) throw new NullPointerException("toolName");
        if (rawArguments == null) throw new NullPointerException("rawArguments");
        if (toolCaller == null) throw new NullPointerException("toolCaller");
        if (resultText == null) throw new NullPointerException("resultText");
    }

    public static ToolExecutionAfterEvent create(
            String toolName, String rawArguments, ToolCaller toolCaller,
            long startTime, String resultText) {
        long now = System.currentTimeMillis();
        return new ToolExecutionAfterEvent(toolName, rawArguments, toolCaller, now, resultText, now - startTime);
    }
}
