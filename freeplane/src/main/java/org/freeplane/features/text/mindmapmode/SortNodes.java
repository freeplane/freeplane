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
import java.util.Comparator;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.NodeSorter;
import org.freeplane.features.text.TextController;

public class SortNodes extends AMultipleNodeAction {
	final static private class NodeTextComparator implements Comparator<NodeModel> {
		@Override
		public int compare(final NodeModel pArg0, final NodeModel pArg1) {
			if (pArg0 instanceof NodeModel) {
				final NodeModel node1 = pArg0;
				if (pArg1 instanceof NodeModel) {
					final NodeModel node2 = pArg1;
					final String nodeText1 = TextController.getController()
					    .getPlainTransformedTextWithoutNodeNumber(node1);
					final String nodeText2 = TextController.getController()
					    .getPlainTransformedTextWithoutNodeNumber(node2);
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
		final NodeTextComparator comparator = new NodeTextComparator();
		new NodeSorter(comparator).sortNodes(node);
	}
}
