package org.freeplane.view.swing.map.edge;

import java.awt.Color;
import java.awt.Point;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class AutomaticEdgeStyle {
	private NodeModel levelStyleNode;
	private EdgeController edgeController;
	public AutomaticEdgeStyle(NodeView node){
		MapView map = node.getMap();
		ModeController modeController = map.getModeController();
		AutomaticLayoutController automaticLayoutController = modeController.getExtension(AutomaticLayoutController.class);
		modeController.getExtension(NodeStyleController.class);
		
		final NodeView rootView = map.getRoot();
		final MapStyleModel mapStyleNodes = MapStyleModel.getExtension(rootView.getModel());
		int gridSize = (node.getMainView().getWidth() + map.getZoomed(LocationModel.HGAP));
		Point origin = new Point();
		UITools.convertPointToAncestor(rootView.getMainView(), origin, rootView);
		Point coordinate = new Point();
		UITools.convertPointToAncestor(node.getMainView(), coordinate, rootView);
		int distance = Math.abs(coordinate.x - origin.x);
		int level = (int) ((float)distance / gridSize + 0.5);
		
		final IStyle levelStyle = automaticLayoutController.getStyle(map.getModel(), level, true);
		levelStyleNode = mapStyleNodes.getStyleNode(levelStyle);
		edgeController = modeController.getExtension(EdgeController.class);
		
		
	}
	
	public Color getColor(){
		return edgeController.getColor(levelStyleNode);
	}
}
