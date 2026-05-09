package org.freeplane.plugin.ai.validation.source;

/**
 * 验证数据源类型枚举,用于日志追溯与降级策略区分。
 */
public enum SourceType {
    /** LLM Prompt 生成的 JSON 响应 */
    PROMPT_RESPONSE,
    
    /** Build 流式 SSE 分块聚合完成 */
    STREAM_ASSEMBLED,
    
    /** 从 .mm/.json 文件导入 */
    FILE_IMPORT,
    
    /** 从远程 URL 拉取(预留) */
    URL_REMOTE
}
