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
package org.freeplane.core.io.xml;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.extension.IExtensionCollection;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.util.Tools;

public class TreeXmlWriter implements ITreeWriter {
	public static String BooleanToXml(final boolean col) {
		return (col) ? "true" : "false";
	}

	public static String colorToXml(final Color col) {
		if (col == null) {
			return null;
		}
		String red = Integer.toHexString(col.getRed());
		if (col.getRed() < 16) {
			red = "0" + red;
		}
		String green = Integer.toHexString(col.getGreen());
		if (col.getGreen() < 16) {
			green = "0" + green;
		}
		String blue = Integer.toHexString(col.getBlue());
		if (col.getBlue() < 16) {
			blue = "0" + blue;
		}
		return "#" + red + green + blue;
	}

	public static String dateToString(final Date date) {
		return Long.toString(date.getTime());
	}

	public static String PointToXml(final Point col) {
		if (col == null) {
			return null;
		}
		final Vector l = new Vector();
		l.add(Integer.toString(col.x));
		l.add(Integer.toString(col.y));
		return Tools.listToString(l);
	}

	private boolean elementStarted = false;
	final private WriteManager writeManager;
	private IXMLElement xmlElement;
	final private XMLWriter xmlwriter;

	public TreeXmlWriter(final WriteManager writeManager, final Writer writer) {
		super();
		this.writeManager = writeManager;
		xmlwriter = new XMLWriter(writer);
		hints = new HashMap<Object, Object>();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addAttribute(java.lang.String, double)
	 */
	public void addAttribute(final String key, final double value) {
		if (elementStarted) {
			throw new RuntimeException();
		}
		xmlElement.setAttribute(key, Double.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addAttribute(java.lang.String, int)
	 */
	public void addAttribute(final String key, final int value) {
		if (elementStarted) {
			throw new RuntimeException();
		}
		xmlElement.setAttribute(key, Integer.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addAttribute(java.lang.String,
	 * java.lang.String)
	 */
	public void addAttribute(final String key, final String value) {
		if (elementStarted) {
			throw new RuntimeException("elementStarted");
		}
		xmlElement.setAttribute(key, value);
	}

	public void addComment(final String comment) throws IOException {
		xmlwriter.write("<!-- ");
		xmlwriter.write(comment);
		xmlwriter.write(" -->\n");
	}

	public void addElement(final Object userObject, final IXMLElement element) throws IOException {
		if (elementStarted == false && xmlElement != null) {
			xmlwriter.write(xmlElement, true, 0, true, false);
		}
		final String name = element.getName();
		xmlElement = element;
		elementStarted = false;
		{
			final Iterator iterator = getAttributeWriters().iterator(name);
			while (iterator.hasNext()) {
				final IAttributeWriter as = (IAttributeWriter) iterator.next();
				as.writeAttributes(this, userObject, name);
			}
			if (userObject instanceof IExtensionCollection) {
				final IExtensionCollection collection = (IExtensionCollection) userObject;
				addExtensionAttributes(userObject, collection);
			}
		}
		if (userObject != null && userObject.getClass().equals(String.class)) {
			addElementContent(userObject.toString());
		}
		else {
			final Iterator iterator = getNodeWriters().iterator(name);
			while (iterator.hasNext()) {
				final IElementWriter nw = (IElementWriter) iterator.next();
				nw.writeContent(this, userObject, name);
			}
			if (userObject instanceof IExtensionCollection) {
				final IExtensionCollection collection = (IExtensionCollection) userObject;
				addExtensionNodes(userObject, collection);
			}
		}
		if (elementStarted == false) {
			xmlwriter.write(xmlElement, true, 0, true, true);
			elementStarted = true;
		}
		else {
			xmlwriter.endElement(name, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addNode(java.lang.String)
	 */
	public void addElement(final Object userObject, final String name) throws IOException {
		final XMLElement element = new XMLElement(name);
		addElement(userObject, element);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addNodeContent(java.lang.String)
	 */
	public void addElementContent(final String content) throws IOException {
		if (content.equals("")) {
			return;
		}
		if (elementStarted == false && xmlElement != null) {
			xmlwriter.write(xmlElement, true, 0, true, false);
			elementStarted = true;
		}
		xmlwriter.write(content);
	}

	public void addExtensionAttributes(final Object element, final IExtensionCollection collection) {
		final Iterator<IExtension> extensionIterator = collection.extensionIterator();
		while (extensionIterator.hasNext()) {
			final IExtension extension = extensionIterator.next();
			final Iterator<IExtensionAttributeWriter> writerIterator = writeManager
			    .getExtensionAttributeWriters().iterator(extension.getClass());
			while (writerIterator.hasNext()) {
				writerIterator.next().writeAttributes(this, element, extension);
			}
		}
	}

	public void addExtensionNodes(final Object element, final IExtensionCollection collection)
	        throws IOException {
		final Iterator<IExtension> extensionIterator = collection.extensionIterator();
		while (extensionIterator.hasNext()) {
			final IExtension extension = extensionIterator.next();
			final Iterator<IExtensionElementWriter> writerIterator = writeManager
			    .getExtensionElementWriters().iterator(extension.getClass());
			while (writerIterator.hasNext()) {
				writerIterator.next().writeContent(this, element, extension);
			}
		}
	}

	private ListHashTable getAttributeWriters() {
		return writeManager.getAttributeWriters();
	}

	private ListHashTable getNodeWriters() {
		return writeManager.getElementWriters();
	}

	final private HashMap<Object, Object> hints;
	public Object getHint(Object key) {
		return hints.get(key);
    }

	public void setHint(Object key, Object value) {
		hints.put(key, value);
    }
}
