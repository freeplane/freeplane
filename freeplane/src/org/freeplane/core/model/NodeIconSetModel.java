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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.icon.UIIcon;

/**
 * @author Dimitry Polivaev 20.11.2008
 */
class NodeIconSetModel {
	/** stores the icons associated with this node. */
	protected List<MindIcon> icons;
	private TreeMap<String, UIIcon> stateIcons;

	void addIcon(final MindIcon icon) {
		createIcons();
		icons.add(icon);
	}

	void addIcon(final MindIcon icon, final int position) {
		createIcons();
		if (position > -1) {
			icons.add(position, icon);
		}
		else {
			icons.add(icon);
		}
	}

	private void createIcons() {
		if (icons == null) {
			icons = new ArrayList<MindIcon>();
		}
	}

	private void createStateIcons() {
		if (stateIcons == null) {
			stateIcons = new TreeMap<String, UIIcon>();
		}
	}

	public MindIcon getIcon(final int position) {
		return getIcons().get(position);
	}

	List<MindIcon> getIcons() {
		if (icons == null) {
			return Collections.emptyList();
		}
		return icons;
	}

	Map<String, UIIcon> getStateIcons() {
		if (stateIcons == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableSortedMap(stateIcons);
	}

	/** 
	 * removes the last icon
	 * 
	 * @return returns the number of remaining icons. 
	 */
	int removeIcon() {
		createIcons();
		if (!icons.isEmpty()) {
			icons.remove(icons.size() - 1);
		}
		return icons.size();
	}

	/** 
	 * @param position of icon to remove
	 * 
	 * @return returns the number of remaining icons. 
	 */
	int removeIcon(int position) {
		createIcons();
		if (position == icons.size()) {
			position = icons.size() - 1;
		}
		icons.remove(position);
		return icons.size();
	}

	/** This method must be synchronized as the TreeMap isn't. */
	void setStateIcon(final String key, final UIIcon icon) {
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

	public void removeStateIcons(final String key) {
		if (stateIcons != null) {
			stateIcons.remove(key);
		}
	}
}
