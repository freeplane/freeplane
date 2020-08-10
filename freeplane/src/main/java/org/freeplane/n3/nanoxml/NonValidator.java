/*
 * NonValidator.java NanoXML/Java $Revision: 1.4 $ $Date: 2002/02/03 21:19:38 $
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
package org.freeplane.n3.nanoxml;

import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;

/**
 * NonValidator is a concrete implementation of IXMLValidator which processes
 * the DTD and handles entity definitions. It does not do any validation itself.
 * 
 * @author Marc De Scheemaecker
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.4 $
 */
public class NonValidator implements IXMLValidator {
	/**
	 * Contains the default values for attributes for the different element
	 * types.
	 */
	protected Hashtable<String, Properties> attributeDefaultValues;
	/**
	 * The stack of elements to be processed.
	 */
	protected Stack<Properties> currentElements;
	/**
	 * The parameter entity resolver.
	 */
	protected IXMLEntityResolver parameterEntityResolver;

	/**
	 * Creates the &quot;validator&quot;.
	 */
	public NonValidator() {
		attributeDefaultValues = new Hashtable<String, Properties>();
		currentElements = new Stack<Properties>();
		parameterEntityResolver = new XMLEntityResolver();
	}

	/**
	 * Indicates that an attribute has been added to the current element.
	 * 
	 * @param key
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @param systemId
	 *            the system ID of the XML data of the element.
	 * @param lineNr
	 *            the line number in the XML data of the element.
	 */
	public void attributeAdded(final String key, final String value, final String systemId, final int lineNr) {
		final Properties props = currentElements.peek();
		if (props.containsKey(key)) {
			props.remove(key);
		}
	}

	/**
	 * This method is called when the attributes of an XML element have been
	 * processed. If there are attributes with a default value which have not
	 * been specified yet, they have to be put into <I>extraAttributes</I>.
	 * 
	 * @param name
	 *            the name of the element.
	 * @param extraAttributes
	 *            where to put extra attributes.
	 * @param systemId
	 *            the system ID of the XML data of the element.
	 * @param lineNr
	 *            the line number in the XML data of the element.
	 */
	public void elementAttributesProcessed(final String name, final Properties extraAttributes, final String systemId,
	                                       final int lineNr) {
		final Properties props = (Properties) currentElements.pop();
		final Enumeration<Object> enumeration = props.keys();
		while (enumeration.hasMoreElements()) {
			final String key = (String) enumeration.nextElement();
			extraAttributes.put(key, props.get(key));
		}
	}

	/**
	 * Indicates that the current element has ended.
	 * 
	 * @param name
	 *            the name of the element.
	 * @param systemId
	 *            the system ID of the XML data of the element.
	 * @param lineNr
	 *            the line number in the XML data of the element.
	 */
	public void elementEnded(final String name, final String systemId, final int lineNr) {
	}

	/**
	 * Indicates that an element has been started.
	 * 
	 * @param name
	 *            the name of the element.
	 * @param systemId
	 *            the system ID of the XML data of the element.
	 * @param lineNr
	 *            the line number in the XML data of the element.
	 */
	public void elementStarted(final String name, final String systemId, final int lineNr) {
		Properties attribs = (Properties) attributeDefaultValues.get(name);
		if (attribs == null) {
			attribs = new Properties();
		}
		else {
			attribs = (Properties) attribs.clone();
		}
		currentElements.push(attribs);
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		parameterEntityResolver = null;
		attributeDefaultValues.clear();
		attributeDefaultValues = null;
		currentElements.clear();
		currentElements = null;
		super.finalize();
	}

	/**
	 * Returns the parameter entity resolver.
	 * 
	 * @return the entity resolver.
	 */
	public IXMLEntityResolver getParameterEntityResolver() {
		return parameterEntityResolver;
	}

	/**
	 * Parses the DTD. The validator object is responsible for reading the full
	 * DTD.
	 * 
	 * @param publicID
	 *            the public ID, which may be null.
	 * @param reader
	 *            the reader to read the DTD from.
	 * @param entityResolver
	 *            the entity resolver.
	 * @param external
	 *            true if the DTD is external.
	 * @throws java.lang.Exception
	 *             If something went wrong.
	 */
	public void parseDTD(final String publicID, final IXMLReader reader, final IXMLEntityResolver entityResolver,
	                     final boolean external) throws Exception {
		XMLUtil.skipWhitespace(reader, null);
		final int origLevel = reader.getStreamLevel();
		for (;;) {
			final String str = XMLUtil.read(reader, '%');
			char ch = str.charAt(0);
			if (ch == '%') {
				XMLUtil.processEntity(str, reader, parameterEntityResolver);
				continue;
			}
			else if (ch == '<') {
				this.processElement(reader, entityResolver);
			}
			else if (ch == ']') {
				return;
			}
			else {
				XMLUtil.errorInvalidInput(reader.getSystemID(), reader.getLineNr(), str);
			}
			do {
				ch = reader.read();
				if (external && (reader.getStreamLevel() < origLevel)) {
					reader.unread(ch);
					return;
				}
			} while ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r'));
			reader.unread(ch);
		}
	}

	/**
	 * Indicates that a new #PCDATA element has been encountered.
	 * 
	 * @param systemId
	 *            the system ID of the XML data of the element.
	 * @param lineNr
	 *            the line number in the XML data of the element.
	 */
	public void PCDataAdded(final String systemId, final int lineNr) {
	}

	/**
	 * Processes an ATTLIST element.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @param entityResolver
	 *            the entity resolver.
	 * @throws java.lang.Exception
	 *             If something went wrong.
	 */
	protected void processAttList(final IXMLReader reader, final IXMLEntityResolver entityResolver) throws Exception {
		if (!XMLUtil.checkLiteral(reader, "TTLIST")) {
			XMLUtil.skipTag(reader);
			return;
		}
		XMLUtil.skipWhitespace(reader, null);
		String str = XMLUtil.read(reader, '%');
		char ch = str.charAt(0);
		while (ch == '%') {
			XMLUtil.processEntity(str, reader, parameterEntityResolver);
			str = XMLUtil.read(reader, '%');
			ch = str.charAt(0);
		}
		reader.unread(ch);
		final String elementName = XMLUtil.scanIdentifier(reader);
		XMLUtil.skipWhitespace(reader, null);
		str = XMLUtil.read(reader, '%');
		ch = str.charAt(0);
		while (ch == '%') {
			XMLUtil.processEntity(str, reader, parameterEntityResolver);
			str = XMLUtil.read(reader, '%');
			ch = str.charAt(0);
		}
		final Properties props = new Properties();
		while (ch != '>') {
			reader.unread(ch);
			final String attName = XMLUtil.scanIdentifier(reader);
			XMLUtil.skipWhitespace(reader, null);
			str = XMLUtil.read(reader, '%');
			ch = str.charAt(0);
			while (ch == '%') {
				XMLUtil.processEntity(str, reader, parameterEntityResolver);
				str = XMLUtil.read(reader, '%');
				ch = str.charAt(0);
			}
			if (ch == '(') {
				while (ch != ')') {
					str = XMLUtil.read(reader, '%');
					ch = str.charAt(0);
					while (ch == '%') {
						XMLUtil.processEntity(str, reader, parameterEntityResolver);
						str = XMLUtil.read(reader, '%');
						ch = str.charAt(0);
					}
				}
			}
			else {
				reader.unread(ch);
				XMLUtil.scanIdentifier(reader);
			}
			XMLUtil.skipWhitespace(reader, null);
			str = XMLUtil.read(reader, '%');
			ch = str.charAt(0);
			while (ch == '%') {
				XMLUtil.processEntity(str, reader, parameterEntityResolver);
				str = XMLUtil.read(reader, '%');
				ch = str.charAt(0);
			}
			if (ch == '#') {
				str = XMLUtil.scanIdentifier(reader);
				XMLUtil.skipWhitespace(reader, null);
				if (!str.equals("FIXED")) {
					XMLUtil.skipWhitespace(reader, null);
					str = XMLUtil.read(reader, '%');
					ch = str.charAt(0);
					while (ch == '%') {
						XMLUtil.processEntity(str, reader, parameterEntityResolver);
						str = XMLUtil.read(reader, '%');
						ch = str.charAt(0);
					}
					continue;
				}
			}
			else {
				reader.unread(ch);
			}
			final String value = XMLUtil.scanString(reader, '%', parameterEntityResolver);
			props.put(attName, value);
			XMLUtil.skipWhitespace(reader, null);
			str = XMLUtil.read(reader, '%');
			ch = str.charAt(0);
			while (ch == '%') {
				XMLUtil.processEntity(str, reader, parameterEntityResolver);
				str = XMLUtil.read(reader, '%');
				ch = str.charAt(0);
			}
		}
		if (!props.isEmpty()) {
			attributeDefaultValues.put(elementName, props);
		}
	}

	/**
	 * Processes a conditional section.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @param entityResolver
	 *            the entity resolver.
	 * @throws java.lang.Exception
	 *             If something went wrong.
	 */
	protected void processConditionalSection(final IXMLReader reader, final IXMLEntityResolver entityResolver)
	        throws Exception {
		XMLUtil.skipWhitespace(reader, null);
		String str = XMLUtil.read(reader, '%');
		char ch = str.charAt(0);
		if (ch != 'I') {
			XMLUtil.skipTag(reader);
			return;
		}
		str = XMLUtil.read(reader, '%');
		ch = str.charAt(0);
		switch (ch) {
			case 'G':
				this.processIgnoreSection(reader, entityResolver);
				return;
			case 'N':
				break;
			default:
				XMLUtil.skipTag(reader);
				return;
		}
		if (!XMLUtil.checkLiteral(reader, "CLUDE")) {
			XMLUtil.skipTag(reader);
			return;
		}
		XMLUtil.skipWhitespace(reader, null);
		str = XMLUtil.read(reader, '%');
		ch = str.charAt(0);
		if (ch != '[') {
			XMLUtil.skipTag(reader);
			return;
		}
		try (Reader subreader = new CDATAReader(reader)) {
	        final StringBuilder buf = new StringBuilder(1024);
	        for (;;) {
	            final int ch2 = subreader.read();
	            if (ch2 < 0) {
	                break;
	            }
	            buf.append((char) ch2);
	        }
	        reader.startNewStream(new StringReader(buf.toString()));
		}
	}

	/**
	 * Processes an element in the DTD.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @param entityResolver
	 *            the entity resolver.
	 * @throws java.lang.Exception
	 *             If something went wrong.
	 */
	protected void processElement(final IXMLReader reader, final IXMLEntityResolver entityResolver) throws Exception {
		String str = XMLUtil.read(reader, '%');
		char ch = str.charAt(0);
		if (ch != '!') {
			XMLUtil.skipTag(reader);
			return;
		}
		str = XMLUtil.read(reader, '%');
		ch = str.charAt(0);
		switch (ch) {
			case '-':
				XMLUtil.skipComment(reader);
				break;
			case '[':
				this.processConditionalSection(reader, entityResolver);
				break;
			case 'E':
				this.processEntity(reader, entityResolver);
				break;
			case 'A':
				this.processAttList(reader, entityResolver);
				break;
			default:
				XMLUtil.skipTag(reader);
		}
	}

	/**
	 * Processes an ENTITY element.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @param entityResolver
	 *            the entity resolver.
	 * @throws java.lang.Exception
	 *             If something went wrong.
	 */
	protected void processEntity(final IXMLReader reader, IXMLEntityResolver entityResolver) throws Exception {
		if (!XMLUtil.checkLiteral(reader, "NTITY")) {
			XMLUtil.skipTag(reader);
			return;
		}
		XMLUtil.skipWhitespace(reader, null);
		char ch = XMLUtil.readChar(reader, '\0');
		if (ch == '%') {
			XMLUtil.skipWhitespace(reader, null);
			entityResolver = parameterEntityResolver;
		}
		else {
			reader.unread(ch);
		}
		final String key = XMLUtil.scanIdentifier(reader);
		XMLUtil.skipWhitespace(reader, null);
		ch = XMLUtil.readChar(reader, '%');
		String systemID = null;
		String publicID = null;
		switch (ch) {
			case 'P':
				if (!XMLUtil.checkLiteral(reader, "UBLIC")) {
					XMLUtil.skipTag(reader);
					return;
				}
				XMLUtil.skipWhitespace(reader, null);
				publicID = XMLUtil.scanString(reader, '%', parameterEntityResolver);
				XMLUtil.skipWhitespace(reader, null);
				systemID = XMLUtil.scanString(reader, '%', parameterEntityResolver);
				XMLUtil.skipWhitespace(reader, null);
				XMLUtil.readChar(reader, '%');
				break;
			case 'S':
				if (!XMLUtil.checkLiteral(reader, "YSTEM")) {
					XMLUtil.skipTag(reader);
					return;
				}
				XMLUtil.skipWhitespace(reader, null);
				systemID = XMLUtil.scanString(reader, '%', parameterEntityResolver);
				XMLUtil.skipWhitespace(reader, null);
				XMLUtil.readChar(reader, '%');
				break;
			case '"':
			case '\'':
				reader.unread(ch);
				final String value = XMLUtil.scanString(reader, '%', parameterEntityResolver);
				entityResolver.addInternalEntity(key, value);
				XMLUtil.skipWhitespace(reader, null);
				XMLUtil.readChar(reader, '%');
				break;
			default:
				XMLUtil.skipTag(reader);
		}
		if (systemID != null) {
			entityResolver.addExternalEntity(key, publicID, systemID);
		}
	}

	/**
	 * Processes an ignore section.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @param entityResolver
	 *            the entity resolver.
	 * @throws java.lang.Exception
	 *             If something went wrong.
	 */
	protected void processIgnoreSection(final IXMLReader reader, final IXMLEntityResolver entityResolver)
	        throws Exception {
		if (!XMLUtil.checkLiteral(reader, "NORE")) {
			XMLUtil.skipTag(reader);
			return;
		}
		XMLUtil.skipWhitespace(reader, null);
		final String str = XMLUtil.read(reader, '%');
		final char ch = str.charAt(0);
		if (ch != '[') {
			XMLUtil.skipTag(reader);
			return;
		}
		try (final Reader subreader = new CDATAReader(reader)){/**/}
		
	}

	/**
	 * Sets the parameter entity resolver.
	 * 
	 * @param resolver
	 *            the entity resolver.
	 */
	public void setParameterEntityResolver(final IXMLEntityResolver resolver) {
		parameterEntityResolver = resolver;
	}
}
