package org.freeplane.plugin.ai.chat;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.Map;

public class AIChatModelFactory {

    public static final String PROVIDER_NAME_OPENROUTER = "openrouter";
    public static final String PROVIDER_NAME_GEMINI = "gemini";
    public static final String PROVIDER_NAME_OLLAMA = "ollama";
    public static final String PROVIDER_NAME_VENDOR = "vendor";
    public static final String DEFAULT_OPENROUTER_SERVICE_ADDRESS = "https://openrouter.ai/api/v1";
    public static final String DEFAULT_VENDOR_SERVICE_ADDRESS = "https://api.z.ai/api/coding/paas/v4/";
    static final int CHAT_MODEL_MAX_RETRIES = 2;

    private AIChatModelFactory() {
    }

    public static ChatModel createChatLanguageModel(AIProviderConfiguration configuration) {
        AIModelSelection selection = AIModelSelection.fromSelectionValue(configuration.getSelectedModelValue());
        if (selection == null) {
            throw new IllegalArgumentException("Missing model selection");
        }
        String providerName = selection.getProviderName();
        String modelName = selection.getModelName();
        if (PROVIDER_NAME_OPENROUTER.equalsIgnoreCase(providerName)) {
            return OpenAiChatModel.builder()
                .baseUrl(getOpenrouterServiceAddress(configuration))
                .apiKey(configuration.getOpenRouterKey())
                .modelName(modelName)
                .maxRetries(CHAT_MODEL_MAX_RETRIES)
                .build();
        }
        if (PROVIDER_NAME_GEMINI.equalsIgnoreCase(providerName)) {
            GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder = GoogleAiGeminiChatModel.builder()
                .apiKey(configuration.getGeminiKey())
                .modelName(modelName)
                .maxRetries(CHAT_MODEL_MAX_RETRIES);
            String serviceAddress = configuration.getGeminiServiceAddress();
            if (serviceAddress != null && !serviceAddress.isEmpty()) {
                builder.baseUrl(serviceAddress);
            }
            if (modelName != null && modelName.startsWith("gemini-3-")) {
                GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                    .includeThoughts(true)
                    .build();
                builder.thinkingConfig(thinkingConfig)
                    .returnThinking(true)
                    .sendThinking(true);
            }
            return builder.build();
        }
        if (PROVIDER_NAME_OLLAMA.equalsIgnoreCase(providerName)) {
            OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                .baseUrl(getOllamaServiceAddress(configuration))
                .modelName(modelName)
                .maxRetries(CHAT_MODEL_MAX_RETRIES);
            Map<String, String> requestHeaders = configuration.getOllamaRequestHeaders();
            if (!requestHeaders.isEmpty()) {
                builder.customHeaders(requestHeaders);
            }
            return builder.build();
        }
        if (PROVIDER_NAME_VENDOR.equalsIgnoreCase(providerName)) {
            return OpenAiChatModel.builder()
                .baseUrl(getVendorServiceAddress(configuration))
                .apiKey(configuration.getVendorKey())
                .modelName(modelName)
                .maxRetries(CHAT_MODEL_MAX_RETRIES)
                .build();
        }
        throw new IllegalArgumentException("Unknown provider name: " + providerName);
    }

    private static String getOpenrouterServiceAddress(AIProviderConfiguration configuration) {
        String serviceAddress = configuration.getOpenrouterServiceAddress();
        if (serviceAddress == null || serviceAddress.isEmpty()) {
            return DEFAULT_OPENROUTER_SERVICE_ADDRESS;
        }
        return serviceAddress;
    }

    private static String getOllamaServiceAddress(AIProviderConfiguration configuration) {
        return configuration.getOllamaServiceAddress();
    }

    private static String getVendorServiceAddress(AIProviderConfiguration configuration) {
        String serviceAddress = configuration.getVendorServiceAddress();
        if (serviceAddress == null || serviceAddress.isEmpty()) {
            return DEFAULT_VENDOR_SERVICE_ADDRESS;
        }
        return serviceAddress;
    }
}
