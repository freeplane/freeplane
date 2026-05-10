package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

class JsonObjectSchemaConverter implements JsonSchemaConverter<JsonObjectSchema> {

    @Override
    public boolean supports(JsonSchemaElement element) {
        return element instanceof JsonObjectSchema;
    }

    @Override
    public Map<String, Object> convert(JsonObjectSchema schema,
                                       Function<JsonSchemaElement, Map<String, Object>> recurse) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "object");
        if (schema.description() != null) {
            map.put("description", schema.description());
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        for (Map.Entry<String, JsonSchemaElement> entry : schema.properties().entrySet()) {
            properties.put(entry.getKey(), recurse.apply(entry.getValue()));
        }
        map.put("properties", properties);
        if (schema.required() != null) {
            map.put("required", schema.required());
        }
        if (schema.additionalProperties() != null) {
            map.put("additionalProperties", schema.additionalProperties());
        }
        Map<String, JsonSchemaElement> definitions = schema.definitions();
        if (definitions != null && !definitions.isEmpty()) {
            Map<String, Object> defs = new LinkedHashMap<>();
            for (Map.Entry<String, JsonSchemaElement> entry : definitions.entrySet()) {
                defs.put(entry.getKey(), recurse.apply(entry.getValue()));
            }
            map.put("$defs", defs);
        }
        return map;
    }
}
