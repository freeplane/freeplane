package org.freeplane.plugin.ai.tools.utilities;

/**
 * Observer interface for the tool execution lifecycle (Observer pattern).
 *
 * <p>Implementations override only the event methods they care about (default no-op).
 * Multiple observers can be composed to handle cross-cutting concerns such as
 * argument pre-validation, performance metrics, and audit logging.
 */
public interface ToolExecutionObserver {

    /**
     * Called before tool execution.
     *
     * @param event immutable event containing the tool name and raw arguments
     * @throws RuntimeException implementations may throw to abort execution (e.g. argument validation failure)
     */
    default void onBefore(ToolExecutionBeforeEvent event) {
    }

    /**
     * Called after successful tool execution.
     *
     * @param event event containing the result text and elapsed time
     */
    default void onAfter(ToolExecutionAfterEvent event) {
    }

    /**
     * Called after a failed tool execution.
     *
     * @param event event containing the exception and elapsed time
     */
    default void onError(ToolExecutionErrorEvent event) {
    }
}
