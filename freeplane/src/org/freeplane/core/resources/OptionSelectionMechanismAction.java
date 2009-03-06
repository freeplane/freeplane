/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.resources;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.freeplane.core.actions.IFreeplaneAction;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;

class OptionSelectionMechanismAction extends AbstractAction implements IFreeplanePropertyListener, IFreeplaneAction {
	static final String NAME = "optionSelectionMechanismAction";
	private static final long serialVersionUID = -5573280308177905728L;
	final private Controller controller;

	OptionSelectionMechanismAction(final Controller controller) {
		this.controller = controller;
		ResourceController.getResourceController().addPropertyChangeListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		final String command = e.getActionCommand();
		changeSelection(command);
	}

	/**
	 */
	private void changeSelection(final String command) {
		ResourceController.getResourceController().setProperty("selection_method", command);
		controller.getModeController().getUserInputListenerFactory().getNodeMouseMotionListener()
		    .updateSelectionMethod();
		final String statusBarString = FreeplaneResourceBundle.getText(command);
		if (statusBarString != null) {
			controller.getViewController().out(statusBarString);
		}
	}

	public String getName() {
		return NAME;
	}

	public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
		if (propertyName.equals(ResourceControllerProperties.RESOURCES_SELECTION_METHOD)) {
			changeSelection(newValue);
		}
	}
}
