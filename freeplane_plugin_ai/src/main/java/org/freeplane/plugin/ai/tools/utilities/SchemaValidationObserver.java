package org.freeplane.plugin.ai.tools.utilities;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.mcpserver.ModelContextProtocolTool;
import org.freeplane.plugin.ai.mcpserver.ToolSchemaIndex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 参数预校验观察者。
 *
 * <p>在工具执行前利用 ToolSchemaIndex 中的 Schema 对参数进行 JSON Schema 级别的验证，
 * 在进入 EDT（事件分发线程）前拦截非法参数，避免复杂的回滚逻辑。
 */
public class SchemaValidationObserver implements ToolExecutionObserver {

    private final ToolSchemaIndex schemaIndex;
    private final ObjectMapper objectMapper;

    public SchemaValidationObserver(ToolSchemaIndex schemaIndex, ObjectMapper objectMapper) {
        this.schemaIndex = schemaIndex;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onBefore(ToolExecutionBeforeEvent event) {
        if (schemaIndex == null) {
            return;
        }
        ModelContextProtocolTool tool = schemaIndex.get(event.toolName());
        if (tool == null) {
            return; // 工具不在索引中（可能未启用 Schema 缓存），跳过
        }

        String rawArgs = event.rawArguments();
        if (rawArgs == null || rawArgs.trim().isEmpty() || "{}".equals(rawArgs.trim())) {
            return;
        }

        try {
            JsonNode argsNode = objectMapper.readTree(rawArgs);
            validateRequiredFields(tool, argsNode);
        } catch (Exception error) {
            LogUtils.info("SchemaValidationObserver: invalid arguments for tool '"
                + event.toolName() + "': " + error.getMessage());
            throw new IllegalArgumentException(
                "Invalid arguments for tool '" + event.toolName() + "': " + error.getMessage(), error);
        }
    }

    /**
     * 基础验证：检查 JSON Schema 中 required 字段的缺失。
     * 完整 JSON Schema 验证可后续引入 networknt/json-schema-validator 等库。
     */
    @SuppressWarnings("unchecked")
    private void validateRequiredFields(ModelContextProtocolTool tool, JsonNode argsNode) {
        Object inputSchema = tool.getInputSchema();
        if (!(inputSchema instanceof java.util.Map)) {
            return;
        }
        java.util.Map<String, Object> schemaMap = (java.util.Map<String, Object>) inputSchema;
        Object requiredObj = schemaMap.get("required");
        if (!(requiredObj instanceof java.util.List)) {
            return;
        }
        for (Object fieldName : (java.util.List<?>) requiredObj) {
            String field = String.valueOf(fieldName);
            if (!argsNode.has(field) || argsNode.get(field).isNull()) {
                throw new IllegalArgumentException(
                    "Missing required field '" + field + "' for tool '" + tool.getName() + "'");
            }
        }
    }
}
