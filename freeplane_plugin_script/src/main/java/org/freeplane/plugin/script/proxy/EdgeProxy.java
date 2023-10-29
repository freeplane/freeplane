/**
 *
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.api.Dash;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.plugin.script.ScriptContext;

class EdgeProxy extends AbstractProxy<NodeModel> implements Proxy.Edge {
	EdgeProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	private MEdgeController getEdgeController() {
		return (MEdgeController) EdgeController.getController();
	}

	@Override
	public Color getColor() {
		return getEdgeController().getColor(getDelegate(), StyleOption.FOR_UNSELECTED_NODE);
	}

	@Override
	public String getColorCode() {
		return ColorUtils.colorToString(getColor());
	}

	@Override
	public boolean isColorSet() {
		EdgeModel edge = EdgeModel.getModel(getDelegate());
		return edge!=null && edge.getColor() != null;
	}

	@Override
	public void setColor(final Color color) {
		getEdgeController().setColor(getDelegate(), color);
	}

	@Override
	public EdgeStyle getType() {
		return getEdgeController().getStyle(getDelegate(), StyleOption.FOR_UNSELECTED_NODE);
	}

	@Override
	public boolean isTypeSet() {
		EdgeModel edge = EdgeModel.getModel(getDelegate());
		return edge!=null && edge.getStyle() != null;

	}

	@Override
	public int getWidth() {
		return getEdgeController().getWidth(getDelegate(), StyleOption.FOR_UNSELECTED_NODE);
	}

	@Override
	public boolean isWidthSet() {
		EdgeModel edge = EdgeModel.getModel(getDelegate());
		return edge!=null && edge.getWidth() != EdgeModel.AUTO_WIDTH;
	}

	@Override
	public void setWidth(final int width) {
		getEdgeController().setWidth(getDelegate(), width);
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
	public Dash getDash() {
		return getEdgeController().getDash(getDelegate(), StyleOption.FOR_UNSELECTED_NODE);
	}

	@Override
	public boolean isDashSet() {
		EdgeModel edge = EdgeModel.getModel(getDelegate());
		return edge!=null && edge.getDash() != null;
	}

	@Override
	public void setDash(Dash dash) {
		getEdgeController().setDash(getDelegate(), dash);
	}
}
