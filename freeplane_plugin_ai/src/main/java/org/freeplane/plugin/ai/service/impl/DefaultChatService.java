package org.freeplane.plugin.ai.service.impl;

import dev.langchain4j.model.output.TokenUsage;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.chat.AIChatService;
import org.freeplane.plugin.ai.chat.AIChatServiceFactory;
import org.freeplane.plugin.ai.chat.AIProviderConfiguration;
import org.freeplane.plugin.ai.chat.ChatTokenUsageTracker;
import org.freeplane.plugin.ai.maps.AvailableMaps;
import org.freeplane.plugin.ai.maps.ControllerMapModelProvider;
import org.freeplane.plugin.ai.service.AIService;
import org.freeplane.plugin.ai.service.AIServiceResponse;
import org.freeplane.plugin.ai.service.AIServiceType;
import org.freeplane.plugin.ai.tools.AIToolSet;
import org.freeplane.plugin.ai.tools.AIToolSetBuilder;
import org.freeplane.plugin.ai.tools.utilities.ToolCallSummaryHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultChatService implements AIService {

    private static volatile AIChatService chatService;
    private static volatile AIToolSet toolSet;
    private static volatile AvailableMaps availableMaps;
    private static final AtomicInteger inputTokens = new AtomicInteger(0);
    private static final AtomicInteger outputTokens = new AtomicInteger(0);

    @Override
    public AIServiceType getServiceType() {
        return AIServiceType.CHAT;
    }

    @Override
    public String getServiceName() {
        return "default_chat_service";
    }

    @Override
    public AIServiceResponse processRequest(Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return AIServiceResponse.error("Message is required");
            }

            String modelSelection = (String) request.get("modelSelection");
            String mapId = (String) request.get("mapId");
            @SuppressWarnings("unused")
            String selectedNodeId = (String) request.get("selectedNodeId");

            ensureServiceInitialized();

            if (chatService == null) {
                return AIServiceResponse.error("AI service not initialized. Please configure AI provider in preferences.");
            }

            String reply = chatService.chat(message);

            Map<String, Object> data = Map.of(
                "reply", reply,
                "tokenUsage", Map.of(
                    "inputTokens", inputTokens.get(),
                    "outputTokens", outputTokens.get()
                )
            );

            return AIServiceResponse.success("Chat completed", data);
        } catch (Exception e) {
            LogUtils.warn("DefaultChatService.processRequest failed", e);
            return AIServiceResponse.error("Chat failed: " + e.getMessage());
        }
    }

    private void ensureServiceInitialized() {
        if (chatService == null) {
            synchronized (DefaultChatService.class) {
                if (chatService == null) {
                    try {
                        AIProviderConfiguration configuration = new AIProviderConfiguration();
                        if (!isProviderConfigured(configuration)) {
                            LogUtils.warn("DefaultChatService: No AI provider configured");
                            return;
                        }
                        // 如果用户未选择模型，自动选择第一个可用 provider 的默认模型
                        autoSelectModelIfNeeded(configuration);

                        availableMaps = new AvailableMaps(new ControllerMapModelProvider());

                        ToolCallSummaryHandler toolCallSummaryHandler = summary -> {
                            LogUtils.info("Tool call: " + summary.getSummaryText());
                        };

                        AvailableMaps.MapAccessListener mapAccessListener = (mapId, mapModel) -> {
                            LogUtils.info("Map accessed: " + mapId);
                        };

                        toolSet = new AIToolSetBuilder()
                            .toolCallSummaryHandler(toolCallSummaryHandler)
                            .availableMaps(availableMaps)
                            .mapAccessListener(mapAccessListener)
                            .build();

                        ChatTokenUsageTracker tokenTracker = new ChatTokenUsageTracker(totals -> {
                            inputTokens.addAndGet((int) totals.getInputTokenCount());
                            outputTokens.addAndGet((int) totals.getOutputTokenCount());
                        });

                        chatService = AIChatServiceFactory.createService(
                            toolSet,
                            null,
                            tokenTracker,
                            toolCallSummaryHandler,
                            () -> false,
                            usage -> {}
                        );

                        LogUtils.info("DefaultChatService: AIChatService initialized successfully");
                    } catch (Exception e) {
                        LogUtils.warn("DefaultChatService: Failed to initialize AIChatService", e);
                    }
                }
            }
        }
    }

    private void autoSelectModelIfNeeded(AIProviderConfiguration configuration) {
        String selected = configuration.getSelectedModelValue();
        if (selected != null && !selected.trim().isEmpty()) {
            return; // 已有选择，无需自动
        }
        // 按优先度顺序选择：ernie > dashscope > openrouter > gemini > ollama
        String providerName = null;
        String modelName = null;
        if (configuration.hasErnieKey()) {
            providerName = "ernie";
            String models = configuration.getErnieModelListValue();
            modelName = (models != null && !models.trim().isEmpty()) ? models.split(",")[0].trim() : "ernie-4.5";
        } else if (configuration.hasDashScopeKey()) {
            providerName = "dashscope";
            String models = configuration.getDashScopeModelListValue();
            modelName = (models != null && !models.trim().isEmpty()) ? models.split(",")[0].trim() : "qwen-max";
        } else if (isNonEmpty(configuration.getOpenRouterKey())) {
            providerName = "openrouter";
            modelName = "deepseek/deepseek-v3.2";
        } else if (isNonEmpty(configuration.getGeminiKey())) {
            providerName = "gemini";
            modelName = "gemini-2.0-flash";
        } else if (configuration.hasOllamaServiceAddress()) {
            providerName = "ollama";
            String models = configuration.getOllamaModelAllowlistValue();
            modelName = (models != null && !models.trim().isEmpty()) ? models.split(",")[0].trim() : "llama3";
        }
        if (providerName != null) {
            String selectionValue = providerName + "|" + modelName;
            configuration.setSelectedModelValue(selectionValue);
            LogUtils.info("DefaultChatService: Auto-selected model: " + selectionValue);
        }
    }

    private boolean isProviderConfigured(AIProviderConfiguration configuration) {
        return isNonEmpty(configuration.getOpenRouterKey())
            || isNonEmpty(configuration.getGeminiKey())
            || configuration.hasOllamaServiceAddress()
            || configuration.hasDashScopeKey()
            || configuration.hasErnieKey();
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public boolean canHandle(Map<String, Object> request) {
        String serviceType = (String) request.get("serviceType");
        return serviceType == null || AIServiceType.CHAT.getCode().equals(serviceType);
    }

    @Override
    public int getPriority() {
        return 10;
    }
}