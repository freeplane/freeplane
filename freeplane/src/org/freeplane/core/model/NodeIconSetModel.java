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
package org.freeplane.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ImageIcon;

/**
 * @author Dimitry Polivaev 20.11.2008
 */
class NodeIconSetModel {
	/** stores the icons associated with this node. */
	protected Vector/* <MindIcon> */icons = null;
	private TreeMap /* of String to MindIcon s */stateIcons = null;

	void addIcon(final MindIcon _icon, final int position) {
		createIcons();
		if (position == MindIcon.LAST) {
			icons.add(_icon);
		}
		else {
			icons.add(position, _icon);
		}
	}

	private void createIcons() {
		if (icons == null) {
			icons = new Vector();
		}
	}

	private void createStateIcons() {
		if (stateIcons == null) {
			stateIcons = new TreeMap();
		}
	}

	public MindIcon getIcon(final int position) {
		final List icons = getIcons();
		return (MindIcon) (position == MindIcon.LAST ? icons.get(icons.size() - 1) : icons.get(position));
	}

	List getIcons() {
		if (icons == null) {
			return Collections.EMPTY_LIST;
		}
		return icons;
	}

	Map getStateIcons() {
		if (stateIcons == null) {
			return Collections.EMPTY_MAP;
		}
		return Collections.unmodifiableSortedMap(stateIcons);
	}

	/** @return returns the number of remaining icons. */
	int removeIcon(int position) {
		createIcons();
		if (position == MindIcon.LAST) {
			position = icons.size() - 1;
		}
		icons.remove(position);
		final int returnSize = icons.size();
		if (returnSize == 0) {
			icons = null;
		}
		return returnSize;
	}

	/** This method must be synchronized as the TreeMap isn't. */
	void setStateIcon(final String key, final ImageIcon icon) {
		createStateIcons();
		if (icon != null) {
			stateIcons.put(key, icon);
		}
		else if (stateIcons.containsKey(key)) {
			stateIcons.remove(key);
		}
		if (stateIcons.size() == 0) {
			stateIcons = null;
		}
	}
}
