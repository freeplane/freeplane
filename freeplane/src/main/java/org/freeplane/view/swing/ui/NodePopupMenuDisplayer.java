/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.ui;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 18.06.2013
 */
public class NodePopupMenuDisplayer {
	final private ControllerPopupMenuListener popupListener;

	public NodePopupMenuDisplayer() {
		popupListener = new ControllerPopupMenuListener();
	}

	public void showMenuAndConsumeEvent(final JPopupMenu popupmenu, final MouseEvent e) {
		if (popupmenu != null) {
			popupmenu.addHierarchyListener(popupListener);
			popupmenu.show(e.getComponent(), e.getX(), e.getY());
			e.consume();
		}
	}

	public void showNodePopupMenu(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			ModeController mc = Controller.getCurrentController().getModeController();
			final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, e.getComponent());
			if (nodeView == null)
				return;
			if (!nodeView.isSelected()) {
				Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeView.getModel());
			}
			final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
			new NodePopupMenuDisplayer().showMenuAndConsumeEvent(popupmenu, e);
		}
	}
}
