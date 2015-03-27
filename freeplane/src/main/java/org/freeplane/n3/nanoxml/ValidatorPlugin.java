/*
 * ValidatorPlugin.java NanoXML/Java $Revision: 1.3 $ $Date: 2002/01/04 21:03:29
 * $ $Name: RELEASE_2_2_1 $ This file is part of NanoXML 2 for Java. Copyright
 * (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved. This software is
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

import java.util.Properties;

/**
 * ValidatorPlugin allows the application to insert additional validators into
 * NanoXML.
 * 
 * @author Marc De Scheemaecker
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.3 $
 */
class ValidatorPlugin implements IXMLValidator {
	/**
	 * The delegate.
	 */
	private IXMLValidator delegate;

	/**
	 * Initializes the plugin.
	 */
	public ValidatorPlugin() {
		delegate = null;
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
	 * @throws java.lang.Exception
	 *             if the element could not be validated.
	 */
	public void attributeAdded(final String key, final String value, final String systemId, final int lineNr)
	        throws Exception {
		delegate.attributeAdded(key, value, systemId, lineNr);
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
	 * @throws java.lang.Exception
	 *             if the attribute could not be validated.
	 */
	public void elementAttributesProcessed(final String name, final Properties extraAttributes, final String systemId,
	                                       final int lineNr) throws Exception {
		delegate.elementAttributesProcessed(name, extraAttributes, systemId, lineNr);
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
	 * @throws java.lang.Exception
	 *             if the element could not be validated.
	 */
	public void elementEnded(final String name, final String systemId, final int lineNr) throws Exception {
		delegate.elementEnded(name, systemId, lineNr);
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
	 * @throws java.lang.Exception
	 *             if the element could not be validated.
	 */
	public void elementStarted(final String name, final String systemId, final int lineNr) throws Exception {
		delegate.elementStarted(name, systemId, lineNr);
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		delegate = null;
		super.finalize();
	}

	/**
	 * Returns the delegate.
	 */
	public IXMLValidator getDelegate() {
		return delegate;
	}

	/**
	 * Returns the parameter entity resolver.
	 * 
	 * @return the entity resolver.
	 */
	public IXMLEntityResolver getParameterEntityResolver() {
		return delegate.getParameterEntityResolver();
	}

	/**
	 * Throws an XMLValidationException to indicate that an attribute has an
	 * invalid value.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param elementName
	 *            the name of the element
	 * @param attributeName
	 *            the name of the attribute
	 * @param attributeValue
	 *            the value of the attribute
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void invalidAttributeValue(final String systemID, final int lineNr, final String elementName,
	                                  final String attributeName, final String attributeValue)
	        throws XMLValidationException {
		XMLUtil.errorInvalidAttributeValue(systemID, lineNr, elementName, attributeName, attributeValue);
	}

	/**
	 * Throws an XMLValidationException to indicate that an attribute is
	 * missing.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param elementName
	 *            the name of the element
	 * @param attributeName
	 *            the name of the missing attribute
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void missingAttribute(final String systemID, final int lineNr, final String elementName,
	                             final String attributeName) throws XMLValidationException {
		XMLUtil.errorMissingAttribute(systemID, lineNr, elementName, attributeName);
	}

	/**
	 * Throws an XMLValidationException to indicate that an element is missing.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param parentElementName
	 *            the name of the parent element
	 * @param missingElementName
	 *            the name of the missing element
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void missingElement(final String systemID, final int lineNr, final String parentElementName,
	                           final String missingElementName) throws XMLValidationException {
		XMLUtil.errorMissingElement(systemID, lineNr, parentElementName, missingElementName);
	}

	/**
	 * Throws an XMLValidationException to indicate that a #PCDATA element was
	 * missing.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param parentElementName
	 *            the name of the parent element
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void missingPCData(final String systemID, final int lineNr, final String parentElementName)
	        throws XMLValidationException {
		XMLUtil.errorMissingPCData(systemID, lineNr, parentElementName);
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
	 *             if something went wrong.
	 */
	public void parseDTD(final String publicID, final IXMLReader reader, final IXMLEntityResolver entityResolver,
	                     final boolean external) throws Exception {
		delegate.parseDTD(publicID, reader, entityResolver, external);
	}

	/**
	 * Indicates that a new #PCDATA element has been encountered.
	 * 
	 * @param systemId
	 *            the system ID of the XML data of the element.
	 * @param lineNr
	 *            the line number in the XML data of the element.
	 * @throws java.lang.Exception
	 *             if the element could not be validated.
	 */
	public void PCDataAdded(final String systemId, final int lineNr) throws Exception {
		delegate.PCDataAdded(systemId, lineNr);
	}

	/**
	 * Sets the delegate.
	 * 
	 * @param delegate
	 *            the delegate
	 */
	public void setDelegate(final IXMLValidator delegate) {
		this.delegate = delegate;
	}

	/**
	 * Sets the parameter entity resolver.
	 * 
	 * @param resolver
	 *            the entity resolver.
	 */
	public void setParameterEntityResolver(final IXMLEntityResolver resolver) {
		delegate.setParameterEntityResolver(resolver);
	}

	/**
	 * Throws an XMLValidationException to indicate that an attribute is
	 * unexpected.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param elementName
	 *            the name of the element
	 * @param attributeName
	 *            the name of the unexpected attribute
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void unexpectedAttribute(final String systemID, final int lineNr, final String elementName,
	                                final String attributeName) throws XMLValidationException {
		XMLUtil.errorUnexpectedAttribute(systemID, lineNr, elementName, attributeName);
	}

	/**
	 * Throws an XMLValidationException to indicate that an element is
	 * unexpected.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param parentElementName
	 *            the name of the parent element
	 * @param unexpectedElementName
	 *            the name of the missing element
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void unexpectedElement(final String systemID, final int lineNr, final String parentElementName,
	                              final String unexpectedElementName) throws XMLValidationException {
		XMLUtil.errorUnexpectedElement(systemID, lineNr, parentElementName, unexpectedElementName);
	}

	/**
	 * Throws an XMLValidationException to indicate that a #PCDATA element was
	 * unexpected.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param parentElementName
	 *            the name of the parent element
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void unexpectedPCData(final String systemID, final int lineNr, final String parentElementName)
	        throws XMLValidationException {
		XMLUtil.errorUnexpectedPCData(systemID, lineNr, parentElementName);
	}

	/**
	 * Throws an XMLValidationException.
	 * 
	 * @param systemID
	 *            the system ID of the XML data of the element
	 * @param lineNr
	 *            the line number in the XML data of the element
	 * @param message
	 *            the error message
	 * @param elementName
	 *            the name of the element (may be null)
	 * @param attributeName
	 *            the name of the attribute (may be null)
	 * @param attributeValue
	 *            the value of the attribute (may be null)
	 * @throws org.freeplane.n3.nanoxml.XMLValidationException
	 *             of course :-)
	 */
	public void validationError(final String systemID, final int lineNr, final String message,
	                            final String elementName, final String attributeName, final String attributeValue)
	        throws XMLValidationException {
		XMLUtil.validationError(systemID, lineNr, message, elementName, attributeName, attributeValue);
	}
}
