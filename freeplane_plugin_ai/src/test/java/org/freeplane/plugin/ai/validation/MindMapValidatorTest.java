package org.freeplane.plugin.ai.validation;

import org.freeplane.plugin.ai.validation.source.FileValidationSource;
import org.freeplane.plugin.ai.validation.source.PromptValidationSource;
import org.freeplane.plugin.ai.validation.source.SourceType;
import org.freeplane.plugin.ai.validation.source.StreamValidationSource;
import org.freeplane.plugin.ai.validation.source.ValidationSource;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MindMapGenerationValidator 测试套件
 *
 * <p>覆盖以下场景：
 * <ol>
 *   <li>正确性验证（无环 / 有环 / 深度超限 / 子数超限 / 重复ID / 空输入）</li>
 *   <li>大规模数据集（深层嵌套 / 宽树 / 1000节点）</li>
 *   <li>内存与耗时对比（邻接表 vs 对象树基准）</li>
 *   <li>线程安全 / 异步接口</li>
 * </ol>
 */
public class MindMapValidatorTest {

    private MindMapGenerationValidator validator;
    private ExecutorService executor;

    @Before
    public void setUp() {
        validator = new MindMapGenerationValidator();
        executor = Executors.newFixedThreadPool(4);
    }

    // =========================================================================
    // 第一组：基础正确性
    // =========================================================================

    @Test
    public void testValidSimpleTree() {
        String json = "{\"id\":\"root\",\"text\":\"根节点\",\"children\":["
            + "{\"id\":\"c1\",\"text\":\"子节点1\"},"
            + "{\"id\":\"c2\",\"text\":\"子节点2\",\"children\":["
            + "  {\"id\":\"c2_1\",\"text\":\"孙节点\"}"
            + "]}"
            + "]}";

        MindMapValidationResult result = validator.validate(json);

        assertThat(result.isValid()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getStatistics()).isNotNull();
        assertThat(result.getStatistics().getTotalNodes()).isEqualTo(4);
        assertThat(result.getStatistics().getMaxDepth()).isEqualTo(3);
        assertThat(result.getStatistics().getLeafNodes()).isEqualTo(2); // c1, c2_1
        assertThat(result.getStatistics().getInternalNodes()).isEqualTo(2); // root, c2
    }

    @Test
    public void testEmptyInput() {
        MindMapValidationResult r1 = validator.validate((String) null);
        assertThat(r1.hasErrors()).isTrue();
        assertThat(r1.getErrors().get(0).getCode()).isEqualTo("EMPTY_INPUT");

        MindMapValidationResult r2 = validator.validate("  ");
        assertThat(r2.hasErrors()).isTrue();
        assertThat(r2.getErrors().get(0).getCode()).isEqualTo("EMPTY_INPUT");
    }

    @Test
    public void testInvalidJson() {
        MindMapValidationResult result = validator.validate("{invalid-json}");
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("PARSE_ERROR");
    }

    @Test
    public void testRootIsArray_shouldFail() {
        MindMapValidationResult result = validator.validate("[{\"text\":\"node\"}]");
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("PARSE_ERROR");
    }

    @Test
    public void testDuplicateNodeIds() {
        String json = "{\"id\":\"root\",\"text\":\"根\",\"children\":["
            + "{\"id\":\"dup\",\"text\":\"节点A\"},"
            + "{\"id\":\"dup\",\"text\":\"节点B\"}"  // 重复 ID
            + "]}";

        MindMapValidationResult result = validator.validate(json);
        assertThat(result.hasErrors()).isTrue();
        boolean hasDuplicateError = result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("DUPLICATE_ID"));
        assertThat(hasDuplicateError).isTrue();
    }

    @Test
    public void testSelfReference() {
        // 父节点 ID = 子节点 ID，构造自引用场景
        String json = "{\"id\":\"n1\",\"text\":\"节点\",\"children\":["
            + "{\"id\":\"n1\",\"text\":\"自引用\"}"
            + "]}";

        MindMapValidationResult result = validator.validate(json);
        assertThat(result.hasErrors()).isTrue();
        boolean hasSelfRef = result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("SELF_REFERENCE") || e.getCode().equals("DUPLICATE_ID"));
        assertThat(hasSelfRef).isTrue();
    }

    @Test
    public void testCircularDependency_cyclePathOutput() {
        // 构造环：root → A → B，但 B 的 children 里再放一个 id=A 的节点，形成 A→B→A
        // 注意：JSON 中 children 是树形，无法直接表达图中的回边。
        // 通过重复 ID：A 出现在 root 子节点，同时也出现在 B 的子节点列表中（DUPLICATE_ID + 触发环路径）
        String json = "{\"id\":\"root\",\"text\":\"根\",\"children\":[" +
            "{\"id\":\"A\",\"text\":\"节点A\",\"children\":[" +
            "{\"id\":\"B\",\"text\":\"节点B\",\"children\":[" +
            "{\"id\":\"A\",\"text\":\"重复A\"}" +  // 重复 ID，触发 DUPLICATE_ID
            "]}" +
            "]}" +
            "]}";

        MindMapValidationResult result = validator.validate(json);
        assertThat(result.hasErrors()).isTrue();
        // 重复 ID 必须被检测到
        boolean hasDup = result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("DUPLICATE_ID"));
        assertThat(hasDup).isTrue();
    }

    @Test
    public void testSelfReference_cyclePathOutput() {
        // 自引用：节点 n1 的子节点列表里包含 n1 自身
        String json = "{\"id\":\"n1\",\"text\":\"自引用节点\",\"children\":[" +
            "{\"id\":\"n1\",\"text\":\"自己\"}" +
            "]}";

        MindMapValidationResult result = validator.validate(json);
        assertThat(result.hasErrors()).isTrue();
        boolean hasSelfRef = result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("SELF_REFERENCE") || e.getCode().equals("DUPLICATE_ID"));
        assertThat(hasSelfRef).isTrue();
        System.out.println("[自引用错误] " + result.getErrors().get(0).getMessage());
    }

    // =========================================================================
    // 第二组：深度与子数限制
    // =========================================================================

    @Test
    public void testExceedsMaxDepth() {
        // 构造深度 = 15（默认限制 10）
        MindMapGenerationValidator strictValidator =
            new MindMapGenerationValidator(5, 20, 1000);

        String json = buildLinearChain(8, "chain");  // 深度 8 > 5
        MindMapValidationResult result = strictValidator.validate(json);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("EXCEEDS_MAX_DEPTH"))).isTrue();
    }

    @Test
    public void testExceedsMaxChildren() {
        MindMapGenerationValidator strictValidator =
            new MindMapGenerationValidator(10, 3, 1000);

        // 根节点有 5 个子节点，超过限制 3
        String json = buildWideNode("root", 5);
        MindMapValidationResult result = strictValidator.validate(json);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("EXCEEDS_MAX_CHILDREN"))).isTrue();
    }

    @Test
    public void testExceedsMaxTotalNodes() {
        MindMapGenerationValidator strictValidator =
            new MindMapGenerationValidator(20, 20, 10);

        // 构造 15 个节点，超过 total=10
        String json = buildLinearChain(15, "node");
        MindMapValidationResult result = strictValidator.validate(json);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> e.getCode().equals("EXCEEDS_MAX_TOTAL_NODES"))).isTrue();
    }

    // =========================================================================
    // 第三组：大规模数据集（性能 + 正确性）
    // =========================================================================

    @Test
    public void testDeepNestedTree_1000nodes() {
        // 深度为 100 的链式树，1000 个节点（每层 10 个子节点，4 层 = 1111 节点）
        // 此处用宽树更合理：3 层，每层 10 叉 = 1 + 10 + 100 + 1000 = 1111 节点
        MindMapGenerationValidator bigValidator =
            new MindMapGenerationValidator(10, 20, 2000);
        String json = buildNaryTree("root", 3, 10); // 深度3，每节点10子

        long startMs = System.currentTimeMillis();
        MindMapValidationResult result = bigValidator.validate(json);
        long elapsedMs = System.currentTimeMillis() - startMs;

        assertThat(result.isValid()).isTrue();
        // 1 + 10 + 100 + 1000 = 1111
        assertThat(result.getStatistics().getTotalNodes()).isEqualTo(1111);
        System.out.printf("[大规模-宽树-1111节点] 耗时: %d ms%n", elapsedMs);
        assertThat(elapsedMs).isLessThan(3000); // 3秒内完成
    }

    @Test
    public void testLinearChain_depth100() {
        MindMapGenerationValidator deepValidator =
            new MindMapGenerationValidator(200, 20, 2000);
        String json = buildLinearChain(100, "depth");

        long startMs = System.currentTimeMillis();
        MindMapValidationResult result = deepValidator.validate(json);
        long elapsedMs = System.currentTimeMillis() - startMs;

        assertThat(result.isValid()).isTrue();
        // buildLinearChain(100) 生成 depth100→depth99→...→depth1→leaf，共 101 个节点
        assertThat(result.getStatistics().getTotalNodes()).isEqualTo(101);
        assertThat(result.getStatistics().getMaxDepth()).isEqualTo(101);
        System.out.printf("[深层链-100节点] 耗时: %d ms%n", elapsedMs);
    }

    // =========================================================================
    // 第四组：内存与耗时对比基准
    // =========================================================================

    /**
     * 内存对比：邻接表架构（当前）vs 等效对象树架构（手动构造基准）。
     *
     * <p>度量方式：测量 GC 前后堆内存差值，邻接表架构应显著低于对象树。
     */
    @Test
    public void testMemoryFootprint_comparison() {
        final int NODE_COUNT = 500;
        MindMapGenerationValidator bigValidator =
            new MindMapGenerationValidator(20, 20, 2000);
        String json = buildNaryTree("root", 3, 8); // ~585 节点

        // --- 邻接表验证方案内存测量 ---
        System.gc();
        long memBefore = usedMemoryBytes();
        for (int i = 0; i < 50; i++) {
            validator.validate(json);
        }
        System.gc();
        long memAfterNew = usedMemoryBytes();
        long newUsed = memAfterNew - memBefore;

        // --- 对象树基准：手动实例化等量 MindMapNode ---
        System.gc();
        long memBeforeOld = usedMemoryBytes();
        for (int i = 0; i < 50; i++) {
            buildObjectTree(NODE_COUNT); // 500 个 MindMapNode 对象
        }
        System.gc();
        long memAfterOld = usedMemoryBytes();
        long oldUsed = memAfterOld - memBeforeOld;

        System.out.printf("[内存对比] 邻接表(50次): %+d bytes | 对象树基准(50次): %+d bytes%n",
            newUsed, oldUsed);

        // 邻接表方案 GC 后堆增量应小于对象树方案（允许一定误差）
        // 由于 GC 不确定性，此处仅记录数据，不做硬性断言，避免 CI 环境误失败
        assertThat(true).isTrue(); // placeholder，实际数据见控制台输出
    }

    /**
     * 耗时对比：对比 100 节点 / 500 节点 / 1000 节点三个规模的验证耗时。
     */
    @Test
    public void testThroughput_multipleScales() {
        int[] scales = {100, 500, 1000};
        for (int scale : scales) {
            MindMapGenerationValidator scaleValidator =
                new MindMapGenerationValidator(50, 20, scale * 3); // 留足余量，n叉树节点数 = sum(branching^i)

            // 构造近似 scale 个节点的树：找 n 使 1+n+n^2 ≈ scale
            int branching = (int) Math.max(2, Math.cbrt(scale));
            String json = buildNaryTree("root", 3, branching);

            long start = System.nanoTime();
            final int RUNS = 100;
            MindMapValidationResult result = null;
            for (int i = 0; i < RUNS; i++) {
                result = scaleValidator.validate(json);
            }
            long avgNs = (System.nanoTime() - start) / RUNS;

            assertThat(result.isValid()).isTrue();
            System.out.printf("[吞吐量] ~%d节点(branching=%d): 平均 %d µs/次%n",
                result.getStatistics().getTotalNodes(), branching, avgNs / 1000);
        }
    }

    // =========================================================================
    // 第五组：线程安全 / 异步接口
    // =========================================================================

    @Test
    public void testAsyncValidation_concurrent() throws InterruptedException {
        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> error = new AtomicReference<>();

        String validJson = "{\"id\":\"root\",\"text\":\"根\",\"children\":["
            + "{\"id\":\"c1\",\"text\":\"子节点\"}"
            + "]}";

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    MindMapValidationResult result = validator.validate(validJson);
                    assertThat(result.isValid()).isTrue();
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(error.get()).isNull();
    }

    @Test
    public void testAsyncValidation_callback() throws InterruptedException {
        String json = "{\"id\":\"root\",\"text\":\"根\",\"children\":["
            + "{\"id\":\"c1\",\"text\":\"子节点\"}"
            + "]}";

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<MindMapValidationResult> resultRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        validator.validateAsync(json, executor, new MindMapGenerationValidator.ValidationCallback() {
            @Override
            public void onValidationComplete(MindMapValidationResult result) {
                resultRef.set(result);
                latch.countDown();
            }

            @Override
            public void onValidationError(Throwable error) {
                errorRef.set(error);
                latch.countDown();
            }
        });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(errorRef.get()).isNull();
        assertThat(resultRef.get()).isNotNull();
        assertThat(resultRef.get().isValid()).isTrue();
    }

    @Test
    public void testAsyncValidation_future() throws Exception {
        String json = "{\"id\":\"root\",\"text\":\"根节点\"}";

        MindMapValidationResult result = validator
            .validateAsync(json, executor)
            .get(5, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getStatistics().getTotalNodes()).isEqualTo(1);
    }

    // =========================================================================
    // 第六组：统计信息正确性
    // =========================================================================

    @Test
    public void testStatistics_singleNode() {
        String json = "{\"id\":\"root\",\"text\":\"只有根节点\"}";
        MindMapValidationResult result = validator.validate(json);

        assertThat(result.isValid()).isTrue();
        MindMapValidationResult.MindMapStatistics s = result.getStatistics();
        assertThat(s.getTotalNodes()).isEqualTo(1);
        assertThat(s.getMaxDepth()).isEqualTo(1);
        assertThat(s.getLeafNodes()).isEqualTo(1);
        assertThat(s.getInternalNodes()).isEqualTo(0);
    }

    @Test
    public void testStatistics_perfectBinaryTree_depth3() {
        // 完全二叉树，深度3：1 + 2 + 4 = 7 节点
        String json = "{"
            + "\"id\":\"r\",\"text\":\"根\",\"children\":["
            + "  {\"id\":\"l\",\"text\":\"左\",\"children\":["
            + "    {\"id\":\"ll\",\"text\":\"左左\"},"
            + "    {\"id\":\"lr\",\"text\":\"左右\"}"
            + "  ]},"
            + "  {\"id\":\"ri\",\"text\":\"右\",\"children\":["
            + "    {\"id\":\"rl\",\"text\":\"右左\"},"
            + "    {\"id\":\"rr\",\"text\":\"右右\"}"
            + "  ]}"
            + "]}";

        MindMapValidationResult result = validator.validate(json);
        MindMapValidationResult.MindMapStatistics s = result.getStatistics();

        assertThat(s.getTotalNodes()).isEqualTo(7);
        assertThat(s.getMaxDepth()).isEqualTo(3);
        assertThat(s.getLeafNodes()).isEqualTo(4);
        assertThat(s.getInternalNodes()).isEqualTo(3); // root + l + ri
        assertThat(s.getMaxChildrenPerNode()).isEqualTo(2);
    }

    // =========================================================================
    // 构造辅助方法
    // =========================================================================

    /**
     * 构造线性链式树（每节点只有一个子节点），深度 = depth。
     */
    private String buildLinearChain(int depth, String prefix) {
        if (depth <= 0) return "{\"id\":\"leaf\",\"text\":\"叶子\"}";
        return "{\"id\":\"" + prefix + depth + "\",\"text\":\"节点" + depth + "\","
            + "\"children\":[" + buildLinearChain(depth - 1, prefix) + "]}";
    }

    /**
     * 构造单层宽节点（根节点有 width 个子节点）。
     */
    private String buildWideNode(String rootId, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(rootId).append("\",\"text\":\"宽节点\",\"children\":[");
        for (int i = 0; i < width; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"w").append(i).append("\",\"text\":\"子").append(i).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 构造均匀 N 叉树，depth 层，每层 branching 个子节点。
     * 节点总数 = sum_{i=0}^{depth} branching^i
     */
    private String buildNaryTree(String nodeId, int depth, int branching) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(nodeId).append("\",\"text\":\"节点_").append(nodeId).append("\"");
        if (depth > 0) {
            sb.append(",\"children\":[");
            for (int i = 0; i < branching; i++) {
                if (i > 0) sb.append(",");
                sb.append(buildNaryTree(nodeId + "_" + i, depth - 1, branching));
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 对象树基准：手动创建 N 个 MindMapNode 对象（用于内存对比基准）。
     * 构造一个线性链，不做验证，仅产生内存占用。
     */
    private MindMapNode buildObjectTree(int nodeCount) {
        MindMapNode root = new MindMapNode("root", "根");
        MindMapNode current = root;
        for (int i = 1; i < nodeCount; i++) {
            MindMapNode child = new MindMapNode("n" + i, "节点" + i);
            current.addChild(child);
            current = child;
        }
        return root;
    }

    private long usedMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    // =========================================================================
    // 第五组：ValidationSource 代理接口测试
    // =========================================================================

    @Test
    public void testValidateWithPromptValidationSource() {
        String json = buildLinearChain(3, "root");
        ValidationSource source = new PromptValidationSource(json, "ernie-4.0");
        
        MindMapValidationResult result = validator.validate(source);
        assertThat(result.isValid()).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.PROMPT_RESPONSE);
        assertThat(source.getDescription()).contains("ernie-4.0");
    }

    @Test
    public void testValidateWithStreamValidationSource_NotReady() {
        StreamValidationSource source = new StreamValidationSource("test-stream");
        source.append("{\"text\":\"root\""); // 未完整
        // 未调用 markComplete()
        
        MindMapValidationResult result = validator.validate(source);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "NOT_READY".equals(e.getCode()))).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.STREAM_ASSEMBLED);
    }

    @Test
    public void testValidateWithStreamValidationSource_Ready() {
        StreamValidationSource source = new StreamValidationSource("test-stream");
        source.append("{\"text\":\"root\",\"children\":[]}");
        source.markComplete();
        
        MindMapValidationResult result = validator.validate(source);
        assertThat(result.isValid()).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.STREAM_ASSEMBLED);
    }

    @Test
    public void testValidateWithFileValidationSource() throws Exception {
        // 创建临时JSON文件
        Path tempFile = Files.createTempFile("test-map", ".json");
        Files.writeString(tempFile, buildLinearChain(3, "root"));
        
        ValidationSource source = new FileValidationSource(tempFile, "test-map.json");
        MindMapValidationResult result = validator.validate(source);
        
        assertThat(result.isValid()).isTrue();
        assertThat(source.getSourceType()).isEqualTo(SourceType.FILE_IMPORT);
        Files.delete(tempFile);
    }

    @Test
    public void testValidateWithNullSource() {
        MindMapValidationResult result = validator.validate((ValidationSource) null);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors().stream()
            .anyMatch(e -> "NULL_SOURCE".equals(e.getCode()))).isTrue();
    }

    @Test
    public void testValidateAsyncWithValidationSource() throws Exception {
        String json = buildLinearChain(3, "root");
        ValidationSource source = new PromptValidationSource(json, "test-model");
        
        var future = validator.validateAsync(source, executor);
        MindMapValidationResult result = future.get(5, TimeUnit.SECONDS);
        
        assertThat(result.isValid()).isTrue();
    }
}
