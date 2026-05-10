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
     * Approach B: lazy-loaded Schema cache.
     * {@code volatile} ensures multi-thread visibility and prevents instruction reordering
     * that could expose a half-initialised object.
     */
    private volatile List<ModelContextProtocolTool> cachedTools = null;

    public ModelContextProtocolToolRegistry(Object toolSet, ObjectMapper objectMapper) {
        this.toolSet = Objects.requireNonNull(toolSet, "toolSet");
        this.schemaConverterFactory = new JsonSchemaConverterFactory(
            Objects.requireNonNull(objectMapper, "objectMapper"));
    }

    /**
     * Returns the list of Schema descriptors for all registered tools.
     * Uses Double-Checked Locking for lazy initialisation:
     *   - Cold path (first call): triggers reflection scan + recursive Schema expansion; result written to cachedTools.
     *   - Hot path (subsequent calls): first check returns immediately, O(1) with no lock contention.
     */
    public List<ModelContextProtocolTool> listTools() {
        // First check: no lock; in 99 % of calls the cache is already ready.
        if (cachedTools == null) {
            synchronized (this) {
                // Second check: re-test under lock to prevent duplicate builds
                // when multiple threads pass the first check simultaneously.
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
     * Invalidates the cache.
     * Call this when the set of dynamically registered tools changes;
     * the next call to {@link #listTools()} will rebuild the Schema list.
     */
    public void invalidateCache() {
        synchronized (this) {
            cachedTools = null;
        }
        LogUtils.info("ModelContextProtocolToolRegistry: tool schema cache invalidated");
    }

    /**
     * Internal build method: performs the reflection scan and recursive Schema expansion.
     * Only called on a cache miss or after invalidation.
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
