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
package org.freeplane.features.tags;

import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class TagsModel implements IExtension {
    Set<String> tags = new LinkedHashSet<>();
	public static TagsModel getModel(final NodeModel node) {
		return node.getExtension(TagsModel.class);
	}

	public static TagsModel createModel(final NodeModel node) {
		final TagsModel extension = node.getExtension(TagsModel.class);
		if (extension != null) {
			return extension;
		}
		final TagsModel tagModel = new TagsModel();
		node.addExtension(tagModel);
		return tagModel;
	}

	public static void setModel(final NodeModel node, final TagsModel cloud) {
		final TagsModel oldCloud = TagsModel.getModel(node);
		if (cloud != null && oldCloud == null) {
			node.addExtension(cloud);
		}
		else if (cloud == null && oldCloud != null) {
			node.removeExtension(TagsModel.class);
		}
	}

}
