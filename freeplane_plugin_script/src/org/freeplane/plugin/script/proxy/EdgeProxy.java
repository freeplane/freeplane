/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;

class EdgeProxy extends AbstractProxy implements Proxy.Edge {
	EdgeProxy(Object delegate, MModeController modeController) {
		super(delegate, modeController);
	}
	
	private MEdgeController getEdgeController(){
		return (MEdgeController) EdgeController.getController(getModeController());
	}

	public void setWidth(int width) {
		getEdgeController().setWidth(getNode(), width);
		
	}

	public void setType(EdgeStyle type) {
		getEdgeController().setStyle(getNode(), type);
		
	}

	public void setColor(Color color) {
		getEdgeController().setColor(getNode(), color);
		
	}

	public int getWidth() {
		return getEdgeController().getWidth(getNode());
	}

	public EdgeStyle getType() {
		return getEdgeController().getStyle(getNode());
	}

	public Color getColor() {
		return getEdgeController().getColor(getNode());
	}
}