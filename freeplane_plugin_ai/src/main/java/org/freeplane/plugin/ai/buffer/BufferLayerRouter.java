package org.freeplane.plugin.ai.buffer;

import org.freeplane.core.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓冲层路由器。
 * 负责识别功能类型，选择合适的缓冲层进行处理。
 * 使用 LinkedHashMap access-order 模式实现真正的 LRU 缓存：
 *   - get/put 均为 O(1)
 *   - 每次访问自动将条目移至链表尾部
 *   - 超限时自动驱逐链表头部（最久未访问）条目
 *   - Collections.synchronizedMap 保证线程安全
 */
public class BufferLayerRouter {

    private final List<IBufferLayer> bufferLayers;

    // 真·LRU 缓存：LinkedHashMap access-order 模式
    private final Map<String, CachedResponse> cache;
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long CACHE_EXPIRY_TIME = TimeUnit.MINUTES.toMillis(10);

    // 缓存统计
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictCount = new AtomicLong(0);

    public BufferLayerRouter() {
        this.bufferLayers = new ArrayList<>();
        // access-order=true：每次 get/put 都将条目移到链表尾部，头部为最久未访问
        this.cache = Collections.synchronizedMap(
            new LinkedHashMap<String, CachedResponse>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CachedResponse> eldest) {
                    boolean shouldEvict = size() > MAX_CACHE_SIZE;
                    if (shouldEvict) {
                        evictCount.incrementAndGet();
                        LogUtils.info("BufferLayerRouter [LRU]: evicted eldest entry - " + eldest.getKey());
                    }
                    return shouldEvict;
                }
            }
        );
        initializeBufferLayers();
    }

    /**
     * 初始化缓冲层列表
     * 使用 ServiceLoader 自动发现所有 IBufferLayer 实现
     */
    private void initializeBufferLayers() {
        // 通过 ServiceLoader 自动发现
        ServiceLoader<IBufferLayer> loader = ServiceLoader.load(IBufferLayer.class);
        for (IBufferLayer layer : loader) {
            bufferLayers.add(layer);
            LogUtils.info("BufferLayerRouter: loaded buffer layer - " + layer.getName());
        }

        // 如果没有通过 ServiceLoader 找到，手动注册默认实现
        if (bufferLayers.isEmpty()) {
            registerDefaultBufferLayers();
        }

        // 按优先级排序
        bufferLayers.sort(Comparator.comparingInt(IBufferLayer::getPriority));
    }

    /**
     * 手动注册默认缓冲层
     */
    private void registerDefaultBufferLayers() {
        try {
            // 注册思维导图缓冲层
            Class<?> clazz = Class.forName("org.freeplane.plugin.ai.buffer.mindmap.MindMapBufferLayer");
            IBufferLayer mindMapLayer = (IBufferLayer) clazz.getDeclaredConstructor().newInstance();
            bufferLayers.add(mindMapLayer);
            LogUtils.info("BufferLayerRouter: registered MindMapBufferLayer");
        } catch (Exception e) {
            LogUtils.warn("BufferLayerRouter: failed to register MindMapBufferLayer", e);
        }
    }

    /**
     * 处理请求
     * @param request 请求上下文
     * @return 处理结果
     */
    public BufferResponse processRequest(BufferRequest request) {
        long startTime = System.currentTimeMillis();

        // 尝试从缓存获取（LinkedHashMap access-order：get 自动将条目移至尾部，维持 LRU 顺序）
        String cacheKey = generateCacheKey(request);
        CachedResponse cachedResponse;
        synchronized (cache) {
            cachedResponse = cache.get(cacheKey);
        }
        if (cachedResponse != null) {
            if (!isCacheExpired(cachedResponse)) {
                hitCount.incrementAndGet();
                LogUtils.info("BufferLayerRouter [LRU]: cache hit for key - " + cacheKey
                    + " | hits=" + hitCount.get() + " misses=" + missCount.get());
                BufferResponse response = cachedResponse.getResponse();
                response.setProcessingTime(System.currentTimeMillis() - startTime);
                response.addLog("[LRU] Cache hit: " + cacheKey);
                return response;
            } else {
                // 惰性删除：命中但已过期，从缓存中移除
                synchronized (cache) {
                    cache.remove(cacheKey);
                }
                LogUtils.info("BufferLayerRouter [LRU]: expired entry removed - " + cacheKey);
            }
        }

        // 缓存未命中，正常处理
        missCount.incrementAndGet();
        LogUtils.info("BufferLayerRouter [LRU]: cache miss for key - " + cacheKey
            + " | hits=" + hitCount.get() + " misses=" + missCount.get());

        // 找到能处理该请求的缓冲层
        IBufferLayer selectedLayer = selectBufferLayer(request);
        if (selectedLayer == null) {
            BufferResponse response = new BufferResponse();
            response.setSuccess(false);
            response.setErrorMessage("未找到合适的缓冲层处理该请求");
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;
        }

        LogUtils.info("BufferLayerRouter: selected layer - " + selectedLayer.getName());

        try {
            // 委托给选中的缓冲层处理
            BufferResponse response = selectedLayer.process(request);
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            // 缓存成功的响应
            if (response.isSuccess()) {
                cacheResponse(cacheKey, response);
                response.addLog("[LRU] Cache stored: " + cacheKey);
            }
            
            return response;
        } catch (Exception e) {
            LogUtils.warn("BufferLayerRouter: processing failed", e);
            BufferResponse response = new BufferResponse();
            response.setSuccess(false);
            response.setErrorMessage("处理失败: " + e.getMessage());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    /**
     * 选择合适的缓冲层
     */
    private IBufferLayer selectBufferLayer(BufferRequest request) {
        for (IBufferLayer layer : bufferLayers) {
            if (layer.canHandle(request)) {
                return layer;
            }
        }
        return null;
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(BufferRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(request.getRequestType())
                 .append("|")
                 .append(request.getUserInput() != null ? request.getUserInput() : "");
        
        // 添加参数信息
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            keyBuilder.append("|");
            request.getParameters().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    keyBuilder.append(entry.getKey())
                             .append("=")
                             .append(entry.getValue())
                             .append("&");
                });
        }
        
        return keyBuilder.toString();
    }

    /**
     * 缓存响应。
     * LinkedHashMap removeEldestEntry 在 put 时自动触发驱逐，无需手动检查大小。
     */
    private void cacheResponse(String key, BufferResponse response) {
        synchronized (cache) {
            cache.put(key, new CachedResponse(response));
        }
        LogUtils.info("BufferLayerRouter [LRU]: cached response for key - " + key
            + " | cacheSize=" + cache.size());
    }

    /**
     * 检查缓存条目是否已超过 TTL 过期时间
     */
    private boolean isCacheExpired(CachedResponse cachedResponse) {
        return System.currentTimeMillis() - cachedResponse.getTimestamp() > CACHE_EXPIRY_TIME;
    }

    /**
     * 清除缓存并重置统计计数
     */
    public void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
        hitCount.set(0);
        missCount.set(0);
        evictCount.set(0);
        LogUtils.info("BufferLayerRouter [LRU]: cache cleared");
    }

    /**
     * 获取缓存当前条目数
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * 获取缓存命中次数
     */
    public long getCacheHitCount() {
        return hitCount.get();
    }

    /**
     * 获取缓存未命中次数
     */
    public long getCacheMissCount() {
        return missCount.get();
    }

    /**
     * 获取 LRU 驱逐次数
     */
    public long getCacheEvictCount() {
        return evictCount.get();
    }

    /**
     * 获取缓存命中率（0.0 ~ 1.0）
     */
    public double getCacheHitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total;
    }

    /**
     * 获取所有已注册的缓冲层
     */
    public List<IBufferLayer> getBufferLayers() {
        return new ArrayList<>(bufferLayers);
    }

    /**
     * 缓存条目包装类，记录写入时间戳用于 TTL 过期判断
     */
    private static class CachedResponse {
        private final BufferResponse response;
        private final long timestamp;

        public CachedResponse(BufferResponse response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        public BufferResponse getResponse() {
            return response;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}