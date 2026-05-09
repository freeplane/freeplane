package org.freeplane.plugin.ai.buffer.mindmap;

import org.freeplane.plugin.ai.buffer.BufferRequest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * YAML 提示词模板加载测试。
 */
public class MindMapPromptOptimizerYamlTest {

    @Test
    public void loadYamlTemplates_shouldLoadSuccessfully() {
        // 当：创建优化器（触发 YAML 加载）
        MindMapPromptOptimizer optimizer = new MindMapPromptOptimizer();
        
        // 则：优化器应成功初始化
        assertNotNull("优化器不应为 null", optimizer);
    }

    @Test
    public void optimizePrompt_mindmapGenerationZh_shouldReturnCoTPrompt() {
        // 给定
        MindMapPromptOptimizer optimizer = new MindMapPromptOptimizer();
        BufferRequest request = new BufferRequest();
        request.setRequestType(BufferRequest.RequestType.MINDMAP_GENERATION);
        request.addParameter("topic", "Java 编程");
        request.addParameter("maxDepth", 3);
        request.addParameter("language", "zh");
        
        // 当：优化提示词
        String prompt = optimizer.optimizePrompt(request);
        
        // 则：应返回包含 CoT 思维链的中文提示词
        assertNotNull("提示词不应为 null", prompt);
        assertTrue("应包含角色定位", prompt.contains("# 角色定位"));
        assertTrue("应包含思考步骤", prompt.contains("## 思考步骤"));
        assertTrue("应包含主题分析步骤", prompt.contains("主题分析"));
        assertTrue("应包含质量要求", prompt.contains("## 质量要求"));
        assertTrue("应包含输出规则", prompt.contains("## 输出规则"));
        assertTrue("应填充 topic 参数", prompt.contains("Java 编程"));
        assertTrue("应填充 maxDepth 参数", prompt.contains("3"));
    }

    @Test
    public void optimizePrompt_mindmapGenerationEn_shouldReturnEnglishPrompt() {
        // 给定
        MindMapPromptOptimizer optimizer = new MindMapPromptOptimizer();
        BufferRequest request = new BufferRequest();
        request.setRequestType(BufferRequest.RequestType.MINDMAP_GENERATION);
        request.addParameter("topic", "Machine Learning");
        request.addParameter("maxDepth", 4);
        request.addParameter("language", "en");
        
        // 当：优化提示词
        String prompt = optimizer.optimizePrompt(request);
        
        // 则：应返回英文提示词
        assertNotNull("提示词不应为 null", prompt);
        assertTrue("应包含 Role", prompt.contains("# Role"));
        assertTrue("应包含 Chain of Thought", prompt.contains("## Chain of Thought"));
        assertTrue("应填充 topic 参数", prompt.contains("Machine Learning"));
        assertTrue("应填充 maxDepth 参数", prompt.contains("4"));
    }

    @Test
    public void optimizePrompt_nodeExpansionZh_shouldReturnExpansionPrompt() {
        // 给定
        MindMapPromptOptimizer optimizer = new MindMapPromptOptimizer();
        BufferRequest request = new BufferRequest();
        request.setRequestType(BufferRequest.RequestType.NODE_EXPANSION);
        request.addParameter("nodeText", "设计模式");
        request.addParameter("count", 5);
        request.addParameter("depth", 2);
        request.addParameter("focus", "创建型模式");
        request.addParameter("language", "zh");
        
        // 当：优化提示词
        String prompt = optimizer.optimizePrompt(request);
        
        // 则：应包含展开相关参数
        assertNotNull("提示词不应为 null", prompt);
        assertTrue("应包含父节点文本", prompt.contains("设计模式"));
        assertTrue("应包含 count 参数", prompt.contains("5"));
        assertTrue("应包含 depth 参数", prompt.contains("2"));
        assertTrue("应包含 focus 参数", prompt.contains("创建型模式"));
    }

    @Test
    public void optimizePrompt_branchSummaryZh_shouldReturnSummaryPrompt() {
        // 给定
        MindMapPromptOptimizer optimizer = new MindMapPromptOptimizer();
        BufferRequest request = new BufferRequest();
        request.setRequestType(BufferRequest.RequestType.BRANCH_SUMMARY);
        request.addParameter("content", "这是一段需要摘要的内容");
        request.addParameter("maxWords", 50);
        request.addParameter("language", "zh");
        
        // 当：优化提示词
        String prompt = optimizer.optimizePrompt(request);
        
        // 则：应包含摘要相关内容
        assertNotNull("提示词不应为 null", prompt);
        assertTrue("应包含待摘要内容", prompt.contains("这是一段需要摘要的内容"));
        assertTrue("应包含 maxWords 参数", prompt.contains("50"));
    }

    @Test
    public void optimizePrompt_templateNotFound_shouldReturnDefaultTemplate() {
        // 给定
        MindMapPromptOptimizer optimizer = new MindMapPromptOptimizer();
        BufferRequest request = new BufferRequest();
        request.setRequestType(BufferRequest.RequestType.MINDMAP_GENERATION);
        request.addParameter("topic", "测试主题");
        request.addParameter("language", "fr"); // 不支持的语言
        
        // 当：优化提示词
        String prompt = optimizer.optimizePrompt(request);
        
        // 则：应返回默认模板
        assertNotNull("提示词不应为 null", prompt);
        assertTrue("应包含测试主题", prompt.contains("测试主题"));
    }
}
