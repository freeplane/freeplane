package org.freeplane.plugin.ai.validation.source;

import java.io.IOException;

/**
 * 验证数据源代理接口 —— 统一接入多来源 JSON 数据。
 * 
 * <p>设计要点:
 * <ul>
 *   <li>readContent() 统一出口,返回完整 JSON 字符串</li>
 *   <li>getSourceType() 枚举来源类型,用于日志/降级策略区分</li>
 *   <li>isReady() 流式场景下判断数据是否已完整聚合</li>
 *   <li>getDescription() 日志追溯用</li>
 * </ul>
 * 
 * <p>代理类实现松耦合架构,不绑定具体处理流程(如SnakeDigestGraph),
 * 确保环检测逻辑与数据来源完全解耦。
 */
public interface ValidationSource {
    
    /**
     * 读取完整 JSON 内容。
     * 
     * @return 完整 JSON 字符串
     * @throws IOException 读取失败时抛出
     * @throws IllegalStateException 数据未就绪时调用
     */
    String readContent() throws IOException;
    
    /**
     * 获取数据源类型。
     * 
     * @return SourceType 枚举值
     */
    SourceType getSourceType();
    
    /**
     * 检查数据是否已就绪(可读取)。
     * 
     * @return true 如果数据完整可用
     */
    boolean isReady();
    
    /**
     * 获取数据源描述(用于日志追溯)。
     * 
     * @return 描述字符串,如 "model=ernie-4.0"
     */
    String getDescription();
}
