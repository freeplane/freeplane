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
package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;

public class SortNodes extends AMultipleNodeAction {
	final private class NodeTextComparator implements Comparator<Object> {
		public int compare(final Object pArg0, final Object pArg1) {
			if (pArg0 instanceof NodeModel) {
				final NodeModel node1 = (NodeModel) pArg0;
				if (pArg1 instanceof NodeModel) {
					final NodeModel node2 = (NodeModel) pArg1;
					final String nodeText1 = TextController.getController().getPlainTextContent(node1);
					final String nodeText2 = TextController.getController().getPlainTextContent(node2);
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
	public SortNodes() {
		super("SortNodes");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		sortNodes(node, 0);
	}

	private void sortNodes(final NodeModel parent, int fromIndex) {
		final int childCount = parent.getChildCount();
		if(fromIndex >= childCount)
			return;
		final Vector<NodeModel> sortVector = new Vector<NodeModel>(childCount - fromIndex);
		int nodeIndex = fromIndex;
		while(nodeIndex < childCount) {
			final NodeModel child = parent.getChildAt(nodeIndex);
			nodeIndex++;
			if(SummaryNode.isSummaryNode(child)) {
				sortNodes(child, 0);
				break;
			}
			else if(SummaryNode.isFirstGroupNode(child)) {
				break;
			}
			else
				sortVector.add(child);
		}
		Collections.sort(sortVector, new NodeTextComparator());
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		for (final NodeModel child : sortVector) {
			((FreeNode)Controller.getCurrentModeController().getExtension(FreeNode.class)).undoableDeactivateHook(child);
			mapController.moveNode(child, fromIndex++);
		}
		sortNodes(parent, nodeIndex);
	}
}
