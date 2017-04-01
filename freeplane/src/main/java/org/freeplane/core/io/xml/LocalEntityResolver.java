package org.freeplane.core.io.xml;

import java.io.Reader;

import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.XMLEntityResolver;
import org.freeplane.n3.nanoxml.XMLParseException;

final class LocalEntityResolver extends XMLEntityResolver {
	@Override
	protected Reader openExternalEntity(IXMLReader xmlReader, String publicID, String systemID)
	        throws XMLParseException {
		throw new XMLParseException("External entities are not allowed");
	}
}