package org.freeplane.core.io.xml;

import org.freeplane.n3.nanoxml.IXMLParser;

public class XMLLocalParserFactory {

	public static IXMLParser createLocalXMLParser() {
		IXMLParser parser = org.freeplane.n3.nanoxml.XMLParserFactory.createDefaultXMLParser();
		parser.setResolver(new LocalEntityResolver());
		return parser;
	}
}
