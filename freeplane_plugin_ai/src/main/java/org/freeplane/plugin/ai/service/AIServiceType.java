package org.freeplane.plugin.ai.service;

/**
 * AI服务类型枚举
 */
public enum AIServiceType {
    /**
     * 智能问答服务
     */
    CHAT("chat", "智能问答"),
    
    /**
     * 智能体服务（如思维导图生成、节点扩展等）
     */
    AGENT("agent", "智能体");
    
    private final String code;
    private final String name;
    
    AIServiceType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static AIServiceType fromCode(String code) {
        for (AIServiceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}