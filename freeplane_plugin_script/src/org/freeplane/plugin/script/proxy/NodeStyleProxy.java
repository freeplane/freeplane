/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.styles.MLogicalStyleController;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeStyleProxy extends AbstractProxy<NodeModel> implements Proxy.NodeStyle {
	NodeStyleProxy(final NodeModel delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public void setStyle(final Object key) {
		getLogicalStyleController().setStyle(getDelegate(), key);
	}

	public Color getBackgroundColor() {
		return getStyleController().getBackgroundColor(getDelegate());
	}

	public Proxy.Edge getEdge() {
		return new EdgeProxy(getDelegate(), getModeController());
	}

	public Proxy.Font getFont() {
		return new FontProxy(getDelegate(), getModeController());
	}

	public Color getNodeTextColor() {
		return getStyleController().getColor(getDelegate());
	}

	private MLogicalStyleController getLogicalStyleController() {
		return (MLogicalStyleController) LogicalStyleController.getController(getModeController());
	}

	private MNodeStyleController getStyleController() {
		return (MNodeStyleController) NodeStyleController.getController(getModeController());
	}

	public void setBackgroundColor(final Color color) {
		getStyleController().setBackgroundColor(getDelegate(), color);
	}

	public void setNodeTextColor(final Color color) {
		getStyleController().setColor(getDelegate(), color);
	}

	public Object getStyle() {
		return LogicalStyleModel.getStyle(getDelegate());
	}

	public Node getStyleNode() {
		final NodeModel styleNode = MapStyleModel.getExtension(getDelegate().getMap()).getStyleNode(getStyle());
		return new NodeProxy(styleNode, getModeController());
	}
}
