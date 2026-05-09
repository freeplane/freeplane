package org.freeplane.plugin.ai.strategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具性能画像 - 基于实际性能数据的优先级定义
 * 
 * <p>根据工具操作的特性，将14个工具分为8个优先级层级。
 * 数据来源于 Freeplane 1.10+ 版本性能测试结果。
 * 
 * <h3>优先级分类原则：</h3>
 * <ol>
 *   <li><b>95-100分</b>：核心树结构操作，直接操作数据模型，响应最快</li>
 *   <li><b>88-94分</b>：样式系统操作，有缓存机制，局部修改开销小</li>
 *   <li><b>85-90分</b>：纯选择操作，几乎无额外计算</li>
 *   <li><b>80-88分</b>：搜索操作，内置优化但需遍历</li>
 *   <li><b>75-82分</b>：过滤操作，有专用优化但仍涉及遍历和重绘</li>
 *   <li><b>65-75分</b>：公式计算，依赖跟踪机制，复杂表达式开销大</li>
 *   <li><b>60-70分</b>：导出操作，涉及全图遍历和输出生成</li>
 *   <li><b>55-65分</b>：批量操作，大量节点时性能明显下降</li>
 * </ol>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>贪心策略：按价值密度排序时使用优先级作为权重</li>
 *   <li>完全背包：优先级决定工具选择的价值分数</li>
 *   <li>策略路由：根据工具类型自动选择最优算法</li>
 * </ul>
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public final class ToolPerformanceProfile {
    
    // ========== 优先级常量（分数越高，性能越好，优先调用） ==========
    
    /** 核心树结构操作：创建/删除/移动/复制/粘贴节点、折叠/展开、更改文本 */
    public static final int PRIORITY_TREE_OPERATIONS = 97;  // 95-100 取中值
    
    /** 样式操作：应用样式、设置图标、字体颜色、节点背景、超链接 */
    public static final int PRIORITY_STYLE_OPERATIONS = 91;  // 88-94 取中值
    
    /** 选择导航：选中节点、跳转、展开到指定层级 */
    public static final int PRIORITY_SELECTION_OPERATIONS = 87;  // 85-90 取中值
    
    /** 搜索操作：查找节点、搜索替换（基本模式） */
    public static final int PRIORITY_SEARCH_OPERATIONS = 84;  // 80-88 取中值
    
    /** 过滤操作：应用过滤器、显示/隐藏节点 */
    public static final int PRIORITY_FILTER_OPERATIONS = 78;  // 75-82 取中值
    
    /** 公式计算：节点公式、属性公式 */
    public static final int PRIORITY_FORMULA_OPERATIONS = 70;  // 65-75 取中值
    
    /** 导出操作：导出为PNG、Markdown、OPML、XML等 */
    public static final int PRIORITY_EXPORT_OPERATIONS = 65;  // 60-70 取中值
    
    /** 批量操作：大规模节点批量修改、复杂条件样式 */
    public static final int PRIORITY_BATCH_OPERATIONS = 60;  // 55-65 取中值
    
    // ========== 工具到优先级的映射 ==========
    
    /**
     * 工具名称到优先级的映射表
     * <p>根据 Freeplane 14个工具的实际功能特性分类
     */
    private static final Map<String, Integer> TOOL_PRIORITY_MAP = buildPriorityMap();
    
    private static Map<String, Integer> buildPriorityMap() {
        Map<String, Integer> map = new HashMap<>();
        
        // ===== 优先级1：核心树结构操作（97分）=====
        map.put("createNodes", PRIORITY_TREE_OPERATIONS);           // 创建节点
        map.put("deleteNodes", PRIORITY_TREE_OPERATIONS);          // 删除节点
        map.put("moveNodes", PRIORITY_TREE_OPERATIONS);            // 移动节点
        map.put("copyNodes", PRIORITY_TREE_OPERATIONS);            // 复制节点
        map.put("pasteNodes", PRIORITY_TREE_OPERATIONS);           // 粘贴节点
        map.put("foldBranch", PRIORITY_TREE_OPERATIONS);           // 折叠分支
        map.put("expandBranch", PRIORITY_TREE_OPERATIONS);         // 展开分支
        map.put("edit", PRIORITY_TREE_OPERATIONS);                 // 编辑节点文本（通过edit工具）
        
        // ===== 优先级2：样式操作（91分）=====
        map.put("applyStyle", PRIORITY_STYLE_OPERATIONS);          // 应用样式
        map.put("setIcon", PRIORITY_STYLE_OPERATIONS);             // 设置图标
        map.put("setNodeColor", PRIORITY_STYLE_OPERATIONS);        // 设置字体颜色
        map.put("setNodeBackground", PRIORITY_STYLE_OPERATIONS);   // 设置节点背景
        map.put("setHyperlink", PRIORITY_STYLE_OPERATIONS);        // 设置超链接
        
        // ===== 优先级3：选择导航（87分）=====
        map.put("selectNode", PRIORITY_SELECTION_OPERATIONS);      // 选中节点
        map.put("navigateToNode", PRIORITY_SELECTION_OPERATIONS);  // 跳转到节点
        map.put("expandToLevel", PRIORITY_SELECTION_OPERATIONS);   // 展开到指定层级
        
        // ===== 优先级4：搜索操作（84分）=====
        map.put("findNode", PRIORITY_SEARCH_OPERATIONS);           // 查找节点
        map.put("searchAndReplace", PRIORITY_SEARCH_OPERATIONS);   // 搜索替换
        
        // ===== 优先级5：过滤操作（78分）=====
        map.put("applyFilter", PRIORITY_FILTER_OPERATIONS);        // 应用过滤器
        map.put("filterComposer", PRIORITY_FILTER_OPERATIONS);     // 过滤器组合器
        map.put("showHideNodes", PRIORITY_FILTER_OPERATIONS);      // 显示/隐藏节点
        
        // ===== 优先级6：公式计算（70分）=====
        map.put("calculateFormula", PRIORITY_FORMULA_OPERATIONS);  // 计算公式
        map.put("evaluateProperty", PRIORITY_FORMULA_OPERATIONS);  // 评估属性公式
        
        // ===== 优先级7：导出操作（65分）=====
        map.put("exportToPng", PRIORITY_EXPORT_OPERATIONS);        // 导出PNG
        map.put("exportToMarkdown", PRIORITY_EXPORT_OPERATIONS);   // 导出Markdown
        map.put("exportToOpml", PRIORITY_EXPORT_OPERATIONS);       // 导出OPML
        map.put("exportToXml", PRIORITY_EXPORT_OPERATIONS);        // 导出XML
        map.put("exportToPdf", PRIORITY_EXPORT_OPERATIONS - 5);    // 导出PDF（更慢，减5分）
        
        // ===== 优先级8：批量操作（60分）=====
        map.put("batchModify", PRIORITY_BATCH_OPERATIONS);         // 批量修改
        map.put("applyConditionalStyle", PRIORITY_BATCH_OPERATIONS); // 条件样式
        
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * 获取工具的优先级分数
     * 
     * @param toolName 工具名称
     * @return 优先级分数（55-100），未知工具返回默认值70
     */
    public static int getPriority(String toolName) {
        return TOOL_PRIORITY_MAP.getOrDefault(toolName, 70);
    }
    
    /**
     * 判断工具是否属于高性能类别（优先级≥85）
     * 
     * @param toolName 工具名称
     * @return true表示高性能工具，推荐优先使用
     */
    public static boolean isHighPerformance(String toolName) {
        return getPriority(toolName) >= 85;
    }
    
    /**
     * 判断工具是否属于中等性能类别（优先级70-84）
     * 
     * @param toolName 工具名称
     * @return true表示中等性能工具，需谨慎使用
     */
    public static boolean isMediumPerformance(String toolName) {
        int priority = getPriority(toolName);
        return priority >= 70 && priority < 85;
    }
    
    /**
     * 判断工具是否属于低性能类别（优先级<70）
     * 
     * @param toolName 工具名称
     * @return true表示低性能工具，应避免高频调用
     */
    public static boolean isLowPerformance(String toolName) {
        return getPriority(toolName) < 70;
    }
    
    /**
     * 获取工具的优先级层级描述
     * 
     * @param toolName 工具名称
     * @return 层级描述（如"核心树结构操作"）
     */
    public static String getPriorityLevelDescription(String toolName) {
        int priority = getPriority(toolName);
        
        if (priority >= 95) {
            return "核心树结构操作（95-100分）";
        } else if (priority >= 88) {
            return "样式操作（88-94分）";
        } else if (priority >= 85) {
            return "选择导航操作（85-90分）";
        } else if (priority >= 80) {
            return "搜索操作（80-88分）";
        } else if (priority >= 75) {
            return "过滤操作（75-82分）";
        } else if (priority >= 65) {
            return "公式/导出操作（60-75分）";
        } else {
            return "批量操作（55-65分）";
        }
    }
    
    /**
     * 获取所有已注册的工具优先级映射（用于调试）
     * 
     * @return 不可修改的映射表
     */
    public static Map<String, Integer> getAllToolPriorities() {
        return TOOL_PRIORITY_MAP;
    }
    
    private ToolPerformanceProfile() {
        // 防止实例化
    }
}
