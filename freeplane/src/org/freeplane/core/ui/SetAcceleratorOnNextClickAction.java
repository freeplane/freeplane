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
	private static final String CONFIRMATION = "confirmation";
	private static final String SET_ACCELERATOR_ON_NEXT_CLICK_ACTION_SET = "set_accelerator_on_next_click_action_set";
	private static final String SET_ACCELERATOR_ON_NEXT_CLICK_ACTION = "set_accelerator_on_next_click_action";

	public SetAcceleratorOnNextClickAction(Controller controller) {
		super("SetAcceleratorOnNextClickAction", controller);
	}

	public void actionPerformed(ActionEvent e) {
		Controller controller = getController();
		final int showResult = OptionalDontShowMeAgainDialog.show(controller, SET_ACCELERATOR_ON_NEXT_CLICK_ACTION, CONFIRMATION, SET_ACCELERATOR_ON_NEXT_CLICK_ACTION_SET, OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		AccelerateableAction.setNewAcceleratorOnNextClick(true);
		controller.getViewController().out(ResourceBundles.getText(SET_ACCELERATOR_ON_NEXT_CLICK_ACTION));
	}
}
