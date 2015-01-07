package org.freeplane.core.ui.menubuilders;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlEntryStructureBuilder implements Builder{


	static final String ENTRY = "entry";
	private Reader stringReader;

	public XmlEntryStructureBuilder(Reader stringReader) {
		this.stringReader = stringReader;
		
	}

	public void build(Entry target) {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new MenuStructureXmlHandler(target));
			xmlReader.parse(new InputSource(stringReader));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static public Entry buildMenuStructure(String xmlWithoutContent) {
		XmlEntryStructureBuilder builder = new XmlEntryStructureBuilder(new StringReader(xmlWithoutContent));
		Entry initialMenuStructure = new Entry();
		builder.build(initialMenuStructure);
		return initialMenuStructure;
	}

}

class MenuStructureXmlHandler extends DefaultHandler {
	final private LinkedList<Entry> childStack;
	private static final String BUILDER = "builder";

	public MenuStructureXmlHandler(Entry root) {
		 childStack = new LinkedList<>();
		 childStack.add(root);
	}

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes)
			throws SAXException {
		if(qName.equals(XmlEntryStructureBuilder.ENTRY)){
			final Entry child = new Entry();
			for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++){
				final String attributeName = attributes.getQName(attributeIndex);
				final String attributeValue = attributes.getValue(attributeName);
				if(attributeName.equals(BUILDER))
					child.setBuilders(Arrays.asList(attributeValue.split("\\s*,\\s*")));
				else if(attributeName.equals("name"))
					child.setName(attributeValue);
				else
					child.setAttribute(attributeName, attributeValue);
			}
			childStack.getLast().addChild(child);
			childStack.add(child);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		childStack.removeLast();
	}
	
	
}
