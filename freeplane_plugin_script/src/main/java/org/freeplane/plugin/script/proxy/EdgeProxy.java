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
import org.freeplane.plugin.script.ScriptExecution;

class EdgeProxy extends AbstractProxy<NodeModel> implements Proxy.Edge {
	EdgeProxy(final NodeModel delegate, final ScriptExecution scriptExecution) {
		super(delegate, scriptExecution);
	}

	@Override
	public Color getColor() {
		return getEdgeController().getColor(getDelegate());
	}

	@Override
	public String getColorCode() {
		return ColorUtils.colorToString(getColor());
	}

	private MEdgeController getEdgeController() {
		return (MEdgeController) EdgeController.getController();
	}

	@Override
	public EdgeStyle getType() {
		return getEdgeController().getStyle(getDelegate());
	}

	@Override
	public int getWidth() {
		return getEdgeController().getWidth(getDelegate());
	}

	@Override
	public void setColor(final Color color) {
		getEdgeController().setColor(getDelegate(), color);
	}

	@Override
	public void setColorCode(final String rgbString) {
		setColor(ColorUtils.stringToColor(rgbString));
	}

	@Override
	public void setType(final String type) {
		setType(EdgeStyle.getStyle(type));
	}

	@Override
	public void setType(final EdgeStyle type) {
		getEdgeController().setStyle(getDelegate(), type);
	}

	@Override
	public void setType(final org.freeplane.api.EdgeStyle type) {
		setType(type.name());
	}

	@Override
	public void setWidth(final int width) {
		getEdgeController().setWidth(getDelegate(), width);
	}
}
