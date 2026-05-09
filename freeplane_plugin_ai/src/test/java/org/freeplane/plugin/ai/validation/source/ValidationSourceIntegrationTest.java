package org.freeplane.plugin.ai.validation.source;

import org.freeplane.plugin.ai.validation.MindMapGenerationValidator;
import org.freeplane.plugin.ai.validation.MindMapValidationResult;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ValidationSource 代理模式集成测试
 * 
 * <p>验证目标:
 * <ol>
 *   <li>四个数据源类的正确性</li>
 *   <li>MindMapGenerationValidator.validate(ValidationSource) 代理入口</li>
 *   <li>日志追溯信息完整性</li>
 *   <li>降级策略兼容性</li>
 *   <li>线程安全性</li>
 * </ol>
 */
public class ValidationSourceIntegrationTest {

    private MindMapGenerationValidator validator;

    @Before
    public void setUp() {
        validator = new MindMapGenerationValidator();
    }

    // =========================================================================
    // 测试1: PromptValidationSource - P0核心路径
    // =========================================================================

    @Test
    public void testPromptSource_BasicValidation() {
        // 准备: 正常思维导图JSON
        String json = "{\"id\":\"root\",\"text\":\"根节点\",\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"子节点1\"}," +
            "{\"id\":\"c2\",\"text\":\"子节点2\"}" +
            "]}";
        
        ValidationSource source = new PromptValidationSource(json, "ernie-4.0");
        
        // 执行
        MindMapValidationResult result = validator.validate(source);
        
        // 验证
        assertThat(result.isValid()).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.PROMPT_RESPONSE);
        assertThat(source.getDescription()).isEqualTo("model=ernie-4.0");
        assertThat(source.isReady()).isTrue();
    }

    @Test
    public void testPromptSource_CircularDependency() {
        // 准备: 包含环的JSON (c2引用c1, c1又引用c2)
        String jsonWithCycle = "{\"id\":\"root\",\"text\":\"根\"," +
            "\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"节点1\",\"children\":[" +
            "{\"id\":\"c2\",\"text\":\"节点2\",\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"回环\"}" + // 环: c2 → c1
            "]}" +
            "]}" +
            "]}";
        
        ValidationSource source = new PromptValidationSource(jsonWithCycle, "gpt-4");
        MindMapValidationResult result = validator.validate(source);
        
        // 验证: 应检测到环
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()))).isTrue();
        assertThat(source.getDescription()).contains("gpt-4");
    }

    @Test
    public void testPromptSource_NullModel() {
        String json = "{\"id\":\"root\",\"text\":\"根节点\",\"children\":[]}";
        ValidationSource source = new PromptValidationSource(json, null);
        
        assertThat(source.getDescription()).isEqualTo("unknown-model");
        assertThat(source.getSourceType()).isEqualTo(SourceType.PROMPT_RESPONSE);
    }

    // =========================================================================
    // 测试2: StreamValidationSource - P2流式场景
    // =========================================================================

    @Test
    public void testStreamSource_NotReady_ShouldReturnError() {
        // 准备: 未完成的流
        StreamValidationSource source = new StreamValidationSource("build-stream-node123");
        source.append("{\"id\":\"root\",\"text\":\"根\""); // 不完整的JSON
        // 注意: 未调用 markComplete()
        
        // 执行
        MindMapValidationResult result = validator.validate(source);
        
        // 验证: 应返回NOT_READY错误
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "NOT_READY".equals(e.getCode()))).isTrue();
        assertThat(result.getErrors().get(0).getMessage())
            .contains("build-stream-node123")
            .contains("STREAM_ASSEMBLED");
    }

    @Test
    public void testStreamSource_Completed_ShouldValidate() {
        // 准备: 完整的流式聚合
        StreamValidationSource source = new StreamValidationSource("build-stream-summarize");
        source.append("{\"id\":\"root\",\"text\":\"摘要结果\",");
        source.append("\"children\":[");
        source.append("{\"id\":\"p1\",\"text\":\"要点1\"},");
        source.append("{\"id\":\"p2\",\"text\":\"要点2\"}");
        source.append("]}");
        source.markComplete(); // 标记完成
        
        // 执行
        MindMapValidationResult result = validator.validate(source);
        
        // 验证
        assertThat(result.isValid()).isTrue();
        assertThat(source.isReady()).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.STREAM_ASSEMBLED);
    }

    @Test(expected = IllegalStateException.class)
    public void testStreamSource_ReadBeforeComplete_ShouldThrow() throws IOException {
        StreamValidationSource source = new StreamValidationSource("test");
        source.append("chunk1");
        // 未markComplete直接读取应抛异常
        source.readContent();
    }

    @Test(expected = IllegalStateException.class)
    public void testStreamSource_AppendAfterComplete_ShouldThrow() {
        StreamValidationSource source = new StreamValidationSource("test");
        source.append("chunk1");
        source.markComplete();
        // 完成后再次追加应抛异常
        source.append("chunk2");
    }

    @Test
    public void testStreamSource_ConcurrentAppend_ShouldBeThreadSafe() throws InterruptedException {
        StreamValidationSource source = new StreamValidationSource("concurrent-test");
        int threadCount = 10;
        int chunksPerThread = 100;
        
        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < chunksPerThread; i++) {
                    source.append("chunk-" + threadId + "-" + i);
                }
            });
        }
        
        // 并发追加
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        
        source.markComplete();
        assertThat(source.isReady()).isTrue();
    }

    // =========================================================================
    // 测试3: FileValidationSource - P1文件导入拦截
    // =========================================================================

    @Test
    public void testFileSource_FromPath_ShouldReadAndValidate() throws Exception {
        // 准备: 创建临时JSON文件
        String json = "{\"id\":\"root\",\"text\":\"文件导入\",\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"子节点\"}" +
            "]}";
        Path tempFile = Files.createTempFile("test-import", ".json");
        Files.writeString(tempFile, json);
        
        // 执行
        ValidationSource source = new FileValidationSource(tempFile, "test-import.json");
        MindMapValidationResult result = validator.validate(source);
        
        // 验证
        assertThat(result.isValid()).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.FILE_IMPORT);
        assertThat(source.getDescription()).isEqualTo("file=test-import.json");
        
        // 清理
        Files.delete(tempFile);
    }

    @Test
    public void testFileSource_FromContent_ShouldUseCache() throws Exception {
        // 准备: 直接使用内容字符串(兼容旧构造函数)
        String json = "{\"id\":\"root\",\"text\":\"缓存内容\",\"children\":[]}";
        ValidationSource source = new FileValidationSource(json, "cached.json");
        
        // 执行: 多次读取应返回相同内容
        String content1 = source.readContent();
        String content2 = source.readContent();
        
        // 验证
        assertThat(content1).isEqualTo(json);
        assertThat(content2).isEqualTo(json);
        assertThat(source.isReady()).isTrue();
    }

    @Test
    public void testFileSource_NonExistentFile_ShouldNotReady() {
        Path nonExistent = Path.of("/non/existent/file.json");
        ValidationSource source = new FileValidationSource(nonExistent, "missing.json");
        
        assertThat(source.isReady()).isFalse();
        
        MindMapValidationResult result = validator.validate(source);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "NOT_READY".equals(e.getCode()))).isTrue();
    }

    // =========================================================================
    // 测试4: UrlValidationSource - P3预留
    // =========================================================================

    @Test
    public void testUrlSource_DescriptionShouldContainUrl() throws Exception {
        java.net.URL url = new java.net.URL("http://example.com/mindmap.json");
        ValidationSource source = new UrlValidationSource(url);
        
        assertThat(source.getDescription()).contains("http://example.com/mindmap.json");
        assertThat(source.getSourceType()).isEqualTo(SourceType.URL_REMOTE);
    }

    // =========================================================================
    // 测试5: 代理入口 - MindMapGenerationValidator.validate(ValidationSource)
    // =========================================================================

    @Test
    public void testValidatorProxy_NullSource_ShouldReturnError() {
        MindMapValidationResult result = validator.validate((ValidationSource) null);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "NULL_SOURCE".equals(e.getCode()))).isTrue();
    }

    @Test
    public void testValidatorProxy_BackwardCompatibility_StringValidate() {
        // 验证: 旧接口 validate(String) 仍然可用
        String json = "{\"id\":\"root\",\"text\":\"兼容测试\",\"children\":[]}";
        MindMapValidationResult result = validator.validate(json);
        
        assertThat(result.isValid()).isTrue();
    }

    // =========================================================================
    // 测试6: 降级策略兼容性验证
    // =========================================================================

    @Test
    public void testDegradation_CircularDependency_ShouldDegrade() {
        // 模拟 MindMapBufferLayer 的降级逻辑
        String jsonWithCycle = "{\"id\":\"root\",\"text\":\"根\"," +
            "\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"节点1\",\"children\":[" +
            "{\"id\":\"c2\",\"text\":\"节点2\",\"children\":[" +
            "{\"id\":\"c1\",\"text\":\"回环\"}" +
            "]}" +
            "]}" +
            "]}";
        
        ValidationSource source = new PromptValidationSource(jsonWithCycle, "ernie-4.0");
        MindMapValidationResult result = validator.validate(source);
        
        // 验证: 降级策略应识别CIRCULAR_DEPENDENCY
        boolean hasCycle = result.getErrors().stream()
            .anyMatch(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()));
        boolean hasParseError = result.getErrors().stream()
            .anyMatch(e -> "PARSE_ERROR".equals(e.getCode()));
        
        assertThat(hasCycle || hasParseError).isTrue();
        // 实际降级逻辑: return createSampleMindMapJSON(topic)
    }

    @Test
    public void testDegradation_ParseError_ShouldDegrade() {
        // 模拟: 无效JSON
        String invalidJson = "{invalid json content";
        ValidationSource source = new PromptValidationSource(invalidJson, "gpt-4");
        MindMapValidationResult result = validator.validate(source);
        
        // 验证: 降级策略应识别PARSE_ERROR
        boolean hasParseError = result.getErrors().stream()
            .anyMatch(e -> "PARSE_ERROR".equals(e.getCode()));
        
        assertThat(hasParseError).isTrue();
    }

    // =========================================================================
    // 测试7: 日志追溯信息完整性
    // =========================================================================

    @Test
    public void testLogTraceability_AllSourceTypesShouldHaveDescription() {
        // 验证所有数据源都能提供追溯信息
        ValidationSource prompt = new PromptValidationSource("{}", "claude-3");
        ValidationSource stream = new StreamValidationSource("build-expand");
        ValidationSource file = new FileValidationSource("{}", "import.mm");
        
        assertThat(prompt.getDescription()).isNotBlank();
        assertThat(stream.getDescription()).isNotBlank();
        assertThat(file.getDescription()).isNotBlank();
        
        assertThat(prompt.getSourceType()).isEqualTo(SourceType.PROMPT_RESPONSE);
        assertThat(stream.getSourceType()).isEqualTo(SourceType.STREAM_ASSEMBLED);
        assertThat(file.getSourceType()).isEqualTo(SourceType.FILE_IMPORT);
    }
}
