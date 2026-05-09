package org.freeplane.plugin.ai.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMonitor {

    private static PerformanceMonitor instance;
    private final Map<String, ModelMetrics> modelMetricsMap;
    private final Map<String, TaskTypeMetrics> taskTypeMetricsMap;
    private static final int MAX_HISTORY_SIZE = 1000;

    private PerformanceMonitor() {
        this.modelMetricsMap = new ConcurrentHashMap<>();
        this.taskTypeMetricsMap = new ConcurrentHashMap<>();
    }

    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }

    public void recordRequest(String modelKey, long startTime) {
        recordRequest(modelKey, "general", startTime);
    }

    public void recordRequest(String modelKey, String taskType, long startTime) {
        long responseTime = System.currentTimeMillis() - startTime;

        ModelMetrics modelMetrics = modelMetricsMap.computeIfAbsent(modelKey, k -> new ModelMetrics());
        modelMetrics.recordResponseTime(responseTime);

        TaskTypeMetrics taskMetrics = taskTypeMetricsMap.computeIfAbsent(taskType, k -> new TaskTypeMetrics());
        taskMetrics.incrementRequestCount();
        taskMetrics.updateAverageResponseTime(responseTime);
    }

    public void recordSuccess(String modelKey) {
        ModelMetrics modelMetrics = modelMetricsMap.get(modelKey);
        if (modelMetrics != null) {
            modelMetrics.incrementSuccessCount();
        }
    }

    public void recordFailure(String modelKey) {
        ModelMetrics modelMetrics = modelMetricsMap.get(modelKey);
        if (modelMetrics != null) {
            modelMetrics.incrementFailureCount();
        }
    }

    public void recordTokenUsage(String modelKey, int inputTokens, int outputTokens) {
        ModelMetrics modelMetrics = modelMetricsMap.get(modelKey);
        if (modelMetrics != null) {
            modelMetrics.addTokenUsage(inputTokens, outputTokens);
        }
    }

    public ModelPerformanceScore calculatePerformanceScore(String modelKey) {
        ModelMetrics metrics = modelMetricsMap.get(modelKey);
        if (metrics == null) {
            return new ModelPerformanceScore(modelKey, 0.0);
        }

        double score = metrics.calculateScore();
        return new ModelPerformanceScore(modelKey, score);
    }

    public String selectBestModelForTask(String taskType, List<String> availableModels) {
        if (availableModels == null || availableModels.isEmpty()) {
            return null;
        }

        if (availableModels.size() == 1) {
            return availableModels.get(0);
        }

        Map<String, Double> scores = new HashMap<>();
        for (String model : availableModels) {
            ModelPerformanceScore performanceScore = calculatePerformanceScore(model);
            double adjustedScore = adjustScoreByTaskType(performanceScore.getScore(), model, taskType);
            scores.put(model, adjustedScore);
        }

        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(availableModels.get(0));
    }

    private double adjustScoreByTaskType(double baseScore, String modelKey, String taskType) {
        TaskTypeMetrics taskMetrics = taskTypeMetricsMap.get(taskType);
        if (taskMetrics == null || taskMetrics.getRequestCount() == 0) {
            return baseScore;
        }

        double taskSpecificScore = taskMetrics.getAverageResponseTime() < 5000 ? baseScore * 1.2 : baseScore;
        return taskSpecificScore;
    }

    public Map<String, ModelPerformanceScore> getAllModelScores() {
        Map<String, ModelPerformanceScore> scores = new HashMap<>();
        for (String modelKey : modelMetricsMap.keySet()) {
            scores.put(modelKey, calculatePerformanceScore(modelKey));
        }
        return scores;
    }

    public ModelMetrics getModelMetrics(String modelKey) {
        return modelMetricsMap.get(modelKey);
    }

    public void resetMetrics() {
        modelMetricsMap.clear();
        taskTypeMetricsMap.clear();
    }

    public void resetModelMetrics(String modelKey) {
        modelMetricsMap.remove(modelKey);
    }

    public Map<String, Object> getPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> modelReports = new ArrayList<>();

        for (Map.Entry<String, ModelMetrics> entry : modelMetricsMap.entrySet()) {
            Map<String, Object> modelReport = new HashMap<>();
            modelReport.put("modelKey", entry.getKey());
            modelReport.put("metrics", entry.getValue().toMap());
            modelReport.put("performanceScore", calculatePerformanceScore(entry.getKey()).getScore());
            modelReports.add(modelReport);
        }

        report.put("models", modelReports);
        report.put("totalModels", modelMetricsMap.size());
        report.put("timestamp", System.currentTimeMillis());

        return report;
    }

    public static class ModelMetrics {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private final AtomicLong totalInputTokens = new AtomicLong(0);
        private final AtomicLong totalOutputTokens = new AtomicLong(0);
        private final Queue<Long> responseTimeHistory = new LinkedList<>();

        public void recordResponseTime(long responseTime) {
            requestCount.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);

            long currentMin = minResponseTime.get();
            if (responseTime < currentMin) {
                minResponseTime.compareAndSet(currentMin, responseTime);
            }

            long currentMax = maxResponseTime.get();
            if (responseTime > currentMax) {
                maxResponseTime.compareAndSet(currentMax, responseTime);
            }

            synchronized (responseTimeHistory) {
                responseTimeHistory.offer(responseTime);
                if (responseTimeHistory.size() > MAX_HISTORY_SIZE) {
                    responseTimeHistory.poll();
                }
            }
        }

        public void incrementSuccessCount() {
            successCount.incrementAndGet();
        }

        public void incrementFailureCount() {
            failureCount.incrementAndGet();
        }

        public void addTokenUsage(int inputTokens, int outputTokens) {
            totalInputTokens.addAndGet(inputTokens);
            totalOutputTokens.addAndGet(outputTokens);
        }

        public double calculateScore() {
            int requests = requestCount.get();
            if (requests == 0) {
                return 50.0;
            }

            double successRate = (double) successCount.get() / requests;
            double avgResponseTime = (double) totalResponseTime.get() / requests;

            double responseTimeScore = Math.max(0, 100 - (avgResponseTime / 100));

            double reliabilityScore = successRate * 100;

            return (reliabilityScore * 0.6 + responseTimeScore * 0.4);
        }

        public double getAverageResponseTime() {
            int requests = requestCount.get();
            return requests > 0 ? (double) totalResponseTime.get() / requests : 0;
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public double getSuccessRate() {
            int requests = requestCount.get();
            return requests > 0 ? (double) successCount.get() / requests : 0;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("requestCount", requestCount.get());
            map.put("successCount", successCount.get());
            map.put("failureCount", failureCount.get());
            map.put("successRate", getSuccessRate());
            map.put("averageResponseTime", getAverageResponseTime());
            map.put("minResponseTime", minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());
            map.put("maxResponseTime", maxResponseTime.get());
            map.put("totalInputTokens", totalInputTokens.get());
            map.put("totalOutputTokens", totalOutputTokens.get());
            return map;
        }
    }

    public static class TaskTypeMetrics {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);

        public void incrementRequestCount() {
            requestCount.incrementAndGet();
        }

        public void updateAverageResponseTime(long responseTime) {
            totalResponseTime.addAndGet(responseTime);
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public double getAverageResponseTime() {
            int requests = requestCount.get();
            return requests > 0 ? (double) totalResponseTime.get() / requests : 0;
        }
    }

    public static class ModelPerformanceScore {
        private final String modelKey;
        private final double score;

        public ModelPerformanceScore(String modelKey, double score) {
            this.modelKey = modelKey;
            this.score = score;
        }

        public String getModelKey() {
            return modelKey;
        }

        public double getScore() {
            return score;
        }
    }
}