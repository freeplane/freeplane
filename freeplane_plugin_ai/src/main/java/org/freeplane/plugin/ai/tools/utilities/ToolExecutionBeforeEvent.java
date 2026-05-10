package org.freeplane.plugin.ai.tools.utilities;

/**
 * Event fired before a tool is executed.
 *
 * <p>Observers may use {@code onBefore} for argument pre-validation, permission checks, or audit logging.
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
