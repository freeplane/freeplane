/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
package org.freeplane.features.format;

import java.util.Locale;

import org.freeplane.n3.nanoxml.XMLElement;

/** Scanner.scan(string) uses a number of Parsers to convert string into a Number, a Date or whatever. */
public abstract class Parser {
	public static final String STYLE_NUMBERLITERAL = "numberliteral";
	public static final String STYLE_DECIMAL = "decimal";
	public static final String STYLE_ISODATE = "isodate";
	public static final String STYLE_DATE = "date";
	private final String style;
	private final String type;
	private final String format;
	private String comment;

	public Parser(String style, String type, String format, String comment) {
		this.style = style;
		this.type = type;
		this.format = format;
		this.comment = comment;
	}

	public Parser(String style, String type, String format) {
		this(style, type, format, null);
	}

	/** tries to parse the string. Returns null if parsing does not succeed. 
	 * @throws nothing May not throw an exception. */
	abstract Object parse(final String string);

	public static Parser createParser(String style, String type, String format, Locale locale, String comment) {
		final Parser parser;
		if (style.equals(STYLE_NUMBERLITERAL))
			parser = new NumberLiteralParser();
		else if (style.equals(STYLE_DECIMAL))
			parser = new DecimalFormatParser(locale);
		else if (style.equals(STYLE_ISODATE))
			parser = new IsoDateParser();
		else if (style.equals(STYLE_DATE))
			parser = new DateFormatParser(format, type);
		else
			throw new IllegalArgumentException("illegal parser style " + style);
		parser.setComment(comment);
		return parser;
	}

	public XMLElement toXml() {
		final XMLElement xmlElement = new XMLElement("parser");
		xmlElement.setAttribute("type", getType());
		xmlElement.setAttribute("style", getStyle());
		if (getFormat() != null)
			xmlElement.setAttribute("format", getFormat());
		if (getComment() != null)
			xmlElement.setAttribute("comment", getComment());
		return xmlElement;
	}

	public String getStyle() {
		return style;
	}

	public String getType() {
		return type;
	}

	public String getFormat() {
		return format;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
