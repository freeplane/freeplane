package org.freeplane.core.ui;

import java.awt.event.ActionEvent;

public class SetAcceleratorOnNextClickAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetAcceleratorOnNextClickAction() {
		super("SetAcceleratorOnNextClickAction");
	}

	public void actionPerformed(final ActionEvent e) {
		AccelerateableAction.setNewAcceleratorOnNextClick();
	}
}
