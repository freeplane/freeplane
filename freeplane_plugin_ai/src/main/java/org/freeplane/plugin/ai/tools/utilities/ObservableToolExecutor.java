package org.freeplane.plugin.ai.tools.utilities;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.List;
import java.util.Objects;

/**
 * 可观察的工具执行装饰器（观察者模式实现）。
 *
 * <p>在委托执行器前后广播事件给所有观察者，观察者中的异常被静默吞掉
 * 以避免影响工具执行链路（{@code onBefore} 抛出的异常除外）。
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
        // Before：执行前通知（可中断）
        ToolExecutionBeforeEvent beforeEvent = ToolExecutionBeforeEvent.create(
            toolName, request.arguments(), toolCaller);
        for (ToolExecutionObserver observer : observers) {
            observer.onBefore(beforeEvent);
        }

        long start = System.currentTimeMillis();
        try {
            ToolExecutionResult result = delegate.executeWithContext(request, invocationContext);
            // After：执行成功通知
            ToolExecutionAfterEvent afterEvent = ToolExecutionAfterEvent.create(
                toolName, request.arguments(), toolCaller, start,
                result == null ? null : result.resultText());
            notifyObserversSafely(o -> o.onAfter(afterEvent));
            return result;
        } catch (RuntimeException error) {
            // Error：执行失败通知
            ToolExecutionErrorEvent errorEvent = ToolExecutionErrorEvent.create(
                toolName, request.arguments(), toolCaller, start, error);
            notifyObserversSafely(o -> o.onError(errorEvent));
            throw error;
        }
    }

    /** 安全通知观察者：单个观察者的异常不影响其他观察者和主流程。 */
    private void notifyObserversSafely(java.util.function.Consumer<ToolExecutionObserver> action) {
        for (ToolExecutionObserver observer : observers) {
            try {
                action.accept(observer);
            } catch (Exception ignored) {
                // 观察者内部异常不应破坏工具执行链路
            }
        }
    }
}
