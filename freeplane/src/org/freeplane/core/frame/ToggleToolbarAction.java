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
package org.freeplane.core.frame;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;

@SelectableAction(checkOnPopup = true)
public class ToggleToolbarAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	final private ViewController controller;
	private final String propertyName;
	private final String toolbarName;

	public ToggleToolbarAction(final Controller controller, final ViewController viewController,
	                           final String actionName, final String toolbarName, final String propertyName) {
		super(actionName, controller);
		this.controller = viewController;
		this.toolbarName = toolbarName;
		this.propertyName = propertyName;
	}

	public void actionPerformed(final ActionEvent event) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final boolean visible = !resourceController.getBooleanProperty(propertyName);
		resourceController.setProperty(propertyName, visible);
		final JToolBar toolBar = getModeController().getUserInputListenerFactory().getToolBar(toolbarName);
		toolBar.setVisible(visible);
		((JComponent) toolBar.getParent()).revalidate();
	}

	@Override
	public void setSelected() {
		final boolean isVisible = ResourceController.getResourceController().getBooleanProperty(propertyName);
		setSelected(isVisible);
	}
}
