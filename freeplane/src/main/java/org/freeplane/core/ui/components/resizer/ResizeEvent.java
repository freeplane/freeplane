package org.freeplane.core.ui.components.resizer;

import java.awt.Component;
import java.util.EventObject;

public class ResizeEvent extends EventObject {

	private static final long serialVersionUID = 3131068483469543037L;
	private final Component component;

	public ResizeEvent(JResizer source, Component component) {
		super(source);
		this.component = component;
	}
	
	public JResizer getSource() {
		return (JResizer) super.getSource();
	}

	public Component getComponent() {
		return component;
	}
}
