package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.Map;
import java.util.function.Function;

/**
 * Converter interface from JSON Schema elements to {@code Map} (Factory Method pattern).
 *
 * <p>Each {@link JsonSchemaElement} subtype has a corresponding concrete implementation.
 * {@link JsonSchemaConverterFactory} dispatches to the correct one, eliminating {@code instanceof} chains.
 *
 * @param <T> the supported {@link JsonSchemaElement} subtype
 */
interface JsonSchemaConverter<T extends JsonSchemaElement> {

    /** Returns {@code true} if this converter supports the given schema element. */
    boolean supports(JsonSchemaElement element);

    /**
     * Converts the schema element to its {@code Map} representation.
     *
     * @param element  the schema element to convert
     * @param recurse  recursive converter for handling nested properties
     * @return a {@code Map} structure equivalent to the JSON Schema
     */
    Map<String, Object> convert(T element, Function<JsonSchemaElement, Map<String, Object>> recurse);
}
