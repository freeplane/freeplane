package org.freeplane.plugin.spreadsheet;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;

@ActionLocationDescriptor(locations = { "/menu_bar/extras" /*TODO: Menu location: only from editor */})
public class SelectNodeReferenceAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public SelectNodeReferenceAction(final Controller controller) {
		super("SelectNodeReference", controller, "SelectNodeReference", null);
	}

	public void actionPerformed(final ActionEvent e) {
	}
}
