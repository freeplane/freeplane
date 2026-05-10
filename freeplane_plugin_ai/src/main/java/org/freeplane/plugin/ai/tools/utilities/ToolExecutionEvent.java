package org.freeplane.plugin.ai.tools.utilities;

/**
 * Immutable carrier for tool execution events.
 *
 * <p>All implementing types are records (Java 16+), guaranteeing immutability and value-based equality.
 */
public sealed interface ToolExecutionEvent
        permits ToolExecutionBeforeEvent, ToolExecutionAfterEvent, ToolExecutionErrorEvent {

    /** tool name */
    String toolName();

    /** raw argument string (JSON); available before execution for pre-validation */
    String rawArguments();

    /** call origin (CHAT / MCP / etc.) */
    ToolCaller toolCaller();

    /** event timestamp in milliseconds */
    long eventTimeMs();
}
