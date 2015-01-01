package org.freeplane.features.icon;

import org.freeplane.features.map.NodeModel;

public class IconClickedEvent {
	private final UIIcon icon;
	private final NodeModel node;
	
	public IconClickedEvent(final UIIcon icon, final NodeModel node) {
		this.icon = icon;
		this.node = node;
	}
	
	public UIIcon getUIIcon() {
		return icon;
	}
	
	public NodeModel getNode() {
		return node;
	}
}
