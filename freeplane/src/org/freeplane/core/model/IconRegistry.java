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
package org.freeplane.core.model;

import java.util.ListIterator;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.MapController;

/**
 * @author Dimitry Polivaev
 * 03.01.2009
 */
public class IconRegistry implements IExtension {
	final private DefaultListModel mapIcons;

	IconRegistry(final MapController mapController, final MapModel map) {
		super();
		mapIcons = new DefaultListModel();
		registryNodeIcons(mapController, map.getRootNode());
	}

	void addIcon(final MindIcon icon) {
		mapIcons.addElement(icon);
	}

	public ListModel getIcons() {
		return mapIcons;
	}

	private void registryNodeIcons(final MapController mapController, final NodeModel node) {
		for(MindIcon icon : node.getIcons()) {
			addIcon(icon);
		}
		final ListIterator<NodeModel> iterator = mapController.childrenUnfolded(node);
		while (iterator.hasNext()) {
			final NodeModel next = iterator.next();
			registryNodeIcons(mapController, next);
		}
	}
}
