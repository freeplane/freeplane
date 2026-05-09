package org.freeplane.plugin.ai.mcpserver;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeplane.core.util.LogUtils;

public class ModelContextProtocolToolRegistry {
    private static final Map<String, Object> EMPTY_SCHEMA = createEmptySchema();

    private final Object toolSet;
    private final JsonSchemaConverterFactory schemaConverterFactory;

    /**
     * 方案B：Schema懒加载缓存。
     * volatile 保证多线程可见性，防止指令重排序导致拿到半初始化对象。
     */
    private volatile List<ModelContextProtocolTool> cachedTools = null;

    public ModelContextProtocolToolRegistry(Object toolSet, ObjectMapper objectMapper) {
        this.toolSet = Objects.requireNonNull(toolSet, "toolSet");
        this.schemaConverterFactory = new JsonSchemaConverterFactory(
            Objects.requireNonNull(objectMapper, "objectMapper"));
    }

    /**
     * 列出所有已注册工具的 Schema 列表。
     * 采用双重检查锁（Double-Checked Locking）实现懒加载：
     *   - 冷路径（首次调用）：触发反射扫描 + 递归展开 Schema，结果写入 cachedTools
     *   - 热路径（后续调用）：第一重检查直接返回，O(1)且无锁竞争
     */
    public List<ModelContextProtocolTool> listTools() {
        // 第一重检查：不加锁，99%情况下缓存已就绪，直接返回
        if (cachedTools == null) {
            synchronized (this) {
                // 第二重检查：加锁后再判断一次，防止多线程同时通过第一重检查导致重复构建
                if (cachedTools == null) {
                    LogUtils.info("ModelContextProtocolToolRegistry: building tool schema cache");
                    cachedTools = buildToolList();
                    LogUtils.info("ModelContextProtocolToolRegistry: cached " + cachedTools.size() + " tool schemas");
                }
            }
        }
        return cachedTools;
    }

    /**
     * 使缓存失效。
     * 当动态工具注册发生变化时调用，下次 listTools() 将重新构建 Schema 列表。
     */
    public void invalidateCache() {
        synchronized (this) {
            cachedTools = null;
        }
        LogUtils.info("ModelContextProtocolToolRegistry: tool schema cache invalidated");
    }

    /**
     * 内部构建方法：执行反射扫描 + 递归 Schema 展开。
     * 只在缓存未命中或失效时被调用。
     */
    private List<ModelContextProtocolTool> buildToolList() {
        List<ToolSpecification> specifications = ToolSpecifications.toolSpecificationsFrom(toolSet);
        List<ModelContextProtocolTool> tools = new ArrayList<>(specifications.size());
        for (ToolSpecification specification : specifications) {
            Map<String, Object> inputSchema = EMPTY_SCHEMA;
            JsonObjectSchema parameters = specification.parameters();
            if (parameters != null) {
                inputSchema = schemaConverterFactory.convert(parameters);
            }
            tools.add(new ModelContextProtocolTool(
                specification.name(),
                specification.description(),
                inputSchema
            ));
        }
        return Collections.unmodifiableList(tools);
    }

    private static Map<String, Object> createEmptySchema() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "object");
        map.put("properties", Collections.emptyMap());
        map.put("required", Collections.emptyList());
        return Collections.unmodifiableMap(map);
    }
}
