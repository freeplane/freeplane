package org.freeplane.core.ui.ribbon;

import java.awt.Component;

public class RibbonTaskBarComponent {
	private final Component component;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public RibbonTaskBarComponent(Component c) {
		this.component = c;
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public Component getComponent() {
		return this.component;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
}
