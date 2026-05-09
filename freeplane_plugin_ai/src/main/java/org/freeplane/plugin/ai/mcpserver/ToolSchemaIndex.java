package org.freeplane.plugin.ai.mcpserver;

import java.util.List;

/**
 * 工具 Schema 索引接口（B+树语义）。
 *
 * <p>设计动机：当工具数量规模化（100+）或需要语义路由时，
 * 顺序遍历 List 的 O(N) 性能不再满足需求。本接口参照 B+树 的核心特性建模：
 * <ul>
 *   <li><b>有序性</b>：工具名按字典序排列，支持范围查询（类比 B+树叶节点链表）</li>
 *   <li><b>前缀检索</b>：按命名空间/前缀批量召回相关工具，减少发给 LLM 的 Token</li>
 *   <li><b>范围查询</b>：按 [fromName, toName] 区间获取工具子集</li>
 *   <li><b>持久化就绪</b>：接口语义与磁盘 B+树存储兼容，便于未来跨会话 Schema 缓存</li>
 * </ul>
 *
 * <p>当前推荐实现：{@link TreeMapToolSchemaIndex}（基于 TreeMap 的内存实现）。
 * 未来可替换为基于文件的 B+树实现（如嵌入式数据库）而不改变调用方代码。
 *
 * <h3>典型使用场景</h3>
 * <pre>
 * // 场景1：LLM 只需要节点操作类工具，减少 Prompt Token 消耗
 * List&lt;ModelContextProtocolTool&gt; nodeTools = index.getByPrefix("node");
 *
 * // 场景2：按范围获取工具（字典序区间）
 * List&lt;ModelContextProtocolTool&gt; rangeTools = index.getRange("create", "delete");
 *
 * // 场景3：精确查找
 * Optional&lt;ModelContextProtocolTool&gt; tool = index.get("createNodes");
 * </pre>
 */
public interface ToolSchemaIndex {

    /**
     * 按工具名精确查找。
     *
     * @param toolName 工具名称，大小写敏感
     * @return 对应工具，不存在时返回 null
     */
    ModelContextProtocolTool get(String toolName);

    /**
     * 按名称前缀批量检索工具（B+树范围扫描语义）。
     *
     * <p>例如 prefix="node" 可召回 nodeCreate / nodeDelete / nodeRead 等所有节点操作工具。
     * 实现应等价于 TreeMap.subMap(prefix, prefix + "\uFFFF")，复杂度 O(k + log N)。
     *
     * @param prefix 工具名前缀，不可为 null
     * @return 名称以 prefix 开头的所有工具列表，按字典序排列，不可修改
     */
    List<ModelContextProtocolTool> getByPrefix(String prefix);

    /**
     * 按名称区间范围检索工具（闭区间，B+树叶节点链表扫描语义）。
     *
     * <p>返回名称在 [fromName, toName] 之间的所有工具（字典序），
     * 等价于 TreeMap.subMap(fromName, true, toName, true)。
     *
     * @param fromName 起始工具名（含），不可为 null
     * @param toName   结束工具名（含），不可为 null，须满足 fromName &lt;= toName
     * @return 区间内所有工具列表，按字典序排列，不可修改
     */
    List<ModelContextProtocolTool> getRange(String fromName, String toName);

    /**
     * 获取索引中所有工具，按字典序排列（B+树全量叶节点遍历语义）。
     *
     * @return 不可修改的有序工具列表
     */
    List<ModelContextProtocolTool> getAll();

    /**
     * 注册或更新单个工具的 Schema（动态工具注册入口）。
     *
     * <p>若工具名已存在则覆盖；支持运行期动态扩展而无需重建整个索引。
     *
     * @param tool 待注册工具，不可为 null
     */
    void register(ModelContextProtocolTool tool);

    /**
     * 移除指定工具（动态工具注销入口）。
     *
     * @param toolName 待移除的工具名
     * @return true 表示成功移除，false 表示工具名不存在
     */
    boolean unregister(String toolName);

    /**
     * 清空所有已注册工具并重建索引（对应 B+树重构）。
     *
     * <p>通常在宿主 toolSet 发生结构性变化时调用，
     * 配合 {@link ModelContextProtocolToolRegistry#invalidateCache()} 联动使用。
     *
     * @param tools 新的工具列表，不可为 null
     */
    void rebuild(List<ModelContextProtocolTool> tools);

    /**
     * 返回当前索引中已注册的工具数量。
     *
     * @return 工具总数
     */
    int size();
}
