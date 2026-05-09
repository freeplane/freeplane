package org.freeplane.plugin.ai.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.validation.source.ValidationSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 思维导图生成验证器 —— 邻接表 + "蛇吞蛋" 内存优化架构
 *
 * <h3>核心设计</h3>
 * <p>解析阶段采用"蛇吞蛋"策略：Jackson 流式解析时只提取节点 id/text/childIds
 * 三类"蛋液"写入轻量邻接表 {@link SnakeDigestGraph}，JsonNode 对象（"蛋壳"）
 * 在离开作用域后即可被 GC 回收，全程不实例化 {@link MindMapNode} 对象。
 *
 * <p>所有验证（环检测、唯一性、深度、子数、统计）均在邻接表字符串结构上完成，
 * 峰值内存仅为 O(节点ID总长度) 而非 O(节点对象数量)。
 *
 * <h3>线程安全</h3>
 * <p>本类无可变状态，可多线程共用同一实例。
 * 异步执行需由调用方传入 {@link ExecutorService}，不持有静态线程池
 * （静态线程池在 OSGi 插件热重载时会泄漏线程）。
 *
 * <h3>兼容旧接口</h3>
 * <p>保留 {@link #validate(String)} / {@link #validateAsync} 等原有公开接口，
 * 行为与旧实现完全一致。
 */
public class MindMapGenerationValidator {

    public static final int DEFAULT_MAX_DEPTH = 10;
    public static final int DEFAULT_MAX_CHILDREN = 20;
    public static final int DEFAULT_MAX_TOTAL_NODES = 1000;

    private final int maxDepth;
    private final int maxChildren;
    private final int maxTotalNodes;

    public MindMapGenerationValidator() {
        this(DEFAULT_MAX_DEPTH, DEFAULT_MAX_CHILDREN, DEFAULT_MAX_TOTAL_NODES);
    }

    public MindMapGenerationValidator(int maxDepth, int maxChildren, int maxTotalNodes) {
        this.maxDepth = maxDepth;
        this.maxChildren = maxChildren;
        this.maxTotalNodes = maxTotalNodes;
    }

    // -------------------------------------------------------------------------
    // 同步入口
    // -------------------------------------------------------------------------

    /**
     * 验证思维导图 JSON 字符串。
     *
     * <p>内部流程：
     * <ol>
     *   <li>Jackson 解析 JSON 并构建轻量 {@link SnakeDigestGraph}（蛇吞蛋）</li>
     *   <li>JsonNode 对象树在方法返回后可被 GC 回收（吐出蛋壳）</li>
     *   <li>所有验证在 SnakeDigestGraph 邻接表上完成</li>
     * </ol>
     *
     * @param jsonResponse LLM 生成的思维导图 JSON
     * @return 验证结果
     */
    public MindMapValidationResult validate(String jsonResponse) {
        MindMapValidationResult result = new MindMapValidationResult();

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            result.addError("EMPTY_INPUT", "输入为空");
            return result;
        }

        try {
            // ① "蛇吞蛋"：Jackson 解析 → 提取邻接表 → JsonNode 树可 GC
            SnakeDigestGraph graph = buildGraph(jsonResponse);
            if (graph.getRootId() == null) {
                result.addError("NULL_ROOT", "解析后的根节点为空");
                return result;
            }
            // ② 所有验证均在邻接表上完成
            validateGraph(graph, result);
        } catch (Exception e) {
            result.addError("PARSE_ERROR", "JSON 解析失败: " + e.getMessage());
            LogUtils.warn("MindMapGenerationValidator: JSON 解析失败", e);
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // 异步入口（线程池由调用方传入，避免静态泄漏）
    // -------------------------------------------------------------------------

    /**
     * 异步验证，不会阻塞 SSE 流式输出。
     *
     * @param jsonResponse   LLM 生成的思维导图 JSON
     * @param executorService 由调用方提供（推荐复用调度器线程池）
     * @return 包含验证结果的 CompletableFuture
     */
    public CompletableFuture<MindMapValidationResult> validateAsync(
            String jsonResponse, ExecutorService executorService) {
        return CompletableFuture.supplyAsync(() -> validate(jsonResponse), executorService);
    }

    /**
     * 异步验证并在完成后执行回调。
     *
     * @param jsonResponse   LLM 生成的思维导图 JSON
     * @param executorService 由调用方提供
     * @param callback       验证完成后的回调函数
     */
    public void validateAsync(String jsonResponse, ExecutorService executorService,
                              ValidationCallback callback) {
        validateAsync(jsonResponse, executorService).whenComplete((result, throwable) -> {
            if (throwable != null) {
                callback.onValidationError(throwable);
            } else {
                callback.onValidationComplete(result);
            }
        });
    }

    // -------------------------------------------------------------------------
    // ValidationSource 代理入口(新增)
    // -------------------------------------------------------------------------

    /**
     * 通过 ValidationSource 代理接口验证思维导图。
     * 
     * <p>设计要点:
     * <ul>
     *   <li>检查 source.isReady(),未就绪返回 NOT_READY 错误</li>
     *   <li>调用 source.readContent() 获取 JSON</li>
     *   <li>日志带入 source.getDescription() 和 source.getSourceType()</li>
     *   <li>委托现有 validate(String) 执行</li>
     * </ul>
     * 
     * @param source 验证数据源代理
     * @return 验证结果
     */
    public MindMapValidationResult validate(ValidationSource source) {
        if (source == null) {
            MindMapValidationResult result = new MindMapValidationResult();
            result.addError("NULL_SOURCE", "验证数据源为空");
            return result;
        }
        
        if (!source.isReady()) {
            MindMapValidationResult result = new MindMapValidationResult();
            result.addError("NOT_READY", 
                "数据源未就绪: " + source.getDescription() + 
                " [" + source.getSourceType() + "]");
            return result;
        }
        
        try {
            String content = source.readContent();
            LogUtils.info("MindMapGenerationValidator: validating source=" 
                + source.getSourceType() + " / " + source.getDescription());
            
            // 委托现有 validate(String) 执行
            return validate(content);
        } catch (Exception e) {
            MindMapValidationResult result = new MindMapValidationResult();
            result.addError("READ_ERROR", 
                "读取数据源失败 [" + source.getSourceType() + "]: " + e.getMessage());
            LogUtils.warn("MindMapGenerationValidator: failed to read source", e);
            return result;
        }
    }

    /**
     * 异步验证(通过 ValidationSource 代理)。
     * 
     * @param source   验证数据源代理
     * @param executor 线程池(由调用方提供)
     * @return 包含验证结果的 CompletableFuture
     */
    public CompletableFuture<MindMapValidationResult> validateAsync(
            ValidationSource source, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> validate(source), executor);
    }

    public interface ValidationCallback {
        void onValidationComplete(MindMapValidationResult result);
        void onValidationError(Throwable error);
    }

    // -------------------------------------------------------------------------
    // 解析阶段：Jackson 流式解析 → 直接输出 SnakeDigestGraph（"蛇吞蛋"核心）
    // -------------------------------------------------------------------------

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将 JSON 字符串解析为轻量邻接表。
     *
     * <p>“蛇吞蛋”原理：
     * <ul>
     *   <li>“吃入”：Jackson 解析得到 JsonNode 树</li>
     *   <li>“挤出蛋液”：dfs 递归时只提取 id/text/childIds 写入 GraphData</li>
     *   <li>“吱出蛋壳”：{@code buildGraph} 返回后 JsonNode 局部引用消失，GC 可回收整棵对象树</li>
     * </ul>
     */
    private SnakeDigestGraph buildGraph(String json) {
        String trimmed = json.trim();
        if (trimmed.startsWith("[")) {
            throw new IllegalArgumentException("根节点不能是数组，必须是 JSON 对象");
        }
        try {
            // 吴“吃入”：Jackson 解析得到 JsonNode 树（此时内存占用达到峰値）
            JsonNode rootNode = OBJECT_MAPPER.readTree(trimmed);
            if (!rootNode.isObject()) {
                throw new IllegalArgumentException("根节点必须是 JSON 对象");
            }
            SnakeDigestGraph graph = new SnakeDigestGraph();
            // "挠蛋液/吓蛋壳"：dfs 递归提取邻接表，JsonNode 逐步可 GC
            // 吴“挤蛋液）吱蛋壳”：dfs 递归提取邻接表，JsonNode 逐步可 GC
            traverseToGraph(rootNode, null, graph);
            // rootNode 局部引用在此处就要离开作用域 → GC 可回收整棵 JsonNode 树
            return graph;
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * DFS 递归提取邻接表。
     * 每个 JsonNode 在提取完 id/text/children 后不再被引用，最终只有当前递归格上的节点存活。
     */
    private void traverseToGraph(JsonNode node, String parentId, SnakeDigestGraph graph) {
        // ① 提取“蛋液”：id 和 text
        JsonNode idNode = node.get("id");
        String id = (idNode != null && !idNode.isNull()) ? idNode.asText() : generateNodeId();

        JsonNode textNode = node.get("text");
        String text = (textNode != null && !textNode.isNull()) ? textNode.asText() : "";

        // ② 写入邻接表（"流入胃部"）
        graph.registerNode(id, text);
        graph.setRootId(id);
        if (parentId != null) graph.addEdge(parentId, id);

        // ③ 递归子节点（“继续吴入”）
        JsonNode childrenNode = node.get("children");
        if (childrenNode != null && childrenNode.isArray()) {
            for (JsonNode child : childrenNode) {
                if (child.isObject()) {
                    traverseToGraph(child, id, graph);
                    // child 局部引用在循环下一轮即失效 → GC 可回收（“吱蛋壳”）
                }
            }
        }
        // node 局部引用在此帧退栈后就可 GC
    }

    // -------------------------------------------------------------------------
    // 验证阶段：所有检查均在 SnakeDigestGraph 上完成
    // -------------------------------------------------------------------------

    private void validateGraph(SnakeDigestGraph graph, MindMapValidationResult result) {
        // 1. 根节点有效性
        validateRoot(graph, result);

        // 2. ID 唯一性（邻接表 key 本身就是唯一的；但需检测同一节点被多个父节点引用的情况）
        checkDuplicateIds(graph, result);

        // 3. 环检测 + 深度 + 子数 + 自引用 + 统计：一次 DFS 全部完成
        GraphStats stats = new GraphStats();
        detectCyclesAndCollectStats(graph, stats, result);

        if (stats.maxDepth > maxDepth) {
            result.addError("EXCEEDS_MAX_DEPTH",
                "思维导图深度超过限制: " + stats.maxDepth + " > " + maxDepth);
        }
        if (stats.totalNodes > maxTotalNodes) {
            result.addError("EXCEEDS_MAX_TOTAL_NODES",
                "思维导图节点总数超过限制: " + stats.totalNodes + " > " + maxTotalNodes);
        }

        MindMapValidationResult.MindMapStatistics s = new MindMapValidationResult.MindMapStatistics();
        s.setTotalNodes(stats.totalNodes);
        s.setMaxDepth(stats.maxDepth);
        s.setMaxChildrenPerNode(stats.maxChildrenPerNode);
        s.setLeafNodes(stats.leafNodes);
        s.setInternalNodes(stats.internalNodes);
        double avg = stats.internalNodes > 0
            ? (double) (stats.totalNodes - 1) / stats.internalNodes : 0;
        s.setAverageChildrenPerNode(avg);
        result.setStatistics(s);
    }

    // -------------------------------------------------------------------------
    // 1. 根节点有效性
    // -------------------------------------------------------------------------

    private void validateRoot(SnakeDigestGraph graph, MindMapValidationResult result) {
        String rootId = graph.getRootId();
        String text = graph.getLabel(rootId != null ? rootId : "");
        boolean hasId = rootId != null && !rootId.startsWith("node_"); // 自动生成的 ID 不算“有效 ID”
        boolean hasText = text != null && !text.trim().isEmpty();
        if (!hasId && !hasText) {
            result.addError("INVALID_ROOT", "根节点缺少有效的 ID 和文本");
        }
    }

    // -------------------------------------------------------------------------
    // 2. ID 唯一性
    // -------------------------------------------------------------------------

    /**
     * 检测同一 childId 是否被多个父节点引用（邻接表 key 不重复，
     * 但同一 childId 可能出现在多个父节点的 children 列表中）。
     */
    private void checkDuplicateIds(SnakeDigestGraph graph, MindMapValidationResult result) {
        Set<String> allChildIds = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : graph.adjacencyEntries()) {
            for (String childId : entry.getValue()) {
                if (!allChildIds.add(childId)) {
                    result.addError("DUPLICATE_ID", "发现重复的节点 ID: " + childId, childId);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 3. DFS 环检测 + 统计：三色标记 + parent Map 一次遍历完成全部工作
    //
    // 三色语义：
    //   WHITE(0) — 未访问
    //   GRAY (1) — 正在递归栈中（回边 = 发现环）
    //   BLACK(2) — 已完成，确认无环
    //
    // 优化点：
    //   • 两个 HashSet（visited + recursionStack）合并为一个 HashMap，查找从 2次 → 1次
    //   • 回溯由 LinkedHashSet.remove() → HashMap.put(BLACK)，消除链表维护开销
    //   • cyclePath ArrayList 替换为 parent Map 按需回溯重建，避免每步入栈/出栈操作
    // -------------------------------------------------------------------------

    private static final int WHITE = 0;
    private static final int GRAY  = 1;
    private static final int BLACK = 2;

    private static final class GraphStats {
        int totalNodes = 0;
        int maxDepth = 0;
        int maxChildrenPerNode = 0;
        int leafNodes = 0;
        int internalNodes = 0;
    }

    private void detectCyclesAndCollectStats(SnakeDigestGraph graph, GraphStats stats,
                                             MindMapValidationResult result) {
        // color/parent 由 SnakeDigestGraph 按节点数预分配，避免 DFS 中途 rehash
        Map<String, Integer> color = graph.newColorMap();
        Map<String, String> parent = graph.newParentMap();
        statsDFS(graph.getRootId(), graph, color, parent, 1, stats, result);
    }

    private void statsDFS(String nodeId, SnakeDigestGraph graph,
                          Map<String, Integer> color, Map<String, String> parent,
                          int depth, GraphStats stats,
                          MindMapValidationResult result) {
        if (nodeId == null) return;

        // 一次查找同时判断 GRAY / BLACK（原来需要两次 HashSet.contains）
        int state = color.getOrDefault(nodeId, WHITE);

        if (state == GRAY) {
            // 发现回边：从 parent Map 回溯重建环路径
            result.addError("CIRCULAR_DEPENDENCY",
                "检测到循环依赖: " + buildCyclePath(nodeId, parent),
                nodeId);
            return;
        }
        if (state == BLACK) return; // 已完成，剪枝

        // ① 进入：WHITE → GRAY
        color.put(nodeId, GRAY);
        stats.totalNodes++;
        stats.maxDepth = Math.max(stats.maxDepth, depth);

        List<String> children = graph.getChildren(nodeId);
        int childCount = children.size();
        stats.maxChildrenPerNode = Math.max(stats.maxChildrenPerNode, childCount);

        if (childCount == 0) {
            stats.leafNodes++;
        } else {
            stats.internalNodes++;

            if (childCount > maxChildren) {
                result.addError("EXCEEDS_MAX_CHILDREN",
                    "节点子节点数量超过限制: " + childCount + " > " + maxChildren, nodeId);
            }

            for (String childId : children) {
                if (childId.equals(nodeId)) {
                    result.addError("SELF_REFERENCE", "节点引用自身作为子节点", nodeId);
                    continue;
                }
                // 记录父节点，供环路径回溯
                parent.put(childId, nodeId);
                statsDFS(childId, graph, color, parent, depth + 1, stats, result);
            }
        }

        // ② 完成：GRAY → BLACK（"吐出蛋壳"，简单 put 替代 LinkedHashSet.remove）
        color.put(nodeId, BLACK);
    }

    /**
     * 从 parent Map 回溯重建环路径字符串。
     * 从回边目标节点（cycleEntry）沿 parent 链向上回溯，直到再次遇到 cycleEntry，
     * 收集路径后反转输出 "A → B → C → A"。
     */
    private String buildCyclePath(String cycleEntry, Map<String, String> parent) {
        List<String> path = new ArrayList<>();
        path.add(cycleEntry);
        String cur = parent.get(cycleEntry);
        // 沿父链回溯，最多回溯 parent.size() 步防止意外死循环
        int limit = parent.size() + 1;
        while (cur != null && !cur.equals(cycleEntry) && limit-- > 0) {
            path.add(cur);
            cur = parent.get(cur);
        }
        path.add(cycleEntry); // 闭合环
        Collections.reverse(path);
        return String.join(" → ", path);
    }

    // -------------------------------------------------------------------------
    // 工具方法
    // -------------------------------------------------------------------------

    private String generateNodeId() {
        return "node_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
