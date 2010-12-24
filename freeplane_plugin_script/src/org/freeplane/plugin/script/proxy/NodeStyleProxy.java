/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.styles.MLogicalStyleController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeStyleProxy extends AbstractProxy<NodeModel> implements Proxy.NodeStyle {
	NodeStyleProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	public void setStyle(final IStyle key) {
		getLogicalStyleController().setStyle(getDelegate(), key);
	}

	public Color getBackgroundColor() {
		return getStyleController().getBackgroundColor(getDelegate());
	}

	public String getBackgroundColorCode() {
		return ColorUtils.colorToString(getBackgroundColor());
	}

	public Proxy.Edge getEdge() {
		return new EdgeProxy(getDelegate(), getScriptContext());
	}

	public Proxy.Font getFont() {
		return new FontProxy(getDelegate(), getScriptContext());
	}

	public Color getTextColor() {
		return getStyleController().getColor(getDelegate());
	}

	@Deprecated
	public Color getNodeTextColor() {
		return getTextColor();
	}

	public String getTextColorCode() {
		return ColorUtils.colorToString(getTextColor());
	}

	private MLogicalStyleController getLogicalStyleController() {
		return (MLogicalStyleController) LogicalStyleController.getController();
	}

	private MNodeStyleController getStyleController() {
		return (MNodeStyleController) NodeStyleController.getController();
	}

	public void setBackgroundColor(final Color color) {
		getStyleController().setBackgroundColor(getDelegate(), color);
	}

	public void setBackgroundColorCode(final String rgbString) {
		setBackgroundColor(ColorUtils.stringToColor(rgbString));
	}

	public void setTextColor(final Color color) {
		getStyleController().setColor(getDelegate(), color);
	}

	@Deprecated
	public void setNodeTextColor(final Color color) {
		setTextColor(color);
	}

	public void setTextColorCode(final String rgbString) {
		setTextColor(ColorUtils.stringToColor(rgbString));
	}

	public IStyle getStyle() {
		return LogicalStyleModel.getStyle(getDelegate());
	}

	public Node getStyleNode() {
		final NodeModel styleNode = MapStyleModel.getExtension(getDelegate().getMap()).getStyleNode(getStyle());
		return new NodeProxy(styleNode, getScriptContext());
	}
}
