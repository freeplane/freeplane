/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapnode.pattern.MPatternController;

class NodeStyleProxy extends AbstractProxy implements Proxy.NodeStyle {
	NodeStyleProxy(final NodeModel delegate,
			final MModeController modeController) {
		super(delegate, modeController);
	}

	public void applyPattern(final String patternName) {
		getPatternController().applyPattern(getNode(), patternName);
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

	private MPatternController getPatternController() {
		return MPatternController.getController(getModeController());
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
}