package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.tools.utilities.ToolCaller;
import org.freeplane.plugin.ai.tools.utilities.ToolExecutionAfterEvent;
import org.freeplane.plugin.ai.tools.utilities.ToolExecutionBeforeEvent;
import org.freeplane.plugin.ai.tools.utilities.ToolExecutionErrorEvent;
import org.freeplane.plugin.ai.tools.utilities.ToolExecutionObserver;
import org.freeplane.plugin.ai.tools.utilities.ToolExecutorFactory;
import org.freeplane.plugin.ai.tools.utilities.ToolExecutorRegistry;

public class ModelContextProtocolToolDispatcher {
    private final ObjectMapper objectMapper;
    private final Map<String, ToolExecutor> toolExecutorsByName;
    private final List<ToolExecutionObserver> observers;

    public ModelContextProtocolToolDispatcher(Object toolSet, ObjectMapper objectMapper) {
        this(toolSet, objectMapper, Collections.emptyList());
    }

    /**
     * Full constructor with observer injection support.
     */
    public ModelContextProtocolToolDispatcher(Object toolSet, ObjectMapper objectMapper,
                                              List<ToolExecutionObserver> observers) {
        Objects.requireNonNull(toolSet, "toolSet");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.observers = observers != null ? Collections.unmodifiableList(observers) : Collections.emptyList();
        ToolExecutorFactory toolExecutorFactory = new ToolExecutorFactory(
            false, false, null, this.observers, ToolCaller.MCP);
        ToolExecutorRegistry toolExecutorRegistry = toolExecutorFactory.createRegistry(toolSet);
        this.toolExecutorsByName = toolExecutorRegistry.getExecutorsByName();
    }

    public ToolExecutionResult dispatch(String toolName, JsonNode argumentsNode) {
        ToolExecutionResult result = executeTool(toolName, argumentsNode);
        return result;
    }

    private ToolExecutionResult executeTool(String toolName, JsonNode argumentsNode) {
        ToolExecutor executor = toolExecutorsByName.get(toolName);
        if (executor == null) {
            LogUtils.info(buildToolCallLog(toolName, null, "Unknown tool name: " + toolName));
            throw new IllegalArgumentException("Unknown tool name: " + toolName);
        }
        String arguments = "{}";
        if (argumentsNode != null && !argumentsNode.isNull()) {
            try {
                arguments = objectMapper.writeValueAsString(argumentsNode);
            } catch (Exception error) {
                LogUtils.info(buildToolCallLog(toolName, null, "Invalid tool arguments."));
                throw new IllegalArgumentException("Invalid tool arguments.", error);
            }
        }
        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name(toolName)
            .arguments(arguments)
            .build();
        long start = System.currentTimeMillis();

        // MCP path: broadcast a 'before' event prior to execution.
        ToolExecutionBeforeEvent beforeEvent = ToolExecutionBeforeEvent.create(
            toolName, arguments, ToolCaller.MCP);
        for (ToolExecutionObserver observer : observers) {
            observer.onBefore(beforeEvent);
        }

        try {
            ToolExecutionResult result = executor.executeWithContext(request, InvocationContext.builder().build());
            if (result != null && result.isError()) {
                LogUtils.info(buildToolCallLog(toolName, arguments, result.resultText()));
            }
            // MCP path: broadcast an 'after' event on success.
            ToolExecutionAfterEvent afterEvent = ToolExecutionAfterEvent.create(
                toolName, arguments, ToolCaller.MCP, start,
                result == null ? null : result.resultText());
            notifyObserversSafely(o -> o.onAfter(afterEvent));
            return result;
        } catch (RuntimeException error) {
            // MCP path: broadcast an 'error' event on failure.
            ToolExecutionErrorEvent errorEvent = ToolExecutionErrorEvent.create(
                toolName, arguments, ToolCaller.MCP, start, error);
            notifyObserversSafely(o -> o.onError(errorEvent));
            throw error;
        }
    }

    private void notifyObserversSafely(java.util.function.Consumer<ToolExecutionObserver> action) {
        for (ToolExecutionObserver observer : observers) {
            try {
                action.accept(observer);
            } catch (Exception ignored) {
                // Observer exceptions must not disrupt the MCP dispatch chain.
            }
        }
    }

    private String buildToolCallLog(String toolName, String arguments, String errorMessage) {
        String safeToolName = toolName == null ? "unknown tool" : toolName;
        String safeArguments = arguments == null ? "" : arguments;
        String safeError = errorMessage == null ? "" : errorMessage;
        return "MCP tool error: tool=" + safeToolName + ", arguments=" + safeArguments + ", error=" + safeError;
    }

}
