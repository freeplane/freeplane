package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for JSON Schema converters (aggregate entry point of the Factory Method pattern).
 *
 * <p>Holds all {@link JsonSchemaConverter} implementations and matches them in registration
 * order via {@code supports}. To add a new Schema type, simply add a new Converter and
 * register it here — no changes to this factory or other converters are needed (Open/Closed Principle).
 */
class JsonSchemaConverterFactory {

    private final List<JsonSchemaConverter<? extends JsonSchemaElement>> converters;

    JsonSchemaConverterFactory(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        List<JsonSchemaConverter<? extends JsonSchemaElement>> list = new ArrayList<>();
        // Registration order: composite types first (to avoid mismatching with primitive converters), then primitives
        list.add(new JsonObjectSchemaConverter());
        list.add(new JsonArraySchemaConverter());
        list.add(new JsonAnyOfSchemaConverter());
        list.add(new JsonReferenceSchemaConverter());
        list.add(new JsonPrimitiveSchemaConverter());
        list.add(new JsonRawSchemaConverter(objectMapper));
        this.converters = Collections.unmodifiableList(list);
    }

    /**
     * Recursively converts a {@link JsonSchemaElement} to its {@code Map} representation.
     */
    Map<String, Object> convert(JsonSchemaElement element) {
        Objects.requireNonNull(element, "element");
        for (JsonSchemaConverter<? extends JsonSchemaElement> converter : converters) {
            if (converter.supports(element)) {
                @SuppressWarnings("unchecked")
                JsonSchemaConverter<JsonSchemaElement> typed = (JsonSchemaConverter<JsonSchemaElement>) converter;
                return typed.convert(element, this::convert);
            }
        }
        throw new IllegalArgumentException("Unsupported schema element: " + element.getClass());
    }
}
