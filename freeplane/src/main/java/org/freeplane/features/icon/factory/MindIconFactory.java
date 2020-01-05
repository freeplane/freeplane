/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Tamas Eppel
 *
 *  This file author is Tamas Eppel
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
package org.freeplane.features.icon.factory;

import javax.swing.Icon;

import org.freeplane.features.icon.MindIcon;

/**
 * @author Tamas Eppel
 */
public class MindIconFactory {
	/**
	 * Constructs a MindIcon with the given name from the property file.
	 * The name of the icon is the file name without the extension.
	 */
	public static MindIcon createIcon(final String name) {
		final String translationKeyLabel = name.indexOf('/') > 0 ? "" : ("icon_" + name);
		return new MindIcon(name, name + ".svg", translationKeyLabel);
	}

	public static Icon createStandardIcon(String iconKey) {
		return createIcon(iconKey).getIcon();
	}
}
