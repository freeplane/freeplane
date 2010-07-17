package org.freeplane.plugin.spreadsheet;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;

@ActionLocationDescriptor(locations = { Activator.MENU_BAR_LOCATION })
public class EvaluateAllAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public EvaluateAllAction(final Controller controller) {
		super("EvaluateAll", "EvaluateAll", null);
	}

	public void actionPerformed(final ActionEvent e) {
	}
}
