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

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.MindIcon;

/**
 * 
 * Factory for MindIcons.
 * 
 * @author Tamas Eppel
 *
 */
public class MindIconFactory {
	private static final String DESC_KEY = "icon_%s";

	/**
	 * Constructs a MindIcon with the given name from the property file.
	 * The name of the icon is the file name without the extension.
	 * 
	 * @param name of the icon
	 * @return
	 */
	public static MindIcon create(final String name) {
		final String description = name.indexOf('/') > 0 ? "" : TextUtils.getText(String.format(DESC_KEY, name), "");
		return new MindIcon(name, name + ".png", description);
	}
}
