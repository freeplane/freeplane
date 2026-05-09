package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证工厂方法模式重构后的 Schema 转换行为等价性。
 */
public class ModelContextProtocolToolRegistryTest {

    private static final class SampleToolSet {
        @Tool("Echo tool with string parameter.")
        public String echo(String message) {
            return message;
        }

        @Tool("Calculate with multiple parameters.")
        public int calculate(int a, int b, boolean verbose) {
            return verbose ? a + b : a * b;
        }
    }

    @Test
    public void buildToolList_convertsSchemaCorrectly() {
        SampleToolSet toolSet = new SampleToolSet();
        ObjectMapper objectMapper = new ObjectMapper();
        ModelContextProtocolToolRegistry registry = new ModelContextProtocolToolRegistry(toolSet, objectMapper);

        List<ModelContextProtocolTool> tools = registry.listTools();
        assertThat(tools).hasSize(2);

        // 验证 echo 工具的 schema 包含 properties 和 required
        ModelContextProtocolTool echoTool = tools.stream()
            .filter(t -> t.getName().equals("echo"))
            .findFirst()
            .orElseThrow();

        assertThat(echoTool.getDescription()).isEqualTo("Echo tool with string parameter.");
        @SuppressWarnings("unchecked")
        Map<String, Object> echoSchema = echoTool.getInputSchema();
        assertThat(echoSchema).containsKey("type");
        assertThat(echoSchema.get("type")).isEqualTo("object");
        assertThat(echoSchema).containsKey("properties");

        @SuppressWarnings("unchecked")
        Map<String, Object> echoProperties = (Map<String, Object>) echoSchema.get("properties");
        assertThat(echoProperties).containsKey("message");

        @SuppressWarnings("unchecked")
        Map<String, Object> messageSchema = (Map<String, Object>) echoProperties.get("message");
        assertThat(messageSchema.get("type")).isEqualTo("string");

        // 验证 calculate 工具的 schema
        ModelContextProtocolTool calcTool = tools.stream()
            .filter(t -> t.getName().equals("calculate"))
            .findFirst()
            .orElseThrow();

        @SuppressWarnings("unchecked")
        Map<String, Object> calcProperties = (Map<String, Object>) calcTool.getInputSchema().get("properties");
        assertThat(calcProperties).containsKey("a");
        assertThat(calcProperties).containsKey("b");
        assertThat(calcProperties).containsKey("verbose");

        @SuppressWarnings("unchecked")
        Map<String, Object> aSchema = (Map<String, Object>) calcProperties.get("a");
        assertThat(aSchema.get("type")).isEqualTo("integer");

        @SuppressWarnings("unchecked")
        Map<String, Object> verboseSchema = (Map<String, Object>) calcProperties.get("verbose");
        assertThat(verboseSchema.get("type")).isEqualTo("boolean");
    }

    @Test
    public void cache_invalidationAndRebuild() {
        SampleToolSet toolSet = new SampleToolSet();
        ObjectMapper objectMapper = new ObjectMapper();
        ModelContextProtocolToolRegistry registry = new ModelContextProtocolToolRegistry(toolSet, objectMapper);

        // 首次调用触发构建
        List<ModelContextProtocolTool> first = registry.listTools();
        assertThat(first).hasSize(2);

        // 缓存命中：返回列表引用相同
        List<ModelContextProtocolTool> second = registry.listTools();
        assertThat(second).isSameAs(first);

        // 失效后重新构建
        registry.invalidateCache();
        List<ModelContextProtocolTool> third = registry.listTools();
        assertThat(third).hasSize(2);
        assertThat(third).isNotSameAs(first);
    }
}
