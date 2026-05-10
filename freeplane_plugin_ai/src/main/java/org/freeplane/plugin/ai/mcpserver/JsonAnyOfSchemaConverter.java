package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonAnyOfSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class JsonAnyOfSchemaConverter implements JsonSchemaConverter<JsonAnyOfSchema> {

    @Override
    public boolean supports(JsonSchemaElement element) {
        return element instanceof JsonAnyOfSchema;
    }

    @Override
    public Map<String, Object> convert(JsonAnyOfSchema schema,
                                       Function<JsonSchemaElement, Map<String, Object>> recurse) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (schema.description() != null) {
            map.put("description", schema.description());
        }
        List<Map<String, Object>> anyOf = new ArrayList<>(schema.anyOf().size());
        for (JsonSchemaElement element : schema.anyOf()) {
            anyOf.add(recurse.apply(element));
        }
        map.put("anyOf", anyOf);
        return map;
    }
}
