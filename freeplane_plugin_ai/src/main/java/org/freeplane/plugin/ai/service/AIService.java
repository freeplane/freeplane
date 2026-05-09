package org.freeplane.plugin.ai.service;

import java.util.Map;

/**
 * AI服务统一接口
 * 所有AI服务提供者必须实现此接口
 */
public interface AIService {
    
    /**
     * 获取服务类型
     * @return 服务类型
     */
    AIServiceType getServiceType();
    
    /**
     * 获取服务名称
     * @return 服务名称
     */
    String getServiceName();
    
    /**
     * 处理请求
     * @param request 请求参数
     * @return 处理结果
     */
    AIServiceResponse processRequest(Map<String, Object> request);
    
    /**
     * 判断是否能处理该请求
     * @param request 请求参数
     * @return true表示能处理
     */
    boolean canHandle(Map<String, Object> request);
    
    /**
     * 获取服务优先级（数字越小优先级越高）
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}