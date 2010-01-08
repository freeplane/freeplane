/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.LogicalStyleModel;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.addins.styles.MLogicalStyleController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeStyleProxy extends AbstractProxy implements Proxy.NodeStyle {
	NodeStyleProxy(final NodeModel delegate,
			final MModeController modeController) {
		super(delegate, modeController);
	}

	public void setStyle(final Object key) {
		getLogicalStyleController().setStyle(getNode(), key);
	}

	public Color getBackgroundColor() {
		return getStyleController().getBackgroundColor(getNode());
	}

	public Proxy.Edge getEdge() {
		return new EdgeProxy(getNode(), getModeController());
	}

	public Proxy.Font getFont() {
		return new FontProxy(getNode(), getModeController());
	}

	public Color getNodeTextColor() {
		return getStyleController().getColor(getNode());
	}

	private MLogicalStyleController getLogicalStyleController() {
		return (MLogicalStyleController) LogicalStyleController.getController(getModeController());
	}

	private MNodeStyleController getStyleController() {
		return (MNodeStyleController) NodeStyleController
				.getController(getModeController());
	}

	public void setBackgroundColor(final Color color) {
		getStyleController().setBackgroundColor(getNode(), color);
	}

	public void setNodeTextColor(final Color color) {
		getStyleController().setColor(getNode(), color);
	}

	public Object getStyle() {
		return LogicalStyleModel.getStyle(getNode());
	}

	public Node getStyleNode() {
		NodeModel styleNode = MapStyleModel.getExtension(getNode().getMap()).getStyleNode(getStyle());
		return new NodeProxy(styleNode, getModeController());
	}
}