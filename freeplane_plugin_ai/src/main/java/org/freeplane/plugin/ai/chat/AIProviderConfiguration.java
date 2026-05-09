package org.freeplane.plugin.ai.chat;

import java.util.Collections;
import java.util.Map;
import org.freeplane.core.resources.ResourceController;

public class AIProviderConfiguration {
    private static final String AI_PROVIDER_NAME_PROPERTY = "ai_provider_name";
    private static final String AI_MODEL_NAME_PROPERTY = "ai_model_name";
    private static final String AI_SELECTED_MODEL_PROPERTY = "ai_selected_model";
    private static final String AI_OPENROUTER_SERVICE_ADDRESS_PROPERTY = "ai_openrouter_service_address";
    private static final String AI_OPENROUTER_KEY_PROPERTY = "ai_openrouter_key";
    private static final String AI_OPENROUTER_MODEL_ALLOWLIST_PROPERTY = "ai_openrouter_model_allowlist";
    private static final String AI_GEMINI_SERVICE_ADDRESS_PROPERTY = "ai_gemini_service_address";
    private static final String AI_GEMINI_KEY_PROPERTY = "ai_gemini_key";
    private static final String AI_GEMINI_MODEL_LIST_PROPERTY = "ai_gemini_model_list";
    private static final String AI_OLLAMA_SERVICE_ADDRESS_PROPERTY = "ai_ollama_service_address";
    private static final String AI_OLLAMA_API_KEY_PROPERTY = "ai_ollama_api_key";
    private static final String AI_OLLAMA_MODEL_ALLOWLIST_PROPERTY = "ai_ollama_model_allowlist";
    private static final String AI_DASHSCOPE_KEY_PROPERTY = "ai_dashscope_key";
    private static final String AI_DASHSCOPE_SERVICE_ADDRESS_PROPERTY = "ai_dashscope_service_address";
    private static final String AI_DASHSCOPE_MODEL_LIST_PROPERTY = "ai_dashscope_model_list";
    private static final String AI_ERNIE_KEY_PROPERTY = "ai_ernie_key";
    private static final String AI_ERNIE_SERVICE_ADDRESS_PROPERTY = "ai_ernie_service_address";
    private static final String AI_ERNIE_MODEL_LIST_PROPERTY = "ai_ernie_model_list";

    private final ResourceController resourceController;

    public AIProviderConfiguration() {
        this(ResourceController.getResourceController());
    }

    AIProviderConfiguration(ResourceController resourceController) {
        this.resourceController = resourceController;
    }

    public String getSelectedModelValue() {
        String selectedModelValue = getStoredSelectedModelValue();
        if (selectedModelValue != null && !selectedModelValue.trim().isEmpty()) {
            return selectedModelValue;
        }
        // 如果用户未配置选中模型，返回 provider+model 的旧式字段 fallback
        String providerName = resourceController.getProperty(AI_PROVIDER_NAME_PROPERTY);
        String modelName = resourceController.getProperty(AI_MODEL_NAME_PROPERTY);
        if (providerName != null && !providerName.isEmpty() && modelName != null && !modelName.isEmpty()) {
            return AIModelSelection.createSelectionValue(providerName, modelName);
        }
        // 最后 fallback：自动根据已配置的 provider 推断默认模型
        return inferDefaultModelSelection();
    }

    private String inferDefaultModelSelection() {
        if (hasErnieKey()) {
            String models = getErnieModelListValue();
            String modelName = (models != null && !models.trim().isEmpty()) ? models.split(",")[0].trim() : "deepseek-v3";
            return AIModelSelection.createSelectionValue("ernie", modelName);
        }
        if (hasDashScopeKey()) {
            String models = getDashScopeModelListValue();
            String modelName = (models != null && !models.trim().isEmpty()) ? models.split(",")[0].trim() : "qwen-max";
            return AIModelSelection.createSelectionValue("dashscope", modelName);
        }
        String openrouterKey = getOpenRouterKey();
        if (openrouterKey != null && !openrouterKey.trim().isEmpty()) {
            return AIModelSelection.createSelectionValue("openrouter", "deepseek/deepseek-v3.2");
        }
        String geminiKey = getGeminiKey();
        if (geminiKey != null && !geminiKey.trim().isEmpty()) {
            return AIModelSelection.createSelectionValue("gemini", "gemini-2.0-flash");
        }
        if (hasOllamaServiceAddress()) {
            String models = getOllamaModelAllowlistValue();
            String modelName = (models != null && !models.trim().isEmpty()) ? models.split(",")[0].trim() : "llama3";
            return AIModelSelection.createSelectionValue("ollama", modelName);
        }
        return null;
    }

    public String getStoredSelectedModelValue() {
        return resourceController.getProperty(AI_SELECTED_MODEL_PROPERTY);
    }

    public void setSelectedModelValue(String selectionValue) {
        resourceController.setProperty(AI_SELECTED_MODEL_PROPERTY, selectionValue);
    }

    public String getOpenrouterServiceAddress() {
        return resourceController.getProperty(AI_OPENROUTER_SERVICE_ADDRESS_PROPERTY);
    }

    public String getOpenRouterKey() {
        return resourceController.getProperty(AI_OPENROUTER_KEY_PROPERTY);
    }

    public String getOpenrouterModelAllowlistValue() {
        return resourceController.getProperty(AI_OPENROUTER_MODEL_ALLOWLIST_PROPERTY);
    }

    public String getGeminiServiceAddress() {
        return resourceController.getProperty(AI_GEMINI_SERVICE_ADDRESS_PROPERTY);
    }

    public String getGeminiKey() {
        return resourceController.getProperty(AI_GEMINI_KEY_PROPERTY);
    }

    public String getGeminiModelListValue() {
        return resourceController.getProperty(AI_GEMINI_MODEL_LIST_PROPERTY);
    }

    public String getOllamaServiceAddress() {
        return resourceController.getProperty(AI_OLLAMA_SERVICE_ADDRESS_PROPERTY);
    }

    public String getOllamaApiKey() {
        return resourceController.getProperty(AI_OLLAMA_API_KEY_PROPERTY);
    }

    public boolean hasOllamaServiceAddress() {
        return hasNonBlankText(getOllamaServiceAddress());
    }

    public Map<String, String> getOllamaRequestHeaders() {
        String apiKey = trimToEmpty(getOllamaApiKey());
        if (apiKey.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap("Authorization", "Bearer " + apiKey);
    }

    public String getOllamaModelAllowlistValue() {
        return resourceController.getProperty(AI_OLLAMA_MODEL_ALLOWLIST_PROPERTY);
    }

    public String getDashScopeKey() {
        return resourceController.getProperty(AI_DASHSCOPE_KEY_PROPERTY);
    }

    public String getDashScopeServiceAddress() {
        return resourceController.getProperty(AI_DASHSCOPE_SERVICE_ADDRESS_PROPERTY);
    }

    public String getDashScopeModelListValue() {
        return resourceController.getProperty(AI_DASHSCOPE_MODEL_LIST_PROPERTY);
    }

    public boolean hasDashScopeKey() {
        return hasNonBlankText(getDashScopeKey());
    }

    public String getErnieKey() {
        return resourceController.getProperty(AI_ERNIE_KEY_PROPERTY);
    }

    public String getErnieServiceAddress() {
        return resourceController.getProperty(AI_ERNIE_SERVICE_ADDRESS_PROPERTY);
    }

    public String getErnieModelListValue() {
        return resourceController.getProperty(AI_ERNIE_MODEL_LIST_PROPERTY);
    }

    public boolean hasErnieKey() {
        return hasNonBlankText(getErnieKey());
    }

    private boolean hasNonBlankText(String value) {
        return !trimToEmpty(value).isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
