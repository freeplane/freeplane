/*
 * StdXMLBuilder.java NanoXML/Java $Revision: 1.3 $ $Date: 2002/01/04 21:03:28 $
 * $Name: RELEASE_2_2_1 $ This file is part of NanoXML 2 for Java. Copyright (C)
 * 2000-2002 Marc De Scheemaecker, All Rights Reserved. This software is
 * provided 'as-is', without any express or implied warranty. In no event will
 * the authors be held liable for any damages arising from the use of this
 * software. Permission is granted to anyone to use this software for any
 * purpose, including commercial applications, and to alter it and redistribute
 * it freely, subject to the following restrictions: 1. The origin of this
 * software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment
 * in the product documentation would be appreciated but is not required. 2.
 * Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software. 3. This notice may not be
 * removed or altered from any source distribution.
 */
package org.freeplane.core.io.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import org.freeplane.n3.nanoxml.IXMLBuilder;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * StdXMLBuilder is a concrete implementation of IXMLBuilder which creates a
 * tree of IXMLElement from an XML data source.
 * 
 * @see org.freeplane.n3.nanoxml.XMLElement
 * @author Marc De Scheemaecker
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.3 $ Modified by Dimitry
 *          Polivaev: method getLastBuiltElement added
 */
class StdXMLBuilder implements IXMLBuilder {
	/**
	 * The last built element of the parsed XML tree.
	 */
	private XMLElement last;
	/**
	 * Prototype element for creating the tree.
	 */
	private XMLElement prototype;
	/**
	 * The root element of the parsed XML tree.
	 */
	private XMLElement root;
	/**
	 * This stack contains the current element and its parents.
	 */
	private Stack<XMLElement> stack;

	/**
	 * Creates the builder.
	 */
	public StdXMLBuilder() {
		this(new XMLElement());
	}

	/**
	 * Creates the builder.
	 * 
	 * @param prototype
	 *            the prototype to use when building the tree.
	 */
	public StdXMLBuilder(final XMLElement prototype) {
		stack = null;
		root = null;
		last = null;
		this.prototype = prototype;
	}

	/**
	 * This method is called when a new attribute of an XML element is
	 * encountered.
	 * 
	 * @param key
	 *            the key (name) of the attribute.
	 * @param nsPrefix
	 *            the prefix used to identify the namespace. If no namespace has
	 *            been specified, this parameter is null.
	 * @param nsURI
	 *            the URI associated with the namespace. If no namespace has
	 *            been specified, or no URI is associated with nsPrefix, this
	 *            parameter is null.
	 * @param value
	 *            the value of the attribute.
	 * @param type
	 *            the type of the attribute. If no type is known, "CDATA" is
	 *            returned.
	 * @throws java.lang.Exception
	 *             If an exception occurred while processing the event.
	 */
	public void addAttribute(final String key, final String nsPrefix, final String nsURI, final String value,
	                         final String type) throws Exception {
		String fullName = key;
		if (nsPrefix != null) {
			fullName = nsPrefix + ':' + key;
		}
		final XMLElement top = stack.peek();
		if (top.hasAttribute(fullName)) {
			throw new XMLParseException(top.getSystemID(), top.getLineNr(), "Duplicate attribute: " + key);
		}
		if (nsPrefix != null) {
			top.setAttribute(fullName, nsURI, value);
		}
		else {
			top.setAttribute(fullName, value);
		}
	}

	/**
	 * This method is called when a PCDATA element is encountered. A Java reader
	 * is supplied from which you can read the data. The reader will only read
	 * the data of the element. You don't need to check for boundaries. If you
	 * don't read the full element, the rest of the data is skipped. You also
	 * don't have to care about entities; they are resolved by the parser.
	 * 
	 * @param reader
	 *            the Java reader from which you can retrieve the data.
	 * @param systemID
	 *            the system ID of the XML data source.
	 * @param lineNr
	 *            the line in the source where the element starts.
	 */
	public void addPCData(final Reader reader, final String systemID, final int lineNr) {
		int bufSize = 2048;
		int sizeRead = 0;
		final StringBuilder str = new StringBuilder(bufSize);
		final char[] buf = new char[bufSize];
		for (;;) {
			if (sizeRead >= bufSize) {
				bufSize *= 2;
				str.ensureCapacity(bufSize);
			}
			int size;
			try {
				size = reader.read(buf);
			}
			catch (final IOException e) {
				break;
			}
			if (size < 0) {
				break;
			}
			str.append(buf, 0, size);
			sizeRead += size;
		}
		final XMLElement elt = prototype.createElement(null, systemID, lineNr);
		elt.setContent(str.toString());
		if (!stack.empty()) {
			final XMLElement top = (XMLElement) stack.peek();
			top.addChild(elt);
		}
	}

	/**
	 * This method is called when the attributes of an XML element have been
	 * processed.
	 * 
	 * @see #startElement
	 * @see #addAttribute
	 * @param name
	 *            the name of the element.
	 * @param nsPrefix
	 *            the prefix used to identify the namespace. If no namespace has
	 *            been specified, this parameter is null.
	 * @param nsURI
	 *            the URI associated with the namespace. If no namespace has
	 *            been specified, or no URI is associated with nsPrefix, this
	 *            parameter is null.
	 */
	public void elementAttributesProcessed(final String name, final String nsPrefix, final String nsURI) {
	}

	/**
	 * This method is called when the end of an XML elemnt is encountered.
	 * 
	 * @see #startElement
	 * @param name
	 *            the name of the element.
	 * @param nsPrefix
	 *            the prefix used to identify the namespace. If no namespace has
	 *            been specified, this parameter is null.
	 * @param nsURI
	 *            the URI associated with the namespace. If no namespace has
	 *            been specified, or no URI is associated with nsPrefix, this
	 *            parameter is null.
	 */
	public void endElement(final String name, final String nsPrefix, final String nsURI) {
		final XMLElement elt = (XMLElement) stack.pop();
		if (elt.getChildrenCount() == 1) {
			final XMLElement child = elt.getChildAtIndex(0);
			if (child.getName() == null) {
				elt.setContent(child.getContent());
				elt.removeChildAtIndex(0);
			}
		}
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		prototype = null;
		root = null;
		last = null;
		stack.clear();
		stack = null;
		super.finalize();
	}

	public XMLElement getLastBuiltElement() {
		return last;
	}

	public XMLElement getParentElement() {
		return root != null ? (XMLElement) stack.peek() : null;
	}

	/**
	 * Returns the result of the building process. This method is called just
	 * before the <I>parse</I> method of IXMLParser returns.
	 * 
	 * @see org.freeplane.n3.nanoxml.IXMLParser#parse
	 * @return the result of the building process.
	 */
	public Object getResult() {
		return root;
	}

	/**
	 * This method is called when a processing instruction is encountered. PIs
	 * with target "xml" are handled by the parser.
	 * 
	 * @param target
	 *            the PI target.
	 * @param reader
	 *            to read the data from the PI.
	 */
	public void newProcessingInstruction(final String target, final Reader reader) {
	}

	/**
	 * This method is called before the parser starts processing its input.
	 * 
	 * @param systemID
	 *            the system ID of the XML data source.
	 * @param lineNr
	 *            the line on which the parsing starts.
	 */
	public void startBuilding(final String systemID, final int lineNr) {
		stack = new Stack<XMLElement>();
		root = null;
		last = null;
	}

	/**
	 * This method is called when a new XML element is encountered.
	 * 
	 * @see #endElement
	 * @param name
	 *            the name of the element.
	 * @param nsPrefix
	 *            the prefix used to identify the namespace. If no namespace has
	 *            been specified, this parameter is null.
	 * @param nsURI
	 *            the URI associated with the namespace. If no namespace has
	 *            been specified, or no URI is associated with nsPrefix, this
	 *            parameter is null.
	 * @param systemID
	 *            the system ID of the XML data source.
	 * @param lineNr
	 *            the line in the source where the element starts.
	 */
	public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID,
	                         final int lineNr) {
		String fullName = name;
		if (nsPrefix != null) {
			fullName = nsPrefix + ':' + name;
		}
		final XMLElement elt = new XMLElement(fullName, nsURI, systemID, lineNr);
		last = elt;
		if (stack.empty()) {
			root = elt;
		}
		else {
			final XMLElement top = (XMLElement) stack.peek();
			top.addChild(elt);
		}
		stack.push(elt);
	}
}
