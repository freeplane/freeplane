package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

import java.util.Map;
import java.util.function.Function;

/**
 * JSON Schema 元素到 Map 的转换器接口（工厂方法模式）。
 *
 * <p>每种 JsonSchemaElement 子类型对应一个具体实现，
 * 通过 {@link JsonSchemaConverterFactory} 统一调度，消除 instanceof 链。
 *
 * @param <T> 支持的 JsonSchemaElement 子类型
 */
interface JsonSchemaConverter<T extends JsonSchemaElement> {

    /**
     * 判断此转换器是否支持给定的 schema 元素。
     */
    boolean supports(JsonSchemaElement element);

    /**
     * 将 schema 元素转换为 Map 表示。
     *
     * @param element  待转换的 schema 元素
     * @param recurse  递归转换器，用于处理嵌套属性
     * @return 等价于 JSON Schema 的 Map 结构
     */
    Map<String, Object> convert(T element, Function<JsonSchemaElement, Map<String, Object>> recurse);
}
