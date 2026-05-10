package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonReferenceSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

class JsonReferenceSchemaConverter implements JsonSchemaConverter<JsonReferenceSchema> {

    @Override
    public boolean supports(JsonSchemaElement element) {
        return element instanceof JsonReferenceSchema;
    }

    @Override
    public Map<String, Object> convert(JsonReferenceSchema schema,
                                       @SuppressWarnings("unused") Function<JsonSchemaElement, Map<String, Object>> recurse) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("$ref", "#/$defs/" + schema.reference());
        return map;
    }
}
