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
package org.freeplane.features.mindmapmode.addins.export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.url.UrlManager;

/**
 * @author Dimitry Polivaev
 * 13.12.2008
 */
class ExportFilter extends FileFilter {
	final private String description;
	final private String type;

	public ExportFilter(final String type, final String description) {
		this.type = type;
		this.description = description;
	}

	@Override
	public boolean accept(final File f) {
		if (f.isDirectory()) {
			return true;
		}
		final String extension = UrlManager.getExtension(f.getName());
		return StringUtils.equalsIgnoreCase(extension, type);
	}

	@Override
	public String getDescription() {
		return description == null ? type : description;
	}
}
