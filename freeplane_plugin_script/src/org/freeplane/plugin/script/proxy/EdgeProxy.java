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
	EdgeProxy(final Object delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public Color getColor() {
		return getEdgeController().getColor(getNode());
	}

	private MEdgeController getEdgeController() {
		return (MEdgeController) EdgeController
				.getController(getModeController());
	}

	public EdgeStyle getType() {
		return getEdgeController().getStyle(getNode());
	}

	public int getWidth() {
		return getEdgeController().getWidth(getNode());
	}

	public void setColor(final Color color) {
		getEdgeController().setColor(getNode(), color);

	}

	public void setType(final EdgeStyle type) {
		getEdgeController().setStyle(getNode(), type);

	}

	public void setWidth(final int width) {
		getEdgeController().setWidth(getNode(), width);

	}
}