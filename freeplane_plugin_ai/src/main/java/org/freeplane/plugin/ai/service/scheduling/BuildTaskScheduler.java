package org.freeplane.plugin.ai.service.scheduling;

import org.freeplane.core.util.LogUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 构建任务调度器 - 主要调度器，负责任务排队和选择逻辑
 */
public class BuildTaskScheduler {
    // 单例实例
    private static volatile BuildTaskScheduler instance;

    // 任务队列 - 使用 LinkedBlockingQueue 而不是 PriorityBlockingQueue，
    // 因为我们需要基于 TopP 动态选择任务
    private final BlockingQueue<BuildTask> taskQueue;
    // 运行中的任务
    private final Set<BuildTask> runningTasks;
    // 执行线程池（内部暴露以使 BuildTaskExecutor 复用）
    private final ExecutorService executorService;
    // 调度线程池
    private final ScheduledExecutorService schedulerService;
    // 调度配置
    private final SchedulingConfig config;
    // 任务执行器
    private final BuildTaskExecutor taskExecutor;
    // 随机数生成器
    private final Random random;
    // 任务超时（秒）：AI 操作耗时较长，默认 120 秒
    private static final long TASK_TIMEOUT_SECONDS = 120;

    // 运行状态
    private volatile boolean running;
    // 任务计数器
    private final AtomicLong taskCounter;

    /**
     * 私有构造函数 - 初始化调度器组件
     */
    private BuildTaskScheduler() {
        this.taskQueue = new LinkedBlockingQueue<>(100);
        this.runningTasks = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newFixedThreadPool(2);
        this.schedulerService = Executors.newScheduledThreadPool(1);
        this.config = SchedulingConfig.getInstance();
        this.taskExecutor = BuildTaskExecutor.getInstance();
        this.running = false;
        this.taskCounter = new AtomicLong(0);

        long seed = config.getSeed() != null ? config.getSeed() : System.nanoTime();
        this.random = new Random(seed);
    }

    /**
     * 获取调度器单例实例
     * @return 调度器实例
     */
    public static BuildTaskScheduler getInstance() {
        if (instance == null) {
            synchronized (BuildTaskScheduler.class) {
                if (instance == null) {
                    instance = new BuildTaskScheduler();
                }
            }
        }
        return instance;
    }

    /**
     * 重置调度器实例
     */
    public static void resetInstance() {
        synchronized (BuildTaskScheduler.class) {
            if (instance != null) {
                instance.stop();
                instance = null;
            }
        }
    }

    /**
     * 启动调度器
     */
    public void start() {
        if (!running) {
            running = true;
            schedulerService.scheduleWithFixedDelay(this::processTasks, 0, 100, TimeUnit.MILLISECONDS);
            LogUtils.info("BuildTaskScheduler: 启动，配置: temperature=" + config.getTemperature() + ", topP=" + config.getTopP());
        }
    }

    /**
     * 停止调度器
     */
    public void stop() {
        running = false;
        schedulerService.shutdown();
        executorService.shutdown();
        try {
            if (!schedulerService.awaitTermination(5, TimeUnit.SECONDS)) {
                schedulerService.shutdownNow();
            }
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            schedulerService.shutdownNow();
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LogUtils.info("BuildTaskScheduler: 停止");
    }

    /**
     * 提交任务到调度器
     * @param action 操作类型
     * @param request 请求参数
     * @return 任务对象，队列满时返回状态为 CANCELLED 的任务
     */
    public BuildTask submitTask(String action, Map<String, Object> request) {
        BuildTask task = new BuildTask(action, request, taskCounter.incrementAndGet());
        boolean offered = taskQueue.offer(task);
        if (offered) {
            LogUtils.info("BuildTaskScheduler: 提交任务: " + task.getId() + ", action: " + action + ", queueSize: " + taskQueue.size());
        } else {
            // 队列已满：标记为 CANCELLED 并通过 future 返回错误，而非静默丢弃
            task.setStatus(BuildTask.TaskStatus.CANCELLED);
            task.getFuture().complete(org.freeplane.plugin.ai.service.AIServiceResponse.error("调度器队列已满，请稍后重试"));
            LogUtils.warn("BuildTaskScheduler: 提交任务失败，队列已满");
        }
        return task;
    }

    /**
     * 处理任务队列 - 使用调度算法选择任务
     */
    private void processTasks() {
        if (!running) {
            return;
        }

        if (runningTasks.size() >= 2) {
            return;
        }

        if (taskQueue.isEmpty()) {
            return;
        }

        // 使用调度算法选择任务
        BuildTask nextTask = selectNextTask();
        if (nextTask == null) {
            return;
        }

        if (runningTasks.contains(nextTask)) {
            return;
        }

        runningTasks.add(nextTask);

        executorService.submit(() -> {
            try {
                nextTask.setStatus(BuildTask.TaskStatus.RUNNING);
                LogUtils.info("BuildTaskScheduler: 开始执行任务: " + nextTask.getId());

                taskExecutor.executeTask(nextTask);

                nextTask.setStatus(BuildTask.TaskStatus.COMPLETED);
                LogUtils.info("BuildTaskScheduler: 任务完成: " + nextTask.getId());
            } catch (Exception e) {
                nextTask.setStatus(BuildTask.TaskStatus.FAILED);
                if (!nextTask.getFuture().isDone()) {
                    nextTask.getFuture().complete(
                        org.freeplane.plugin.ai.service.AIServiceResponse.error("任务执行失败: " + e.getMessage()));
                }
                LogUtils.warn("BuildTaskScheduler: 任务失败: " + nextTask.getId(), e);
            } finally {
                runningTasks.remove(nextTask);
            }
        });

        // 超时看门狗：若 TASK_TIMEOUT_SECONDS 秒内 future 仍未完成，则强制终止
        schedulerService.schedule(() -> {
            if (!nextTask.getFuture().isDone()) {
                nextTask.setStatus(BuildTask.TaskStatus.FAILED);
                nextTask.getFuture().complete(
                    org.freeplane.plugin.ai.service.AIServiceResponse.error("任务超时（" + TASK_TIMEOUT_SECONDS + "秒）"));
                runningTasks.remove(nextTask);
                LogUtils.warn("BuildTaskScheduler: 任务超时并被强制终止: " + nextTask.getId());
            }
        }, TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 使用调度算法选择下一个要执行的任务
     * @return 选中的任务，如果没有可用任务返回 null
     */
    private BuildTask selectNextTask() {
        if (taskQueue.isEmpty()) {
            return null;
        }

        // 获取所有待处理任务
        List<BuildTask> pendingTasks = new ArrayList<>();
        int drainCount = taskQueue.drainTo(pendingTasks);
        
        if (pendingTasks.isEmpty()) {
            return null;
        }

        // 使用 TopP 选择算法选择任务
        BuildTask selectedTask = selectTaskByTopP(pendingTasks);
        
        // 将未选中的任务放回队列
        if (selectedTask != null) {
            pendingTasks.remove(selectedTask);
            for (BuildTask task : pendingTasks) {
                taskQueue.offer(task);
            }
        }

        return selectedTask;
    }

    /**
     * 获取内部执行线程池（供 BuildTaskExecutor 复用）
     * @return 执行线程池
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * 获取队列大小
     * @return 队列大小
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * 获取运行中任务数量
     * @return 运行中任务数量
     */
    public int getRunningTaskCount() {
        return runningTasks.size();
    }

    /**
     * 获取待处理任务列表
     * @return 待处理任务列表
     */
    public List<BuildTask> getPendingTasks() {
        return new ArrayList<>(taskQueue);
    }

    /**
     * 获取运行中任务列表
     * @return 运行中任务列表
     */
    public List<BuildTask> getRunningTasks() {
        return new ArrayList<>(runningTasks);
    }

    /**
     * 清空任务队列
     */
    public void clearQueue() {
        taskQueue.clear();
        LogUtils.info("BuildTaskScheduler: 队列已清空");
    }

    /**
     * 计算任务优先级 - 不输出调试日志以提高性能
     * @param task 任务对象
     * @return 优先级分数
     */
    private double calculateTaskPriority(BuildTask task) {
        double temperature = config.getTemperature();

        double baseScore = task.getPriority();
        double ageScore = Math.min(task.getWaitTime() / 1000.0, 5.0);

        double randomFactor = 0;
        if (temperature > 0) {
            randomFactor = (random.nextDouble() - 0.5) * 2 * temperature;
        }

        return baseScore + ageScore + randomFactor;
    }

    /**
     * 使用TopP选择任务 - 每个任务只计算一次优先级分数，保证排序与采样一致性
     * @param tasks 任务列表
     * @return 选中的任务
     */
    public BuildTask selectTaskByTopP(List<BuildTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }

        double topP = config.getTopP();

        // 每个任务只计算一次优先级分数，避免多次调用产生不一致的随机值
        Map<BuildTask, Double> scoreCache = new java.util.IdentityHashMap<>();
        for (BuildTask task : tasks) {
            scoreCache.put(task, calculateTaskPriority(task));
        }

        // 按缓存分数降序排序（比较器使用固定分数，满足传递性）
        tasks.sort((t1, t2) -> Double.compare(scoreCache.get(t2), scoreCache.get(t1)));

        if (topP >= 1.0 || tasks.size() == 1) {
            return tasks.get(0);
        }

        // 计算候选集大小
        int nucleusSize = Math.max(1, (int) Math.ceil(tasks.size() * topP));
        nucleusSize = Math.min(nucleusSize, tasks.size());

        List<BuildTask> nucleusTasks = tasks.subList(0, nucleusSize);

        // 计算候选集中任务的优先级总和（使用缓存分数）
        double totalScore = 0;
        for (BuildTask task : nucleusTasks) {
            totalScore += scoreCache.get(task);
        }

        if (totalScore <= 0) {
            return nucleusTasks.get(0);
        }

        // 使用随机数从候选集中按比例采样（使用缓存分数，结果与排序一致）
        double randomValue = random.nextDouble() * totalScore;
        double cumulative = 0;

        for (BuildTask task : nucleusTasks) {
            cumulative += scoreCache.get(task);
            if (randomValue <= cumulative) {
                return task;
            }
        }

        // 兜底返回第一个任务
        return nucleusTasks.get(0);
    }

    /**
     * 获取调度器运行状态
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running;
    }
}