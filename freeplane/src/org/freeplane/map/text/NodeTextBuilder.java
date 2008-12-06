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
package org.freeplane.map.text;

import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.INodeContentHandler;
import org.freeplane.io.ReadManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.map.tree.NodeBuilder;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

public class NodeTextBuilder implements INodeContentHandler, IAttributeHandler {
	public static final String XML_NODE_TEXT = "TEXT";
	public static final String XML_NODE_XHTML_CONTENT_TAG = "richcontent";
	public static final String XML_NODE_XHTML_TYPE_NODE = "NODE";
	public static final String XML_NODE_XHTML_TYPE_NOTE = "NOTE";
	public static final String XML_NODE_XHTML_TYPE_TAG = "TYPE";

	public boolean parseAttribute(final Object userObject, final String tag,
	                              final String name, final String value) {
		if (tag.equals(NodeBuilder.XML_NODE)
		        && userObject instanceof NodeObject) {
			final NodeModel node = ((NodeObject) userObject).node;
			if (name.equals(NodeTextBuilder.XML_NODE_TEXT)) {
				node.setText(value);
				return true;
			}
		}
		return false;
	}

	/**
	 */
	public void registerBy(final ReadManager reader) {
		reader.addAttributeHandler("node", this);
		reader.addNodeContentHandler("richcontent", this);
	}

	public boolean setContent(final Object node, final String tag,
	                          final IXMLElement attributes, final String content) {
		if (tag.equals("richcontent")) {
			final String xmlText = content;
			final Object typeAttribute = attributes.getAttribute(
			    NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, null);
			if (typeAttribute == null
			        || NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE
			            .equals(typeAttribute)) {
				((NodeObject) node).node.setXmlText(xmlText);
				return true;
			}
		}
		return false;
	}
}
