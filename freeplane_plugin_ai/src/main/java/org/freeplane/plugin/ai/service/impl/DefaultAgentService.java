package org.freeplane.plugin.ai.service.impl;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.ai.buffer.BufferLayerRouter;
import org.freeplane.plugin.ai.buffer.mindmap.MindMapPromptOptimizer;
import org.freeplane.plugin.ai.chat.AIChatService;
import org.freeplane.plugin.ai.chat.AIChatModelFactory;
import org.freeplane.plugin.ai.chat.AIChatServiceFactory;
import org.freeplane.plugin.ai.chat.AIProviderConfiguration;
import org.freeplane.plugin.ai.chat.ChatTokenUsageTracker;
import org.freeplane.plugin.ai.maps.AvailableMaps;
import org.freeplane.plugin.ai.maps.ControllerMapModelProvider;
import org.freeplane.plugin.ai.service.AIService;
import org.freeplane.plugin.ai.service.AIServiceResponse;
import org.freeplane.plugin.ai.service.AIServiceType;
import org.freeplane.plugin.ai.service.ToolExecutionService;
import org.freeplane.plugin.ai.service.impl.DefaultToolExecutionService;
import org.freeplane.plugin.ai.service.scheduling.BuildTask;
import org.freeplane.plugin.ai.service.scheduling.BuildTaskScheduler;
import org.freeplane.plugin.ai.service.scheduling.SchedulingConfig;
import org.freeplane.plugin.ai.service.scheduling.SchedulingMonitor;

import java.util.concurrent.TimeUnit;
import org.freeplane.plugin.ai.tools.AIToolSet;
import org.freeplane.plugin.ai.tools.AIToolSetBuilder;
import org.freeplane.plugin.ai.tools.utilities.ToolCallSummaryHandler;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultAgentService implements AIService {

    private static volatile AIChatService agentService;
    private static volatile ChatModel chatModel;
    private static volatile StreamingChatModel streamingChatModel;
    private static volatile AIToolSet toolSet;
    private static volatile AvailableMaps availableMaps;
    private static volatile BufferLayerRouter bufferLayerRouter;
    private static final AtomicInteger inputTokens = new AtomicInteger(0);
    private static final AtomicInteger outputTokens = new AtomicInteger(0);
    private static final MindMapPromptOptimizer promptOptimizer = new MindMapPromptOptimizer();
    private static volatile ToolExecutionService toolExecutionService;
    private static volatile BuildTaskScheduler taskScheduler;
    private static volatile SchedulingConfig schedulingConfig;
    private static volatile SchedulingMonitor schedulingMonitor;

    @Override
    public AIServiceType getServiceType() {
        return AIServiceType.AGENT;
    }

    @Override
    public String getServiceName() {
        return "default_agent_service";
    }

    @Override
    public AIServiceResponse processRequest(Map<String, Object> request) {
        try {
            String action = (String) request.get("action");
            if (action == null) {
                return AIServiceResponse.error("Action is required");
            }

            // 调度器已启用：将任务提交并通过 future 异步等待结果
            if (schedulingConfig != null && schedulingConfig.isEnabled() && taskScheduler != null) {
                BuildTask task = taskScheduler.submitTask(action, request);
                // 队列已满时直接返回 CANCELLED 结果
                if (task.getStatus() == BuildTask.TaskStatus.CANCELLED) {
                    return task.getFuture().getNow(AIServiceResponse.error("调度器队列已满"));
                }
                // 等待任务执行完成，最长等待 120 秒
                return task.getFuture().get(120, TimeUnit.SECONDS);
            }

            // 调度器未启用：直接执行
            return dispatchAction(action, request);
        } catch (java.util.concurrent.TimeoutException e) {
            LogUtils.warn("DefaultAgentService.processRequest 等待任务超时", e);
            return AIServiceResponse.error("Agent action timed out after 120s");
        } catch (java.util.concurrent.ExecutionException e) {
            LogUtils.warn("DefaultAgentService.processRequest 任务执行异常", e);
            return AIServiceResponse.error("Agent action failed: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AIServiceResponse.error("Agent action interrupted");
        } catch (Exception e) {
            LogUtils.warn("DefaultAgentService.processRequest failed", e);
            return AIServiceResponse.error("Agent action failed: " + e.getMessage());
        }
    }

    /**
     * 按 action 类型分发到具体 handler。
     * public 以便跨包访问（BuildTaskExecutor 直接调用，避免再次进入调度路径防死锁）。
     */
    public AIServiceResponse dispatchAction(String action, Map<String, Object> request) {
        switch (action) {
            case "generate-mindmap":
                return handleGenerateMindMap(request);
            case "expand-node":
                return handleExpandNode(request);
            case "summarize":
                return handleSummarize(request);
            case "tag":
                return handleTag(request);
            case "execute-tool":
                return handleExecuteTool(request);
            default:
                return AIServiceResponse.error("Unknown action: " + action);
        }
    }

    private AIServiceResponse handleGenerateMindMap(Map<String, Object> request) {
        String topic = (String) request.get("topic");
        if (topic == null || topic.trim().isEmpty()) {
            return AIServiceResponse.error("Topic is required");
        }

        // Create task for monitoring
        BuildTask task = new BuildTask("generate-mindmap", request);
        
        try {
            ensureAgentInitialized();
            
            // Record task start
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskStart(task);
            }

            String prompt = buildMindMapPrompt(topic, request);
            // 使用简单的 System Prompt，完整的 Prompt 工程在 prompts.properties 模板中
            String result = chatWithModel(
                "You are a helpful assistant. Follow the instructions below carefully.",
                prompt
            );

            Map<String, Object> data = Map.of(
                "success", true,
                "topic", topic,
                "result", result,
                "tokenUsage", Map.of(
                    "inputTokens", inputTokens.get(),
                    "outputTokens", outputTokens.get()
                )
            );

            // Record task completion
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, true);
            }

            return AIServiceResponse.success("Mindmap prompt generated", data);
        } catch (Exception e) {
            // Record task failure
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, false);
            }
            LogUtils.warn("DefaultAgentService.handleGenerateMindMap failed", e);
            return AIServiceResponse.error("Failed to generate mindmap: " + e.getMessage());
        }
    }

    private AIServiceResponse handleExpandNode(Map<String, Object> request) {
        String nodeId = (String) request.get("nodeId");
        if (nodeId == null) {
            return AIServiceResponse.error("NodeId is required");
        }

        // Create task for monitoring
        BuildTask task = new BuildTask("expand-node", request);
        
        try {
            ensureAgentInitialized();
            
            // Record task start
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskStart(task);
            }

            String mapId = (String) request.get("mapId");
            Integer depth = (Integer) request.get("depth");
            Integer count = (Integer) request.get("count");
            String focus = (String) request.get("focus");

            String prompt = buildExpandNodePrompt(nodeId, mapId, depth, count, focus);
            String result = chatWithModel(
                "You are a mind map expert. Return only valid JSON with a 'children' array, no markdown, no explanation.",
                prompt
            );

            Map<String, Object> data = Map.of(
                "nodeId", nodeId,
                "result", result,
                "tokenUsage", Map.of(
                    "inputTokens", inputTokens.get(),
                    "outputTokens", outputTokens.get()
                )
            );

            // Record task completion
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, true);
            }

            return AIServiceResponse.success("Node expansion prompt generated", data);
        } catch (Exception e) {
            // Record task failure
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, false);
            }
            LogUtils.warn("DefaultAgentService.handleExpandNode failed", e);
            return AIServiceResponse.error("Failed to expand node: " + e.getMessage());
        }
    }

    private AIServiceResponse handleSummarize(Map<String, Object> request) {
        String nodeId = (String) request.get("nodeId");
        if (nodeId == null) {
            return AIServiceResponse.error("NodeId is required");
        }

        // Create task for monitoring
        BuildTask task = new BuildTask("summarize", request);
        
        try {
            ensureAgentInitialized();
            
            // Record task start
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskStart(task);
            }

            String mapId = (String) request.get("mapId");
            Integer maxWords = (Integer) request.get("maxWords");
            Boolean writeToNote = (Boolean) request.get("writeToNote");

            String prompt = buildSummarizePrompt(nodeId, mapId, maxWords, writeToNote);
            String summary = chatWithModel(
                "You are a professional summarizer. Return only the summary text, no extra content.",
                prompt
            );

            Map<String, Object> data = Map.of(
                "nodeId", nodeId,
                "summary", summary,
                "tokenUsage", Map.of(
                    "inputTokens", inputTokens.get(),
                    "outputTokens", outputTokens.get()
                )
            );

            // Record task completion
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, true);
            }

            return AIServiceResponse.success("Branch summarized", data);
        } catch (Exception e) {
            // Record task failure
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, false);
            }
            LogUtils.warn("DefaultAgentService.handleSummarize failed", e);
            return AIServiceResponse.error("Failed to summarize: " + e.getMessage());
        }
    }

    private AIServiceResponse handleTag(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        java.util.List<String> nodeIds = (java.util.List<String>) request.get("nodeIds");
        if (nodeIds == null || nodeIds.isEmpty()) {
            return AIServiceResponse.error("NodeIds is required");
        }

        // Create task for monitoring
        BuildTask task = new BuildTask("tag", request);
        
        try {
            ensureAgentInitialized();
            
            // Record task start
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskStart(task);
            }

            String mapId = (String) request.get("mapId");
            String prompt = buildTagPrompt(nodeIds, mapId);
            String result = chatWithModel(
                "You are a tagging expert. Return only valid JSON with a 'tags' array, no markdown, no explanation.",
                prompt
            );

            Map<String, Object> data = Map.of(
                "nodeIds", nodeIds,
                "result", result,
                "tokenUsage", Map.of(
                    "inputTokens", inputTokens.get(),
                    "outputTokens", outputTokens.get()
                )
            );

            // Record task completion
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, true);
            }

            return AIServiceResponse.success("Tags generated", data);
        } catch (Exception e) {
            // Record task failure
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, false);
            }
            LogUtils.warn("DefaultAgentService.handleTag failed", e);
            return AIServiceResponse.error("Failed to generate tags: " + e.getMessage());
        }
    }

    /**
     * 供 BuildTaskExecutor 调用，确保 agent 已完成初始化。
     * public 以便跨包访问（scheduling 包需要调用）。
     */
    public void ensureAgentInitializedPublic() {
        ensureAgentInitialized();
    }

    /**
     * 分支摘要流式接口。
     * 复用 buildSummarizePrompt 构建 prompt，然后经由 StreamingChatModel 邀 token 回调。
     *
     * @param nodeId   节点 ID
     * @param mapId    导图 ID（可为 null）
     * @param maxWords 摘要最大字数（可为 null，默认 100）
     * @param handler  SSE 回调处理器
     */
    public void summarizeStream(String nodeId, String mapId, Integer maxWords,
                                StreamingChatResponseHandler handler) {
        ensureAgentInitialized();
        if (streamingChatModel == null) {
            handler.onError(new UnsupportedOperationException("Streaming not configured for current model"));
            return;
        }
        String prompt = buildSummarizePrompt(nodeId, mapId, maxWords, null);
        java.util.List<dev.langchain4j.data.message.ChatMessage> messages = java.util.Arrays.asList(
            SystemMessage.from("You are a professional summarizer. Return only the summary text, no extra content."),
            UserMessage.from(prompt)
        );
        streamingChatModel.chat(messages, handler);
    }

    private void ensureAgentInitialized() {
        if (agentService == null) {
            synchronized (DefaultAgentService.class) {
                if (agentService == null) {
                    try {
                        AIProviderConfiguration configuration = new AIProviderConfiguration();
                        if (!isProviderConfigured(configuration)) {
                            LogUtils.warn("DefaultAgentService: No AI provider configured");
                            return;
                        }

                        availableMaps = new AvailableMaps(new ControllerMapModelProvider());
                        bufferLayerRouter = new BufferLayerRouter();

                        ToolCallSummaryHandler toolCallSummaryHandler = summary -> {
                            LogUtils.info("Agent tool call: " + summary.getSummaryText());
                        };

                        AvailableMaps.MapAccessListener mapAccessListener = (mapId, mapModel) -> {
                            LogUtils.info("Map accessed: " + mapId);
                        };

                        toolSet = new AIToolSetBuilder()
                            .toolCallSummaryHandler(toolCallSummaryHandler)
                            .availableMaps(availableMaps)
                            .mapAccessListener(mapAccessListener)
                            .toolCaller(org.freeplane.plugin.ai.tools.utilities.ToolCaller.CHAT)
                            .build();

                        ChatTokenUsageTracker tokenTracker = new ChatTokenUsageTracker(totals -> {
                            inputTokens.addAndGet((int) totals.getInputTokenCount());
                            outputTokens.addAndGet((int) totals.getOutputTokenCount());
                        });

                        agentService = AIChatServiceFactory.createService(
                            toolSet,
                            null,
                            tokenTracker,
                            toolCallSummaryHandler,
                            () -> false,
                            usage -> {}
                        );

                        // 初始化底层 ChatModel（用于 generate/expand/summarize，绕开工具注册）
                        chatModel = AIChatModelFactory.createChatLanguageModel(configuration);
                        // 初始化流式 ChatModel（用于 summarizeStream）
                        streamingChatModel = AIChatModelFactory.createStreamingChatModel(configuration);

                        // 初始化工具执行服务
                        toolExecutionService = new DefaultToolExecutionService();
                        toolExecutionService.setToolSet(toolSet);

                        // 初始化调度组件
                        schedulingConfig = SchedulingConfig.getInstance();
                        schedulingMonitor = SchedulingMonitor.getInstance();
                        taskScheduler = BuildTaskScheduler.getInstance();
                        taskScheduler.start();

                        LogUtils.info("DefaultAgentService: Agent AIChatService initialized successfully");
                        LogUtils.info("DefaultAgentService: ToolExecutionService initialized successfully");
                        LogUtils.info("DefaultAgentService: Scheduling components initialized successfully");
                    } catch (Exception e) {
                        LogUtils.warn("DefaultAgentService: Failed to initialize Agent AIChatService", e);
                    }
                }
            }
        }
    }

    private boolean isProviderConfigured(AIProviderConfiguration configuration) {
        return isNonEmpty(configuration.getOpenRouterKey())
            || isNonEmpty(configuration.getGeminiKey())
            || configuration.hasOllamaServiceAddress()
            || configuration.hasErnieKey()
            || configuration.hasDashScopeKey();
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 使用底层 ChatModel 直接发送请求，不带工具注册和控制指令 system message。
     * 用于 generate/expand/summarize，避免 AIChatService 的复杂 system message 干扰输出。
     */
    private String chatWithModel(String systemPrompt, String userPrompt) {
        if (chatModel == null) {
            throw new IllegalStateException("ChatModel not initialized");
        }
        ChatRequest request = ChatRequest.builder()
            .messages(Arrays.asList(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userPrompt)
            ))
            .build();
        ChatResponse response = chatModel.chat(request);
        return response.aiMessage().text();
    }

    private String buildMindMapPrompt(String topic, Map<String, Object> request) {
        // 使用 MindMapPromptOptimizer 从 prompts.yaml 加载 CoT 模板
        org.freeplane.plugin.ai.buffer.BufferRequest bufferRequest = 
            new org.freeplane.plugin.ai.buffer.BufferRequest("生成思维导图：" + topic);
        bufferRequest.setRequestType(org.freeplane.plugin.ai.buffer.BufferRequest.RequestType.MINDMAP_GENERATION);
        bufferRequest.addParameter("topic", topic);
        bufferRequest.addParameter("maxDepth", request.get("maxDepth") != null ? (Integer) request.get("maxDepth") : 3);
        bufferRequest.addParameter("language", "zh");
        
        return promptOptimizer.optimizePrompt(bufferRequest);
    }

    private String buildExpandNodePrompt(String nodeId, String mapId, Integer depth, Integer count, String focus) {
        if (depth == null) depth = 1;
        if (count == null) count = 3;
        if (focus == null) focus = "相关内容";

        // Java 侧预读取节点真实文字及上下文
        String nodeText = nodeId;
        String contextInfo = "";
        try {
            if (availableMaps != null) {
                MapModel mapModel = availableMaps.getCurrentMapModel();
                if (mapModel != null) {
                    NodeModel node = mapModel.getNodeForID(nodeId);
                    if (node != null) {
                        nodeText = node.getText();
                        NodeModel parent = node.getParentNode();
                        if (parent != null) {
                            contextInfo += "\n父节点：" + parent.getText();
                        }
                        if (node.getChildCount() > 0) {
                            StringBuilder existingChildren = new StringBuilder("\n已有子节点：");
                            for (int i = 0; i < Math.min(node.getChildCount(), 5); i++) {
                                existingChildren.append("\n- ").append(node.getChildAt(i).getText());
                            }
                            contextInfo += existingChildren.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.warn("DefaultAgentService: failed to read node text for " + nodeId, e);
        }

        // 使用 MindMapPromptOptimizer 从 prompts.yaml 加载 CoT 模板
        org.freeplane.plugin.ai.buffer.BufferRequest bufferRequest = 
            new org.freeplane.plugin.ai.buffer.BufferRequest("展开节点：" + nodeText);
        bufferRequest.setRequestType(org.freeplane.plugin.ai.buffer.BufferRequest.RequestType.NODE_EXPANSION);
        bufferRequest.addParameter("nodeText", nodeText);
        bufferRequest.addParameter("contextInfo", contextInfo);
        bufferRequest.addParameter("depth", depth);
        bufferRequest.addParameter("count", count);
        bufferRequest.addParameter("focus", focus);
        bufferRequest.addParameter("language", "zh");
        
        return promptOptimizer.optimizePrompt(bufferRequest);
    }

    private String buildSummarizePrompt(String nodeId, String mapId, Integer maxWords, Boolean writeToNote) {
        if (maxWords == null) maxWords = 100;

        // Java 侧预读取整棵子树文字
        String branchContent = "（无法读取节点内容）";
        try {
            if (availableMaps != null) {
                MapModel mapModel = availableMaps.getCurrentMapModel();
                if (mapModel != null) {
                    NodeModel node = mapModel.getNodeForID(nodeId);
                    if (node != null) {
                        branchContent = extractBranchText(node);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.warn("DefaultAgentService: failed to read branch text for " + nodeId, e);
        }

        // 使用 MindMapPromptOptimizer 从 prompts.yaml 加载 CoT 模板
        org.freeplane.plugin.ai.buffer.BufferRequest bufferRequest = 
            new org.freeplane.plugin.ai.buffer.BufferRequest("生成摘要：" + branchContent.substring(0, Math.min(50, branchContent.length())));
        bufferRequest.setRequestType(org.freeplane.plugin.ai.buffer.BufferRequest.RequestType.BRANCH_SUMMARY);
        bufferRequest.addParameter("content", branchContent);
        bufferRequest.addParameter("maxWords", maxWords);
        bufferRequest.addParameter("language", "zh");
        
        return promptOptimizer.optimizePrompt(bufferRequest);
    }

    private String extractBranchText(NodeModel node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getText());
        if (node.getChildCount() > 0) {
            sb.append("\n");
            extractChildrenText(node, sb, 1);
        }
        return sb.toString();
    }

    private void extractChildrenText(NodeModel node, StringBuilder sb, int depth) {
        if (depth > 5) return;
        String indent = "  ".repeat(depth);
        for (int i = 0; i < node.getChildCount(); i++) {
            NodeModel child = (NodeModel) node.getChildAt(i);
            sb.append(indent).append("- ").append(child.getText()).append("\n");
            if (child.getChildCount() > 0) {
                extractChildrenText(child, sb, depth + 1);
            }
        }
    }

    private String buildTagPrompt(java.util.List<String> nodeIds, String mapId) {
        return String.format(
            "你是一位专业的标签生成专家，擅长为思维导图节点生成简洁、有意义的标签。\n\n" +
            "任务：请为以下节点生成合适的标签。\n\n" +
            "节点列表：\n%s\n\n" +
            "详细指令：\n" +
            "1. 每个节点生成1-3个标签\n" +
            "2. 标签应该简洁、有意义\n" +
            "3. 标签应与节点内容相关\n\n" +
            "重要提示：由于您无法直接访问思维导图节点，请按以下步骤操作：\n" +
            "1. 首先使用 getSelectedMapAndNodeIdentifiers 或 readNodesWithDescendants 工具获取节点信息\n" +
            "2. 然后使用 readNodesWithDescendants 工具读取每个节点的内容\n" +
            "3. 基于内容生成标签\n" +
            "4. 如需写入标签，请使用以下流程：\n" +
            "   a) 对于TAGS/ICONS：可直接使用 edit 工具（无需 fetchNodesForEditing）\n" +
            "   b) 对于TEXT/DETAILS/NOTE：必须先调用 fetchNodesForEditing 获取 originalContentType\n\n" +
            "返回格式：\n" +
            "请返回JSON格式，包含每个节点的ID和对应的标签列表：\n" +
            "{\n" +
            "  \"tags\": [\n" +
            "    {\"nodeId\": \"节点ID\", \"tags\": [\"标签1\", \"标签2\"]},\n" +
            "    ...\n" +
            "  ]\n" +
            "}",
            nodeIds
        );
    }

    private AIServiceResponse handleExecuteTool(Map<String, Object> request) {
        // Create task for monitoring
        BuildTask task = new BuildTask("execute-tool", request);
        
        try {
            ensureAgentInitialized();
            
            // Record task start
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskStart(task);
            }

            String toolName = (String) request.get("toolName");
            if (toolName == null || toolName.trim().isEmpty()) {
                return AIServiceResponse.error("Tool name is required");
            }

            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            if (parameters == null) {
                parameters = new HashMap<>();
            }

            if (toolExecutionService == null) {
                return AIServiceResponse.error("Tool execution service not initialized");
            }

            if (!toolExecutionService.isToolSupported(toolName)) {
                return AIServiceResponse.error("Tool not supported: " + toolName);
            }

            Object result = toolExecutionService.executeTool(toolName, parameters);

            Map<String, Object> data = Map.of(
                "toolName", toolName,
                "result", result
            );

            // Record task completion
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, true);
            }

            return AIServiceResponse.success("Tool executed successfully", data);
        } catch (Exception e) {
            // Record task failure
            if (schedulingMonitor != null) {
                schedulingMonitor.recordTaskComplete(task, false);
            }
            LogUtils.warn("DefaultAgentService.handleExecuteTool failed", e);
            return AIServiceResponse.error("Failed to execute tool: " + e.getMessage());
        }
    }

    @Override
    public boolean canHandle(Map<String, Object> request) {
        String serviceType = (String) request.get("serviceType");
        return AIServiceType.AGENT.getCode().equals(serviceType);
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
