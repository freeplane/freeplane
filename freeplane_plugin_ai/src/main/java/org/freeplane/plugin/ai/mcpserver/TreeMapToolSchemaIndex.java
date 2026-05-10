package org.freeplane.plugin.ai.mcpserver;

import org.freeplane.core.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory TreeMap implementation of {@link ToolSchemaIndex}, providing ordered leaf-list semantics.
 *
 * <p><b>Data structure</b>: {@link TreeMap} (red-black tree), naturally ordered:
 * <ul>
 *   <li>{@code get} / {@code put} / {@code remove}: O(log N)</li>
 *   <li>{@code subMap} prefix/range query: O(k + log N), where k is the number of matching tools</li>
 *   <li>Ordered traversal: O(N), no additional sort needed</li>
 * </ul>
 *
 * <p><b>Thread safety</b>: uses {@link ReentrantReadWriteLock} to allow concurrent reads;
 * write operations (register / unregister / rebuild) are exclusive.
 *
 * <p><b>Future migration path</b>: replace this class with an embedded B+-tree (e.g. MapDB / RocksDB)
 * when persistence or large tool counts are needed — callers need no changes.
 */
public class TreeMapToolSchemaIndex implements ToolSchemaIndex {

    private final TreeMap<String, ModelContextProtocolTool> index = new TreeMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructs and initializes the index from a tool list.
     *
     * @param tools initial tool list; may be empty but must not be null
     */
    public TreeMapToolSchemaIndex(List<ModelContextProtocolTool> tools) {
        Objects.requireNonNull(tools, "tools");
        for (ModelContextProtocolTool tool : tools) {
            index.put(tool.getName(), tool);
        }
        LogUtils.info("TreeMapToolSchemaIndex: initialized with " + index.size() + " tools");
    }

    /** No-arg constructor; creates an empty index. Populate via {@link #rebuild(List)} or {@link #register(ModelContextProtocolTool)}. */
    public TreeMapToolSchemaIndex() {}

    @Override
    public ModelContextProtocolTool get(String toolName) {
        Objects.requireNonNull(toolName, "toolName");
        lock.readLock().lock();
        try {
            return index.get(toolName);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Prefix lookup: equivalent to a B+-tree leaf scan from {@code prefix} to {@code prefix+MAX_CHAR}.
     * Uses TreeMap.subMap(prefix, prefix+"\uFFFF") in O(k + log N).
     */
    @Override
    public List<ModelContextProtocolTool> getByPrefix(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        lock.readLock().lock();
        try {
            // "\uFFFF" is the largest Unicode char, ensuring subMap covers all keys starting with prefix
            String upperBound = prefix + "\uFFFF";
            List<ModelContextProtocolTool> result =
                new ArrayList<>(index.subMap(prefix, upperBound).values());
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Closed-interval range lookup: equivalent to sequential scanning B+-tree leaves from
     * {@code fromName} to {@code toName}.
     */
    @Override
    public List<ModelContextProtocolTool> getRange(String fromName, String toName) {
        Objects.requireNonNull(fromName, "fromName");
        Objects.requireNonNull(toName, "toName");
        lock.readLock().lock();
        try {
            // true, true = both endpoints are inclusive: [fromName, toName]
            List<ModelContextProtocolTool> result =
                new ArrayList<>(index.subMap(fromName, true, toName, true).values());
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ModelContextProtocolTool> getAll() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(index.values()));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void register(ModelContextProtocolTool tool) {
        Objects.requireNonNull(tool, "tool");
        lock.writeLock().lock();
        try {
            boolean isUpdate = index.containsKey(tool.getName());
            index.put(tool.getName(), tool);
            LogUtils.info("TreeMapToolSchemaIndex: " + (isUpdate ? "updated" : "registered")
                + " tool '" + tool.getName() + "', total=" + index.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean unregister(String toolName) {
        Objects.requireNonNull(toolName, "toolName");
        lock.writeLock().lock();
        try {
            boolean removed = index.remove(toolName) != null;
            if (removed) {
                LogUtils.info("TreeMapToolSchemaIndex: unregistered tool '" + toolName
                    + "', total=" + index.size());
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Performs a full rebuild of the index.
     * Intended to be used in conjunction with {@link ModelContextProtocolToolRegistry#invalidateCache()}:
     * <pre>
     * registry.invalidateCache();           // clear the Schema reflection cache
     * List&lt;...&gt; fresh = registry.listTools(); // rebuild
     * schemaIndex.rebuild(fresh);           // rebuild the ordered index
     * </pre>
     */
    @Override
    public void rebuild(List<ModelContextProtocolTool> tools) {
        Objects.requireNonNull(tools, "tools");
        lock.writeLock().lock();
        try {
            index.clear();
            for (ModelContextProtocolTool tool : tools) {
                index.put(tool.getName(), tool);
            }
            LogUtils.info("TreeMapToolSchemaIndex: rebuilt with " + index.size() + " tools");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return index.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
