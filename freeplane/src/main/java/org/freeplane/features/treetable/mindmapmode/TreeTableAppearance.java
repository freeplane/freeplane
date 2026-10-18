package org.freeplane.features.treetable.mindmapmode;

import java.awt.Color;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.api.Dash;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.treetable.TreeTableAutoStyle;

class TreeTableAppearance {
	static final ChildNodesLayout CHILD_NODES_LAYOUT = ChildNodesLayout.TOPTOBOTTOM_RIGHT_BOTTOM;
	static final EdgeStyle ROW_EDGE_STYLE = EdgeStyle.EDGESTYLE_HIDDEN;
	static final Color ROW_LINE_COLOR = Color.GRAY;
	static final int ROW_LINE_WIDTH = 1;
	static final Dash ROW_LINE_DASH = Dash.SOLID;
	static final Color TABLE_BORDER_COLOR = Color.GRAY;
	static final Quantity<LengthUnit> TABLE_BORDER_WIDTH = new Quantity<LengthUnit>(2, LengthUnit.px);
	static final Dash TABLE_BORDER_DASH = Dash.SOLID;

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

	static void ensureStylesExistIn(final MapModel map) {
		final MLogicalStyleController styleController = (MLogicalStyleController) LogicalStyleController
			.getController();
		styleController.addUserStyleIfAbsent(map, TreeTableAutoStyle.ROW.getName(), TreeTableAppearance::initRowStyle);
		styleController.addUserStyleIfAbsent(map, TreeTableAutoStyle.TABLE.getName(),
			TreeTableAppearance::initTableStyle);
	}

	private static void initRowStyle(final NodeModel styleNode) {
		final EdgeModel edge = EdgeModel.createEdgeModel(styleNode);
		edge.setStyle(ROW_EDGE_STYLE);
		edge.setColor(ROW_LINE_COLOR);
		edge.setWidth(ROW_LINE_WIDTH);
		edge.setDash(ROW_LINE_DASH);
	}

	private static void initTableStyle(final NodeModel styleNode) {
		final NodeBorderModel border = NodeBorderModel.createNodeBorderModel(styleNode);
		border.setBorderWidthMatchesEdgeWidth(false);
		border.setBorderWidth(TABLE_BORDER_WIDTH);
		border.setBorderColorMatchesEdgeColor(false);
		border.setBorderColor(TABLE_BORDER_COLOR);
		border.setBorderDashMatchesEdgeDash(false);
		border.setBorderDash(TABLE_BORDER_DASH);
	}

	static NodeAppearance applyTo(final NodeModel treeTableRoot) {
		final NodeAppearance replaced = new NodeAppearance(treeTableRoot);
		LayoutModel.createLayoutModel(treeTableRoot).setChildNodesLayout(CHILD_NODES_LAYOUT);
		return replaced;
	}

	private TreeTableAppearance() {
	}
}
