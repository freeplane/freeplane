package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON Schema 转换器工厂（工厂方法模式的聚合入口）。
 *
 * <p>持有所有 {@link JsonSchemaConverter} 实现，按注册顺序匹配 {@code supports}，
 * 找到首个支持的转换器执行转换。新增 Schema 类型只需添加新的 Converter 并注册，
 * 无需修改此工厂或其他转换器（开闭原则）。
 */
class JsonSchemaConverterFactory {

    private final List<JsonSchemaConverter<? extends JsonSchemaElement>> converters;

    JsonSchemaConverterFactory(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        List<JsonSchemaConverter<? extends JsonSchemaElement>> list = new ArrayList<>();
        // 注册顺序：复合类型优先（避免被原始类型误匹配），原始类型次之
        list.add(new JsonObjectSchemaConverter());
        list.add(new JsonArraySchemaConverter());
        list.add(new JsonAnyOfSchemaConverter());
        list.add(new JsonReferenceSchemaConverter());
        list.add(new JsonPrimitiveSchemaConverter());
        list.add(new JsonRawSchemaConverter(objectMapper));
        this.converters = Collections.unmodifiableList(list);
    }

    /**
     * 将 JsonSchemaElement 递归转换为 Map 表示。
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
