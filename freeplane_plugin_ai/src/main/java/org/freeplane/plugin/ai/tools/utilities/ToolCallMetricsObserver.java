package org.freeplane.plugin.ai.tools.utilities;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Observer that collects per-tool call counts, total elapsed time, average elapsed time, and error counts
 * to support performance analysis and capacity planning.
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
        // errors count as a call too
        callCounts.computeIfAbsent(event.toolName(), k -> new LongAdder()).increment();
        totalElapsedMs.computeIfAbsent(event.toolName(), k -> new LongAdder()).add(event.elapsedMs());
    }

    /** Returns the average elapsed time (ms) per tool; tools never called are excluded. */
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

    /** Returns the call count per tool. */
    public Map<String, Long> getCallCounts() {
        ConcurrentHashMap<String, Long> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, LongAdder> entry : callCounts.entrySet()) {
            result.put(entry.getKey(), entry.getValue().sum());
        }
        return Collections.unmodifiableMap(result);
    }

    /** Returns the error count per tool. */
    public Map<String, Long> getErrorCounts() {
        ConcurrentHashMap<String, Long> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, LongAdder> entry : errorCounts.entrySet()) {
            result.put(entry.getKey(), entry.getValue().sum());
        }
        return Collections.unmodifiableMap(result);
    }

    /** Resets all statistics. */
    public void reset() {
        callCounts.clear();
        totalElapsedMs.clear();
        errorCounts.clear();
    }
}
