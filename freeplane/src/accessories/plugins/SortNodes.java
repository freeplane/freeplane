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
package accessories.plugins;

import java.awt.datatransfer.Transferable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.freeplane.map.clipboard.mindmapmode.MClipboardController;
import org.freeplane.map.tree.NodeModel;

import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author foltin
 */
public class SortNodes extends MindMapNodeHookAdapter {
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
	public SortNodes() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode,
	 * java.util.List)
	 */
	@Override
	public void invoke(final NodeModel node) {
		final Vector sortVector = new Vector();
		sortVector.addAll(node.getChildren());
		Collections.sort(sortVector, new NodeTextComparator());
		for (final Iterator iter = sortVector.iterator(); iter.hasNext();) {
			final NodeModel child = (NodeModel) iter.next();
			final Vector childList = new Vector();
			childList.add(child);
			final Transferable cut = ((MClipboardController) getMindMapController()
			    .getClipboardController()).cut(childList);
			((MClipboardController) getMindMapController()
			    .getClipboardController()).paste(cut, node);
		}
	}
}
