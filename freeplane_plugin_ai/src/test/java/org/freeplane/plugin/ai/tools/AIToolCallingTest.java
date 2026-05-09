package org.freeplane.plugin.ai.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.freeplane.plugin.ai.tools.create.CreateNodesRequest;
import org.freeplane.plugin.ai.tools.create.CreateNodesResponse;
import org.freeplane.plugin.ai.tools.move.CreateSummaryRequest;
import org.freeplane.plugin.ai.tools.move.CreateSummaryResponse;
import org.freeplane.plugin.ai.tools.read.ReadNodesWithDescendantsRequest;
import org.freeplane.plugin.ai.tools.read.ReadNodesWithDescendantsResponse;
import org.junit.Test;

import dev.langchain4j.agent.tool.Tool;

/**
 * AI插件工具调用能力验证测试
 * 重点验证：AI能否识别和调用正确的工具
 * 
 * 测试覆盖：
 * 1. 工具注解完整性（@Tool）
 * 2. 工具描述质量
 * 3. 工具方法签名正确性
 */
public class AIToolCallingTest {

    // ==================== 工具注解验证 ====================

    /**
     * TC01: 验证createNodes工具有@Tool注解且描述完整
     * 用途：AI生成思维导图、展开节点
     */
    @Test
    public void toolAnnotation_createNodes_shouldBeComplete() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createNodes", CreateNodesRequest.class);
        
        // 验证有@Tool注解
        assertThat(method.isAnnotationPresent(Tool.class))
            .withFailMessage("createNodes方法必须有@Tool注解，AI才能调用")
            .isTrue();
        
        Tool toolAnnotation = method.getAnnotation(Tool.class);
        String description = toolAnnotation.value()[0];
        
        // 验证描述包含关键信息
        assertThat(description)
            .as("工具描述应包含'create'关键词")
            .contains("Create");
        
        assertThat(description)
            .as("工具描述应说明相对anchor节点操作")
            .contains("anchor");
        
        // 打印描述供审查
        System.out.println("=== createNodes工具描述 ===");
        System.out.println(description);
        System.out.println("===========================");
    }

    /**
     * TC02: 验证createSummary工具有@Tool注解且描述完整
     * 用途：AI创建节点摘要
     */
    @Test
    public void toolAnnotation_createSummary_shouldBeComplete() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createSummary", CreateSummaryRequest.class);
        
        assertThat(method.isAnnotationPresent(Tool.class))
            .withFailMessage("createSummary方法必须有@Tool注解")
            .isTrue();
        
        Tool toolAnnotation = method.getAnnotation(Tool.class);
        String description = toolAnnotation.value()[0];
        
        assertThat(description)
            .as("工具描述应包含'summary'关键词")
            .contains("summary");
        
        System.out.println("=== createSummary工具描述 ===");
        System.out.println(description);
        System.out.println("=============================");
    }

    /**
     * TC03: 验证readNodesWithDescendants工具有@Tool注解
     * 用途：AI读取节点内容（必须先读取再操作）
     */
    @Test
    public void toolAnnotation_readNodes_shouldBeComplete() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("readNodesWithDescendants", ReadNodesWithDescendantsRequest.class);
        
        assertThat(method.isAnnotationPresent(Tool.class))
            .withFailMessage("readNodesWithDescendants方法必须有@Tool注解")
            .isTrue();
        
        Tool toolAnnotation = method.getAnnotation(Tool.class);
        String description = toolAnnotation.value()[0];
        
        assertThat(description)
            .as("工具描述应包含'read'关键词")
            .contains("Read");
        
        System.out.println("=== readNodesWithDescendants工具描述 ===");
        System.out.println(description);
        System.out.println("=======================================");
    }

    /**
     * TC04: 验证所有核心工具都有@Tool注解
     */
    @Test
    public void toolAnnotations_allCoreTools_shouldExist() {
        // 核心工具列表
        String[] coreTools = {
            "createNodes",           // 生成思维导图、展开节点
            "readNodesWithDescendants", // 读取节点
            "searchNodes",           // 搜索节点
            "deleteNodes",           // 删除节点
            "createSummary",         // 创建摘要
            "moveNodes",             // 移动节点
            "edit",                  // 编辑节点内容
            "selectSingleNode"       // 选择节点
        };
        
        System.out.println("\n=== 核心工具注解检查 ===");
        for (String toolName : coreTools) {
            Method method = findMethod(toolName);
            assertThat(method)
                .withFailMessage("工具方法 " + toolName + " 不存在")
                .isNotNull();
            
            boolean hasAnnotation = method.isAnnotationPresent(Tool.class);
            assertThat(hasAnnotation)
                .withFailMessage("工具方法 " + toolName + " 缺少@Tool注解，AI无法调用")
                .isTrue();
            
            Tool annotation = method.getAnnotation(Tool.class);
            String desc = annotation.value()[0];
            System.out.println("✓ " + toolName + ": " + desc.substring(0, Math.min(50, desc.length())) + "...");
        }
        System.out.println("=========================\n");
    }

    /**
     * TC05: 验证工具方法返回类型正确
     * AI需要结构化的返回值来判断操作是否成功
     */
    @Test
    public void toolReturnTypes_createNodes_shouldReturnResponse() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createNodes", CreateNodesRequest.class);
        
        // 验证返回CreateNodesResponse
        assertThat(method.getReturnType())
            .as("createNodes应返回CreateNodesResponse，AI需要结构化结果")
            .isEqualTo(CreateNodesResponse.class);
    }

    /**
     * TC06: 验证工具方法返回类型正确 - createSummary
     */
    @Test
    public void toolReturnTypes_createSummary_shouldReturnResponse() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createSummary", CreateSummaryRequest.class);
        
        assertThat(method.getReturnType())
            .as("createSummary应返回CreateSummaryResponse")
            .isEqualTo(CreateSummaryResponse.class);
    }

    /**
     * TC07: 验证工具方法返回类型正确 - readNodes
     */
    @Test
    public void toolReturnTypes_readNodes_shouldReturnResponse() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("readNodesWithDescendants", ReadNodesWithDescendantsRequest.class);
        
        assertThat(method.getReturnType())
            .as("readNodesWithDescendants应返回ReadNodesWithDescendantsResponse")
            .isEqualTo(ReadNodesWithDescendantsResponse.class);
    }

    // ==================== 工具描述质量验证 ====================

    /**
     * TC08: 验证工具描述包含使用场景说明
     * 好的描述能帮助AI正确选择工具
     */
    @Test
    public void toolDescription_createNodes_shouldIncludeUsageContext() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createNodes", CreateNodesRequest.class);
        Tool annotation = method.getAnnotation(Tool.class);
        String description = annotation.value()[0];
        
        // 验证描述包含使用说明
        assertThat(description)
            .as("工具描述应说明相对哪个节点操作")
            .contains("relative to");
        
        assertThat(description)
            .as("工具描述应提到anchor节点")
            .contains("anchor");
    }

    /**
     * TC09: 验证工具描述包含参数说明
     */
    @Test
    public void toolDescription_createNodes_shouldDocumentParameters() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createNodes", CreateNodesRequest.class);
        Tool annotation = method.getAnnotation(Tool.class);
        String description = annotation.value()[0];
        
        // 验证描述提到optional fields（参数说明）
        assertThat(description)
            .as("工具描述应说明可选字段")
            .contains("Optional");
        
        assertThat(description)
            .as("工具描述应说明如何省略字段")
            .contains("Omit");
    }

    /**
     * TC10: 验证createSummary描述包含使用提示
     */
    @Test
    public void toolDescription_createSummary_shouldIncludeTips() throws NoSuchMethodException {
        Method method = AIToolSet.class.getMethod("createSummary", CreateSummaryRequest.class);
        Tool annotation = method.getAnnotation(Tool.class);
        String description = annotation.value()[0];
        
        // 验证描述包含使用技巧
        assertThat(description)
            .as("工具描述应包含使用提示（Tip）")
            .contains("Tip");
    }

    // ==================== 辅助方法 ====================

    /**
     * 查找指定名称的方法
     */
    private Method findMethod(String methodName) {
        for (Method method : AIToolSet.class.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
