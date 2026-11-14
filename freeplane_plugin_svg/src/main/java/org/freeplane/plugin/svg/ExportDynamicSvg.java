// SPDX-License-Identifier: GPL-2.0-or-later
// Adapted from shlomiv/freeplane-interactive-html; see dynamic-svg-NOTICE.txt.
package org.freeplane.plugin.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.batik.svggen.SVGGraphics2D;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.AlwaysUnfoldedNode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewExportCapture;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.ConnectorView;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

final class ExportDynamicSvg extends ExportVectorGraphic {
    @Override
    public void export(List<NodeModel> branches, File chosenFile) {
        Controller controller = Controller.getCurrentController();
        if (!ExportController.getContoller().checkCurrentMap(branches.get(0).getMap()))
            return;
        MapView view = (MapView) controller.getMapViewManager().getMapViewComponent();
        if (view == null)
            return;
        try {
            controller.getViewController().setWaitingCursor(true);
            DynamicSvgDocument document = new MapViewExportCapture(view).capture(this::capture);
            boolean embedsFonts = ResourceController.getResourceController().getBooleanProperty(SVG_EMBED_FONTS);
            DynamicSvgFonts.Embedding embedding = new DynamicSvgFonts().embed(document.textByFont, embedsFonts);
            document.write(chosenFile.toPath(), embedding);
            if (!embedding.warnings.isEmpty())
                UITools.informationMessage(view,
                        TextUtils.format("dynamic_svg_font_embedding_incomplete", String.join("\n", embedding.warnings)),
                        TextUtils.getText("export_dynamic_svg_text"), JOptionPane.WARNING_MESSAGE);
        } catch (Exception failure) {
            UITools.errorMessage(TextUtils.format("dynamic_svg_export_failed", failure.getLocalizedMessage()));
        } finally {
            controller.getViewController().setWaitingCursor(false);
        }
    }

    DynamicSvgDocument capture(MapViewExportCapture.ExportContext context) {
        MapView view = context.root.getMap();
        List<NodeView> nodes = new ArrayList<>(context.nodes);
        Map<NodeModel, String> ids = exportIds(nodes);
        Map<NodeModel, NodeView> content = new IdentityHashMap<>();
        for (NodeView node : nodes)
            if (node.isContentVisible())
                content.put(node.getNode(), node);
        JsonArray summaries = captureSummaries(context, nodes, ids);
        Set<String> requiredMarkers = new HashSet<>();
        summaries.forEach(summary -> requiredMarkers.add(summary.getAsJsonObject().get("startMarkerId").getAsString()));
        nodes.removeIf(node -> node != context.root && node.isFirstGroupNode() && !node.isContentVisible()
                && node.getChildrenViews().isEmpty() && !requiredMarkers.contains(ids.get(node.getNode())));
        List<ConnectorModel> connectors = connectors(view, nodes, content, ids);
        Map<java.awt.Font, String> textByFont = new LinkedHashMap<>();
        JsonArray records = new JsonArray();
        TextController text = TextController.getController(view.getModeController());
        for (NodeView node : nodes) {
            NodeModel model = node.getNode();
            JsonObject record = new JsonObject();
            record.addProperty("id", ids.get(model));
            record.add("parentId", node == context.root ? JsonNull.INSTANCE
                    : new com.google.gson.JsonPrimitive(ids.get(model.getParentNode())));
            boolean visible = content.containsKey(model);
            String label = visible ? plain(text, model) : "";
            record.addProperty("label", label);
            String details = visible ? DetailModel.getDetailText(model) : null;
            record.addProperty("searchText", visible ? label + (details == null ? "" : "\n" + HtmlUtils.htmlToPlain(details)) : "");
            int width = visible ? node.getContent().getWidth() + 6 : 0;
            int height = visible ? node.getContent().getHeight() + 6 : 0;
            if (visible && (width <= 6 || height <= 6))
                throw new IllegalStateException("Native node artwork has no layout: " + ids.get(model));
            record.addProperty("width", width);
            record.addProperty("height", height);
            record.addProperty("artwork", visible ? artwork(context, node, width, height, textByFont) : "");
            record.addProperty("topOrLeft", node.isTopOrLeft());
            record.addProperty("firstGroup", node.isFirstGroupNode());
            record.addProperty("summary", node.isSummary());
            record.addProperty("alwaysUnfolded", AlwaysUnfoldedNode.isAlwaysUnfolded(model));
            record.addProperty("collapsed", context.initialFolding.get(model));
            record.addProperty("contentVisible", visible);
            record.addProperty("linkHref", visible ? NodeLinks.getLinkAsString(model) : null);
            records.add(record);
        }
        JsonArray links = new JsonArray();
        for (ConnectorModel connector : connectors)
            links.add(captureConnector(view, connector, content, ids));
        JsonObject manifest = new JsonObject();
        manifest.addProperty("rootId", ids.get(context.root.getNode()));
        manifest.add("nodes", records);
        manifest.add("links", links);
        manifest.add("summaries", summaries);
        manifest.add("fontFaces", new JsonArray());
        String title = content.containsKey(context.root.getNode()) ? plain(text, context.root.getNode()) : "";
        return new DynamicSvgDocument(title.isEmpty() ? "Mind map" : title, manifest, textByFont);
    }

    private String plain(TextController controller, NodeModel node) {
        String value = controller.getPlainTransformedTextWithoutNodeNumber(node);
        return value == null ? "" : value;
    }

    private Map<NodeModel, String> exportIds(List<NodeView> nodes) {
        Map<NodeModel, String> ids = new IdentityHashMap<>();
        Set<String> used = new HashSet<>();
        for (NodeView view : nodes) {
            String id = view.getNode().getID();
            if (id != null && !id.isEmpty()) {
                if (!used.add(id))
                    throw new IllegalStateException("Duplicate exported node ID: " + id);
                ids.put(view.getNode(), id);
            }
        }
        int index = 0;
        for (NodeView view : nodes)
            if (!ids.containsKey(view.getNode())) {
                String id;
                do { id = "export-node-" + index++; } while (!used.add(id));
                ids.put(view.getNode(), id);
            }
        return ids;
    }

    private String artwork(MapViewExportCapture.ExportContext context, NodeView node, int width, int height,
            Map<java.awt.Font, String> textByFont) {
        SVGGraphics2D graphics = createSVGGraphics2D(new Dimension(width, height),
                (font, run) -> textByFont.merge(font, run, String::concat));
        try {
            graphics.setClip(0, 0, width, height);
            graphics.translate(3, 3);
            graphics.setFont(node.getMainView().getFont());
            graphics.setColor(node.getMainView().getForeground());
            context.paintNode(node, graphics);
            StringWriter writer = new StringWriter();
            graphics.stream(writer, true);
            return writer.toString();
        } catch (org.apache.batik.svggen.SVGGraphics2DIOException failure) {
            throw new IllegalStateException("Could not capture native node artwork", failure);
        } finally {
            graphics.dispose();
        }
    }

    private List<ConnectorModel> connectors(MapView view, List<NodeView> nodes,
            Map<NodeModel, NodeView> content, Map<NodeModel, String> ids) {
        List<ConnectorModel> result = new ArrayList<>();
        LinkController links = LinkController.getController(view.getModeController());
        Filter filter = view.getFilter();
        for (NodeView node : nodes)
            for (NodeLinkModel link : NodeLinks.getLinks(node.getNode())) {
                if (!(link instanceof ConnectorModel))
                    continue;
                ConnectorModel connector = (ConnectorModel) link;
                if (!content.containsKey(connector.getSource()) || !content.containsKey(connector.getTarget())
                        || filter != null && !filter.isVisible(connector))
                    continue;
                ConnectorShape shape = links.getShape(connector);
                if (shape != ConnectorShape.CUBIC_CURVE)
                    throw new IllegalArgumentException(TextUtils.format("dynamic_svg_unsupported_connector",
                            ids.get(connector.getSource()), ids.get(connector.getTarget()), shape));
                result.add(connector);
            }
        return result;
    }

    private JsonObject captureConnector(MapView view, ConnectorModel model,
            Map<NodeModel, NodeView> content, Map<NodeModel, String> ids) {
        NodeView source = content.get(model.getSource()), target = content.get(model.getTarget());
        ConnectorView nativeView = new ConnectorView(model, source, target, view.getBackground(), true);
        Graphics2D graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            nativeView.paint(graphics);
        } finally {
            graphics.dispose();
        }
        ConnectorView.ExportGeometry geometry = nativeView.getExportGeometry();
        CubicCurve2D curve = geometry.curve;
        Point sourceOrigin = SwingUtilities.convertPoint(source.getContent(), 0, 0, view);
        Point targetOrigin = SwingUtilities.convertPoint(target.getContent(), 0, 0, view);
        JsonObject captured = new JsonObject();
        captured.addProperty("zoom", 1);
        captured.add("sourceAnchor", point(curve.getX1() - sourceOrigin.x + 3, curve.getY1() - sourceOrigin.y + 3));
        captured.add("targetAnchor", point(curve.getX2() - targetOrigin.x + 3, curve.getY2() - targetOrigin.y + 3));
        captured.add("sourceControl", point(curve.getCtrlX1() - curve.getX1(), curve.getCtrlY1() - curve.getY1()));
        captured.add("targetControl", point(curve.getCtrlX2() - curve.getX2(), curve.getCtrlY2() - curve.getY2()));
        LinkController links = LinkController.getController(view.getModeController());
        ConnectorArrows arrows = links.getArrows(model);
        List<String> ends = new ArrayList<>();
        if (arrows.start != ArrowType.NONE) ends.add("source");
        if (arrows.end != ArrowType.NONE) ends.add("target");
        if (ends.size() != geometry.arrows.size())
            throw new IllegalStateException("Incomplete native connector arrows: " + ids.get(model.getSource()));
        JsonArray polygons = new JsonArray();
        for (int index = 0; index < ends.size(); index++) {
            boolean atSource = ends.get(index).equals("source");
            Polygon polygon = geometry.arrows.get(index);
            JsonArray points = new JsonArray();
            for (int vertex = 0; vertex < polygon.npoints; vertex++)
                points.add(point(polygon.xpoints[vertex] - (atSource ? curve.getX1() : curve.getX2()),
                        polygon.ypoints[vertex] - (atSource ? curve.getY1() : curve.getY2())));
            JsonObject arrow = new JsonObject();
            arrow.addProperty("end", ends.get(index));
            arrow.add("points", points);
            polygons.add(arrow);
        }
        captured.add("arrowPolygons", polygons);
        BasicStroke stroke = geometry.stroke;
        captured.addProperty("strokeWidth", stroke.getLineWidth());
        captured.addProperty("strokeCap", stroke.getEndCap());
        captured.addProperty("strokeJoin", stroke.getLineJoin());
        JsonArray dash = new JsonArray();
        if (stroke.getDashArray() != null)
            for (float length : stroke.getDashArray()) dash.add(length);
        captured.add("strokeDash", dash);
        JsonObject resolved = new JsonObject();
        Color color = links.getColor(model);
        resolved.addProperty("color", String.format(java.util.Locale.ROOT, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
        resolved.addProperty("alpha", links.getOpacity(model));
        resolved.addProperty("shape", ConnectorShape.CUBIC_CURVE.name());
        JsonObject result = new JsonObject();
        result.addProperty("sourceId", ids.get(model.getSource()));
        result.addProperty("targetId", ids.get(model.getTarget()));
        result.add("resolved", resolved);
        result.add("geometry", captured);
        return result;
    }

    private JsonObject point(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y))
            throw new IllegalStateException("Non-finite native connector geometry");
        JsonObject point = new JsonObject();
        point.addProperty("x", x);
        point.addProperty("y", y);
        return point;
    }

    private JsonArray captureSummaries(MapViewExportCapture.ExportContext context, List<NodeView> nodes, Map<NodeModel, String> ids) {
        JsonArray result = new JsonArray();
        NodeModel root = context.root.getNode();
        for (NodeView parent : nodes) {
            Map<Boolean, List<Integer>> starts = new HashMap<>();
            starts.put(false, new ArrayList<>());
            starts.put(true, new ArrayList<>());
            List<NodeModel> children = parent.getNode().getChildren();
            for (int index = 0; index < children.size(); index++) {
                NodeModel child = children.get(index);
                List<Integer> sideStarts = starts.get(child.isTopOrLeft(root));
                if (SummaryNode.isFirstGroupNode(child)) sideStarts.add(index);
                if (!SummaryNode.isSummaryNode(child)) continue;
                Integer start = sideStarts.isEmpty() ? null : sideStarts.remove(sideStarts.size() - 1);
                if (!ids.containsKey(child)) continue;
                if (start == null || !sideStarts.isEmpty() || SummaryNode.getSummaryLevel(root, child) > 1)
                    throw unsupportedSummary(ids.get(child));
                for (NodeModel ancestor = parent.getNode(); ancestor != root; ancestor = ancestor.getParentNode())
                    if (SummaryNode.isSummaryNode(ancestor)) throw unsupportedSummary(ids.get(child));
                JsonArray members = new JsonArray();
                for (NodeModel member : children.subList(start + 1, index))
                    if (ids.containsKey(member) && member.isTopOrLeft(root) == child.isTopOrLeft(root)
                            && !SummaryNode.isFirstGroupNode(member) && !SummaryNode.isSummaryNode(member))
                        members.add(ids.get(member));
                JsonArray labels = new JsonArray();
                child.getChildren().stream().filter(ids::containsKey).forEach(label -> labels.add(ids.get(label)));
                if (members.isEmpty() || labels.isEmpty()) continue;
                NodeModel marker = children.get(start);
                if (!ids.containsKey(marker)) throw unsupportedSummary(ids.get(child));
                JsonObject summary = new JsonObject();
                summary.addProperty("markerId", ids.get(child));
                summary.addProperty("startMarkerId", ids.get(marker));
                summary.addProperty("parentId", ids.get(parent.getNode()));
                summary.add("memberIds", members);
                summary.add("summaryChildIds", labels);
                result.add(summary);
            }
            for (Map.Entry<Boolean, List<Integer>> side : starts.entrySet())
                for (int start : side.getValue())
                    if (children.subList(start + 1, children.size()).stream().anyMatch(child -> ids.containsKey(child)
                            && child.isTopOrLeft(root) == side.getKey() && !SummaryNode.isFirstGroupNode(child)))
                        throw unsupportedSummary(ids.get(parent.getNode()));
        }
        return result;
    }

    private IllegalArgumentException unsupportedSummary(String id) {
        return new IllegalArgumentException(TextUtils.format("dynamic_svg_unsupported_summary", id));
    }
}
