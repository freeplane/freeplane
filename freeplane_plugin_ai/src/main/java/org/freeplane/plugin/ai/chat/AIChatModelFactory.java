package org.freeplane.plugin.ai.chat;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import java.util.Map;

public class AIChatModelFactory {

    public static final String PROVIDER_NAME_OPENROUTER = "openrouter";
    public static final String PROVIDER_NAME_GEMINI = "gemini";
    public static final String PROVIDER_NAME_OLLAMA = "ollama";
    public static final String PROVIDER_NAME_DASHSCOPE = "dashscope";
    public static final String PROVIDER_NAME_ERNIE = "ernie";
    public static final String DEFAULT_OPENROUTER_SERVICE_ADDRESS = "https://openrouter.ai/api/v1";
    public static final String DEFAULT_DASHSCOPE_SERVICE_ADDRESS = "https://dashscope.aliyuncs.com/api/v1";
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
        if (PROVIDER_NAME_DASHSCOPE.equalsIgnoreCase(providerName)) {
            String serviceAddress = getDashScopeServiceAddress(configuration);
            return OpenAiChatModel.builder()
                .baseUrl(serviceAddress)
                .apiKey(configuration.getDashScopeKey())
                .modelName(modelName)
                .maxRetries(CHAT_MODEL_MAX_RETRIES)
                .build();
        }
        if (PROVIDER_NAME_ERNIE.equalsIgnoreCase(providerName)) {
            String serviceAddress = configuration.getErnieServiceAddress();
            if (serviceAddress == null || serviceAddress.isEmpty()) {
                serviceAddress = "https://qianfan.baidubce.com/v2";
            } else {
                // 如果用户配置了完整路径（包含 /chat/completions），自动截断为 base URL
                if (serviceAddress.endsWith("/chat/completions")) {
                    serviceAddress = serviceAddress.substring(0, serviceAddress.length() - "/chat/completions".length());
                }
            }
            return OpenAiChatModel.builder()
                .baseUrl(serviceAddress)
                .apiKey(configuration.getErnieKey())
                .modelName(modelName)
                .maxRetries(CHAT_MODEL_MAX_RETRIES)
                .build();
        }
        throw new IllegalArgumentException("Unknown provider name: " + providerName);
    }

    /**
     * Creates a {@link StreamingChatModel} for the given provider configuration.
     * Returns {@code null} when streaming is not supported for the selected provider.
     */
    public static StreamingChatModel createStreamingChatModel(AIProviderConfiguration configuration) {
        AIModelSelection selection = AIModelSelection.fromSelectionValue(configuration.getSelectedModelValue());
        if (selection == null) {
            return null;
        }
        String providerName = selection.getProviderName();
        String modelName = selection.getModelName();
        try {
            if (PROVIDER_NAME_OPENROUTER.equalsIgnoreCase(providerName)) {
                return OpenAiStreamingChatModel.builder()
                    .baseUrl(getOpenrouterServiceAddress(configuration))
                    .apiKey(configuration.getOpenRouterKey())
                    .modelName(modelName)
                    .build();
            }
            if (PROVIDER_NAME_GEMINI.equalsIgnoreCase(providerName)) {
                GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder =
                    GoogleAiGeminiStreamingChatModel.builder()
                        .apiKey(configuration.getGeminiKey())
                        .modelName(modelName);
                String serviceAddress = configuration.getGeminiServiceAddress();
                if (serviceAddress != null && !serviceAddress.isEmpty()) {
                    builder.baseUrl(serviceAddress);
                }
                return builder.build();
            }
            if (PROVIDER_NAME_OLLAMA.equalsIgnoreCase(providerName)) {
                OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = OllamaStreamingChatModel.builder()
                    .baseUrl(getOllamaServiceAddress(configuration))
                    .modelName(modelName);
                Map<String, String> requestHeaders = configuration.getOllamaRequestHeaders();
                if (!requestHeaders.isEmpty()) {
                    builder.customHeaders(requestHeaders);
                }
                return builder.build();
            }
            if (PROVIDER_NAME_DASHSCOPE.equalsIgnoreCase(providerName)) {
                return OpenAiStreamingChatModel.builder()
                    .baseUrl(getDashScopeServiceAddress(configuration))
                    .apiKey(configuration.getDashScopeKey())
                    .modelName(modelName)
                    .build();
            }
            if (PROVIDER_NAME_ERNIE.equalsIgnoreCase(providerName)) {
                String serviceAddress = configuration.getErnieServiceAddress();
                if (serviceAddress == null || serviceAddress.isEmpty()) {
                    serviceAddress = "https://qianfan.baidubce.com/v2";
                } else if (serviceAddress.endsWith("/chat/completions")) {
                    serviceAddress = serviceAddress.substring(0, serviceAddress.length() - "/chat/completions".length());
                }
                return OpenAiStreamingChatModel.builder()
                    .baseUrl(serviceAddress)
                    .apiKey(configuration.getErnieKey())
                    .modelName(modelName)
                    .build();
            }
        } catch (Exception e) {
            // Streaming model creation failed; fallback to non-streaming
            return null;
        }
        return null;
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

    private static String getDashScopeServiceAddress(AIProviderConfiguration configuration) {
        String serviceAddress = configuration.getDashScopeServiceAddress();
        if (serviceAddress == null || serviceAddress.isEmpty()) {
            return DEFAULT_DASHSCOPE_SERVICE_ADDRESS;
        }
        return serviceAddress;
    }
}
