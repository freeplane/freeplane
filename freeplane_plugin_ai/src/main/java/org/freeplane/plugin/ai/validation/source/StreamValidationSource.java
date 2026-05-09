package org.freeplane.plugin.ai.validation.source;

import java.io.IOException;

/**
 * Build 流式聚合数据源 —— 逐块接收 SSE chunk,标记完成后统一验证。
 * 
 * <p>关键设计:流结束时触发验证,而不是每收到一块就触发。
 */
public final class StreamValidationSource implements ValidationSource {
    
    private final StringBuilder buffer = new StringBuilder();
    private final String description;
    private volatile boolean completed = false;
    
    /**
     * @param description 流描述,如 "build-stream" 或节点ID
     */
    public StreamValidationSource(String description) {
        this.description = description;
    }
    
    /**
     * 追加流式分块。
     * 
     * @param chunk SSE chunk 内容
     */
    public synchronized void append(String chunk) {
        if (completed) {
            throw new IllegalStateException("Stream already completed");
        }
        buffer.append(chunk);
    }
    
    /**
     * 标记流聚合完成。
     */
    public synchronized void markComplete() {
        this.completed = true;
    }
    
    @Override
    public synchronized String readContent() throws IOException {
        if (!completed) {
            throw new IllegalStateException(
                "Stream not completed yet. Current buffer length: " + buffer.length());
        }
        return buffer.toString();
    }
    
    @Override
    public SourceType getSourceType() {
        return SourceType.STREAM_ASSEMBLED;
    }
    
    @Override
    public boolean isReady() {
        return completed;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
}
