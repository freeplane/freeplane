package org.freeplane.plugin.ai.validation.source;

import org.freeplane.plugin.ai.validation.MindMapGenerationValidator;
import org.freeplane.plugin.ai.validation.MindMapValidationResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 端到端场景验证测试
 * 
 * <p>模拟三大接入点的真实使用场景:
 * <ol>
 *   <li>MindMapBufferLayer步骤4.5 - Prompt生成验证</li>
 *   <li>MapRestController.handleImportMap - 文件导入前置拦截</li>
 *   <li>Build流式输出 - 流式分块聚合验证(未来)</li>
 * </ol>
 */
public class EndToEndScenarioTest {

    // =========================================================================
    // 场景1: MindMapBufferLayer.validateAndHandleDegradation()
    // =========================================================================

    @Test
    public void scenario1_MindMapBufferLayer_SuccessPath() {
        // 模拟: AI生成正常思维导图
        String aiResponse = "{" +
            "\"id\": \"root-001\"," +
            "\"text\": \"Java核心技术\"," +
            "\"children\": [" +
            "  {\"id\": \"c1\", \"text\": \"基础语法\", \"children\": [" +
            "    {\"id\": \"c1-1\", \"text\": \"变量与类型\"}," +
            "    {\"id\": \"c1-2\", \"text\": \"控制流\"}" +
            "  ]}," +
            "  {\"id\": \"c2\", \"text\": \"面向对象\", \"children\": [" +
            "    {\"id\": \"c2-1\", \"text\": \"类与对象\"}," +
            "    {\"id\": \"c2-2\", \"text\": \"继承与多态\"}" +
            "  ]}," +
            "  {\"id\": \"c3\", \"text\": \"集合框架\"}" +
            "]" +
            "}";
        
        // 步骤1: 创建PromptValidationSource
        ValidationSource source = new PromptValidationSource(aiResponse, "ernie-4.0");
        
        // 步骤2: 调用验证器(模拟MindMapBufferLayer的validateAndHandleDegradation)
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult validationResult = validator.validate(source);
        
        // 步骤3: 验证结果处理
        if (validationResult.isValid()) {
            // 验证通过,继续创建节点
            assertThat(validationResult.isValid()).isTrue();
            // root + c1 + c1-1 + c1-2 + c2 + c2-1 + c2-2 + c3 = 8节点
            assertThat(validationResult.getStatistics().getTotalNodes()).isEqualTo(8);
            System.out.println("✅ [场景1-成功路径] 思维导图验证通过,节点数: " + 
                validationResult.getStatistics().getTotalNodes());
        } else {
            throw new AssertionError("验证不应失败");
        }
    }

    @Test
    public void scenario1_MindMapBufferLayer_CircularDependencyDegrade() {
        // 模拟: AI生成了包含环的思维导图(LLM幻觉)
        String aiResponseWithCycle = "{" +
            "\"id\": \"root\"," +
            "\"text\": \"错误示例\"," +
            "\"children\": [" +
            "  {\"id\": \"node-a\", \"text\": \"节点A\", \"children\": [" +
            "    {\"id\": \"node-b\", \"text\": \"节点B\", \"children\": [" +
            "      {\"id\": \"node-a\", \"text\": \"错误回环\"}" + // 环: node-b → node-a
            "    ]}" +
            "  ]}" +
            "]" +
            "}";
        
        ValidationSource source = new PromptValidationSource(aiResponseWithCycle, "gpt-4");
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult validationResult = validator.validate(source);
        
        // 模拟降级策略
        boolean hasCycle = validationResult.getErrors().stream()
            .anyMatch(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()));
        boolean hasParseError = validationResult.getErrors().stream()
            .anyMatch(e -> "PARSE_ERROR".equals(e.getCode()));
        
        if (hasCycle || hasParseError) {
            // 必须降级 → 返回sampleJSON
            String reason = hasCycle ? "CIRCULAR_DEPENDENCY" : "PARSE_ERROR";
            System.out.println("⚠️ [场景1-降级路径] 检测到 " + reason + ", 降级为示例思维导图");
            System.out.println("   错误信息: " + validationResult.getErrors().stream()
                .filter(e -> reason.equals(e.getCode()))
                .findFirst().map(e -> e.getMessage()).orElse("未知"));
            
            // 实际代码: return createSampleMindMapJSON(topic);
            String sampleJSON = "{\"text\":\"降级示例\",\"children\":[" +
                "{\"text\":\"分支1\"},{\"text\":\"分支2\"}" +
                "]}";
            assertThat(sampleJSON).contains("降级示例");
        } else {
            throw new AssertionError("应检测到环");
        }
    }

    @Test
    public void scenario1_MindMapBufferLayer_ParseErrorDegrade() {
        // 模拟: AI返回了无效JSON
        String invalidAIResponse = "Here is your mindmap:\n" +
            "```json\n" +
            "{invalid json content}\n" +
            "```";
        
        ValidationSource source = new PromptValidationSource(invalidAIResponse, "claude-3");
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult validationResult = validator.validate(source);
        
        boolean hasParseError = validationResult.getErrors().stream()
            .anyMatch(e -> "PARSE_ERROR".equals(e.getCode()));
        
        assertThat(hasParseError).isTrue();
        System.out.println("⚠️ [场景1-解析失败] JSON解析错误, 降级为示例思维导图");
    }

    // =========================================================================
    // 场景2: MapRestController.handleImportMap - 文件导入前置拦截
    // =========================================================================

    @Test
    public void scenario2_MapImport_ValidFile_ShouldPass() {
        // 模拟: 导入有效的.mm文件内容
        String mmContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<map version=\"1.0.1\">\n" +
            "<node TEXT=\"导入的思维导图\">\n" +
            "  <node TEXT=\"分支1\"/>\n" +
            "  <node TEXT=\"分支2\"/>\n" +
            "</node>\n" +
            "</map>";
        
        String filename = "valid-map.mm";
        
        // 步骤0: 前置环检测拦截
        // 注意: .mm是XML格式,这里用JSON模拟(实际应解析XML后转JSON验证)
        String jsonContent = "{\"id\":\"imported-root\",\"text\":\"导入的思维导图\"," +
            "\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"分支1\"}," +
            "{\"id\":\"c2\",\"text\":\"分支2\"}" +
            "]}";
        
        ValidationSource source = new FileValidationSource(jsonContent, filename);
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult preValidation = validator.validate(source);
        
        // 检查是否有环
        boolean hasCycle = preValidation.getErrors().stream()
            .anyMatch(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()));
        
        if (hasCycle) {
            // 拒绝导入
            throw new AssertionError("有效文件不应被拒绝");
        } else {
            // 允许导入,继续XML解析
            System.out.println("✅ [场景2-文件导入] 前置验证通过, 允许导入 " + filename);
            assertThat(preValidation.isValid()).isTrue();
        }
    }

    @Test
    public void scenario2_MapImport_CircularDependency_ShouldReject() {
        // 模拟: 导入包含环的思维导图(可能是恶意文件或bug)
        String maliciousContent = "{\"id\":\"root\",\"text\":\"恶意文件\"," +
            "\"children\":[" +
            "{\"id\":\"a\",\"text\":\"A\",\"children\":[" +
            "{\"id\":\"b\",\"text\":\"B\",\"children\":[" +
            "{\"id\":\"a\",\"text\":\"环\"}" + // 环
            "]}" +
            "]}" +
            "]}";
        
        String filename = "malicious.mm";
        
        // 步骤0: 前置环检测拦截
        ValidationSource source = new FileValidationSource(maliciousContent, filename);
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult preValidation = validator.validate(source);
        
        boolean hasCycle = preValidation.getErrors().stream()
            .anyMatch(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()));
        
        if (hasCycle) {
            // 拒绝导入,返回400
            String errorMsg = preValidation.getErrors().stream()
                .filter(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()))
                .findFirst().map(e -> e.getMessage()).orElse("未知环");
            
            System.out.println("🚫 [场景2-拒绝导入] 检测到循环依赖, 拒绝导入 " + filename);
            System.out.println("   错误: " + errorMsg);
            
            // 实际代码: sendError(exchange, 400, "导入失败: 检测到循环依赖");
            assertThat(preValidation.isValid()).isFalse();
        } else {
            throw new AssertionError("恶意文件应被拒绝");
        }
    }

    // =========================================================================
    // 场景3: Build流式输出 - 流式分块聚合验证(未来接入点)
    // =========================================================================

    @Test
    public void scenario3_BuildStream_AggregateAndValidate() throws Exception {
        // 模拟: Build功能的SSE流式输出
        StreamValidationSource streamSource = new StreamValidationSource("build-expand-node123");
        
        // 模拟SSE chunk逐个到达
        String[] chunks = {
            "{\"id\": \"expanded-node\",",
            "\"text\": \"展开的节点\",",
            "\"children\": [",
            "  {\"id\": \"child-1\", \"text\": \"子节点1\"},",
            "  {\"id\": \"child-2\", \"text\": \"子节点2\"},",
            "  {\"id\": \"child-3\", \"text\": \"子节点3\"}",
            "]",
            "}"
        };
        
        // 每个SSE chunk到达时追加
        for (String chunk : chunks) {
            streamSource.append(chunk);
            // 注意: 此时不应调用验证,流未完成
            assertThat(streamSource.isReady()).isFalse();
        }
        
        // 流结束,标记完成
        streamSource.markComplete();
        assertThat(streamSource.isReady()).isTrue();
        
        // 触发验证
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult result = validator.validate(streamSource);
        
        assertThat(result.isValid()).isTrue();
        // expanded-node + child-1 + child-2 + child-3 = 4节点
        assertThat(result.getStatistics().getTotalNodes()).isEqualTo(4);
        System.out.println("✅ [场景3-流式聚合] 流式输出聚合完成, 节点数: " + 
            result.getStatistics().getTotalNodes());
    }

    @Test
    public void scenario3_BuildStream_IncompleteStreamShouldNotValidate() {
        // 模拟: 流中断或超时
        StreamValidationSource streamSource = new StreamValidationSource("build-timeout");
        streamSource.append("{\"id\": \"root\", \"text\": \"未完整\"");
        // 流中断,未markComplete
        
        // 尝试验证应返回NOT_READY
        MindMapGenerationValidator validator = new MindMapGenerationValidator();
        MindMapValidationResult result = validator.validate(streamSource);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "NOT_READY".equals(e.getCode()))).isTrue();
        
        System.out.println("⚠️ [场景3-流中断] 检测到未完成的流, 返回NOT_READY错误");
    }

    // =========================================================================
    // 场景4: 日志追溯验证
    // =========================================================================

    @Test
    public void scenario4_LogTraceability_AllSourcesShouldHaveContext() {
        // 验证: 所有数据源都能提供完整的追溯信息
        
        // 1. Prompt来源
        ValidationSource prompt = new PromptValidationSource("{}", "ernie-4.0");
        System.out.println("📝 [日志追溯-Prompt] " + 
            "来源=" + prompt.getSourceType() + 
            ", 描述=" + prompt.getDescription());
        assertThat(prompt.getDescription()).contains("ernie-4.0");
        
        // 2. 流式来源
        StreamValidationSource stream = new StreamValidationSource("build-summarize-node456");
        stream.append("{}");
        stream.markComplete();
        System.out.println("📝 [日志追溯-Stream] " + 
            "来源=" + stream.getSourceType() + 
            ", 描述=" + stream.getDescription());
        assertThat(stream.getDescription()).contains("build-summarize-node456");
        
        // 3. 文件来源
        ValidationSource file = new FileValidationSource("{}", "imported.mm");
        System.out.println("📝 [日志追溯-File] " + 
            "来源=" + file.getSourceType() + 
            ", 描述=" + file.getDescription());
        assertThat(file.getDescription()).contains("imported.mm");
    }
}
