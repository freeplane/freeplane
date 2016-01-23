/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.mode;

import java.util.ArrayList;
import java.util.LinkedList;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class SelectionController {
	final private LinkedList<INodeSelectionListener> nodeSelectionListeners;

	public SelectionController() {
		super();
		nodeSelectionListeners = new LinkedList<INodeSelectionListener>();
	}

	public void addNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.add(listener);
	}

	public LinkedList<INodeSelectionListener> getNodeSelectionListeners() {
		return nodeSelectionListeners;
	}

	public void onDeselect(final NodeModel node) {
		try {
			final ArrayList<INodeSelectionListener> copy = new ArrayList<INodeSelectionListener>(nodeSelectionListeners);
			for (final INodeSelectionListener listener : copy) {
				listener.onDeselect(node);
			}
		}
		catch (final RuntimeException e) {
			LogUtils.severe("Error in node selection listeners", e);
		}
	}

	public void onSelect(final NodeModel node) {
		final ArrayList<INodeSelectionListener> copy = new ArrayList<INodeSelectionListener>(nodeSelectionListeners);
		for (final INodeSelectionListener listener : copy) {
			listener.onSelect(node);
		}
	}

	public void removeNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.remove(listener);
	}
}
