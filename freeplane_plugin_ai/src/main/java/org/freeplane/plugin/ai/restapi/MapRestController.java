package org.freeplane.plugin.ai.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.plugin.ai.maps.AvailableMaps;
import org.freeplane.plugin.ai.validation.MindMapGenerationValidator;
import org.freeplane.plugin.ai.validation.MindMapValidationResult;
import org.freeplane.plugin.ai.validation.source.FileValidationSource;
import org.freeplane.plugin.ai.validation.source.ValidationSource;

import javax.swing.SwingUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 导图数据接口控制器。
 * 负责处理 /api/map/* 路径下的所有请求。
 * 主要将 Freeplane 内存中的 MapModel/NodeModel 对象序列化为 JSON 返回给前端。
 */
public class MapRestController {

    private final AvailableMaps availableMaps;
    private final ObjectMapper objectMapper;

    public MapRestController(AvailableMaps availableMaps) {
        this.availableMaps = availableMaps;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * GET /api/map/current
     * 返回当前 Freeplane 中打开的导图节点树（JSON 格式）。
     */
    public void handleCurrentMap(HttpExchange exchange) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            MapModel mapModel = availableMaps.getCurrentMapModel();
            if (mapModel == null) {
                sendError(exchange, 404, "No map is currently open in Freeplane");
                return;
            }

            UUID mapId = availableMaps.getCurrentMapIdentifier();
            NodeModel rootNode = mapModel.getRootNode();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("mapId", mapId != null ? mapId.toString() : "unknown");
            result.put("title", rootNode != null ? rootNode.getText() : "");
            result.put("root", rootNode != null ? serializeNode(rootNode) : null);

            sendJson(exchange, 200, result);
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleCurrentMap error", e);
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * GET /api/maps
     * 返回所有已打开的导图列表（不含节点树，仅元数据）。
     */
    public void handleGetAllMaps(HttpExchange exchange) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            List<UUID> mapIds = availableMaps.getAvailableMapIdentifiers();
            List<Map<String, Object>> maps = new ArrayList<>();

            for (UUID mapId : mapIds) {
                MapModel mapModel = availableMaps.findMapModel(mapId);
                if (mapModel != null) {
                    NodeModel rootNode = mapModel.getRootNode();
                    Map<String, Object> mapInfo = new LinkedHashMap<>();
                    mapInfo.put("mapId", mapId.toString());
                    mapInfo.put("title", rootNode != null ? rootNode.getText() : "Untitled");
                    mapInfo.put("isCurrent", mapId.equals(availableMaps.getCurrentMapIdentifier()));
                    maps.add(mapInfo);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("maps", maps);
            result.put("count", maps.size());

            sendJson(exchange, 200, result);
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleGetAllMaps error", e);
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * POST /api/maps/create
     * 创建新导图（空导图，带默认根节点）。
     * 请求体可选：{ "title": "New Map" }
     */
    public void handleCreateMap(HttpExchange exchange) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            Controller controller = Controller.getCurrentController();
            if (controller == null) {
                sendError(exchange, 500, "Freeplane controller not available");
                return;
            }

            ModeController modeController = controller.getModeController(MModeController.MODENAME);
            if (modeController == null) {
                sendError(exchange, 500, "Mindmap mode not available");
                return;
            }

            MapController mapController = modeController.getMapController();
            MapModel newMap = mapController.newMap();

            // 解析请求体获取自定义标题（可选）
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body != null && !body.isEmpty()) {
                try {
                    Map<String, String> request = objectMapper.readValue(body, Map.class);
                    String title = request.get("title");
                    if (title != null && !title.isEmpty()) {
                        NodeModel rootNode = newMap.getRootNode();
                        if (rootNode != null) {
                            rootNode.setText(title);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.warn("Failed to parse create map request body", e);
                }
            }

            UUID mapId = availableMaps.getOrCreateMapIdentifier(newMap);
            NodeModel rootNode = newMap.getRootNode();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("mapId", mapId.toString());
            result.put("title", rootNode != null ? rootNode.getText() : "Untitled");
            result.put("success", true);

            sendJson(exchange, 201, result);
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleCreateMap error", e);
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * POST /api/maps/switch
     * 切换到指定导图。
     * 请求体：{ "mapId": "uuid-string" }
     */
    public void handleSwitchMap(HttpExchange exchange) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> request = objectMapper.readValue(body, Map.class);
            String mapIdStr = request.get("mapId");

            if (mapIdStr == null || mapIdStr.isEmpty()) {
                sendError(exchange, 400, "mapId is required");
                return;
            }

            UUID mapId;
            try {
                mapId = UUID.fromString(mapIdStr);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, "Invalid mapId format");
                return;
            }

            MapModel targetMap = availableMaps.findMapModel(mapId);
            if (targetMap == null) {
                sendError(exchange, 404, "Map not found: " + mapIdStr);
                return;
            }

            Controller controller = Controller.getCurrentController();
            if (controller == null) {
                sendError(exchange, 500, "Freeplane controller not available");
                return;
            }

            IMapViewManager mapViewManager = controller.getMapViewManager();
            if (mapViewManager == null) {
                sendError(exchange, 500, "Map view manager not available");
                return;
            }

            mapViewManager.changeToMap(targetMap);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("mapId", mapId.toString());
            result.put("title", targetMap.getRootNode() != null ? targetMap.getRootNode().getText() : "Untitled");
            result.put("success", true);

            sendJson(exchange, 200, result);
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleSwitchMap error", e);
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * POST /api/maps/import
     * 从前端传入的 .mm XML 内容字符串导入思维导图并切换到该导图。
     * 请求体：{ "content": "<map>...", "filename": "example.mm" }
     */
    public void handleImportMap(HttpExchange exchange) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> request = objectMapper.readValue(body, Map.class);

            String content = (String) request.get("content");
            String filename = request.containsKey("filename") ? (String) request.get("filename") : "imported.mm";

            if (content == null || content.trim().isEmpty()) {
                sendError(exchange, 400, "content is required");
                return;
            }

            Controller controller = Controller.getCurrentController();
            if (controller == null) {
                sendError(exchange, 500, "Freeplane controller not available");
                return;
            }

            ModeController modeController = controller.getModeController(MModeController.MODENAME);
            if (!(modeController instanceof MModeController)) {
                sendError(exchange, 500, "Mindmap mode not available");
                return;
            }
            MModeController mmodeController = (MModeController) modeController;
            MMapController mapController = (MMapController) mmodeController.getMapController();

            // 步骤0:前置环检测拦截(避免解析无效XML)
            MindMapGenerationValidator validator = new MindMapGenerationValidator();
            ValidationSource source = new FileValidationSource(content, filename);
            MindMapValidationResult preValidation = validator.validate(source);

            if (preValidation.getErrors().stream()
                .anyMatch(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()))) {
                LogUtils.warn("MapRestController: import rejected due to circular dependency in " + filename);
                sendError(exchange, 400, 
                    "导入失败: 检测到循环依赖 - " + 
                    preValidation.getErrors().stream()
                        .filter(e -> "CIRCULAR_DEPENDENCY".equals(e.getCode()))
                        .findFirst().map(e -> e.getMessage()).orElse("未知环"));
                return;
            }

            // 步骤1：在当前线程解析 XML（createNodeTreeFromXml 内部 synchronized，线程安全）
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
            final MMapModel[] mapHolder = {null};
            final Exception[] errorHolder = {null};

            try {
                MMapModel parsedMap = new MMapModel(mapController.duplicator());
                try (java.io.InputStreamReader reader = new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    mapController.getMapReader().createNodeTreeFromXml(
                        parsedMap,
                        reader,
                        MapWriter.Mode.FILE
                    );
                }
                if (parsedMap.getRootNode() == null) {
                    parsedMap.createNewRoot();
                }
                mapHolder[0] = parsedMap;
            } catch (Exception e) {
                errorHolder[0] = e;
            }

            if (errorHolder[0] != null) {
                sendError(exchange, 500, "Failed to parse map content: " + errorHolder[0].getMessage());
                return;
            }

            final MMapModel newMap = mapHolder[0];

            // 步骤2：在 EDT 上执行 UI 注册（fireMapCreated / addLoadedMap / createMapView 需在 EDT）
            try {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        mapController.fireMapCreated(newMap);
                        mapController.addLoadedMap(newMap);
                        mapController.createMapView(newMap);
                    } catch (Exception e) {
                        errorHolder[0] = e;
                    }
                });
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to create map view: " + e.getMessage());
                return;
            }

            if (errorHolder[0] != null) {
                sendError(exchange, 500, "Failed to register map: " + errorHolder[0].getMessage());
                return;
            }

            // 设置标题：优先使用根节点文本，备选扩展名作为 fallback
            NodeModel rootNode = newMap.getRootNode();
            String title = (rootNode != null && !rootNode.getText().isEmpty())
                ? rootNode.getText()
                : filename.replaceAll("\\.mm$", "");

            UUID mapId = availableMaps.getOrCreateMapIdentifier(newMap);

            // 步骤3：切换视图到新导入的导图
            IMapViewManager mapViewManager = controller.getMapViewManager();
            if (mapViewManager != null) {
                SwingUtilities.invokeLater(() -> mapViewManager.changeToMap(newMap));
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("mapId", mapId.toString());
            result.put("title", title);
            result.put("filename", filename);

            sendJson(exchange, 200, result);
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleImportMap error", e);
            sendError(exchange, 500, "Import failed: " + e.getMessage());
        }
    }

    /**
     * GET /api/maps/{mapId}
     * 返回指定导图的完整节点树。
     */
    public void handleGetMapById(HttpExchange exchange, String mapId) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            UUID mapUuid;
            try {
                mapUuid = UUID.fromString(mapId);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, "Invalid mapId format");
                return;
            }

            MapModel mapModel = availableMaps.findMapModel(mapUuid);
            if (mapModel == null) {
                sendError(exchange, 404, "Map not found: " + mapId);
                return;
            }

            NodeModel rootNode = mapModel.getRootNode();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("mapId", mapId);
            result.put("title", rootNode != null ? rootNode.getText() : "");
            result.put("root", rootNode != null ? serializeNode(rootNode) : null);

            sendJson(exchange, 200, result);
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleGetMapById error", e);
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * GET /api/nodes/{nodeId}
     * 返回单个节点的详细信息（含属性、备注、子节点列表）。
     */
    public void handleGetNode(HttpExchange exchange, String nodeId) throws IOException {
        CorsFilter.addCorsHeaders(exchange);
        if (CorsFilter.handlePreflight(exchange)) return;

        try {
            MapModel mapModel = availableMaps.getCurrentMapModel();
            if (mapModel == null) {
                sendError(exchange, 404, "No map is currently open");
                return;
            }

            NodeModel node = mapModel.getNodeForID(nodeId);
            if (node == null) {
                sendError(exchange, 404, "Node not found: " + nodeId);
                return;
            }

            sendJson(exchange, 200, serializeNodeDetail(node));
        } catch (Exception e) {
            LogUtils.warn("MapRestController.handleGetNode error", e);
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * 将节点递归序列化为 Map（用于 JSON 输出），包含完整子节点树。
     * 字段与前端 types/mindmap.ts 中 MindMapNode 接口完全对应。
     */
    private Map<String, Object> serializeNode(NodeModel node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", node.getID());
        map.put("text", node.getText());
        map.put("parentId", node.getParentNode() != null ? node.getParentNode().getID() : null);
        map.put("folded", node.isFolded());
        map.put("note", ""); // Note 字段需要 NoteModel 扩展，此处简化

        List<Map<String, Object>> children = new ArrayList<>();
        for (NodeModel child : node.getChildren()) {
            children.add(serializeNode(child));
        }
        map.put("children", children);
        return map;
    }

    /**
     * 序列化节点详情（含属性键值对）。
     */
    private Map<String, Object> serializeNodeDetail(NodeModel node) {
        Map<String, Object> map = serializeNode(node);
        // 属性字段（attributes）暂返回空数组，后续可扩展读取 NodeAttributeTableModel
        map.put("attributes", new ArrayList<>());
        return map;
    }

    // ──────────────────────────────────────────────
    // 通用工具方法
    // ──────────────────────────────────────────────

    void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", message);
        sendJson(exchange, statusCode, error);
    }
}
