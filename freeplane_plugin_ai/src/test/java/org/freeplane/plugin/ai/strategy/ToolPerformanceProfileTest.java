package org.freeplane.plugin.ai.strategy;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * 工具性能画像测试
 * 
 * 测试基于实际性能数据的工具优先级定义
 * 
 * @author AI Plugin Team
 * @since 1.13.x
 */
public class ToolPerformanceProfileTest {
    
    @Test
    public void treeOperations_should_haveHighestPriority() {
        // 核心树结构操作应该是最高优先级（95-100分）
        assertEquals(97, ToolPerformanceProfile.getPriority("createNodes"));
        assertEquals(97, ToolPerformanceProfile.getPriority("deleteNodes"));
        assertEquals(97, ToolPerformanceProfile.getPriority("moveNodes"));
        assertEquals(97, ToolPerformanceProfile.getPriority("copyNodes"));
        assertEquals(97, ToolPerformanceProfile.getPriority("pasteNodes"));
        assertEquals(97, ToolPerformanceProfile.getPriority("foldBranch"));
        assertEquals(97, ToolPerformanceProfile.getPriority("expandBranch"));
        assertEquals(97, ToolPerformanceProfile.getPriority("edit"));
    }
    
    @Test
    public void styleOperations_should_haveHighPriority() {
        // 样式操作应该是高优先级（88-94分）
        assertEquals(91, ToolPerformanceProfile.getPriority("applyStyle"));
        assertEquals(91, ToolPerformanceProfile.getPriority("setIcon"));
        assertEquals(91, ToolPerformanceProfile.getPriority("setNodeColor"));
        assertEquals(91, ToolPerformanceProfile.getPriority("setNodeBackground"));
        assertEquals(91, ToolPerformanceProfile.getPriority("setHyperlink"));
    }
    
    @Test
    public void selectionOperations_should_haveMediumHighPriority() {
        // 选择导航操作应该是中高优先级（85-90分）
        assertEquals(87, ToolPerformanceProfile.getPriority("selectNode"));
        assertEquals(87, ToolPerformanceProfile.getPriority("navigateToNode"));
        assertEquals(87, ToolPerformanceProfile.getPriority("expandToLevel"));
    }
    
    @Test
    public void searchOperations_should_haveMediumPriority() {
        // 搜索操作应该是中优先级（80-88分）
        assertEquals(84, ToolPerformanceProfile.getPriority("findNode"));
        assertEquals(84, ToolPerformanceProfile.getPriority("searchAndReplace"));
    }
    
    @Test
    public void filterOperations_should_haveLowerMediumPriority() {
        // 过滤操作应该是中低优先级（75-82分）
        assertEquals(78, ToolPerformanceProfile.getPriority("applyFilter"));
        assertEquals(78, ToolPerformanceProfile.getPriority("filterComposer"));
        assertEquals(78, ToolPerformanceProfile.getPriority("showHideNodes"));
    }
    
    @Test
    public void formulaOperations_should_haveLowPriority() {
        // 公式计算应该是低优先级（65-75分）
        assertEquals(70, ToolPerformanceProfile.getPriority("calculateFormula"));
        assertEquals(70, ToolPerformanceProfile.getPriority("evaluateProperty"));
    }
    
    @Test
    public void exportOperations_should_haveVeryLowPriority() {
        // 导出操作应该是很低优先级（60-70分）
        assertEquals(65, ToolPerformanceProfile.getPriority("exportToPng"));
        assertEquals(65, ToolPerformanceProfile.getPriority("exportToMarkdown"));
        assertEquals(65, ToolPerformanceProfile.getPriority("exportToOpml"));
        assertEquals(65, ToolPerformanceProfile.getPriority("exportToXml"));
        assertEquals(60, ToolPerformanceProfile.getPriority("exportToPdf"));  // PDF更慢
    }
    
    @Test
    public void batchOperations_should_haveLowestPriority() {
        // 批量操作应该是最低优先级（55-65分）
        assertEquals(60, ToolPerformanceProfile.getPriority("batchModify"));
        assertEquals(60, ToolPerformanceProfile.getPriority("applyConditionalStyle"));
    }
    
    @Test
    public void unknownTool_should_returnDefaultPriority() {
        // 未知工具应该返回默认优先级70
        assertEquals(70, ToolPerformanceProfile.getPriority("unknownTool"));
    }
    
    @Test
    public void isHighPerformance_should_correctlyIdentify() {
        // 高性能工具（≥85分）
        assertTrue(ToolPerformanceProfile.isHighPerformance("createNodes"));      // 97
        assertTrue(ToolPerformanceProfile.isHighPerformance("applyStyle"));       // 91
        assertTrue(ToolPerformanceProfile.isHighPerformance("selectNode"));       // 87
        
        // 非高性能工具
        assertFalse(ToolPerformanceProfile.isHighPerformance("findNode"));        // 84
        assertFalse(ToolPerformanceProfile.isHighPerformance("calculateFormula")); // 70
        assertFalse(ToolPerformanceProfile.isHighPerformance("exportToPng"));     // 65
    }
    
    @Test
    public void isMediumPerformance_should_correctlyIdentify() {
        // 中等性能工具（70-84分）
        assertTrue(ToolPerformanceProfile.isMediumPerformance("findNode"));        // 84
        assertTrue(ToolPerformanceProfile.isMediumPerformance("applyFilter"));     // 78
        assertTrue(ToolPerformanceProfile.isMediumPerformance("calculateFormula")); // 70
        
        // 非中等性能工具
        assertFalse(ToolPerformanceProfile.isMediumPerformance("createNodes"));    // 97
        assertFalse(ToolPerformanceProfile.isMediumPerformance("exportToPng"));    // 65
    }
    
    @Test
    public void isLowPerformance_should_correctlyIdentify() {
        // 低性能工具（<70分）
        assertTrue(ToolPerformanceProfile.isLowPerformance("exportToPng"));       // 65
        assertTrue(ToolPerformanceProfile.isLowPerformance("batchModify"));       // 60
        assertTrue(ToolPerformanceProfile.isLowPerformance("exportToPdf"));       // 60
        
        // 非低性能工具
        assertFalse(ToolPerformanceProfile.isLowPerformance("createNodes"));      // 97
        assertFalse(ToolPerformanceProfile.isLowPerformance("calculateFormula")); // 70
    }
    
    @Test
    public void priorityLevelDescription_should_returnCorrectDescription() {
        assertEquals("核心树结构操作（95-100分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("createNodes"));
        assertEquals("样式操作（88-94分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("applyStyle"));
        assertEquals("选择导航操作（85-90分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("selectNode"));
        assertEquals("搜索操作（80-88分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("findNode"));
        assertEquals("过滤操作（75-82分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("applyFilter"));
        assertEquals("公式/导出操作（60-75分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("calculateFormula"));
        assertEquals("批量操作（55-65分）", 
                     ToolPerformanceProfile.getPriorityLevelDescription("batchModify"));
    }
    
    @Test
    public void getAllToolPriorities_should_returnImmutableMap() {
        Map<String, Integer> allPriorities = ToolPerformanceProfile.getAllToolPriorities();
        
        // 应该包含所有已注册的工具
        assertTrue(allPriorities.size() > 20);
        
        // 应该是不可修改的
        try {
            allPriorities.put("test", 50);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test
    public void priorityOrder_should_beConsistent() {
        // 验证优先级顺序符合预期
        int treePriority = ToolPerformanceProfile.getPriority("createNodes");
        int stylePriority = ToolPerformanceProfile.getPriority("applyStyle");
        int selectionPriority = ToolPerformanceProfile.getPriority("selectNode");
        int searchPriority = ToolPerformanceProfile.getPriority("findNode");
        int filterPriority = ToolPerformanceProfile.getPriority("applyFilter");
        int formulaPriority = ToolPerformanceProfile.getPriority("calculateFormula");
        int exportPriority = ToolPerformanceProfile.getPriority("exportToPng");
        int batchPriority = ToolPerformanceProfile.getPriority("batchModify");
        
        // 应该严格递减
        assertTrue("树操作 > 样式操作", treePriority > stylePriority);
        assertTrue("样式操作 > 选择操作", stylePriority > selectionPriority);
        assertTrue("选择操作 > 搜索操作", selectionPriority > searchPriority);
        assertTrue("搜索操作 > 过滤操作", searchPriority > filterPriority);
        assertTrue("过滤操作 > 公式操作", filterPriority > formulaPriority);
        assertTrue("公式操作 > 导出操作", formulaPriority > exportPriority);
        assertTrue("导出操作 > 批量操作", exportPriority >= batchPriority);
    }
}
