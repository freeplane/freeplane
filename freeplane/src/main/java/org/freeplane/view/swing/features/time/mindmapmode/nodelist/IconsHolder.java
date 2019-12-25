/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.map.NodeModel;
/**
 * @author Dimitry Polivaev
 * 03.10.2013
 */
class IconsHolder implements Comparable<IconsHolder> {
	final private String iconNames;
	List<NamedIcon> icons = new ArrayList<>();

	public IconsHolder(final NodeModel node) {
		icons.addAll(IconController.getController().getIcons(node));
		if (icons.size() > 0) {
			final List<NamedIcon> toSort = new ArrayList<>(icons);
			Collections.sort(toSort);
			final StringBuilder builder = new StringBuilder();
			for (final NamedIcon icon : toSort) {
				builder.append(icon.getName()).append(" ");
			}
			iconNames = builder.toString();
		}
		else {
			iconNames = "";
		}
	}

	public int compareTo(final IconsHolder compareToObject) {
		return toString().compareTo(compareToObject.toString());
	}

	public List<NamedIcon> getIcons() {
		return icons;
	}

	/** Returns a sorted list of icon names. */
	@Override
	public String toString() {
		return iconNames;
	}
}