package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

class JsonArraySchemaConverter implements JsonSchemaConverter<JsonArraySchema> {

    @Override
    public boolean supports(JsonSchemaElement element) {
        return element instanceof JsonArraySchema;
    }

    @Override
    public Map<String, Object> convert(JsonArraySchema schema,
                                       Function<JsonSchemaElement, Map<String, Object>> recurse) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "array");
        if (schema.description() != null) {
            map.put("description", schema.description());
        }
        JsonSchemaElement items = schema.items();
        if (items != null) {
            map.put("items", recurse.apply(items));
        }
        return map;
    }
}
