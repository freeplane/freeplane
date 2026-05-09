package org.freeplane.plugin.ai.restapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.ai.chat.AIChatPanel;
import org.freeplane.plugin.ai.maps.AvailableMaps;

import java.io.IOException;

/**
 * REST API 路由分发器。
 * 负责将所有 /api/* 路径的请求分发到对应的 Controller 方法。
 * 使用 HttpServer 的上下文注册机制，将路径前缀绑定到 HttpHandler。
 *
 * 路由表：
 *   GET  /api/map/current           → MapRestController.handleCurrentMap
 *   GET  /api/maps                  → MapRestController.handleGetAllMaps
 *   POST /api/maps/create           → MapRestController.handleCreateMap
 *   POST /api/maps/switch           → MapRestController.handleSwitchMap
 *   POST /api/maps/import           → MapRestController.handleImportMap
 *   GET  /api/maps/{id}             → MapRestController.handleGetMapById
 *   GET  /api/nodes/{id}            → MapRestController.handleGetNode
 *   POST /api/nodes/search          → NodeRestController.handleSearch
 *   POST /api/nodes/create          → NodeRestController.handleCreate
 *   POST /api/nodes/edit            → NodeRestController.handleEdit
 *   POST /api/nodes/delete          → NodeRestController.handleDelete
 *   GET  /api/ai/chat/models        → AiRestController.handleGetModels
 *   POST /api/ai/chat/message       → AiRestController.handleChat
 *   POST /api/ai/chat/stream        → AiRestController.handleChatStream
 *   POST /api/ai/chat/smart         → AiRestController.handleSmartRequest
 *   POST /api/ai/build/expand-node  → AiRestController.handleExpandNode
 *   POST /api/ai/build/summarize    → AiRestController.handleSummarize
 *   POST /api/ai/build/summarize-stream → AiRestController.handleSummarizeStream
 *   POST /api/ai/build/generate-mindmap → AiRestController.handleGenerateMindMap
 *   POST /api/ai/build/tag          → AiRestController.handleTag
 *   POST /api/ai/config/save        → AiRestController.handleSaveConfig
 */
public class RestApiRouter {

    private final MapRestController mapController;
    private final NodeRestController nodeController;
    private final AiRestController aiController;

    public RestApiRouter(AvailableMaps availableMaps, AIChatPanel aiChatPanel) {
        this.mapController = new MapRestController(availableMaps);
        this.nodeController = new NodeRestController(availableMaps);
        this.aiController = new AiRestController(availableMaps, aiChatPanel);
    }

    /**
     * 将所有路由注册到 HttpServer 实例上。
     * 每个路径前缀绑定一个 HttpHandler，内部再做二级路径分发。
     */
    public void registerAll(HttpServer server) {
        server.createContext("/api/map", buildMapHandler());
        server.createContext("/api/maps", buildMapsHandler());
        server.createContext("/api/nodes", buildNodeHandler());
        server.createContext("/api/ai", buildAiHandler());
    }

    private HttpHandler buildMapHandler() {
        return exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();

                if ("GET".equalsIgnoreCase(method) && path.equals("/api/map/current")) {
                    mapController.handleCurrentMap(exchange);
                } else if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/nodes/") && !isSpecialNodePath(path)) {
                    // /api/nodes/{nodeId} - 单节点详情，由 map context 统一处理
                    String nodeId = path.substring("/api/nodes/".length());
                    mapController.handleGetNode(exchange, nodeId);
                } else {
                    sendNotFound(exchange);
                }
            } catch (Exception e) {
                LogUtils.warn("RestApiRouter map handler error", e);
            }
        };
    }

    private HttpHandler buildMapsHandler() {
        return exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();

                if ("OPTIONS".equalsIgnoreCase(method)) {
                    CorsFilter.handlePreflight(exchange);
                    return;
                }

                if ("GET".equalsIgnoreCase(method) && path.equals("/api/maps")) {
                    // GET /api/maps - 获取所有导图列表
                    mapController.handleGetAllMaps(exchange);
                } else if ("GET".equalsIgnoreCase(method) && path.matches("/api/maps/[^/]+")) {
                    // GET /api/maps/{mapId} - 获取指定导图
                    String mapId = path.substring("/api/maps/".length());
                    mapController.handleGetMapById(exchange, mapId);
                } else if ("POST".equalsIgnoreCase(method)) {
                    switch (path) {
                        case "/api/maps/create":
                            mapController.handleCreateMap(exchange);
                            break;
                        case "/api/maps/switch":
                            mapController.handleSwitchMap(exchange);
                            break;
                        case "/api/maps/import":
                            mapController.handleImportMap(exchange);
                            break;
                        default:
                            sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
            } catch (Exception e) {
                LogUtils.warn("RestApiRouter maps handler error", e);
            }
        };
    }

    private HttpHandler buildNodeHandler() {
        return exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();

                if ("OPTIONS".equalsIgnoreCase(method)) {
                    CorsFilter.handlePreflight(exchange);
                    return;
                }

                if ("GET".equalsIgnoreCase(method) && path.matches("/api/nodes/[^/]+")) {
                    // GET /api/nodes/{nodeId}
                    String nodeId = path.substring("/api/nodes/".length());
                    mapController.handleGetNode(exchange, nodeId);
                } else if ("POST".equalsIgnoreCase(method)) {
                    switch (path) {
                        case "/api/nodes/search":
                            nodeController.handleSearch(exchange);
                            break;
                        case "/api/nodes/create":
                            nodeController.handleCreate(exchange);
                            break;
                        case "/api/nodes/edit":
                            nodeController.handleEdit(exchange);
                            break;
                        case "/api/nodes/delete":
                            nodeController.handleDelete(exchange);
                            break;
                        case "/api/nodes/toggle-fold":
                            nodeController.handleToggleFold(exchange);
                            break;
                        default:
                            sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
            } catch (Exception e) {
                LogUtils.warn("RestApiRouter node handler error", e);
            }
        };
    }

    private HttpHandler buildAiHandler() {
        return exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();

                if ("OPTIONS".equalsIgnoreCase(method)) {
                    CorsFilter.handlePreflight(exchange);
                    return;
                }

                if ("GET".equalsIgnoreCase(method) && "/api/ai/chat/models".equals(path)) {
                    aiController.handleGetModels(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    switch (path) {
                        case "/api/ai/chat/message":
                            aiController.handleChat(exchange);
                            break;
                        case "/api/ai/chat/stream":
                            aiController.handleChatStream(exchange);
                            break;
                        case "/api/ai/chat/smart":
                            aiController.handleSmartRequest(exchange);
                            break;
                        case "/api/ai/build/generate-mindmap":
                            aiController.handleGenerateMindMap(exchange);
                            break;
                        case "/api/ai/build/expand-node":
                            aiController.handleExpandNode(exchange);
                            break;
                        case "/api/ai/build/summarize":
                            aiController.handleSummarize(exchange);
                            break;
                        case "/api/ai/build/summarize-stream":
                            aiController.handleSummarizeStream(exchange);
                            break;
                        case "/api/ai/build/tag":
                            aiController.handleTag(exchange);
                            break;
                        case "/api/ai/config/save":
                            aiController.handleSaveConfig(exchange);
                            break;
                        default:
                            sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
            } catch (Exception e) {
                LogUtils.warn("RestApiRouter ai handler error", e);
            }
        };
    }

    private boolean isSpecialNodePath(String path) {
        return path.equals("/api/nodes/search")
            || path.equals("/api/nodes/create")
            || path.equals("/api/nodes/edit")
            || path.equals("/api/nodes/delete")
            || path.equals("/api/nodes/toggle-fold");
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        byte[] body = "{\"error\":\"Not found\"}".getBytes();
        exchange.sendResponseHeaders(404, body.length);
        exchange.getResponseBody().write(body);
        exchange.getResponseBody().close();
    }
}
