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
package org.freeplane.core.filter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.SelectableAction;

@SelectableAction
class ShowFilterToolbarAction extends AbstractAction {
	final private FilterController filterController;

	/**
	 *
	 */
	ShowFilterToolbarAction(final FilterController filterController) {
		super(null, new ImageIcon(Controller.getResourceController().getResource("/images/filter.gif")));
		this.filterController = filterController;
	}

	public void actionPerformed(final ActionEvent event) {
		final JToggleButton btnFilter = (JToggleButton) event.getSource();
		if (btnFilter.getModel().isSelected()) {
			filterController.showFilterToolbar(true);
		}
		else {
			filterController.showFilterToolbar(false);
		}
	}
}
