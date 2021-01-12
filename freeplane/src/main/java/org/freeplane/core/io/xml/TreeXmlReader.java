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
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.IXMLBuilder;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.NonValidator;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

public class TreeXmlReader implements IXMLBuilder {
	public static boolean xmlToBoolean(final String string) {
		if (string == null) {
			return false;
		}
		if (string.equals("true")) {
			return true;
		}
		return false;
	}

	/**
	 * Extracts a long from xml. Only useful for dates.
	 */
	public static Date xmlToDate(final String xmlString) {
		try {
			return new Date(Long.valueOf(xmlString).longValue());
		}
		catch (final Exception e) {
			return new Date(System.currentTimeMillis());
		}
	}

	public static Point xmlToPoint(String string) {
		if (string == null) {
			return null;
		}
		if (string.startsWith("java.awt.Point")) {
			string = string.replaceAll("java\\.awt\\.Point\\[x=(-?[0-9]+),y=(-?[0-9]+)\\]", "$1;$2");
		}
		final StringTokenizer tok = new StringTokenizer(string, ";");
		if (tok.countTokens() != 2) {
			throw new IllegalArgumentException("A point must consist of two numbers (and not: '" + string + "').");
		}
		final int x = Quantity.fromString(tok.nextToken(), LengthUnit.px).toBaseUnitsRounded();
		final int y = Quantity.fromString(tok.nextToken(), LengthUnit.px).toBaseUnitsRounded();
		return new Point(x, y);
	}

	private Hashtable<String, IAttributeHandler> attributeHandlersForTag;
	private Object currentElement;
	private String elementContentAsString;
	final private LinkedList<Object> elementStack = new LinkedList<Object>();
	private IElementHandler nodeCreator;
	final private LinkedList<IElementHandler> nodeCreatorStack = new LinkedList<IElementHandler>();
	private Object parentElement;
	final private ReadManager parseManager;
	private XMLParser parser;
	private XMLElement saveAsXmlUntil;
	private String tag;
	private StdXMLBuilder xmlBuilder;

	public TreeXmlReader(final ReadManager parseManager) {
		super();
		this.parseManager = parseManager;
	}

	private boolean addAttribute(final String key, final String value) {
		if (saveAsXmlUntil == null && attributeHandlersForTag != null) {
			final IAttributeHandler attributeHandler = attributeHandlersForTag.get(key);
			if (attributeHandler != null) {
				try {
					attributeHandler.setAttribute(currentElement, value);
					return true;
				} catch (Exception e) {
					LogUtils.severe("Can not process attribute" + key + " = '" + value + "'", e);
				}
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
	public void addAttribute(final String key, final String nsPrefix, final String nsURI, final String value,
	                         final String type) throws Exception {
		if (!addAttribute(key, value)) {
			xmlBuilder.addAttribute(key, nsPrefix, nsURI, value, type);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#addPCData(java.io.Reader
	 * , java.lang.String, int)
	 */
	public void addPCData(final Reader reader, final String systemID, final int lineNr) throws Exception {
		xmlBuilder.addPCData(reader, systemID, lineNr);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#elementAttributesProcessed
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	public void elementAttributesProcessed(final String name, final String nsPrefix, final String nsURI)
	        throws Exception {
		xmlBuilder.elementAttributesProcessed(name, nsPrefix, nsURI);
		if (saveAsXmlUntil != null || nodeCreator != null) {
			return;
		}
		final Iterator<IElementHandler> iterator = getElementHandlers().iterator(tag);
		final XMLElement lastBuiltElement = xmlBuilder.getLastBuiltElement();
		while (iterator.hasNext() && currentElement == null) {
			nodeCreator = iterator.next();
			try {
				currentElement = nodeCreator.createElement(parentElement, name, lastBuiltElement);
			} catch (Exception e) {
				LogUtils.severe("Can not process element" + name, e);
			}
		}
		if (currentElement != null) {
			if (nodeCreator instanceof IElementContentHandler && ((IElementContentHandler)nodeCreator).containsXml(lastBuiltElement)) {
				parser.notParseNextElementContent();
			}
			attributeHandlersForTag = getAttributeLoaders().get(tag);
			if (attributeHandlersForTag == null) {
				return;
			}
			final Enumeration<String> attributeNames = lastBuiltElement.enumerateAttributeNames();
			while (attributeNames.hasMoreElements()) {
				final String atName = (String) attributeNames.nextElement();
				if (addAttribute(atName, lastBuiltElement.getAttribute(atName, null))) {
					lastBuiltElement.removeAttribute(atName);
				}
			}
		}
		else {
			currentElement = null;
			nodeCreator = null;
			saveAsXmlUntil = lastBuiltElement;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#endElement(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void endElement(final String name, final String nsPrefix, final String nsURI) throws Exception {
		final XMLElement lastBuiltElement = xmlBuilder.getParentElement();
		xmlBuilder.endElement(name, nsPrefix, nsURI);
		if (saveAsXmlUntil == lastBuiltElement) {
			saveAsXmlUntil = null;
		}
		if (saveAsXmlUntil != null) {
			return;
		}
		tag = null;
		if (0 == elementStack.size()) {
			return;
		}
		final Object element = currentElement;
		currentElement = elementStack.removeLast();
		try {
			if (nodeCreator instanceof IElementContentHandler) {
				IElementContentHandler contentHandler = (IElementContentHandler) nodeCreator;
                contentHandler.endElement(currentElement, name, element, lastBuiltElement,
						contentHandler.containsXml(lastBuiltElement) ? elementContentAsString : lastBuiltElement.getContent());
			}
			else if (nodeCreator instanceof IElementDOMHandler) {
				((IElementDOMHandler) nodeCreator).endElement(currentElement, name, element, lastBuiltElement);
			}
		} catch (Exception e) {
			LogUtils.severe("Can not process element" + name, e);
		}
		final XMLElement top = lastBuiltElement.getParent();
		if (nodeCreator != null && top != null && top.hasChildren()) {
			final int lastChildIndex = top.getChildrenCount() - 1;
			top.removeChildAtIndex(lastChildIndex);
		}
		nodeCreator = (IElementHandler) nodeCreatorStack.removeLast();
		elementContentAsString = null;
	}

	private Hashtable<String, Hashtable<String, IAttributeHandler>> getAttributeLoaders() {
		return parseManager.getAttributeHandlers();
	}

	private ListHashTable<String, IElementHandler> getElementHandlers() {
		return parseManager.getElementHandlers();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#getResult()
	 */
	public Object getResult() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#load()
	 */
	public void load(final Reader reader) throws XMLException {
		parser = new XMLParser();
		final IXMLReader nanoxmlReader = new StdXMLReader(reader);
		parser.setReader(nanoxmlReader);
		parser.setBuilder(this);
		parser.setValidator(new NonValidator());
		parser.parse();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#newProcessingInstruction
	 * (java.lang.String, java.io.Reader)
	 */
	public void newProcessingInstruction(final String target, final Reader reader) throws Exception {
		xmlBuilder.newProcessingInstruction(target, reader);
	}

	private void pushParentObjects() {
		elementStack.addLast(currentElement);
		nodeCreatorStack.addLast(nodeCreator);
	}

	/**
	 */
	public void setElementContent(final String content) {
		elementContentAsString = content;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.xml.n3.nanoxml.IXMLBuilder#startBuilding(java.lang
	 * .String, int)
	 */
	public void startBuilding(final String systemID, final int lineNr) throws Exception {
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
	public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID,
	                         final int lineNr) throws Exception {
		if (saveAsXmlUntil != null) {
			xmlBuilder.startElement(name, nsPrefix, nsURI, systemID, lineNr);
			return;
		}
		xmlBuilder.startElement(name, nsPrefix, nsURI, systemID, lineNr);
		tag = name;
		pushParentObjects();
		parentElement = currentElement;
		currentElement = null;
		final List<IElementHandler> handlers = getElementHandlers().list(tag);
		if (handlers != null && handlers.size() == 1) {
			nodeCreator = handlers.get(0);
			try {
				currentElement = nodeCreator.createElement(parentElement, tag, null);
			} catch (Exception e) {
				LogUtils.severe("Can not process element" + tag, e);
			}
		}
		if (currentElement != null) {
			attributeHandlersForTag = getAttributeLoaders().get(tag);
			if (nodeCreator instanceof IElementContentHandler) {
				parser.notParseNextElementContent();
			}
		}
		else {
			attributeHandlersForTag = null;
			currentElement = null;
			nodeCreator = null;
		}
	}

	public void load(Object currentElement, Reader pReader) throws XMLException {
	    this.currentElement = currentElement;
	    load(pReader);
    }
}
