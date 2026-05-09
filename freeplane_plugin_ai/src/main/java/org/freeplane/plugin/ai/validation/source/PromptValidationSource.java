package org.freeplane.plugin.ai.validation.source;

import java.io.IOException;

/**
 * Prompt 响应数据源 —— 包装 LLM 生成的 JSON 字符串。
 * 
 * <p>用于替换 MindMapBufferLayer 步骤4.5 当前的直接字符串传入。
 */
public final class PromptValidationSource implements ValidationSource {
    
    private final String aiResponse;
    private final String description;
    
    /**
     * @param aiResponse   LLM 返回的 JSON 字符串
     * @param selectedModel 使用的模型名称(如 "ernie-4.0"),可为 null
     */
    public PromptValidationSource(String aiResponse, String selectedModel) {
        this.aiResponse = aiResponse;
        this.description = (selectedModel != null) 
            ? "model=" + selectedModel 
            : "unknown-model";
    }
    
    @Override
    public String readContent() throws IOException {
        return aiResponse;
    }
    
    @Override
    public SourceType getSourceType() {
        return SourceType.PROMPT_RESPONSE;
    }
    
    @Override
    public boolean isReady() {
        return true; // Prompt 响应始终完整
    }
    
    @Override
    public String getDescription() {
        return description;
    }
}
