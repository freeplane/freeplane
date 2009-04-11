/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.map.attribute;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.view.swing.map.MapView;

public class EditAttributesAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EditAttributesAction(final Controller controller) {
		super("EditAttributesAction", controller);
	};

	public void actionPerformed(final ActionEvent e) {
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		final Controller controller = getController();
		final AttributeView attributeView = (((MapView) controller.getViewController().getMapView()).getSelected())
		    .getAttributeView();
		final boolean attributesClosed = null == SwingUtilities.getAncestorOfClass(AttributeTable.class, focusOwner);
		if (attributesClosed) {
			attributeView.startEditing();
		}
		else {
			attributeView.stopEditing();
		}
	}
}
