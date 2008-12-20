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
package org.freeplane.io;

import org.freeplane.io.xml.n3.nanoxml.IXMLElement;

/**
 * @author Dimitry Polivaev
 * 20.12.2008
 */
public abstract class NodeCreatorAdapter implements INodeCreator {
	public void completeNode(final Object parent, final String tag, final Object node) {
	}

	public boolean parseAttribute(final Object node, final String tag, final String name,
	                              final String value) {
		return false;
	}

	public void setAttributes(final String tag, final Object node, final IXMLElement attributes) {
	}
}
