package org.freeplane.features.treetable.mindmapmode;

import java.awt.Color;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.api.Dash;
import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.treetable.TreeTableAutoStyle;
import org.freeplane.features.treetable.TreeTableHeader;

class TreeTableAppearance {
	static final ChildNodesLayout CHILD_NODES_LAYOUT = ChildNodesLayout.TOPTOBOTTOM_RIGHT_BOTTOM;
	static final EdgeStyle ROW_EDGE_STYLE = EdgeStyle.EDGESTYLE_HIDDEN;
	static final EdgeStyle HEADER_EDGE_STYLE = EdgeStyle.EDGESTYLE_HIDDEN;
	static final EdgeStyle COLUMN_EDGE_STYLE = EdgeStyle.EDGESTYLE_HIDDEN;
	static final HorizontalTextAlignment COLUMN_TEXT_ALIGNMENT = HorizontalTextAlignment.CENTER;
	static final Color COLUMN_BORDER_COLOR = Color.BLACK;
	static final ChildNodesLayout HEADER_CHILD_NODES_LAYOUT = ChildNodesLayout.LEFTTORIGHT_BOTTOM_CENTERED;
	static final Quantity<LengthUnit> COLUMN_MIN_WIDTH = new Quantity<LengthUnit>(60, LengthUnit.pt);
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
		styleController.addUserStyleIfAbsent(map, TreeTableHeader.STYLE_NAME, TreeTableAppearance::initHeaderStyle);
		styleController.addUserStyleIfAbsent(map, TreeTableAutoStyle.COLUMN.getName(),
			TreeTableAppearance::initColumnStyle);
	}

	private static void initRowStyle(final NodeModel styleNode) {
		final EdgeModel edge = EdgeModel.createEdgeModel(styleNode);
		edge.setStyle(ROW_EDGE_STYLE);
	}

	private static void initHeaderStyle(final NodeModel styleNode) {
		EdgeModel.createEdgeModel(styleNode).setStyle(HEADER_EDGE_STYLE);
		LayoutModel.createLayoutModel(styleNode).setChildNodesLayout(HEADER_CHILD_NODES_LAYOUT);
	}

	private static void initColumnStyle(final NodeModel styleNode) {
		EdgeModel.createEdgeModel(styleNode).setStyle(COLUMN_EDGE_STYLE);
		LayoutModel.createLayoutModel(styleNode).setChildNodesLayout(HEADER_CHILD_NODES_LAYOUT);
		NodeSizeModel.createNodeSizeModel(styleNode).setMinNodeWidth(COLUMN_MIN_WIDTH);
		NodeStyleModel.createNodeStyleModel(styleNode).setHorizontalTextAlignment(COLUMN_TEXT_ALIGNMENT);
		final NodeBorderModel border = NodeBorderModel.createNodeBorderModel(styleNode);
		border.setBorderColorMatchesEdgeColor(false);
		border.setBorderColor(COLUMN_BORDER_COLOR);
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
