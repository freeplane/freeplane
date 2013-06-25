/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
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
package org.freeplane.features.styles;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class LogicalStyleModel implements IExtension {
	private IStyle style;

	public IStyle getStyle() {
		return style;
	}

	public void setStyle(final IStyle style) {
		this.style = style;
	}

	static public LogicalStyleModel getExtension(final NodeModel node) {
		return (LogicalStyleModel) node.getExtension(LogicalStyleModel.class);
	}

	static public IStyle getStyle(final NodeModel node) {
		final LogicalStyleModel extension = LogicalStyleModel.getExtension(node);
		if (extension == null) {
			return null;
		}
		final IStyle style = extension.getStyle();
		return style;
	}

	static public LogicalStyleModel createExtension(final NodeModel node) {
		LogicalStyleModel extension = (LogicalStyleModel) node.getExtension(LogicalStyleModel.class);
		if (extension == null) {
			extension = new LogicalStyleModel();
			node.addExtension(extension);
		}
		return extension;
	}
}
