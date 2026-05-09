package org.freeplane.plugin.ai.validation;

import java.util.*;

/**
 * 蛇吞蛋图容器（SnakeDigestGraph）—— 环检测专用数据容器。
 *
 * <h3>命名语义</h3>
 * <ul>
 *   <li><b>Snake</b>：蛇吞蛋原理中的"蛇"，驱动整个解析流程</li>
 *   <li><b>Digest</b>：消化行为——只保留 id/text/edges 三类"蛋液"，
 *       JsonNode 对象（"蛋壳"）在解析后即可被 GC 回收</li>
 *   <li><b>Graph</b>：底层数据结构为有向图（邻接表）</li>
 * </ul>
 *
 * <h3>蛇缓张嘴扩容策略</h3>
 * <ul>
 *   <li>外层邻接表 Map 初始容量 {@value #INITIAL_CAPACITY}，
 *       超限后 LinkedHashMap 内部 ×2 扩容</li>
 *   <li>每个节点的子节点列表初始容量 <b>0</b>，叶子节点不分配任何数组空间；
 *       首次 {@code addEdge} 时扩容至1，后续按 ArrayList 1.5 倍增长
 *       （1 → 2 → 3 → 4 → 6 → 9...）</li>
 *   <li>DFS 辅助 Map 由 {@link #newColorMap()} / {@link #newParentMap()} 按
 *       当前节点总数预分配，避免 DFS 过程中触发 rehash</li>
 * </ul>
 *
 * <h3>职责边界</h3>
 * <p>本类只负责持有图结构数据并提供读写操作。
 * 不含验证逻辑、不含 DFS 算法、不含 JSON 解析——这三部分全部由
 * {@link MindMapGenerationValidator} 负责。
 */
public final class SnakeDigestGraph {

    /** 初始桶数：针对思维导图小图（4~8节点）设计，超过6节点后首次自动扩容 */
    static final int   INITIAL_CAPACITY = 8;
    static final float LOAD_FACTOR      = 0.75f;

    private String rootId;

    /**
     * 邻接表：nodeId → 子节点 ID 列表（保序）。
     * 初始8桶，超限 ×2 扩容；每条子列表初始容量0（叶子节点不分配数组空间）。
     */
    private final Map<String, List<String>> adjacency =
            new LinkedHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR);

    /**
     * 标签表：nodeId → text（仅用于验证报告和根节点有效性检查）。
     * 与邻接表同步扩容节奏。
     */
    private final Map<String, String> labels =
            new HashMap<>(INITIAL_CAPACITY, LOAD_FACTOR);

    // -------------------------------------------------------------------------
    // 写入接口（JSON 解析 / DFS 遍历阶段逐步调用，"蛇缓张嘴"）
    // -------------------------------------------------------------------------

    /**
     * 注册节点到邻接表。
     * 子节点列表初始容量为 0，叶子节点不分配任何 Object[] 空间。
     *
     * @param id   节点 ID
     * @param text 节点文本（用于验证报告）
     */
    public void registerNode(String id, String text) {
        adjacency.putIfAbsent(id, new ArrayList<>(0));
        labels.put(id, text);
    }

    /**
     * 添加有向边 parentId → childId。
     * 触发子节点列表的 ArrayList 懒扩容：首次 add 从0扩至1，后续按1.5倍增长。
     *
     * @param parentId 父节点 ID
     * @param childId  子节点 ID
     */
    public void addEdge(String parentId, String childId) {
        adjacency.computeIfAbsent(parentId, k -> new ArrayList<>(0)).add(childId);
    }

    /**
     * 设置根节点 ID。幂等保护：仅在首次调用（rootId 为 null）时生效，
     * 防止 DFS 递归中意外覆盖根节点。
     */
    public void setRootId(String id) {
        if (this.rootId == null) {
            this.rootId = id;
        }
    }

    // -------------------------------------------------------------------------
    // 读取接口（验证阶段调用）
    // -------------------------------------------------------------------------

    /** 返回根节点 ID，未设置时返回 null。 */
    public String getRootId() {
        return rootId;
    }

    /** 返回当前已注册的节点总数。 */
    public int nodeCount() {
        return adjacency.size();
    }

    /**
     * 返回指定节点的子节点列表（不可变视图，防止外部篡改）。
     * 节点不存在或为叶子节点时返回空列表。
     */
    public List<String> getChildren(String nodeId) {
        List<String> children = adjacency.get(nodeId);
        return children == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(children);
    }

    /**
     * 返回节点文本，节点不存在时返回空字符串。
     */
    public String getLabel(String nodeId) {
        return labels.getOrDefault(nodeId, "");
    }

    /**
     * 返回邻接表全部条目的不可变视图（用于 ID 唯一性检查等遍历场景）。
     */
    public Set<Map.Entry<String, List<String>>> adjacencyEntries() {
        return Collections.unmodifiableSet(adjacency.entrySet());
    }

    // -------------------------------------------------------------------------
    // DFS 辅助工厂方法（按当前节点数预分配，避免 DFS 中途 rehash）
    // -------------------------------------------------------------------------

    /**
     * 创建 DFS 三色标记 Map（WHITE / GRAY / BLACK）。
     * 容量 = max(INITIAL_CAPACITY, nodeCount × 2)，
     * 使装填率保持在 0.5 以下，DFS 全程无 rehash。
     */
    public Map<String, Integer> newColorMap() {
        return new HashMap<>(Math.max(INITIAL_CAPACITY, adjacency.size() * 2));
    }

    /**
     * 创建 DFS 父节点记录 Map（用于发现环时重建完整路径）。
     * 容量策略与 {@link #newColorMap()} 相同。
     */
    public Map<String, String> newParentMap() {
        return new HashMap<>(Math.max(INITIAL_CAPACITY, adjacency.size() * 2));
    }
}
