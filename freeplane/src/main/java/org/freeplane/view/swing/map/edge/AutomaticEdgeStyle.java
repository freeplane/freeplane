package org.freeplane.view.swing.map.edge;

import java.awt.Color;
import java.awt.Point;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class AutomaticEdgeStyle {
	private Color color;

	public AutomaticEdgeStyle(NodeView node){
		MapView map = node.getMap();
		ModeController modeController = map.getModeController();
		modeController.getExtension(NodeStyleController.class);

		final NodeView rootView = map.getRoot();
		Point origin = new Point();
		final MainView rootContent = rootView.getMainView();
		UITools.convertPointToAncestor(rootContent, origin, rootView);
		Point coordinate = new Point();
		final MainView nodeContent = node.getMainView();
		UITools.convertPointToAncestor(nodeContent, coordinate, rootView);
		final MapStyleModel mapStyleNodes = MapStyleModel.getExtension(map.getMap());

		final int distance;
		final int nodeColumnWidth;
		if(map.getLayoutType() == MapViewLayout.OUTLINE){
			distance = Math.max(0, coordinate.x - origin.x);
			final int hgapProperty = ResourceController.getResourceController().getLengthProperty("outline_hgap");
			nodeColumnWidth = Math.max(1, map.getZoomed(hgapProperty));
		}
		else {
			if(origin.x < coordinate.x ){
				distance = Math.max(0, coordinate.x  - origin.x + nodeContent.getWidth() - rootContent.getWidth());
			}
			else{
				distance = origin.x - coordinate.x;
			}
			final NodeModel defaultStyleNode = mapStyleNodes.getDefaultStyleNode();
			final NodeStyleController nodeStyleController = modeController.getExtension(NodeStyleController.class);
			nodeColumnWidth = map.getZoomed(nodeStyleController.getMaxWidth(defaultStyleNode, StyleOption.FOR_UNSELECTED_NODE).toBaseUnitsRounded() + LocationModel.DEFAULT_HGAP_PX);
		}
		int level = (int) ((float)distance / nodeColumnWidth + 0.5);
		if(SummaryNode.isHidden(node.getNode()))
			level++;

		EdgeController edgeController = modeController.getExtension(EdgeController.class);
		color = edgeController.areEdgeColorsAvailable(map.getMap()) ? edgeController.getEdgeColor(map.getMap(), level) : EdgeController.STANDARD_EDGE_COLOR;


	}

	public Color getColor(){
		return color;
	}
}
