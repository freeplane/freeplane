/*
 * StdXMLParser.java NanoXML/Java $Revision: 1.5 $ $Date: 2002/03/24 11:37:00 $
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

import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * StdXMLParser is the core parser of NanoXML.
 * 
 * @author Marc De Scheemaecker re-factored by Dimitry Polivaev : method
 *         processElementContent() extracted
 */
public class StdXMLParser implements IXMLParser {
	/**
	 * The builder which creates the logical structure of the XML data.
	 */
	private IXMLBuilder builder;
	/**
	 * The entity resolver.
	 */
	private IXMLEntityResolver entityResolver;
	/**
	 * The reader from which the parser retrieves its data.
	 */
	private IXMLReader reader;
	/**
	 * The validator that will process entity references and validate the XML
	 * data.
	 */
	private IXMLValidator validator;

	/**
	 * Creates a new parser.
	 */
	public StdXMLParser() {
		builder = null;
		validator = null;
		reader = null;
		entityResolver = new XMLEntityResolver();
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		builder = null;
		reader = null;
		entityResolver = null;
		validator = null;
		super.finalize();
	}

	/**
	 * Returns the builder which creates the logical structure of the XML data.
	 * 
	 * @return the builder
	 */
	public IXMLBuilder getBuilder() {
		return builder;
	}

	/**
	 * Returns the reader from which the parser retrieves its data.
	 * 
	 * @return the reader
	 */
	public IXMLReader getReader() {
		return reader;
	}

	/**
	 * Returns the entity resolver.
	 * 
	 * @return the non-null resolver
	 */
	public IXMLEntityResolver getResolver() {
		return entityResolver;
	}

	/**
	 * Returns the validator that validates the XML data.
	 * 
	 * @return the validator
	 */
	public IXMLValidator getValidator() {
		return validator;
	}

	/**
	 * Parses the data and lets the builder create the logical data structure.
	 * 
	 * @return the logical structure built by the builder
	 * @throws org.freeplane.n3.nanoxml.XMLException
	 *             if an error occurred reading or parsing the data
	 */
	public Object parse() throws XMLException {
		try {
			builder.startBuilding(reader.getSystemID(), reader.getLineNr());
			this.scanData();
			return builder.getResult();
		}
		catch (final XMLException e) {
			throw e;
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Processes an attribute of an element.
	 * 
	 * @param attrNames
	 *            contains the names of the attributes.
	 * @param attrValues
	 *            contains the values of the attributes.
	 * @param attrTypes
	 *            contains the types of the attributes.
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void processAttribute(final Vector<String> attrNames, final Vector<String> attrValues,
	                                final Vector<String> attrTypes) throws Exception {
		final String key = XMLUtil.scanIdentifier(reader);
		XMLUtil.skipWhitespace(reader, null);
		if (!XMLUtil.read(reader, '&').equals("=")) {
			XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "`='");
		}
		XMLUtil.skipWhitespace(reader, null);
		final String value = XMLUtil.scanString(reader, '&', entityResolver);
		attrNames.addElement(key);
		attrValues.addElement(value);
		attrTypes.addElement("CDATA");
		validator.attributeAdded(key, value, reader.getSystemID(), reader.getLineNr());
	}

	/**
	 * Processes a CDATA section.
	 * 
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void processCDATA() throws Exception {
		if (!XMLUtil.checkLiteral(reader, "CDATA[")) {
			XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "<![[CDATA[");
		}
		validator.PCDataAdded(reader.getSystemID(), reader.getLineNr());
		try (final Reader reader = new CDATAReader(this.reader)) {
		    builder.addPCData(reader, this.reader.getSystemID(), this.reader.getLineNr());
		}
	}

	/**
	 * Processes a document type declaration.
	 * 
	 * @throws java.lang.Exception
	 *             if an error occurred reading or parsing the data
	 */
	protected void processDocType() throws Exception {
		if (!XMLUtil.checkLiteral(reader, "OCTYPE")) {
			XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "<!DOCTYPE");
			return;
		}
		XMLUtil.skipWhitespace(reader, null);
		String systemID = null;
		final StringBuilder publicID = new StringBuilder();
		XMLUtil.scanIdentifier(reader);
		XMLUtil.skipWhitespace(reader, null);
		char ch = reader.read();
		if (ch == 'P') {
			systemID = XMLUtil.scanPublicID(publicID, reader);
			XMLUtil.skipWhitespace(reader, null);
			ch = reader.read();
		}
		else if (ch == 'S') {
			systemID = XMLUtil.scanSystemID(reader);
			XMLUtil.skipWhitespace(reader, null);
			ch = reader.read();
		}
		if (ch == '[') {
			validator.parseDTD(publicID.toString(), reader, entityResolver, false);
			XMLUtil.skipWhitespace(reader, null);
			ch = reader.read();
		}
		if (ch != '>') {
			XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "`>'");
		}
		if (systemID != null) {
			final Reader reader = this.reader.openStream(publicID.toString(), systemID);
			this.reader.startNewStream(reader);
			this.reader.setSystemID(systemID);
			this.reader.setPublicID(publicID.toString());
			validator.parseDTD(publicID.toString(), this.reader, entityResolver, true);
		}
	}

	/**
	 * Processes a regular element.
	 * 
	 * @param defaultNamespace
	 *            the default namespace URI (or null)
	 * @param namespaces
	 *            list of defined namespaces
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void processElement(String defaultNamespace, final Properties namespaces) throws Exception {
		final String fullName = XMLUtil.scanIdentifier(reader);
		String name = fullName;
		XMLUtil.skipWhitespace(reader, null);
		String prefix = null;
		int colonIndex = name.indexOf(':');
		if (colonIndex > 0) {
			prefix = name.substring(0, colonIndex);
			name = name.substring(colonIndex + 1);
		}
		final Vector<String> attrNames = new Vector<String>();
		final Vector<String> attrValues = new Vector<String>();
		final Vector<String> attrTypes = new Vector<String>();
		validator.elementStarted(fullName, reader.getSystemID(), reader.getLineNr());
		char ch;
		for (;;) {
			ch = reader.read();
			if ((ch == '/') || (ch == '>')) {
				break;
			}
			reader.unread(ch);
			this.processAttribute(attrNames, attrValues, attrTypes);
			XMLUtil.skipWhitespace(reader, null);
		}
		final Properties extraAttributes = new Properties();
		validator.elementAttributesProcessed(fullName, extraAttributes, reader.getSystemID(), reader.getLineNr());
		final Enumeration<Object> enumeration = extraAttributes.keys();
		while (enumeration.hasMoreElements()) {
			final String key = (String) enumeration.nextElement();
			final String value = extraAttributes.getProperty(key);
			attrNames.addElement(key);
			attrValues.addElement(value);
			attrTypes.addElement("CDATA");
		}
		for (int i = 0; i < attrNames.size(); i++) {
			final String key = attrNames.elementAt(i);
			final String value = attrValues.elementAt(i);
			if (key.equals("xmlns")) {
				defaultNamespace = value;
			}
			else if (key.startsWith("xmlns:")) {
				namespaces.put(key.substring(6), value);
			}
		}
		if (prefix == null) {
			builder.startElement(name, prefix, defaultNamespace, reader.getSystemID(), reader.getLineNr());
		}
		else {
			builder
			    .startElement(name, prefix, namespaces.getProperty(prefix), reader.getSystemID(), reader.getLineNr());
		}
		for (int i = 0; i < attrNames.size(); i++) {
			String key = attrNames.elementAt(i);
			if (key.startsWith("xmlns")) {
				continue;
			}
			final String value = attrValues.elementAt(i);
			final String type = attrTypes.elementAt(i);
			colonIndex = key.indexOf(':');
			if (colonIndex > 0) {
				final String attPrefix = key.substring(0, colonIndex);
				key = key.substring(colonIndex + 1);
				builder.addAttribute(key, attPrefix, namespaces.getProperty(attPrefix), value, type);
			}
			else {
				builder.addAttribute(key, null, null, value, type);
			}
		}
		if (prefix == null) {
			builder.elementAttributesProcessed(name, prefix, defaultNamespace);
		}
		else {
			builder.elementAttributesProcessed(name, prefix, namespaces.getProperty(prefix));
		}
		if (ch == '/') {
			if (reader.read() != '>') {
				XMLUtil.errorExpectedInput(reader.getSystemID(), reader.getLineNr(), "`>'");
			}
			validator.elementEnded(name, reader.getSystemID(), reader.getLineNr());
			if (prefix == null) {
				builder.endElement(name, prefix, defaultNamespace);
			}
			else {
				builder.endElement(name, prefix, namespaces.getProperty(prefix));
			}
			return;
		}
		processElementContent(defaultNamespace, namespaces, fullName, name, prefix);
		XMLUtil.skipWhitespace(reader, null);
		final String str = XMLUtil.scanIdentifier(reader);
		if (!str.equals(fullName)) {
			XMLUtil.errorWrongClosingTag(reader.getSystemID(), reader.getLineNr(), name, str);
		}
		XMLUtil.skipWhitespace(reader, null);
		if (reader.read() != '>') {
			XMLUtil.errorClosingTagNotEmpty(reader.getSystemID(), reader.getLineNr());
		}
		validator.elementEnded(fullName, reader.getSystemID(), reader.getLineNr());
		if (prefix == null) {
			builder.endElement(name, prefix, defaultNamespace);
		}
		else {
			builder.endElement(name, prefix, namespaces.getProperty(prefix));
		}
	}

	protected void processElementContent(final String defaultNamespace, final Properties namespaces,
	                                     final String fullName, final String name, final String prefix)
	        throws IOException, XMLParseException, Exception {
		char ch;
		final StringBuilder buffer = new StringBuilder(16);
		for (;;) {
			buffer.setLength(0);
			String str;
			for (;;) {
				XMLUtil.skipWhitespace(reader, buffer);
				str = XMLUtil.read(reader, '&');
				if ((str.charAt(0) == '&') && (str.charAt(1) != '#')) {
					XMLUtil.processEntity(str, reader, entityResolver);
				}
				else {
					break;
				}
			}
			if (str.charAt(0) == '<') {
				str = XMLUtil.read(reader, '\0');
				if (str.charAt(0) == '/') {
					break;
				}
				else {
					reader.unread(str.charAt(0));
					this.scanSomeTag(true, defaultNamespace, (Properties) namespaces.clone());
				}
			}
			else {
				if (str.charAt(0) == '&') {
					ch = XMLUtil.processCharLiteral(str);
					buffer.append(ch);
				}
				else {
					reader.unread(str.charAt(0));
				}
				validator.PCDataAdded(reader.getSystemID(), reader.getLineNr());
				try (final Reader r = new ContentReader(reader, entityResolver, buffer.toString())) {
				    builder.addPCData(r, reader.getSystemID(), reader.getLineNr());
				}
			}
		}
	}

	/**
	 * Processes a "processing instruction".
	 * 
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void processPI() throws Exception {
		XMLUtil.skipWhitespace(reader, null);
		final String target = XMLUtil.scanIdentifier(reader);
		XMLUtil.skipWhitespace(reader, null);
		try (final Reader reader = new PIReader(this.reader)) {
		    if (!target.equalsIgnoreCase("xml")) {
		        builder.newProcessingInstruction(target, reader);
		    }
		}
	}

	/**
	 * Processes a tag that starts with a bang (&lt;!...&gt;).
	 * 
	 * @param allowCDATA
	 *            true if CDATA sections are allowed at this point
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void processSpecialTag(final boolean allowCDATA) throws Exception {
		final String str = XMLUtil.read(reader, '&');
		final char ch = str.charAt(0);
		if (ch == '&') {
			XMLUtil.errorUnexpectedEntity(reader.getSystemID(), reader.getLineNr(), str);
		}
		switch (ch) {
			case '[':
				if (allowCDATA) {
					this.processCDATA();
				}
				else {
					XMLUtil.errorUnexpectedCDATA(reader.getSystemID(), reader.getLineNr());
				}
				return;
			case 'D':
				this.processDocType();
				return;
			case '-':
				XMLUtil.skipComment(reader);
				return;
		}
	}

	/**
	 * Scans the XML data for elements.
	 * 
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void scanData() throws Exception {
		while ((!reader.atEOF()) && (builder.getResult() == null)) {
			final String str = XMLUtil.read(reader, '&');
			final char ch = str.charAt(0);
			if (ch == '&') {
				XMLUtil.processEntity(str, reader, entityResolver);
				continue;
			}
			switch (ch) {
				case '<':
					this.scanSomeTag(false, null, new Properties());
					break;
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break;
				default:
					XMLUtil.errorInvalidInput(reader.getSystemID(), reader.getLineNr(), "`" + ch + "' (0x"
					        + Integer.toHexString(ch) + ')');
			}
		}
	}

	/**
	 * Scans an XML tag.
	 * 
	 * @param allowCDATA
	 *            true if CDATA sections are allowed at this point
	 * @param defaultNamespace
	 *            the default namespace URI (or null)
	 * @param namespaces
	 *            list of defined namespaces
	 * @throws java.lang.Exception
	 *             if something went wrong
	 */
	protected void scanSomeTag(final boolean allowCDATA, final String defaultNamespace, final Properties namespaces)
	        throws Exception {
		final String str = XMLUtil.read(reader, '&');
		final char ch = str.charAt(0);
		if (ch == '&') {
			XMLUtil.errorUnexpectedEntity(reader.getSystemID(), reader.getLineNr(), str);
		}
		switch (ch) {
			case '?':
				this.processPI();
				break;
			case '!':
				this.processSpecialTag(allowCDATA);
				break;
			default:
				reader.unread(ch);
				this.processElement(defaultNamespace, namespaces);
		}
	}

	/**
	 * Sets the builder which creates the logical structure of the XML data.
	 * 
	 * @param builder
	 *            the non-null builder
	 */
	public void setBuilder(final IXMLBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Sets the reader from which the parser retrieves its data.
	 * 
	 * @param reader
	 *            the reader
	 */
	public void setReader(final IXMLReader reader) {
		this.reader = reader;
	}

	/**
	 * Sets the entity resolver.
	 * 
	 * @param resolver
	 *            the non-null resolver
	 */
	public void setResolver(final IXMLEntityResolver resolver) {
		entityResolver = resolver;
	}

	/**
	 * Sets the validator that validates the XML data.
	 * 
	 * @param validator
	 *            the non-null validator
	 */
	public void setValidator(final IXMLValidator validator) {
		this.validator = validator;
	}
}
