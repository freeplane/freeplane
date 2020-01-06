package org.freeplane.core.ui.menubuilders;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.UserRoleConstraint;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlEntryStructureBuilder implements EntryVisitor{


	static final String ENTRY = "Entry";
	private Reader stringReader;

	public XmlEntryStructureBuilder(Reader stringReader) {
		this.stringReader = stringReader;
		
	}

	public void visit(Entry target) {
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

	static public Entry buildMenuStructure(String xml) {
		final Reader reader = new StringReader(xml);
		return buildMenuStructure(reader);
	}

	static public Entry buildMenuStructure(final Reader reader) {
		XmlEntryStructureBuilder builder = new XmlEntryStructureBuilder(reader);
		Entry initialMenuStructure = new Entry();
		builder.visit(initialMenuStructure);
		return initialMenuStructure;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}

}

class MenuStructureXmlHandler extends DefaultHandler {
	private static final String USED_BY = "usedBy";
	private static final String NAME = "name";
	private static final String BUILDER = "builder";
	final private LinkedList<Entry> childStack;

	public MenuStructureXmlHandler(Entry root) {
		 childStack = new LinkedList<Entry>();
		 childStack.add(root);
	}

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes)
			throws SAXException {
		if(qName.equals(XmlEntryStructureBuilder.ENTRY)){
			final Entry child = new Entry();
			for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++){
				final String attributeName = attributes.getQName(attributeIndex).intern();
				final String attributeValue = attributes.getValue(attributeName).intern();
				if(attributeName == BUILDER)
					child.setBuilders(Arrays.asList(attributeValue.split("\\s*,\\s*")));
				else if(attributeName == NAME)
					child.setName(attributeValue);
				else if(attributeName == USED_BY)
					child.addConstraint(UserRoleConstraint.valueOf(attributeValue));
				else
					child.setAttribute(attributeName, toValueObject(attributeValue));
			}
			Entry parent = childStack.getLast();
			child.addConstraint(parent);
			parent.addChild(child);
			childStack.add(child);
		}
	}

	private Object toValueObject(String attributeValue) {
		if("true".equalsIgnoreCase(attributeValue))
			return Boolean.TRUE;
		else if("false".equalsIgnoreCase(attributeValue))
			return Boolean.FALSE;
		else
		return attributeValue;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		childStack.removeLast();
	}
	
	
}
