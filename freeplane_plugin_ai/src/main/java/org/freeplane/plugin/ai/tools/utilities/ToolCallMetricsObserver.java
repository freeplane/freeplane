package org.freeplane.plugin.ai.tools.utilities;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 工具调用性能统计观察者。
 *
 * <p>记录每个工具的调用次数、总耗时、平均耗时和错误次数，
 * 为性能优化和容量规划提供数据支撑。
 */
public class ToolCallMetricsObserver implements ToolExecutionObserver {

    private final ConcurrentHashMap<String, LongAdder> callCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> totalElapsedMs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> errorCounts = new ConcurrentHashMap<>();

    @Override
    public void onAfter(ToolExecutionAfterEvent event) {
        callCounts.computeIfAbsent(event.toolName(), k -> new LongAdder()).increment();
        totalElapsedMs.computeIfAbsent(event.toolName(), k -> new LongAdder()).add(event.elapsedMs());
    }

    @Override
    public void onError(ToolExecutionErrorEvent event) {
        errorCounts.computeIfAbsent(event.toolName(), k -> new LongAdder()).increment();
        // 失败也算一次调用
        callCounts.computeIfAbsent(event.toolName(), k -> new LongAdder()).increment();
        totalElapsedMs.computeIfAbsent(event.toolName(), k -> new LongAdder()).add(event.elapsedMs());
    }

    /** 返回每个工具的平均耗时（毫秒），未调用过的工具不返回。 */
    public Map<String, Long> getAverageElapsedMs() {
        ConcurrentHashMap<String, Long> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, LongAdder> entry : callCounts.entrySet()) {
            String toolName = entry.getKey();
            long count = entry.getValue().sum();
            if (count > 0) {
                long total = totalElapsedMs.getOrDefault(toolName, new LongAdder()).sum();
                result.put(toolName, total / count);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /** 返回每个工具的调用次数。 */
    public Map<String, Long> getCallCounts() {
        ConcurrentHashMap<String, Long> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, LongAdder> entry : callCounts.entrySet()) {
            result.put(entry.getKey(), entry.getValue().sum());
        }
        return Collections.unmodifiableMap(result);
    }

    /** 返回每个工具的错误次数。 */
    public Map<String, Long> getErrorCounts() {
        ConcurrentHashMap<String, Long> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, LongAdder> entry : errorCounts.entrySet()) {
            result.put(entry.getKey(), entry.getValue().sum());
        }
        return Collections.unmodifiableMap(result);
    }

    /** 重置所有统计。 */
    public void reset() {
        callCounts.clear();
        totalElapsedMs.clear();
        errorCounts.clear();
    }
}
