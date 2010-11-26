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
package org.freeplane.features.common.icon;

import java.io.File;

/**
 * MindIcon class is used in the nodes of MindMaps.
 *
 * @author Tamas Eppel
 *
 */
public class MindIcon extends UIIcon {
	private static final String DEFAULT_IMAGE_PATH = "/images/icons";
	private static final String SHORTCUT_KEY = "IconAction.%s.shortcut";
	private final String imagePath;
	
	public MindIcon(final String name) {
		this(name, name + ".png", "");
	}

	/** this constructor expects an absolute path. It extracts name, filename and imagePath. */
	public MindIcon(final File file) {
		this(extractName(file), file.getName(), "", file.getParent().replace("\\", SEPARATOR));
	}

	private static String extractName(File file) {
	    return file.getName().replaceFirst("\\.png$", "");
    }

	public MindIcon(final String name, final String fileName) {
		this(name, fileName, "");
	}
	
	public MindIcon(final String name, final String fileName, final String description) {
		this(name, fileName, description, DEFAULT_IMAGE_PATH);
	}
	
	public MindIcon(final String name, final String fileName, final String description, final String imagePath) {
		super(name, fileName, description, String.format(SHORTCUT_KEY, name));
		this.imagePath = imagePath;
	}

	@Override
	public String getImagePath() {
		return imagePath == null ? DEFAULT_IMAGE_PATH : imagePath;
	}
}
