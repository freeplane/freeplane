/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;


class NodeStyleProxy extends AbstractProxy implements Proxy.NodeStyle {
	public void setNodeTextColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	public void setBackgroundColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	public Color getNodeTextColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Proxy.Font getFont() {
		return new FontProxy();
	}

	public Proxy.Edge getEdge() {
		return new EdgeProxy(getNode(), getModeController());
	}

	public Color getBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void applyPattern(String patternName) {
		// TODO Auto-generated method stub
		
	}
}