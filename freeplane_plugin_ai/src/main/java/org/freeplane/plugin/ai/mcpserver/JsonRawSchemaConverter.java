package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

class JsonRawSchemaConverter implements JsonSchemaConverter<JsonRawSchema> {

    private final ObjectMapper objectMapper;

    JsonRawSchemaConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(JsonSchemaElement element) {
        return element instanceof JsonRawSchema;
    }

    @Override
    public Map<String, Object> convert(JsonRawSchema schema,
                                       @SuppressWarnings("unused") Function<JsonSchemaElement, Map<String, Object>> recurse) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(schema.schema(), Map.class);
            return map;
        } catch (IOException error) {
            throw new IllegalArgumentException("Invalid raw schema JSON.", error);
        }
    }
}
