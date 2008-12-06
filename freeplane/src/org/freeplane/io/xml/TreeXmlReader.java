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

import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.INodeContentHandler;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.ITreeReader;
import org.freeplane.io.IXMLElementHandler;
import org.freeplane.io.ListHashTable;
import org.freeplane.io.ReadManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLBuilder;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.IXMLReader;
import org.freeplane.io.xml.n3.nanoxml.NonValidator;
import org.freeplane.io.xml.n3.nanoxml.StdXMLReader;
import org.freeplane.io.xml.n3.nanoxml.XMLException;

public class TreeXmlReader implements IXMLBuilder, ITreeReader {
	private INodeCreator nodeCreator;
	final private LinkedList nodeCreatorStack = new LinkedList();
	final private ReadManager parseManager;
	private XMLParser parser;
	private IXMLElement saveAsXmlUntil;
	private String tag;
	private Object userObject;
	final private LinkedList userObjectStack = new LinkedList();
	private StdXMLBuilder xmlBuilder;

	public TreeXmlReader(final ReadManager parseManager) {
		super();
		this.parseManager = parseManager;
	}

	private boolean addAttribute(final Iterator iterator, final String key,
	                             final String value) {
		while (iterator.hasNext()) {
			final IAttributeHandler al = (IAttributeHandler) iterator.next();
			if (al.parseAttribute(userObject, tag, key, value)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#addAttribute(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public void addAttribute(final String key, final String nsPrefix,
	                         final String nsURI, final String value,
	                         final String type) throws Exception {
		if (saveAsXmlUntil == null) {
			final Iterator ncIter = getNodeCreators().iterator(tag);
			if (addAttribute(ncIter, key, value)) {
				return;
			}
			final Iterator alIter = getAttributeLoaders().iterator(tag);
			if (addAttribute(alIter, key, value)) {
				return;
			}
		}
		xmlBuilder.addAttribute(key, nsPrefix, nsURI, value, type);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#addPCData(java.io.Reader
	 * , java.lang.String, int)
	 */
	public void addPCData(final Reader reader, final String systemID,
	                      final int lineNr) throws Exception {
		xmlBuilder.addPCData(reader, systemID, lineNr);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#elementAttributesProcessed
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	public void elementAttributesProcessed(final String name,
	                                       final String nsPrefix,
	                                       final String nsURI) throws Exception {
		xmlBuilder.elementAttributesProcessed(name, nsPrefix, nsURI);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#endElement(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void endElement(final String name, final String nsPrefix,
	                       final String nsURI) throws Exception {
		final IXMLElement lastBuiltElement = xmlBuilder.getParentElement();
		xmlBuilder.endElement(name, nsPrefix, nsURI);
		if (saveAsXmlUntil == lastBuiltElement) {
			saveAsXmlUntil = null;
		}
		if (saveAsXmlUntil != null) {
			return;
		}
		final Iterator iterator = getXmlLoaders().iterator(name);
		while (iterator.hasNext()) {
			final IXMLElementHandler xl = (IXMLElementHandler) iterator.next();
			if (xl.parse(userObject, tag, lastBuiltElement)) {
				break;
			}
		}
		tag = null;
		if (0 == userObjectStack.size()) {
			return;
		}
		final Object node = userObject;
		userObject = userObjectStack.removeLast();
		if (nodeCreator != null) {
			nodeCreator.completeNode(userObject, name, node);
		}
		nodeCreator = (INodeCreator) nodeCreatorStack.removeLast();
	}

	private ListHashTable getAttributeLoaders() {
		return parseManager.getAttributeHandlers();
	}

	private ListHashTable getNodeContentLoaders() {
		return parseManager.getNodeContentHandlers();
	}

	private ListHashTable getNodeCreators() {
		return parseManager.getNodeCreators();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#getResult()
	 */
	public Object getResult() throws Exception {
		return null;
	}

	private ListHashTable getXmlLoaders() {
		return parseManager.getXmlHandlers();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#load()
	 */
	public void load(final Reader reader) {
		try {
			parser = new XMLParser();
			final IXMLReader nanoxmlReader = new StdXMLReader(reader);
			parser.setReader(nanoxmlReader);
			parser.setBuilder(this);
			parser.setValidator(new NonValidator());
			parser.parse();
		}
		catch (final XMLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#newProcessingInstruction
	 * (java.lang.String, java.io.Reader)
	 */
	public void newProcessingInstruction(final String target,
	                                     final Reader reader) throws Exception {
		xmlBuilder.newProcessingInstruction(target, reader);
	}

	private void pushParentObjects() {
		userObjectStack.addLast(userObject);
		nodeCreatorStack.addLast(nodeCreator);
	}

	/**
	 */
	public void setElementContent(final String content) {
		final Iterator iterator = getNodeContentLoaders().iterator(tag);
		final IXMLElement xmlElement = xmlBuilder.getLastBuiltElement();
		while (iterator.hasNext() && userObject != null) {
			final INodeContentHandler ncl = (INodeContentHandler) iterator
			    .next();
			if (ncl.setContent(userObject, tag, xmlElement, content)) {
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#startBuilding(java.lang
	 * .String, int)
	 */
	public void startBuilding(final String systemID, final int lineNr)
	        throws Exception {
		xmlBuilder = new StdXMLBuilder();
		xmlBuilder.startBuilding(systemID, lineNr);
		saveAsXmlUntil = null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#startElement(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void startElement(final String name, final String nsPrefix,
	                         final String nsURI, final String systemID,
	                         final int lineNr) throws Exception {
		if (saveAsXmlUntil != null) {
			xmlBuilder.startElement(name, nsPrefix, nsURI, systemID, lineNr);
			return;
		}
		final IXMLElement top = xmlBuilder.getParentElement();
		if (top != null && top.hasChildren()) {
			top.removeChildAtIndex(0);
		}
		xmlBuilder.startElement(name, nsPrefix, nsURI, systemID, lineNr);
		tag = name;
		pushParentObjects();
		final Object parent = userObject;
		userObject = null;
		final Iterator iterator = getNodeCreators().iterator(tag);
		while (iterator.hasNext() && userObject == null) {
			nodeCreator = (INodeCreator) iterator.next();
			userObject = nodeCreator.createNode(parent, name);
		}
		if (userObject != null) {
			if (!getNodeContentLoaders().isEmpty(tag)) {
				parser.notParseNextElementContent();
			}
		}
		else {
			userObject = parent;
			saveAsXmlUntil = xmlBuilder.getLastBuiltElement();
		}
	}
}
