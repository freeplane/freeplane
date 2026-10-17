package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.treetable.TreeTableRowStyle;

class TreeTableAppearance {
	static final ChildNodesLayout CHILD_NODES_LAYOUT = ChildNodesLayout.TOPTOBOTTOM_RIGHT_BOTTOM;
	static final EdgeStyle EDGE_STYLE = EdgeStyle.EDGESTYLE_HORIZONTAL;

	static class NodeAppearance {
		private final NodeModel node;
		private final ChildNodesLayout childNodesLayout;

		private NodeAppearance(final NodeModel node) {
			this.node = node;
			this.childNodesLayout = LayoutModel.getModel(node).getChildNodesLayout();
		}

		void restore() {
			LayoutModel.createLayoutModel(node).setChildNodesLayout(childNodesLayout);
		}
	}

	static void ensureRowStyleExistsIn(final MapModel map) {
		final MLogicalStyleController styleController = (MLogicalStyleController) LogicalStyleController
			.getController();
		styleController.addUserStyleIfAbsent(map, TreeTableRowStyle.NAME,
			styleNode -> EdgeModel.createEdgeModel(styleNode).setStyle(EDGE_STYLE));
	}

	static NodeAppearance applyTo(final NodeModel treeTableRoot) {
		final NodeAppearance replaced = new NodeAppearance(treeTableRoot);
		LayoutModel.createLayoutModel(treeTableRoot).setChildNodesLayout(CHILD_NODES_LAYOUT);
		return replaced;
	}

	private TreeTableAppearance() {
	}
}
