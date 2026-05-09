package org.freeplane.plugin.ai.service.scheduling;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.service.AIServiceResponse;
import org.freeplane.plugin.ai.service.impl.DefaultAgentService;

import java.util.concurrent.ExecutorService;

/**
 * 构建任务执行器 - 执行构建任务并通过 CompletableFuture 回传结果
 * 
 * 重要：执行时必须调用 dispatchAction 而非 processRequest，
 * 否则调度器线程池中的线程会再次提交任务并等待 future，导致死锁。
 */
public class BuildTaskExecutor {
    // 单例实例
    private static final BuildTaskExecutor instance = new BuildTaskExecutor();
    // 代理服务
    private final DefaultAgentService agentService;
    
    /**
     * 私有构造函数 - 初始化执行器组件
     */
    private BuildTaskExecutor() {
        this.agentService = new DefaultAgentService();
    }
    
    /**
     * 获取执行器单例实例
     * @return 执行器实例
     */
    public static BuildTaskExecutor getInstance() {
        return instance;
    }
    
    /**
     * 执行任务并通过 task.getFuture() 完成结果。
     * 
     * 应在调度器的线程池中调用。
     * 直接调用 dispatchAction 而非 processRequest，避免再次进入调度路径（防死锁）。
     * @param task 任务对象
     * @return 服务响应
     */
    public AIServiceResponse executeTask(BuildTask task) {
        LogUtils.info("BuildTaskExecutor: 执行任务: " + task);
        try {
            // 确保 agent 已初始化（chatModel 、toolSet 等）
            agentService.ensureAgentInitializedPublic();
            // 直接调用底层分发方法，跳过调度器入口
            AIServiceResponse response = agentService.dispatchAction(task.getAction(), task.getRequest());
            if (response.isSuccess()) {
                LogUtils.info("BuildTaskExecutor: 任务执行成功: " + task.getId());
            } else {
                LogUtils.warn("BuildTaskExecutor: 任务执行失败: " + task.getId() + ", error: " + response.getErrorMessage());
            }
            // 通过 future 将结果回传给等待方
            task.getFuture().complete(response);
            return response;
        } catch (Exception e) {
            LogUtils.warn("BuildTaskExecutor: 执行任务异常: " + task.getId(), e);
            AIServiceResponse errResp = AIServiceResponse.error("执行任务失败: " + e.getMessage());
            task.getFuture().complete(errResp);
            throw new RuntimeException("执行任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步执行任务，复用外部传入的线程池而非新建裸线程。
     * @param task 任务对象
     * @param executorService 线程池（应使用调度器的内部线程池）
     * @param callback 异步回调（可选）
     */
    public void executeTaskAsync(BuildTask task, ExecutorService executorService, TaskExecutionCallback callback) {
        executorService.submit(() -> {
            try {
                AIServiceResponse response = executeTask(task);
                if (callback != null) callback.onComplete(task, response);
            } catch (Exception e) {
                if (callback != null) callback.onError(task, e);
            }
        });
    }

    /**
     * @deprecated 请使用 executeTaskAsync(task, executorService, callback)。
     * 保留此方法仅为兼容旧代码，将在未来版本移除。
     */
    @Deprecated
    public void executeTaskAsync(BuildTask task, TaskExecutionCallback callback) {
        BuildTaskScheduler scheduler = BuildTaskScheduler.getInstance();
        ExecutorService es = scheduler.getExecutorService();
        executeTaskAsync(task, es, callback);
    }
    
    /**
     * 任务执行回调接口
     */
    public interface TaskExecutionCallback {
        void onComplete(BuildTask task, AIServiceResponse response);
        void onError(BuildTask task, Exception error);
    }
}
