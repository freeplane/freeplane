package org.freeplane.plugin.ai.tools.utilities;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ToolExecutorFactory {
    private final boolean wrapToolArgumentsExceptions;
    private final boolean propagateToolExecutionExceptions;
    private final Supplier<Boolean> cancellationSupplier;
    private final List<ToolExecutionObserver> observers;
    private final ToolCaller toolCaller;

    public ToolExecutorFactory(boolean wrapToolArgumentsExceptions, boolean propagateToolExecutionExceptions) {
        this(wrapToolArgumentsExceptions, propagateToolExecutionExceptions, null, Collections.emptyList(), ToolCaller.CHAT);
    }

    public ToolExecutorFactory(boolean wrapToolArgumentsExceptions,
                               boolean propagateToolExecutionExceptions,
                               Supplier<Boolean> cancellationSupplier) {
        this(wrapToolArgumentsExceptions, propagateToolExecutionExceptions, cancellationSupplier,
            Collections.emptyList(), ToolCaller.CHAT);
    }

    /**
     * 完整构造函数，支持注入观察者和自定义调用来源。
     */
    public ToolExecutorFactory(boolean wrapToolArgumentsExceptions,
                               boolean propagateToolExecutionExceptions,
                               Supplier<Boolean> cancellationSupplier,
                               List<ToolExecutionObserver> observers,
                               ToolCaller toolCaller) {
        this.wrapToolArgumentsExceptions = wrapToolArgumentsExceptions;
        this.propagateToolExecutionExceptions = propagateToolExecutionExceptions;
        this.cancellationSupplier = cancellationSupplier;
        this.observers = observers != null ? Collections.unmodifiableList(new ArrayList<>(observers)) : Collections.emptyList();
        this.toolCaller = toolCaller != null ? toolCaller : ToolCaller.CHAT;
    }

    public ToolExecutorRegistry createRegistry(Object toolSet) {
        Objects.requireNonNull(toolSet, "toolSet");
        Map<String, ToolExecutor> executorsByName = new LinkedHashMap<>();
        Map<ToolSpecification, ToolExecutor> executorsBySpecification = new LinkedHashMap<>();
        List<ToolSpecification> specifications = new ArrayList<>();
        for (Method method : toolSet.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Tool.class)) {
                continue;
            }
            ToolSpecification specification = ToolSpecifications.toolSpecificationFrom(method);
            DefaultToolExecutor executor = DefaultToolExecutor.builder()
                .object(toolSet)
                .originalMethod(method)
                .methodToInvoke(method)
                .wrapToolArgumentsExceptions(wrapToolArgumentsExceptions)
                .propagateToolExecutionExceptions(propagateToolExecutionExceptions)
                .build();
            ToolExecutor toolExecutor = new EventDispatchToolExecutor(executor);
            if (cancellationSupplier != null) {
                toolExecutor = new CancellationToolExecutor(toolExecutor, cancellationSupplier);
            }
            if (!observers.isEmpty()) {
                toolExecutor = new ObservableToolExecutor(
                    toolExecutor, specification.name(), toolCaller, observers);
            }
            executorsByName.put(specification.name(), toolExecutor);
            executorsBySpecification.put(specification, toolExecutor);
            specifications.add(specification);
        }
        ToolSpecifications.validateSpecifications(specifications);
        return new ToolExecutorRegistry(
            Collections.unmodifiableMap(executorsByName),
            Collections.unmodifiableMap(executorsBySpecification));
    }
}
