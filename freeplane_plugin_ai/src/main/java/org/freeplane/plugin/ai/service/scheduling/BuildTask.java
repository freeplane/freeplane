package org.freeplane.plugin.ai.service.scheduling;

import org.freeplane.plugin.ai.service.AIServiceResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 构建任务类 - 表示具有优先级和元数据的构建任务
 */
public class BuildTask {
    // 任务ID
    private final String id;
    // 操作类型
    private final String action;
    // 请求参数
    private final Map<String, Object> request;
    // 创建时间
    private final long createdAt;
    // 序列号
    private final long sequenceNumber;
    // 基础优先级
    private final int priority;
    // 任务状态
    private TaskStatus status;
    // 开始时间
    private long startedAt;
    // 完成时间
    private long completedAt;
    // 优先级分数
    private double priorityScore;
    // 异步结果：调度器执行完任务后通过此 future 回传结调用方
    private final CompletableFuture<AIServiceResponse> future;

    /**
     * 构造任务对象
     * @param action 操作类型
     * @param request 请求参数
     */
    public BuildTask(String action, Map<String, Object> request) {
        this(action, request, 0L);
    }

    /**
     * 构造任务对象
     * @param action 操作类型
     * @param request 请求参数
     * @param sequenceNumber 序列号
     */
    public BuildTask(String action, Map<String, Object> request, long sequenceNumber) {
        this.id = UUID.randomUUID().toString();
        this.action = action;
        this.request = request;
        this.createdAt = System.currentTimeMillis();
        this.sequenceNumber = sequenceNumber;
        this.status = TaskStatus.PENDING;
        this.priority = calculateBasePriority(action);
        this.priorityScore = priority;
        this.future = new CompletableFuture<>();
    }

    /**
     * 计算基础优先级
     * @param action 操作类型
     * @return 基础优先级 (1-5)
     */
    private int calculateBasePriority(String action) {
        switch (action) {
            case "generate-mindmap":
                return 5;
            case "expand-node":
                return 4;
            case "summarize":
                return 3;
            case "tag":
                return 2;
            case "execute-tool":
                return 1;
            default:
                return 0;
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public Map<String, Object> getRequest() {
        return request;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public int getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    /**
     * 设置任务状态
     * @param status 任务状态
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status == TaskStatus.RUNNING) {
            this.startedAt = System.currentTimeMillis();
        } else if (status == TaskStatus.COMPLETED) {
            this.completedAt = System.currentTimeMillis();
        }
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    /**
     * 获取异步结果 Future。调用方可通过 future.get(timeout) 等待任务完成。
     * @return CompletableFuture
     */
    public CompletableFuture<AIServiceResponse> getFuture() {
        return future;
    }

    /**
     * 获取任务等待时间
     * @return 等待时间（毫秒）
     */
    public long getWaitTime() {
        if (status == TaskStatus.PENDING) {
            return System.currentTimeMillis() - createdAt;
        } else if (status == TaskStatus.RUNNING) {
            return startedAt - createdAt;
        } else {
            return startedAt - createdAt;
        }
    }

    /**
     * 获取任务执行时间
     * @return 执行时间（毫秒）
     */
    public long getExecutionTime() {
        if (status == TaskStatus.COMPLETED) {
            return completedAt - startedAt;
        } else if (status == TaskStatus.RUNNING) {
            return System.currentTimeMillis() - startedAt;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "BuildTask{" +
                "id='" + id + '\'' +
                ", action='" + action + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", priorityScore=" + priorityScore +
                '}';
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,    // 待处理
        RUNNING,    // 运行中
        COMPLETED,  // 已完成
        FAILED,     // 失败
        CANCELLED   // 已取消（队列满、超时、主动取消）
    }
}
