package org.freeplane.core.ui;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;

public class SetAcceleratorOnNextClickAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetAcceleratorOnNextClickAction(final Controller controller) {
		super("SetAcceleratorOnNextClickAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = getController();
		AccelerateableAction.setNewAcceleratorOnNextClick(controller);
	}
}
