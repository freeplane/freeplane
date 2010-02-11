package org.freeplane.core.ui;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;


public class SetAcceleratorOnNextClickAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetAcceleratorOnNextClickAction(Controller controller) {
		super("SetAcceleratorOnNextClickAction", controller);
	}

	public void actionPerformed(ActionEvent e) {
		Controller controller = getController();
		AccelerateableAction.setNewAcceleratorOnNextClick(controller);
	}
}
