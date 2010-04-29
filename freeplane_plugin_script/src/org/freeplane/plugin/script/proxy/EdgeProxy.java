/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;

class EdgeProxy extends AbstractProxy<NodeModel> implements Proxy.Edge {
	EdgeProxy(final NodeModel delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public Color getColor() {
		return getEdgeController().getColor(getDelegate());
	}

	private MEdgeController getEdgeController() {
		return (MEdgeController) EdgeController.getController(getModeController());
	}

	public EdgeStyle getType() {
		return getEdgeController().getStyle(getDelegate());
	}

	public int getWidth() {
		return getEdgeController().getWidth(getDelegate());
	}

	public void setColor(final Color color) {
		getEdgeController().setColor(getDelegate(), color);
	}

	public void setType(final EdgeStyle type) {
		getEdgeController().setStyle(getDelegate(), type);
	}

	public void setWidth(final int width) {
		getEdgeController().setWidth(getDelegate(), width);
	}
}
