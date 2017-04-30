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
package org.freeplane.features.mode;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelection.NodePosition;

/**
 * @author foltin
 */
class MoveSelectedNodeAction extends AFreeplaneAction {
	private static final String MOVE_SLOWLY_PROPERTY = "slow_scroll_selected_node";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final NodePosition nodePosition;

	public MoveSelectedNodeAction(NodePosition nodePosition) {
		super("MoveSelectedNodeAction." + nodePosition.name());
		this.nodePosition = nodePosition;
	}

	public void actionPerformed(final ActionEvent e) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final Component mapView = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		final JRootPane rootPane = SwingUtilities.getRootPane(mapView);
		if (!rootPane.isValid()) {
			rootPane.revalidate();
		}
		if(ResourceController.getResourceController().getBooleanProperty(MOVE_SLOWLY_PROPERTY))
			selection.slowlyMoveNodeTo(selection.getSelected(), nodePosition);
		else
			selection.moveNodeTo(selection.getSelected(), nodePosition);
	}
}
