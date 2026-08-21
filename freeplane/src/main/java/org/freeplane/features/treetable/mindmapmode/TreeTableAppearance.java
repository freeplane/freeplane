package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.NodeModel;

class TreeTableAppearance {
	static final ChildNodesLayout CHILD_NODES_LAYOUT = ChildNodesLayout.TOPTOBOTTOM_RIGHT_BOTTOM;
	static final EdgeStyle EDGE_STYLE = EdgeStyle.EDGESTYLE_HORIZONTAL;

	static class NodeAppearance {
		private final NodeModel node;
		private final ChildNodesLayout childNodesLayout;
		private final EdgeStyle edgeStyle;

		private NodeAppearance(final NodeModel node) {
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			this.node = node;
			this.childNodesLayout = LayoutModel.getModel(node).getChildNodesLayout();
			this.edgeStyle = edgeModel != null ? edgeModel.getStyle() : null;
		}

		void restore() {
			LayoutModel.createLayoutModel(node).setChildNodesLayout(childNodesLayout);
			EdgeModel.createEdgeModel(node).setStyle(edgeStyle);
		}
	}

	static NodeAppearance applyTo(final NodeModel treeTableRoot) {
		final NodeAppearance replaced = new NodeAppearance(treeTableRoot);
		LayoutModel.createLayoutModel(treeTableRoot).setChildNodesLayout(CHILD_NODES_LAYOUT);
		EdgeModel.createEdgeModel(treeTableRoot).setStyle(EDGE_STYLE);
		return replaced;
	}

	private TreeTableAppearance() {
	}
}
