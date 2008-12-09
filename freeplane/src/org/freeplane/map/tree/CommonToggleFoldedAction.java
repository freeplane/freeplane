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
package org.freeplane.map.tree;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ListIterator;

import org.freeplane.controller.FreeMindAction;
import org.freeplane.main.Tools;

/**
 * @author foltin
 */
class CommonToggleFoldedAction extends FreeMindAction {
	public CommonToggleFoldedAction() {
		super("toggle_folded");
	}

	public void actionPerformed(final ActionEvent e) {
		toggleFolded();
	}

	public void toggleFolded() {
		toggleFolded(getModeController().getSelectedNodes().listIterator());
	}

	public void toggleFolded(final ListIterator listIterator) {
		final boolean fold = getModeController().getMapController()
		    .getFoldingState(Tools.resetIterator(listIterator));
		for (final Iterator i = Tools.resetIterator(listIterator); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			getModeController().getMapController().setFolded(node, fold);
		}
	}
}
