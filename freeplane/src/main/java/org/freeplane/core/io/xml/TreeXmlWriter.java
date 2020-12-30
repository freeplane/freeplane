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

import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class TreeXmlWriter implements ITreeWriter {
	public static String BooleanToXml(final boolean col) {
		return (col) ? "true" : "false";
	}

	public static String dateToString(final Date date) {
		return Long.toString(date.getTime());
	}

	public static String listToString(final List<?> list) {
		final ListIterator<?> it = list.listIterator(0);
		final StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			sb.append(it.next().toString()).append(";");
		}
		return sb.toString();
	}

	public static String pointToXml(final Point col) {
		if (col == null) {
			return null;
		}
		final Vector<String> l = new Vector<String>();
		l.add(new Quantity<>(col.x, LengthUnit.px).in(LengthUnit.pt).toString());
		l.add(new Quantity<>(col.y, LengthUnit.px).in(LengthUnit.pt).toString());
		return TreeXmlWriter.listToString(l);
	}

	private boolean elementStarted = false;
	final private HashMap<Object, Object> hints;
	final private WriteManager writeManager;
	private XMLElement xmlElement;
	final private XMLWriter xmlwriter;

	public void flush() {
	    xmlwriter.flush();
    }

	public TreeXmlWriter(final WriteManager writeManager, final Writer writer, boolean restrictedCharset) {
		super();
		this.writeManager = writeManager;
		xmlwriter = new XMLWriter(writer, restrictedCharset);
		hints = new HashMap<Object, Object>();
	}

	public void addAttribute(final String key, final double value) {
		addAttribute(key, Double.toString(value));
	}

	public void addAttribute(final String key, final int value) {
		addAttribute(key, Integer.toString(value));
	}

	public void addAttribute(final String key, final String value) {
		if (elementStarted) {
			throw new RuntimeException("elementStarted");
		}
		if (null != xmlElement.getAttribute(key, null)) {
			LogUtils.warn("attribute \"" + key + "\" already exist with value \"" + value);
			return;
		}
		xmlElement.setAttribute(key, value);
	}

	public void addComment(final String comment) throws IOException {
		xmlwriter.write("<!-- ");
		xmlwriter.write(comment);
		xmlwriter.write(" -->\n");
	}

	public void addElement(final Object userObject, final String name) throws IOException {
		final XMLElement element = new XMLElement(name);
		addElement(userObject, element);
	}

	@SuppressWarnings("unchecked")
    public void addElement(final Object userObject, final XMLElement element) throws IOException {
		final boolean isString = userObject instanceof String;
		if (elementStarted == false && xmlElement != null) {
			xmlwriter.write(xmlElement, ! isString, 0, true, false);
		}
		final String name = element.getName();
		xmlElement = element;
		elementStarted = false;
		{
			final Iterator<IAttributeWriter> iterator = getAttributeWriters().iterator(name);
			while (iterator.hasNext()) {
				final IAttributeWriter as = iterator.next();
				as.writeAttributes(this, userObject, name);
			}
			if (userObject instanceof List<?>) {
				addExtensionAttributes(userObject, (List<IExtension>) userObject);
			}
		}
		if (isString) {
			addElementContent((String)userObject);
		}
		else {
			final Iterator<IElementWriter> iterator = getNodeWriters().iterator(name);
			while (iterator.hasNext()) {
				final IElementWriter nw = iterator.next();
				nw.writeContent(this, userObject, name);
			}
			if (userObject instanceof List<?>) {
				addExtensionNodes(userObject, (List<IExtension>) userObject);
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

	public void addExtensionAttributes(final Object map, final Collection<IExtension> extensions) {
		final Iterator<IExtension> extensionIterator = extensions.iterator();
		while (extensionIterator.hasNext()) {
			final IExtension extension = extensionIterator.next();
			final Iterator<IExtensionAttributeWriter> writerIterator = writeManager.getExtensionAttributeWriters()
			    .iterator(extension.getClass());
			while (writerIterator.hasNext()) {
				writerIterator.next().writeAttributes(this, map, extension);
			}
		}
	}

	public void addExtensionNodes(final Object map, final Collection<IExtension> extensions) throws IOException {
		final Iterator<IExtension> extensionIterator = extensions.iterator();
		while (extensionIterator.hasNext()) {
			final IExtension extension = extensionIterator.next();
			final Iterator<IExtensionElementWriter> writerIterator = writeManager.getExtensionElementWriters()
			    .iterator(extension.getClass());
			while (writerIterator.hasNext()) {
				writerIterator.next().writeContent(this, map, extension);
			}
		}
	}

	private ListHashTable<String, IAttributeWriter> getAttributeWriters() {
		return writeManager.getAttributeWriters();
	}

	public Object getHint(final Object key) {
		final Object object = hints.get(key);
		return object == null ? Boolean.FALSE : object;
	}

	private ListHashTable<String, IElementWriter> getNodeWriters() {
		return writeManager.getElementWriters();
	}

	public void setHint(final Object key) {
		hints.put(key, Boolean.TRUE);
	}

	public void setHint(final Object key, final Object value) {
		hints.put(key, value);
	}
}
