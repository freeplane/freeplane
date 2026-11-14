package org.freeplane.view.swing.map;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;

public final class MapViewExportCapture {
    private final MapView view;

    public MapViewExportCapture(MapView view) {
        this.view = view;
    }

    /**
     * Captures scoped native artwork synchronously, without changing model folding.
     * The context and its native views may only be used inside the callback.
     */
    public <T> T capture(Function<ExportContext, T> capture) {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("Export capture requires the Swing event dispatch thread");
        return view.withPreservedExportState(() -> capturePreservingFolding(capture));
    }

    private <T> T capturePreservingFolding(Function<ExportContext, T> capture) {
        Map<NodeModel, Boolean> initialFolding = new LinkedHashMap<>();
        collectExportScope(view.getRoot().getNode(), initialFolding);
        Map<NodeView, Boolean> viewFolding = new LinkedHashMap<>();
        ExportContext context = null;
        try {
            List<NodeView> nodes = new ArrayList<>();
            unfoldForExport(view.getRoot(), initialFolding, viewFolding, nodes);
            view.prepareForExport();
            context = new ExportContext(view.getRoot(), nodes, initialFolding);
            return capture.apply(context);
        } finally {
            if (context != null)
                context.active = false;
            List<NodeView> nodes = new ArrayList<>(viewFolding.keySet());
            Collections.reverse(nodes);
            for (NodeView node : nodes)
                if (node.isFolded() != viewFolding.get(node))
                    node.setFolded(viewFolding.get(node));
        }
    }

    private void collectExportScope(NodeModel node, Map<NodeModel, Boolean> folding) {
        if (node != view.getRoot().getNode() && !node.subtreeHasVisibleContent(view.getFilter())
                && !SummaryNode.isFirstGroupNode(node))
            return;
        NodeView nodeView = view.getNodeView(node);
        folding.put(node, nodeView == null ? view.getModeController().getMapController().isFolded(node)
                : nodeView.isFolded());
        EncryptionModel encryption = EncryptionModel.getModel(node);
        if (encryption != null && !encryption.isAccessible())
            return;
        for (NodeModel child : node.getChildren()) {
            if (nodeView != null && nodeView.isChildHidden(child))
                break;
            collectExportScope(child, folding);
        }
    }

    private void unfoldForExport(NodeView node, Map<NodeModel, Boolean> folding,
            Map<NodeView, Boolean> viewFolding, List<NodeView> nodes) {
        if (!folding.containsKey(node.getNode()))
            return;
        viewFolding.put(node, node.isFolded());
        nodes.add(node);
        EncryptionModel encryption = EncryptionModel.getModel(node.getNode());
        if (encryption != null && !encryption.isAccessible())
            return;
        if (node.isFolded())
            node.setFolded(false);
        for (NodeView child : node.getChildrenViews())
            unfoldForExport(child, folding, viewFolding, nodes);
    }

    public static final class ExportContext {
        public final NodeView root;
        public final List<NodeView> nodes;
        public final Map<NodeModel, Boolean> initialFolding;
        private boolean active = true;

        private ExportContext(NodeView root, List<NodeView> nodes, Map<NodeModel, Boolean> initialFolding) {
            this.root = root;
            this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
            this.initialFolding = Collections.unmodifiableMap(new LinkedHashMap<>(initialFolding));
        }

        public void paintNode(NodeView node, Graphics2D graphics) {
            if (!active || !SwingUtilities.isEventDispatchThread())
                throw new IllegalStateException("Export context is only valid during capture");
            if (!nodes.contains(node))
                throw new IllegalArgumentException("Node is outside export scope");
            root.getMap().paintNodeForExport(node, graphics);
        }
    }
}
