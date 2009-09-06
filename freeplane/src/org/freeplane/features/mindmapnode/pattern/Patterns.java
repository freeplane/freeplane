package org.freeplane.features.mindmapnode.pattern;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.util.LogTool;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParserFactory;

public class Patterns {
	public static Patterns unMarshall(final Reader reader) {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader xmlReader = new StdXMLReader(reader);
			parser.setReader(xmlReader);
			final XMLElement xml = (XMLElement) parser.parse();
			final Patterns patterns = new Patterns();
			final Enumeration xmlPatterns = xml.enumerateChildren();
			while (xmlPatterns.hasMoreElements()) {
				final XMLElement xmlPattern = (XMLElement) xmlPatterns.nextElement();
				patterns.addChoice(Pattern.unMarshall(xmlPattern));
			}
			return patterns;
		}
		catch (final XMLException e) {
			LogTool.severe(e);
			return null;
		}
	}

	public static Patterns unMarshall(final String patternsString) {
		return Patterns.unMarshall(new StringReader(patternsString));
	}

	protected List<Pattern> choiceList = new ArrayList<Pattern>();

	public void addAtChoice(final int position, final Pattern choice) {
		choiceList.add(position, choice);
	}

	public void addChoice(final Pattern choice) {
		choiceList.add(choice);
	}

	public void clearChoiceList() {
		choiceList.clear();
	}

	public Pattern getChoice(final int index) {
		return choiceList.get(index);
	}

	public List<Pattern> getListChoiceList() {
		return java.util.Collections.unmodifiableList(choiceList);
	}

	public String marshall() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><patterns>");
		final Iterator<Pattern> iterator = choiceList.iterator();
		while (iterator.hasNext()) {
			final Pattern pattern = iterator.next();
			buffer.append(pattern.marshall());
		}
		buffer.append("</patterns>");
		return buffer.toString();
	}

	public int sizeChoiceList() {
		return choiceList.size();
	}
}
