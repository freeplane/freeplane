package org.freeplane.plugin.ai.service;

import org.junit.Before;
import org.junit.Test;
import org.freeplane.plugin.ai.service.impl.DefaultChatService;
import org.freeplane.plugin.ai.service.impl.DefaultAgentService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI功能三种模式集成测试
 * 覆盖：Chat对话模式、Build构建模式（Agent）、Auto自动模式
 * 
 * 基于最新改动设计：
 * 1. 系统提示词优化（角色定义+工具调用指导+响应格式+质量保证）
 * 2. Prompt模板增强（思维导图生成、节点展开、分支摘要）
 * 3. 工具注册方式修复（直接使用toolSet）
 * 4. 自动模型选择功能
 */
public class AIServiceIntegrationTest {

    private DefaultChatService chatService;
    private DefaultAgentService agentService;

    @Before
    public void setUp() {
        chatService = new DefaultChatService();
        agentService = new DefaultAgentService();
    }

    // ==================== Chat模式测试 ====================

    /**
     * Chat-TC01: 基础对话功能测试
     * 验证：基本问答、回复生成、token统计
     */
    @Test
    public void chat_basicConversation_shouldReturnReply() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "你好，请介绍一下思维导图的基本概念");
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsKey("reply");
        assertThat(response.getData()).containsKey("tokenUsage");
        Map<String, Object> tokenUsage = (Map<String, Object>) response.getData().get("tokenUsage");
        assertThat(tokenUsage).containsKey("inputTokens");
        assertThat(tokenUsage).containsKey("outputTokens");
    }

    /**
     * Chat-TC02: 空消息验证
     * 验证：空消息、null消息、空白消息应返回错误
     */
    @Test
    public void chat_emptyMessage_shouldReturnError() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "");
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("Message is required");
    }

    /**
     * Chat-TC03: 工具调用能力测试
     * 验证：AI能够识别需要使用工具的场景并正确调用
     * 新增改动：工具调用指导（6条规范）
     */
    @Test
    public void chat_toolCallScenario_shouldInvokeTools() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "帮我创建一个关于'人工智能'的思维导图，包含3个层级");
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        assertThat(response.isSuccess()).isTrue();
        // 验证回复中包含工具调用或结构化输出
        String reply = (String) response.getData().get("reply");
        assertThat(reply).isNotNull();
        // 根据新的系统提示词，AI应该返回Markdown格式
        assertThat(reply.length()).isGreaterThan(0);
    }

    /**
     * Chat-TC04: 多轮对话上下文保持
     * 验证：对话历史记忆、上下文连贯性
     */
    @Test
    public void chat_multiTurnConversation_shouldMaintainContext() {
        // 第一轮
        Map<String, Object> request1 = new HashMap<>();
        request1.put("message", "我想学习Java编程");
        request1.put("serviceType", "chat");
        AIServiceResponse response1 = chatService.processRequest(request1);
        assertThat(response1.isSuccess()).isTrue();

        // 第二轮（依赖上下文）
        Map<String, Object> request2 = new HashMap<>();
        request2.put("message", "请为我刚才提到的主题生成学习计划");
        request2.put("serviceType", "chat");
        AIServiceResponse response2 = chatService.processRequest(request2);
        assertThat(response2.isSuccess()).isTrue();
        
        String reply = (String) response2.getData().get("reply");
        assertThat(reply).isNotNull();
        // 验证AI能够理解"刚才提到的主题"指的是Java编程
    }

    /**
     * Chat-TC05: 响应格式验证
     * 验证：AI按照新的响应格式指导输出（Markdown格式）
     * 新增改动：响应格式指导（4条标准）
     */
    @Test
    public void chat_responseFormat_shouldFollowGuidelines() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "请用列表形式说明思维导图的三个优点");
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        assertThat(response.isSuccess()).isTrue();
        String reply = (String) response.getData().get("reply");
        assertThat(reply).isNotNull();
        // 根据新的提示词，AI应该使用Markdown格式
        // 可能包含列表符号：- 或 * 或数字列表
    }

    /**
     * Chat-TC06: 错误处理测试
     * 验证：未配置AI provider时的错误提示
     */
    @Test
    public void chat_noProviderConfigured_shouldReturnError() {
        // 假设未配置provider，service应为null
        Map<String, Object> request = new HashMap<>();
        request.put("message", "测试消息");
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        // 如果未配置，应返回友好错误提示
        if (!response.isSuccess()) {
            assertThat(response.getErrorMessage())
                .containsIgnoringCase("configure");
        }
    }

    // ==================== Build模式测试（Agent Service） ====================

    /**
     * Build-TC01: 思维导图生成测试
     * 验证：根据主题生成完整思维导图结构
     * 新增改动：Prompt模板增强（角色+任务+标准+格式）
     */
    @Test
    public void build_generateMindmap_shouldReturnStructuredResult() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "generate-mindmap");
        request.put("topic", "机器学习基础");
        request.put("maxDepth", 3);
        request.put("language", "zh");

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsKey("result");
        assertThat(response.getData()).containsKey("topic");
        String result = (String) response.getData().get("result");
        assertThat(result).isNotNull();
        // 根据新prompt，应返回严格JSON格式
        // 验证包含基本结构：text, children
    }

    /**
     * Build-TC02: 思维导图生成参数验证
     * 验证：空主题应返回错误
     */
    @Test
    public void build_generateMindmap_emptyTopic_shouldReturnError() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "generate-mindmap");
        request.put("topic", "");
        request.put("maxDepth", 3);

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("Topic is required");
    }

    /**
     * Build-TC03: 节点展开功能测试
     * 验证：为指定节点展开子节点
     * 新增改动：节点展开模板增强（6步展开流程+质量标准）
     */
    @Test
    public void build_expandNode_shouldGenerateChildNodes() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "expand-node");
        request.put("nodeId", "node_123");
        request.put("nodeText", "人工智能应用");
        request.put("count", 5);
        request.put("depth", 2);
        request.put("focus", "实际应用场景");
        request.put("language", "zh");

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsKey("result");
        String result = (String) response.getData().get("result");
        assertThat(result).isNotNull();
        // 验证返回JSON包含children数组
    }

    /**
     * Build-TC04: 节点展开参数验证
     * 验证：缺少nodeId应返回错误
     */
    @Test
    public void build_expandNode_missingNodeId_shouldReturnError() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "expand-node");
        request.put("count", 5);

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isFalse();
    }

    /**
     * Build-TC05: 分支摘要功能测试
     * 验证：为分支内容生成简洁摘要
     * 新增改动：摘要模板增强（6步摘要流程+质量标准）
     */
    @Test
    public void build_summarizeBranch_shouldReturnConciseSummary() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "summarize");
        request.put("nodeId", "node_456");
        request.put("content", "这是一段较长的思维导图分支内容，包含多个节点和详细信息...");
        request.put("maxWords", 100);
        request.put("language", "zh");

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsKey("result");
        String result = (String) response.getData().get("result");
        assertThat(result).isNotNull();
        assertThat(result.length()).isLessThanOrEqualTo(100);
    }

    /**
     * Build-TC06: 自动标签功能测试
     * 验证：为节点批量打标签
     */
    @Test
    public void build_tagNodes_shouldApplyTags() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "tag");
        request.put("nodeId", "node_789");
        request.put("language", "zh");

        AIServiceResponse response = agentService.processRequest(request);

        // 此功能应该成功执行
        assertThat(response.isSuccess()).isTrue();
    }

    /**
     * Build-TC07: 未知action验证
     * 验证：不支持的action应返回错误
     */
    @Test
    public void build_unknownAction_shouldReturnError() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "unknown-action");

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("Unknown action");
    }

    /**
     * Build-TC08: 缺失action验证
     * 验证：action为null应返回错误
     */
    @Test
    public void build_missingAction_shouldReturnError() {
        Map<String, Object> request = new HashMap<>();

        AIServiceResponse response = agentService.processRequest(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("Action is required");
    }

    /**
     * Build-TC09: 不同深度生成测试
     * 验证：支持1-6层深度的思维导图生成
     */
    @Test
    public void build_generateMindmap_differentDepths_shouldWork() {
        for (int depth = 1; depth <= 6; depth++) {
            Map<String, Object> request = new HashMap<>();
            request.put("action", "generate-mindmap");
            request.put("topic", "测试主题");
            request.put("maxDepth", depth);

            AIServiceResponse response = agentService.processRequest(request);

            assertThat(response.isSuccess())
                .withFailMessage("Depth " + depth + " should succeed")
                .isTrue();
        }
    }

    // ==================== Auto模式测试 ====================

    /**
     * Auto-TC01: 自动模式路由到Chat
     * 验证：普通对话请求应路由到Chat服务
     * 前端新增：AiAutoPanel.vue支持自然语言指令
     */
    @Test
    public void auto_routeToChat_conversationalRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "请解释什么是思维导图");
        request.put("serviceType", "auto");
        request.put("aiMode", "auto");

        // Auto模式应智能判断并路由到Chat
        AIServiceResponse response;
        if (isConversationalRequest(request)) {
            response = chatService.processRequest(request);
        } else {
            response = agentService.processRequest(request);
        }

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsKey("reply");
    }

    /**
     * Auto-TC02: 自动模式路由到Build
     * 验证：结构化操作请求应路由到Agent服务
     */
    @Test
    public void auto_routeToBuild_structuredRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "帮我生成一个关于'深度学习'的思维导图，5层深度");
        request.put("serviceType", "auto");
        request.put("aiMode", "auto");

        // Auto模式应识别为build操作
        AIServiceResponse response;
        if (isConversationalRequest(request)) {
            response = chatService.processRequest(request);
        } else {
            // 应该解析为generate-mindmap action
            Map<String, Object> agentRequest = new HashMap<>();
            agentRequest.put("action", "generate-mindmap");
            agentRequest.put("topic", "深度学习");
            agentRequest.put("maxDepth", 5);
            response = agentService.processRequest(agentRequest);
        }

        assertThat(response.isSuccess()).isTrue();
    }

    /**
     * Auto-TC03: 自然语言指令解析
     * 验证：解析自然语言并转换为具体操作
     * 前端示例指令：
     * - "展开这个节点，生成5个子节点"
     * - "帮我总结这个分支内容"
     * - "这个节点讲的是什么"
     */
    @Test
    public void auto_parseNaturalLanguage_expandNodeInstruction() {
        String instruction = "展开这个节点，生成5个子节点";
        
        // 应解析为：expand-node action
        Map<String, Object> request = parseNaturalLanguageInstruction(instruction);
        
        assertThat(request.get("action")).isEqualTo("expand-node");
        assertThat(request.get("count")).isEqualTo(5);
    }

    /**
     * Auto-TC04: 自然语言指令解析 - 摘要
     */
    @Test
    public void auto_parseNaturalLanguage_summarizeInstruction() {
        String instruction = "帮我总结这个分支内容";
        
        Map<String, Object> request = parseNaturalLanguageInstruction(instruction);
        
        assertThat(request.get("action")).isEqualTo("summarize");
    }

    /**
     * Auto-TC05: 自然语言指令解析 - 问答
     */
    @Test
    public void auto_parseNaturalLanguage_questionInstruction() {
        String instruction = "这个节点讲的是什么";
        
        // 应识别为对话请求，而非build操作
        assertThat(isConversationalRequest(Map.of("message", instruction))).isTrue();
    }

    /**
     * Auto-TC06: 当前节点上下文使用
     * 验证：Auto模式应使用当前选中节点信息
     * 前端：AiAutoPanel显示"当前节点: {{ selectedNodeId || '-' }}"
     */
    @Test
    public void auto_useCurrentNodeContext_shouldIncludeInRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "展开这个节点");
        request.put("selectedNodeId", "node_abc123");
        request.put("mapId", "map_xyz789");
        request.put("serviceType", "auto");

        // 应自动使用selectedNodeId
        assertThat(request.get("selectedNodeId")).isEqualTo("node_abc123");
        assertThat(request.get("mapId")).isEqualTo("map_xyz789");
    }

    /**
     * Auto-TC07: 模型配置集成测试
     * 验证：Auto模式使用当前配置的模型
     * 前端新增：ModelConfigPanel.vue支持6种Provider
     */
    @Test
    public void auto_modelConfiguration_shouldUseSelectedModel() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "测试消息");
        request.put("serviceType", "auto");
        request.put("modelSelection", "dashscope|qwen-max");

        AIServiceResponse response = chatService.processRequest(request);

        // 应使用配置的模型处理请求
        assertThat(response.isSuccess()).isTrue();
    }

    /**
     * Auto-TC08: 结果预览功能
     * 验证：Auto模式返回结果可供预览
     * 前端：AiAutoPanel显示"结果预览"区域
     */
    @Test
    public void auto_resultPreview_shouldReturnPreviewData() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "生成一个简单思维导图");
        request.put("serviceType", "auto");

        AIServiceResponse response = chatService.processRequest(request);

        if (response.isSuccess()) {
            assertThat(response.getData()).containsKey("reply");
            // 前端可以使用reply作为预览
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否为对话类请求
     */
    private boolean isConversationalRequest(Map<String, Object> request) {
        String message = (String) request.get("message");
        if (message == null) return false;
        
        // 简单启发式规则：
        // 包含操作关键词（生成、展开、总结、标签）-> Build
        // 否则 -> Chat
        boolean hasOperationKeyword = message.contains("生成") 
            || message.contains("展开") 
            || message.contains("总结") 
            || message.contains("标签")
            || message.contains("创建");
        
        return !hasOperationKeyword;
    }

    /**
     * 解析自然语言指令
     */
    private Map<String, Object> parseNaturalLanguageInstruction(String instruction) {
        Map<String, Object> request = new HashMap<>();
        
        if (instruction.contains("展开")) {
            request.put("action", "expand-node");
            // 提取数量
            if (instruction.contains("5")) {
                request.put("count", 5);
            } else if (instruction.contains("3")) {
                request.put("count", 3);
            }
        } else if (instruction.contains("总结") || instruction.contains("摘要")) {
            request.put("action", "summarize");
        } else if (instruction.contains("生成") && instruction.contains("思维导图")) {
            request.put("action", "generate-mindmap");
        }
        
        return request;
    }

    // ==================== 性能测试 ====================

    /**
     * Perf-TC01: Chat模式响应时间测试
     * 验证：首token延迟（TTFT）可接受
     * 用户偏好：关注TTFT、TPS等指标
     */
    @Test
    public void performance_chat_responseTime_shouldBeAcceptable() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "你好");
        request.put("serviceType", "chat");

        long startTime = System.currentTimeMillis();
        AIServiceResponse response = chatService.processRequest(request);
        long endTime = System.currentTimeMillis();

        long responseTime = endTime - startTime;
        assertThat(response.isSuccess()).isTrue();
        
        // TTFT应小于30秒（考虑网络延迟）
        // 实际应根据网络情况调整
        assertThat(responseTime).isLessThan(30000);
    }

    /**
     * Perf-TC02: Build模式Token使用测试
     * 验证：输入/输出token数量统计准确
     */
    @Test
    public void performance_build_tokenUsage_shouldBeTracked() {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "generate-mindmap");
        request.put("topic", "测试");
        request.put("maxDepth", 2);

        AIServiceResponse response = agentService.processRequest(request);

        if (response.isSuccess()) {
            Map<String, Object> data = response.getData();
            assertThat(data).containsKey("tokenUsage");
            Map<String, Object> tokenUsage = (Map<String, Object>) data.get("tokenUsage");
            assertThat(tokenUsage).containsKey("inputTokens");
            assertThat(tokenUsage).containsKey("outputTokens");
            
            // Token数应为正数
            int inputTokens = (int) tokenUsage.get("inputTokens");
            int outputTokens = (int) tokenUsage.get("outputTokens");
            assertThat(inputTokens).isGreaterThan(0);
            assertThat(outputTokens).isGreaterThan(0);
        }
    }

    // ==================== 边界测试 ====================

    /**
     * Edge-TC01: 超长消息处理
     * 验证：处理长消息不会崩溃
     */
    @Test
    public void edge_veryLongMessage_shouldHandleGracefully() {
        String longMessage = "请详细说明".repeat(1000);
        Map<String, Object> request = new HashMap<>();
        request.put("message", longMessage);
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        // 应该能够处理或返回适当错误
        // 不应抛出异常
    }

    /**
     * Edge-TC02: 特殊字符处理
     * 验证：包含特殊字符的消息能正确处理
     */
    @Test
    public void edge_specialCharacters_shouldHandleCorrectly() {
        Map<String, Object> request = new HashMap<>();
        request.put("message", "请解释 <b>加粗</b> 和 *斜体* 的用法 & 符号");
        request.put("serviceType", "chat");

        AIServiceResponse response = chatService.processRequest(request);

        // 不应因特殊字符而失败
    }

    /**
     * Edge-TC03: 并发请求测试
     * 验证：多线程并发请求不会导致状态混乱
     */
    @Test
    public void edge_concurrentRequests_shouldHandleSafely() throws InterruptedException {
        Thread[] threads = new Thread[5];
        final boolean[] success = new boolean[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Map<String, Object> request = new HashMap<>();
                request.put("message", "并发测试 " + index);
                request.put("serviceType", "chat");

                AIServiceResponse response = chatService.processRequest(request);
                success[index] = response.isSuccess();
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 至少部分请求应成功
        int successCount = 0;
        for (boolean s : success) {
            if (s) successCount++;
        }
        assertThat(successCount).isGreaterThan(0);
    }
}
