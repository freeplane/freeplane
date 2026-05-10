package org.freeplane.plugin.ai.tools.utilities;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.List;
import java.util.Objects;

/**
 * Observable decorator for tool execution (Observer pattern implementation).
 *
 * <p>Broadcasts lifecycle events to all registered observers before and after delegating
 * to the wrapped executor. Exceptions thrown by observers are silently swallowed to avoid
 * disrupting the execution chain, except for exceptions from {@code onBefore}.
 */
public class ObservableToolExecutor implements ToolExecutor {
    private final ToolExecutor delegate;
    private final String toolName;
    private final ToolCaller toolCaller;
    private final List<ToolExecutionObserver> observers;

    public ObservableToolExecutor(
            ToolExecutor delegate,
            String toolName,
            ToolCaller toolCaller,
            List<ToolExecutionObserver> observers) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.toolName = Objects.requireNonNull(toolName, "toolName");
        this.toolCaller = Objects.requireNonNull(toolCaller, "toolCaller");
        this.observers = Objects.requireNonNull(observers, "observers");
    }

    @Override
    public String execute(ToolExecutionRequest request, Object memoryId) {
        InvocationContext invocationContext = InvocationContext.builder()
            .chatMemoryId(memoryId)
            .build();
        ToolExecutionResult result = executeWithContext(request, invocationContext);
        return result == null ? null : result.resultText();
    }

    @Override
    public ToolExecutionResult executeWithContext(ToolExecutionRequest request, InvocationContext invocationContext) {
        // Before: notify observers before execution (may abort by throwing)
        ToolExecutionBeforeEvent beforeEvent = ToolExecutionBeforeEvent.create(
            toolName, request.arguments(), toolCaller);
        for (ToolExecutionObserver observer : observers) {
            observer.onBefore(beforeEvent);
        }

        long start = System.currentTimeMillis();
        try {
            ToolExecutionResult result = delegate.executeWithContext(request, invocationContext);
            // After: notify observers on success
            ToolExecutionAfterEvent afterEvent = ToolExecutionAfterEvent.create(
                toolName, request.arguments(), toolCaller, start,
                result == null ? null : result.resultText());
            notifyObserversSafely(o -> o.onAfter(afterEvent));
            return result;
        } catch (RuntimeException error) {
            // Error: notify observers on failure
            ToolExecutionErrorEvent errorEvent = ToolExecutionErrorEvent.create(
                toolName, request.arguments(), toolCaller, start, error);
            notifyObserversSafely(o -> o.onError(errorEvent));
            throw error;
        }
    }

    /** Notifies observers safely: an exception in one observer does not affect others or the main flow. */
    private void notifyObserversSafely(java.util.function.Consumer<ToolExecutionObserver> action) {
        for (ToolExecutionObserver observer : observers) {
            try {
                action.accept(observer);
            } catch (Exception ignored) {
                // exceptions in observers must not disrupt the tool execution chain
            }
        }
    }
}
