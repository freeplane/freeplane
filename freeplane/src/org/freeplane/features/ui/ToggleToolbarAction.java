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
package org.freeplane.features.ui;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.mode.Controller;

@SelectableAction(checkOnPopup = true)
public class ToggleToolbarAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private final String toolbarName;

	public ToggleToolbarAction( final String actionName, final String toolbarName) {
		super(actionName);
		this.toolbarName = toolbarName;
	}

	public void actionPerformed(final ActionEvent event) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final JComponent toolBar = getToolbar();
		final String propertyName = Controller.getCurrentController().getViewController().completeVisiblePropertyKey(toolBar);
		final boolean wasVisible = resourceController.getBooleanProperty(propertyName);
		final boolean visible = !wasVisible;
		resourceController.setProperty(propertyName, visible);
		setVisible(toolBar, visible);
	}

	protected void setVisible(final JComponent toolBar, final boolean visible) {
	    toolBar.setVisible(visible);
		((JComponent) toolBar.getParent()).revalidate();
    }

	private JComponent getToolbar() {
		final JComponent toolBar = Controller.getCurrentModeController().getUserInputListenerFactory().getToolBar(toolbarName);
		return toolBar;
	}

	@Override
	public void setSelected() {
		final boolean isVisible = isVisible();
		setSelected(isVisible);
	}

	public boolean isVisible() {
		final JComponent toolBar = getToolbar();
		final boolean isVisible = Controller.getCurrentController().getViewController().isToolbarVisible(toolBar);
		return isVisible;
	}
	
	@Override
	public void afterMapChange(final Object newMap) {
	}
}
