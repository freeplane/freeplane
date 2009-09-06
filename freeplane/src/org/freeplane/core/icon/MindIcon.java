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
package org.freeplane.core.icon;

/**
 * MindIcon class is used in the nodes of MindMaps.
 *
 * @author Tamas Eppel
 *
 */
public class MindIcon extends UIIcon {

	private static final String DEFAULT_IMAGE_PATH = "/images/icons";

	public MindIcon(final String name, final String fileName) {
		super(name, fileName);
	}

	public MindIcon(final String name, final String fileName, final String description) {
		super(name, fileName, description);
	}

	public MindIcon(final String name, final String fileName, final String description, final String shortcutKey) {
		super(name, fileName, description, shortcutKey);
	}

	@Override
	public String getDefaultImagePath() {
		return DEFAULT_IMAGE_PATH;
	}
}
