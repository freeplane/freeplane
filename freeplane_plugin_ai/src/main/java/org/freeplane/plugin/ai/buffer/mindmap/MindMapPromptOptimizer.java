package org.freeplane.plugin.ai.buffer.mindmap;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.buffer.BufferRequest;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * 思维导图提示词优化器。
 * 维护领域 Prompt 模板库，根据需求选择模板并填充参数。
 * 支持 YAML 格式（多行文本原生支持）。
 */
public class MindMapPromptOptimizer {

    private Map<String, Object> promptTemplates;

    public MindMapPromptOptimizer() {
        loadTemplates();
    }

    /**
     * 加载 Prompt 模板（YAML 格式）
     */
    private void loadTemplates() {
        try (InputStream is = getClass().getResourceAsStream("/org/freeplane/plugin/ai/buffer/prompts.yaml")) {
            if (is != null) {
                Yaml yaml = new Yaml();
                promptTemplates = yaml.load(is);
                LogUtils.info("MindMapPromptOptimizer: loaded YAML prompt templates");
            } else {
                LogUtils.warn("MindMapPromptOptimizer: prompts.yaml not found");
            }
        } catch (Exception e) {
            LogUtils.warn("MindMapPromptOptimizer: failed to load templates", e);
        }
    }

    /**
     * 优化提示词
     * @param request 请求对象
     * @return 优化后的提示词
     */
    public String optimizePrompt(BufferRequest request) {
        String templateKey = selectTemplateKey(request);
        String template = getTemplate(templateKey);

        if (template == null) {
            LogUtils.warn("MindMapPromptOptimizer: template not found - " + templateKey);
            template = getDefaultTemplate(request);
        }

        // 填充参数
        String optimizedPrompt = fillTemplate(template, request);
        LogUtils.info("MindMapPromptOptimizer: optimized prompt length - " + optimizedPrompt.length() + " chars");

        return optimizedPrompt;
    }

    /**
     * 从嵌套 Map 中获取模板值
     */
    @SuppressWarnings("unchecked")
    private String getTemplate(String key) {
        if (promptTemplates == null) return null;
        
        String[] parts = key.split("\\.");
        Map<String, Object> current = promptTemplates;
        
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return null;
            }
        }
        
        Object value = current.get(parts[parts.length - 1]);
        return value instanceof String ? (String) value : null;
    }

    /**
     * 选择合适的模板键
     */
    private String selectTemplateKey(BufferRequest request) {
        String language = request.getParameter("language", "zh");
        BufferRequest.RequestType requestType = request.getRequestType();

        switch (requestType) {
            case MINDMAP_GENERATION:
                return "mindmap.generation." + language;
            case NODE_EXPANSION:
                return "mindmap.expansion." + language;
            case BRANCH_SUMMARY:
                return "mindmap.summary." + language;
            default:
                return "mindmap.generation." + language;
        }
    }

    /**
     * 获取默认模板
     */
    private String getDefaultTemplate(BufferRequest request) {
        String topic = request.getParameter("topic", "未知主题");
        int maxDepth = request.getParameter("maxDepth", 3);
        String language = request.getParameter("language", "zh");

        if ("zh".equals(language)) {
            return String.format(
                "请为'%s'生成一个完整的思维导图结构。\n" +
                "要求：\n" +
                "1. 包含 %d 层级的节点\n" +
                "2. 返回严格的 JSON 格式\n" +
                "3. 使用中文\n\n" +
                "返回格式：\n" +
                "{\n" +
                "  \"text\": \"%s\",\n" +
                "  \"children\": []\n" +
                "}\n\n" +
                "请只返回 JSON。",
                topic, maxDepth, topic
            );
        } else {
            return String.format(
                "Generate a complete mind map structure for '%s'.\n" +
                "Requirements:\n" +
                "1. Include %d levels of nodes\n" +
                "2. Return strict JSON format\n" +
                "3. Use English\n\n" +
                "Return format:\n" +
                "{\n" +
                "  \"text\": \"%s\",\n" +
                "  \"children\": []\n" +
                "}\n\n" +
                "Return JSON only.",
                topic, maxDepth, topic
            );
        }
    }

    /**
     * 填充模板参数
     */
    private String fillTemplate(String template, BufferRequest request) {
        String result = template;

        // 替换常见参数
        result = result.replace("{topic}", request.getParameter("topic", ""));
        result = result.replace("{maxDepth}", String.valueOf(request.getParameter("maxDepth", 3)));
        result = result.replace("{language}", request.getParameter("language", "zh"));
        result = result.replace("{nodeText}", request.getParameter("nodeText", ""));
        result = result.replace("{count}", String.valueOf(request.getParameter("count", 5)));
        result = result.replace("{depth}", String.valueOf(request.getParameter("depth", 2)));
        result = result.replace("{focus}", request.getParameter("focus", ""));
        result = result.replace("{content}", request.getParameter("content", ""));
        result = result.replace("{maxWords}", String.valueOf(request.getParameter("maxWords", 100)));

        return result;
    }
}