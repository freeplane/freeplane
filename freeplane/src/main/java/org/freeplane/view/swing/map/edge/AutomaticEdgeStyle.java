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
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.IStyle;
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
		AutomaticLayoutController automaticLayoutController = modeController.getExtension(AutomaticLayoutController.class);
		modeController.getExtension(NodeStyleController.class);
		
		final NodeView rootView = map.getRoot();
		Point origin = new Point();
		final MainView rootContent = rootView.getMainView();
		UITools.convertPointToAncestor(rootContent, origin, rootView);
		Point coordinate = new Point();
		final MainView nodeContent = node.getMainView();
		UITools.convertPointToAncestor(nodeContent, coordinate, rootView);
		final MapStyleModel mapStyleNodes = MapStyleModel.getExtension(rootView.getModel());

		final int distance;
		final int nodeColumnWidth;
		if(map.getLayoutType() == MapViewLayout.OUTLINE){
			distance = coordinate.x - origin.x;
			final int hgapProperty = ResourceController.getResourceController().getLengthProperty("outline_hgap");
			nodeColumnWidth = map.getZoomed(hgapProperty);
		}
		else {
			if(origin.x < coordinate.x ){
				distance = coordinate.x + nodeContent.getWidth() - origin.x - rootContent.getWidth();
			}
			else{
				distance = origin.x - coordinate.x;
			}
			final NodeModel defaultStyleNode = mapStyleNodes.getStyleNode(MapStyleModel.DEFAULT_STYLE);
			final NodeStyleController nodeStyleController = modeController.getExtension(NodeStyleController.class);
			nodeColumnWidth = map.getZoomed(nodeStyleController.getMaxWidth(defaultStyleNode).toBaseUnitsRounded() + LocationModel.DEFAULT_HGAP_PX);
		}
		int level = (int) ((float)distance / nodeColumnWidth + 0.5);
		if(SummaryNode.isHidden(node.getModel()))
			level++;
		
		EdgeController edgeController = modeController.getExtension(EdgeController.class);
		color = edgeController.areEdgeColorsAvailable(map.getModel()) ? edgeController.getEdgeColor(map.getModel(), level) : EdgeController.STANDARD_EDGE_COLOR;
		
		
	}
	
	public Color getColor(){
		return color;
	}
}
