package org.freeplane.plugin.ai.tools.utilities;

/**
 * 工具执行观察者（观察者模式核心接口）。
 *
 * <p>实现类只覆盖感兴趣的事件方法（默认空实现），可组合多个观察者
 * 实现参数预校验、性能统计、日志审计等横切关注点。
 */
public interface ToolExecutionObserver {

    /**
     * 工具执行前通知。
     *
     * @param event 包含工具名、原始参数等，不可变
     * @throws RuntimeException 可抛出异常中断工具执行（如参数校验失败）
     */
    default void onBefore(ToolExecutionBeforeEvent event) {
    }

    /**
     * 工具执行成功通知。
     *
     * @param event 包含结果文本和耗时
     */
    default void onAfter(ToolExecutionAfterEvent event) {
    }

    /**
     * 工具执行失败通知。
     *
     * @param event 包含异常和耗时
     */
    default void onError(ToolExecutionErrorEvent event) {
    }
}
