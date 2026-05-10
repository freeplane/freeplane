package org.freeplane.plugin.ai.tools.utilities;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.mcpserver.ModelContextProtocolTool;
import org.freeplane.plugin.ai.mcpserver.ToolSchemaIndex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pre-execution argument validation observer.
 *
 * <p>Uses the {@link ToolSchemaIndex} to perform JSON-Schema-level validation of tool arguments
 * before execution reaches the EDT (Event Dispatch Thread), preventing complex rollback logic.
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
            return; // tool not in index (schema cache may be disabled), skip validation
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
     * Basic validation: checks for missing required fields declared in the JSON Schema.
     * Full JSON Schema validation can be added later via a library like networknt/json-schema-validator.
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
