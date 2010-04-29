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

class NodeStyleProxy extends AbstractProxy<NodeModel> implements Proxy.NodeStyle {
	NodeStyleProxy(final NodeModel delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public void applyPattern(final String patternName) {
		getPatternController().applyPattern(getDelegate(), patternName);
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

	private MPatternController getPatternController() {
		return MPatternController.getController(getModeController());
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
}
