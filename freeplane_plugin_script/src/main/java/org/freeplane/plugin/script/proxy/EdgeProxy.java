/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

class EdgeProxy extends AbstractProxy<NodeModel> implements Proxy.Edge {
	EdgeProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	public Color getColor() {
		return getEdgeController().getColor(getDelegate());
	}
	
	public String getColorCode() {
		return ColorUtils.colorToString(getColor());
	}

	private MEdgeController getEdgeController() {
		return (MEdgeController) EdgeController.getController();
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

	public void setColorCode(final String rgbString) {
		setColor(ColorUtils.stringToColor(rgbString));
	}

	public void setType(final EdgeStyle type) {
		getEdgeController().setStyle(getDelegate(), type);
	}

	public void setWidth(final int width) {
		getEdgeController().setWidth(getDelegate(), width);
	}
}
