package org.freeplane.plugin.ai.mcpserver;

import java.util.List;

/**
 * Tool schema index interface modelled after B+-tree semantics.
 *
 * <p>Motivation: when the number of tools grows (100+) or semantic routing is needed,
 * sequential O(N) list traversal is no longer sufficient. This interface is modelled
 * on the core properties of a B+-tree:
 * <ul>
 *   <li><b>Ordered</b>: tool names stored in lexicographic order; range queries supported</li>
 *   <li><b>Prefix lookup</b>: batch-recall tools by namespace/prefix to reduce LLM token usage</li>
 *   <li><b>Range query</b>: retrieve a sub-set of tools within [fromName, toName]</li>
 *   <li><b>Persistence-ready</b>: interface semantics are compatible with on-disk B+-tree storage
 *       for future cross-session schema caching</li>
 * </ul>
 *
 * <p>Recommended implementation: {@link TreeMapToolSchemaIndex} (in-memory TreeMap).
 * Future implementations backed by an embedded B+-tree can replace it without changing callers.
 *
 * <h3>Typical usage</h3>
 * <pre>
 * // Case 1: LLM only needs node-operation tools, reducing prompt token cost
 * List&lt;ModelContextProtocolTool&gt; nodeTools = index.getByPrefix("node");
 *
 * // Case 2: retrieve tools in a lexicographic range
 * List&lt;ModelContextProtocolTool&gt; rangeTools = index.getRange("create", "delete");
 *
 * // Case 3: exact lookup
 * Optional&lt;ModelContextProtocolTool&gt; tool = index.get("createNodes");
 * </pre>
 */
public interface ToolSchemaIndex {

    /**
     * Exact lookup by tool name.
     *
     * @param toolName case-sensitive tool name
     * @return the matching tool, or {@code null} if not found
     */
    ModelContextProtocolTool get(String toolName);

    /**
     * Retrieves all tools whose name starts with the given prefix (B+-tree range-scan semantics).
     *
     * <p>E.g. prefix="node" recalls nodeCreate / nodeDelete / nodeRead etc.
     * Implementations should be equivalent to TreeMap.subMap(prefix, prefix + "\uFFFF"), O(k + log N).
     *
     * @param prefix tool name prefix, must not be null
     * @return unmodifiable list of matching tools in lexicographic order
     */
    List<ModelContextProtocolTool> getByPrefix(String prefix);

    /**
     * Retrieves tools in the closed name range [fromName, toName] (B+-tree leaf list scan semantics).
     *
     * <p>Equivalent to TreeMap.subMap(fromName, true, toName, true).
     *
     * @param fromName start of range (inclusive), must not be null
     * @param toName   end of range (inclusive), must not be null; must satisfy fromName &lt;= toName
     * @return unmodifiable list of tools within the range in lexicographic order
     */
    List<ModelContextProtocolTool> getRange(String fromName, String toName);

    /**
     * Returns all registered tools in lexicographic order (B+-tree full leaf traversal semantics).
     *
     * @return unmodifiable ordered list of all tools
     */
    List<ModelContextProtocolTool> getAll();

    /**
     * Registers or updates a single tool schema (dynamic tool registration entry point).
     *
     * <p>Overwrites any existing entry with the same tool name.
     * Supports runtime extension without rebuilding the entire index.
     *
     * @param tool tool to register, must not be null
     */
    void register(ModelContextProtocolTool tool);

    /**
     * Removes the specified tool (dynamic tool deregistration entry point).
     *
     * @param toolName tool name to remove
     * @return {@code true} if the tool was found and removed, {@code false} if it did not exist
     */
    boolean unregister(String toolName);

    /**
     * Clears all registered tools and rebuilds the index (equivalent to a B+-tree reconstruction).
     *
     * <p>Typically called when the host toolSet undergoes structural changes,
     * in coordination with {@link ModelContextProtocolToolRegistry#invalidateCache()}.
     *
     * @param tools new tool list, must not be null
     */
    void rebuild(List<ModelContextProtocolTool> tools);

    /**
     * Returns the number of tools currently registered in the index.
     *
     * @return total tool count
     */
    int size();
}
