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
package org.freeplane.io.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.freeplane.extension.IExtension;
import org.freeplane.extension.IExtensionCollection;
import org.freeplane.io.IAttributeWriter;
import org.freeplane.io.INodeWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.IXMLElementWriter;
import org.freeplane.io.ListHashTable;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;

public class TreeXmlWriter implements ITreeWriter {
	private boolean elementStarted = false;
	final private WriteManager writeManager;
	private IXMLElement xmlElement;
	final private XMLWriter xmlwriter;

	public TreeXmlWriter(final WriteManager writeManager, final Writer writer) {
		super();
		this.writeManager = writeManager;
		xmlwriter = new XMLWriter(writer);
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

	public void addNode(final Object userObject, final IXMLElement element)
	        throws IOException {
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
				final Iterator<IExtension> extensionIterator = collection
				    .extensionIterator();
				while (extensionIterator.hasNext()) {
					final IExtension extension = extensionIterator.next();
					final Iterator<IAttributeWriter<IExtension>> writerIterator = writeManager
					    .getExtensionAttributeWriters().iterator(
					        extension.getClass());
					while (writerIterator.hasNext()) {
						writerIterator.next().writeAttributes(this, userObject,
						    extension);
					}
				}
			}
		}
		if (userObject != null && userObject.getClass().equals(String.class)) {
			addNodeContent(userObject.toString());
		}
		else {
			final Iterator iterator = getNodeWriters().iterator(name);
			while (iterator.hasNext()) {
				final INodeWriter nw = (INodeWriter) iterator.next();
				nw.writeContent(this, userObject, name);
			}
			if (userObject instanceof IExtensionCollection) {
				final IExtensionCollection collection = (IExtensionCollection) userObject;
				final Iterator<IExtension> extensionIterator = collection
				    .extensionIterator();
				while (extensionIterator.hasNext()) {
					final IExtension extension = extensionIterator.next();
					final Iterator<INodeWriter<IExtension>> writerIterator = writeManager
					    .getExtensionNodeWriters().iterator(
					        extension.getClass());
					while (writerIterator.hasNext()) {
						writerIterator.next().writeContent(this, userObject,
						    extension);
					}
				}
			}
		}
		{
			final Iterator iterator = getXmlWriters().iterator(name);
			while (iterator.hasNext()) {
				final IXMLElementWriter xs = (IXMLElementWriter) iterator
				    .next();
				xs.write(this, userObject, xmlElement);
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
	public void addNode(final Object userObject, final String name)
	        throws IOException {
		final XMLElement element = new XMLElement(name);
		addNode(userObject, element);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addNodeContent(java.lang.String)
	 */
	public void addNodeContent(final String content) throws IOException {
		if (content.equals("")) {
			return;
		}
		if (elementStarted == false) {
			xmlwriter.write(xmlElement, true, 0, true, false);
			elementStarted = true;
		}
		xmlwriter.write(content);
	}

	private ListHashTable getAttributeWriters() {
		return writeManager.getAttributeWriters();
	}

	private ListHashTable getNodeWriters() {
		return writeManager.getNodeWriters();
	}

	private ListHashTable getXmlWriters() {
		return writeManager.getXmlWriters();
	}
}
