/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.addins;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.mindmapmode.MMapController;

/**
 * @author foltin
 */
@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/nodes/sorting" })
public class SortNodes extends AMultipleNodeAction {
	final private class NodeTextComparator implements Comparator {
		public int compare(final Object pArg0, final Object pArg1) {
			if (pArg0 instanceof NodeModel) {
				final NodeModel node1 = (NodeModel) pArg0;
				if (pArg1 instanceof NodeModel) {
					final NodeModel node2 = (NodeModel) pArg1;
					final String nodeText1 = node1.getPlainTextContent();
					final String nodeText2 = node2.getPlainTextContent();
					return nodeText1.compareToIgnoreCase(nodeText2);
				}
			}
			return 0;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param controller 
	 *
	 */
	public SortNodes(final Controller controller) {
		super("SortNodes", controller);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final Vector<NodeModel> sortVector = new Vector<NodeModel>();
		sortVector.addAll(node.getChildren());
		Collections.sort(sortVector, new NodeTextComparator());
		final MMapController mapController = (MMapController) getModeController().getMapController();
		int i = 0;
		for (final NodeModel child : sortVector) {
			mapController.moveNode(child, node, i++);
		}
	}
}
