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
package org.freeplane.map.icon;

import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.ReadManager;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

class IconBuilder implements INodeCreator, IAttributeHandler {
	static class IconProperties {
		String iconName;
	}

	public void completeNode(final Object parent, final String tag,
	                         final Object userObject) {
		if (parent instanceof NodeObject && tag.equals("icon")) {
			final NodeModel node = ((NodeObject) parent).node;
			final IconProperties ip = (IconProperties) userObject;
			node.addIcon(MindIcon.factory(ip.iconName), MindIcon.LAST);
			return;
		}
	}

	public Object createNode(final Object parent, final String tag) {
		if (tag.equals("icon")) {
			return new IconProperties();
		}
		return null;
	}

	public boolean parseAttribute(final Object userObject, final String tag,
	                              final String name, final String value) {
		/* icons */
		if (tag.equals("icon")) {
			final IconProperties ip = (IconProperties) userObject;
			if (name.equals("BUILTIN")) {
				ip.iconName = value.toString();
			}
			return true;
		}
		return false;
	}

	/**
	 */
	public void registerBy(final ReadManager reader) {
		reader.addNodeCreator("icon", this);
	}
}
