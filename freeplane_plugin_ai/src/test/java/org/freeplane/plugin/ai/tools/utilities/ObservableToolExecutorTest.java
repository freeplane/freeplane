package org.freeplane.plugin.ai.tools.utilities;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证观察者模式在工具执行链路中的事件广播。
 */
public class ObservableToolExecutorTest {

    private static final class MockToolExecutor implements ToolExecutor {
        private final String result;
        private final boolean shouldFail;

        MockToolExecutor(String result) {
            this.result = result;
            this.shouldFail = false;
        }

        MockToolExecutor(boolean shouldFail) {
            this.result = null;
            this.shouldFail = shouldFail;
        }

        @Override
        public String execute(ToolExecutionRequest request, Object memoryId) {
            if (shouldFail) {
                throw new RuntimeException("Simulated failure");
            }
            return result;
        }

        @Override
        public ToolExecutionResult executeWithContext(ToolExecutionRequest request, InvocationContext invocationContext) {
            if (shouldFail) {
                throw new RuntimeException("Simulated failure");
            }
            return ToolExecutionResult.builder().resultText(result).build();
        }
    }

    @Test
    public void execute_success_broadcastsBeforeAndAfter() {
        List<String> events = new ArrayList<>();
        ToolExecutionObserver observer = new ToolExecutionObserver() {
            @Override
            public void onBefore(ToolExecutionBeforeEvent event) {
                events.add("before:" + event.toolName());
            }

            @Override
            public void onAfter(ToolExecutionAfterEvent event) {
                events.add("after:" + event.toolName() + ":" + event.elapsedMs() + "ms");
            }
        };

        ToolExecutor delegate = new MockToolExecutor("OK");
        ToolExecutor observable = new ObservableToolExecutor(delegate, "testTool", ToolCaller.CHAT, List.of(observer));

        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name("testTool")
            .arguments("{\"key\":\"value\"}")
            .build();

        String result = observable.execute(request, "mem1");
        assertThat(result).isEqualTo("OK");
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isEqualTo("before:testTool");
        assertThat(events.get(1)).startsWith("after:testTool:");
    }

    @Test
    public void execute_error_broadcastsBeforeAndError() {
        List<String> events = new ArrayList<>();
        AtomicReference<Throwable> capturedError = new AtomicReference<>();

        ToolExecutionObserver observer = new ToolExecutionObserver() {
            @Override
            public void onBefore(ToolExecutionBeforeEvent event) {
                events.add("before:" + event.toolName());
            }

            @Override
            public void onError(ToolExecutionErrorEvent event) {
                events.add("error:" + event.toolName());
                capturedError.set(event.error());
            }
        };

        ToolExecutor delegate = new MockToolExecutor(true);
        ToolExecutor observable = new ObservableToolExecutor(delegate, "failTool", ToolCaller.MCP, List.of(observer));

        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name("failTool")
            .arguments("{}")
            .build();

        assertThatThrownBy(() -> observable.execute(request, "mem1"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Simulated failure");

        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isEqualTo("before:failTool");
        assertThat(events.get(1)).isEqualTo("error:failTool");
        assertThat(capturedError.get()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void onBefore_canInterruptExecution() {
        AtomicInteger executionCount = new AtomicInteger(0);

        ToolExecutionObserver validationObserver = new ToolExecutionObserver() {
            @Override
            public void onBefore(ToolExecutionBeforeEvent event) {
                throw new IllegalArgumentException("Missing required field");
            }
        };

        ToolExecutor delegate = new MockToolExecutor("should not reach here");
        ToolExecutor observable = new ObservableToolExecutor(delegate, "validateTool", ToolCaller.CHAT, List.of(validationObserver));

        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name("validateTool")
            .arguments("{}")
            .build();

        assertThatThrownBy(() -> observable.execute(request, "mem1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Missing required field");

        // 委托执行器未被调用
        assertThat(executionCount.get()).isEqualTo(0);
    }

    @Test
    public void multipleObservers_allNotified() {
        AtomicInteger beforeCount = new AtomicInteger(0);
        AtomicInteger afterCount = new AtomicInteger(0);

        ToolExecutionObserver observer1 = new ToolExecutionObserver() {
            @Override public void onBefore(ToolExecutionBeforeEvent e) { beforeCount.incrementAndGet(); }
            @Override public void onAfter(ToolExecutionAfterEvent e) { afterCount.incrementAndGet(); }
        };

        ToolExecutionObserver observer2 = new ToolExecutionObserver() {
            @Override public void onBefore(ToolExecutionBeforeEvent e) { beforeCount.incrementAndGet(); }
            @Override public void onAfter(ToolExecutionAfterEvent e) { afterCount.incrementAndGet(); }
        };

        ToolExecutor delegate = new MockToolExecutor("OK");
        ToolExecutor observable = new ObservableToolExecutor(
            delegate, "multiTool", ToolCaller.CHAT, List.of(observer1, observer2));

        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name("multiTool")
            .arguments("{}")
            .build();

        observable.execute(request, "mem1");

        assertThat(beforeCount.get()).isEqualTo(2);
        assertThat(afterCount.get()).isEqualTo(2);
    }

    @Test
    public void observerException_doesNotBreakOtherObservers() {
        AtomicBoolean goodObserverCalled = new AtomicBoolean(false);

        ToolExecutionObserver badObserver = new ToolExecutionObserver() {
            @Override
            public void onAfter(ToolExecutionAfterEvent event) {
                throw new RuntimeException("Observer failure");
            }
        };

        ToolExecutionObserver goodObserver = new ToolExecutionObserver() {
            @Override
            public void onAfter(ToolExecutionAfterEvent event) {
                goodObserverCalled.set(true);
            }
        };

        ToolExecutor delegate = new MockToolExecutor("OK");
        // 坏观察者在前面，好观察者在后面
        ToolExecutor observable = new ObservableToolExecutor(
            delegate, "safeTool", ToolCaller.CHAT, List.of(badObserver, goodObserver));

        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name("safeTool")
            .arguments("{}")
            .build();

        // 执行不应抛出异常（after 异常被吞掉）
        String result = observable.execute(request, "mem1");
        assertThat(result).isEqualTo("OK");
        // 好观察者仍被调用
        assertThat(goodObserverCalled.get()).isTrue();
    }
}
