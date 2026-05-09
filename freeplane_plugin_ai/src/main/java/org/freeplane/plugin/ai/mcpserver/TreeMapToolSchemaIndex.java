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
 * {@link ToolSchemaIndex} 的 TreeMap 内存实现，对应 B+树的有序叶节点链表语义。
 *
 * <p><b>数据结构选择</b>：{@link TreeMap} 基于红黑树，天然有序，提供：
 * <ul>
 *   <li>{@code get} / {@code put} / {@code remove}：O(log N)</li>
 *   <li>{@code subMap} 前缀/范围查询：O(k + log N)，k 为命中工具数</li>
 *   <li>有序遍历：O(N)，无需额外排序</li>
 * </ul>
 *
 * <p><b>线程安全</b>：使用 {@link ReentrantReadWriteLock}，允许多个读线程并发，
 * 写操作（register / unregister / rebuild）互斥。
 *
 * <p><b>未来替换路径</b>：当需要持久化或工具数超过内存承载时，
 * 替换实现类为基于嵌入式 B+树（如 MapDB / RocksDB）的版本，调用方代码零改动。
 */
public class TreeMapToolSchemaIndex implements ToolSchemaIndex {

    private final TreeMap<String, ModelContextProtocolTool> index = new TreeMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 从工具列表构造并初始化索引。
     *
     * @param tools 初始工具列表，允许为空列表但不可为 null
     */
    public TreeMapToolSchemaIndex(List<ModelContextProtocolTool> tools) {
        Objects.requireNonNull(tools, "tools");
        for (ModelContextProtocolTool tool : tools) {
            index.put(tool.getName(), tool);
        }
        LogUtils.info("TreeMapToolSchemaIndex: initialized with " + index.size() + " tools");
    }

    /** 无参构造，创建空索引，后续通过 rebuild() 或 register() 填充。 */
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
     * 前缀检索：等价于 B+树叶节点从 prefix 到 prefix+MAX_CHAR 的范围扫描。
     * 实现利用 TreeMap.subMap(prefix, prefix+"\uFFFF") 在 O(k + log N) 内完成。
     */
    @Override
    public List<ModelContextProtocolTool> getByPrefix(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        lock.readLock().lock();
        try {
            // "\uFFFF" 是 Unicode 最大字符，保证 subMap 覆盖所有以 prefix 开头的键
            String upperBound = prefix + "\uFFFF";
            List<ModelContextProtocolTool> result =
                new ArrayList<>(index.subMap(prefix, upperBound).values());
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 闭区间范围检索：等价于 B+树叶节点链表从 fromName 到 toName 的顺序扫描。
     */
    @Override
    public List<ModelContextProtocolTool> getRange(String fromName, String toName) {
        Objects.requireNonNull(fromName, "fromName");
        Objects.requireNonNull(toName, "toName");
        lock.readLock().lock();
        try {
            // true, true 表示两端闭区间 [fromName, toName]
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
     * 全量重建索引。配合 {@link ModelContextProtocolToolRegistry#invalidateCache()} 联动：
     * <pre>
     * registry.invalidateCache();           // 清除 Schema 反射缓存
     * List&lt;...&gt; fresh = registry.listTools(); // 重新构建
     * schemaIndex.rebuild(fresh);           // 重建有序索引
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
