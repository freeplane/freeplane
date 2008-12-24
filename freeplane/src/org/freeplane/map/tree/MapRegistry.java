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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.freeplane.controller.filter.util.SortedMapListModel;
import org.freeplane.io.ITreeWriter;
import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class MapRegistry {
	/**
	 *
	 */
	final private AttributeRegistry attributes;
	final private SortedMapListModel mapIcons;

	public MapRegistry(final MapModel map, final ModeController modeController) {
		super();
		mapIcons = new SortedMapListModel();
		attributes = new AttributeRegistry(modeController.getAttributeController());
	}

	public void addIcon(final MindIcon icon) {
		mapIcons.add(icon);
	}

	public AttributeRegistry getAttributes() {
		return attributes;
	}

	/**
	 */
	public SortedMapListModel getIcons() {
		return mapIcons;
	}

	private void registryAttributes(final NodeModel node) {
		final NodeAttributeTableModel model = node.getAttributes();
		if (model == null) {
			return;
		}
		for (int i = 0; i < model.getRowCount(); i++) {
			attributes.registry(model.getAttribute(i));
		}
		final ListIterator<NodeModel> iterator = node.getModeController().getMapController()
		    .childrenUnfolded(node);
		while (iterator.hasNext()) {
			final NodeModel next = iterator.next();
			registryAttributes(next);
		}
	}

	private void registryNodeIcons(final NodeModel node) {
		final List icons = node.getIcons();
		final Iterator i = icons.iterator();
		while (i.hasNext()) {
			final MindIcon icon = (MindIcon) i.next();
			addIcon(icon);
		}
		final ListIterator<NodeModel> iterator = node.getModeController().getMapController()
		    .childrenUnfolded(node);
		while (iterator.hasNext()) {
			final NodeModel next = iterator.next();
			registryNodeIcons(next);
		}
	}

	public void registrySubtree(final NodeModel root) {
		registryNodeIcons(root);
		registryAttributes(root);
	}

	/**
	 * @throws IOException
	 */
	public void write(final ITreeWriter writer) throws IOException {
		getAttributes().write(writer);
	}
}
