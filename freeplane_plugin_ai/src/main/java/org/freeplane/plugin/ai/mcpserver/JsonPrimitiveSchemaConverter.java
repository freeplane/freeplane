package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNullSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Primitive type converters (String / Integer / Number / Boolean / Enum / Null),
 * merged into a single class to reduce file count. These types are structurally simple
 * and require no recursive nesting.
 */
class JsonPrimitiveSchemaConverter
        implements JsonSchemaConverter<JsonSchemaElement> {

    @Override
    public boolean supports(JsonSchemaElement element) {
        return element instanceof JsonStringSchema
            || element instanceof JsonIntegerSchema
            || element instanceof JsonNumberSchema
            || element instanceof JsonBooleanSchema
            || element instanceof JsonEnumSchema
            || element instanceof JsonNullSchema;
    }

    @Override
    public Map<String, Object> convert(JsonSchemaElement element,
                                       @SuppressWarnings("unused") Function<JsonSchemaElement, Map<String, Object>> recurse) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (element instanceof JsonStringSchema s) {
            map.put("type", "string");
            if (s.description() != null) map.put("description", s.description());
        } else if (element instanceof JsonIntegerSchema s) {
            map.put("type", "integer");
            if (s.description() != null) map.put("description", s.description());
        } else if (element instanceof JsonNumberSchema s) {
            map.put("type", "number");
            if (s.description() != null) map.put("description", s.description());
        } else if (element instanceof JsonBooleanSchema s) {
            map.put("type", "boolean");
            if (s.description() != null) map.put("description", s.description());
        } else if (element instanceof JsonEnumSchema s) {
            map.put("type", "string");
            map.put("enum", s.enumValues());
            if (s.description() != null) map.put("description", s.description());
        } else if (element instanceof JsonNullSchema) {
            map.put("type", "null");
        }
        return map;
    }
}
