package org.freeplane.features.icon;

import org.freeplane.features.map.NodeModel;

public class IconClickedEvent {
	private final NamedIcon icon;
	private final NodeModel node;
	
	public IconClickedEvent(final NamedIcon icon, final NodeModel node) {
		this.icon = icon;
		this.node = node;
	}
	
	public NamedIcon getUIIcon() {
		return icon;
	}
	
	public NodeModel getNode() {
		return node;
	}
}
