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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

/** a thin wrapper around {@link SimpleDateFormat}, {@link DecimalFormat} and {@link Formatter}.
 * <p>
 * Parsing is not supported! */
public abstract class PatternFormat /*extends Format*/ {
	private static final String SERIALIZATION_SEPARATOR = ":";
	public static final String IDENTITY_PATTERN = "NO_FORMAT";
	public static final String STANDARD_FORMAT_PATTERN = "STANDARD_FORMAT";

	private static final PatternFormat IDENTITY = new IdentityPatternFormat();
	private static final PatternFormat STANDARD = new StandardPatternFormat();
	static final String STYLE_FORMATTER = "formatter";
	static final String STYLE_DATE = "date";
	static final String STYLE_DECIMAL = "decimal";
	static final String TYPE_IDENTITY = "identity";
	static final String TYPE_STANDARD = "standard";
	private static final String ELEMENT_NAME = "format";
	private final String type;
	private final String pattern;
	private String name;
	private Locale locale;

	public PatternFormat(String pattern, String type) {
		this.pattern = pattern;
		this.type = type;
	}

	/** the formal format description. */
	public String getPattern() {
		return pattern;
	}

	/** selects the kind of data the formatter is intended to format. */
	public String getType() {
		return type;
	}

	public String getName() {
		return name;
    }
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
    }

	/** selects the formatter implementation, e.g. "formatter" or "date" */
	public abstract String getStyle();
	
	// yyyy-MM-dd HH:mm:ss
	final static Pattern datePattern = Pattern.compile("yy|[Hh]{1,2}:mm");

	// %[argument_index$] [flags] [width] conversion
	// == conversions
	// ignore boolean: bB
	// ignore hash: hH
	// sS
	// ignore char: cC
	// number: doxXeEfgGaA
	// ignore literals: %n
	// time prefix: tT
	final static Pattern formatterPattern = Pattern.compile("%" //
		// + "(?:[\\d<]+\\$)?" // Freeplane: no support for argument index$!
		+ "(?:[-#+ 0,(]+)?" // flags
		+ "(?:[\\d.]+)?" // width
		+ "([sSdoxXeEfgGaA]|[tT][HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc])"); // conversion

	// this method is null safe
	public static PatternFormat guessPatternFormat(final String pattern) {
		try {
			if (pattern == null || pattern.length() == 0)
				return null;
			final Matcher matcher = formatterPattern.matcher(pattern);
			if (matcher.find()) {
				// System.err.println("pattern='" + pattern + "' match='" + matcher.group() + "'");
				final char conversion = matcher.group(1).charAt(0);
				if (matcher.find()) {
					LogUtils.warn("found multiple formats in this formatter pattern: '" + pattern + "'");
					return null;
				}
				switch (conversion) {
					case 's':
					case 'S':
						return new FormatterPatternFormat(pattern, IFormattedObject.TYPE_STRING);
					case 'd':
					case 'o':
					case 'x':
					case 'X':
					case 'e':
					case 'E':
					case 'f':
					case 'g':
					case 'G':
					case 'a':
					case 'A':
						return new FormatterPatternFormat(pattern, IFormattedObject.TYPE_NUMBER);
					case 't':
					case 'T':
						return new FormatterPatternFormat(pattern, IFormattedObject.TYPE_DATE);
				}
			}
			if (datePattern.matcher(pattern).find()) {
				return new DatePatternFormat(pattern);
			}
			if (pattern.indexOf('#') != -1 || pattern.indexOf('0') != -1) {
				return new DecimalPatternFormat(pattern);
			}
			// only as a last resort?!
			for(PatternFormat f : Controller.getCurrentController().getExtension(FormatController.class).getSpecialFormats()){
				if (pattern.equals(f.getPattern())) {
					return f;
				}
			}
			LogUtils.warn("not a pattern format: '" + pattern + "'");
			return null;
		}
		catch (Exception e) {
			LogUtils.warn("can't build a formatter for this pattern '" + pattern + "'", e);
			return null;
		}
	}

	public static PatternFormat getIdentityPatternFormat() {
	    return IDENTITY;
    }

    public static PatternFormat getStandardPatternFormat() {
        return STANDARD;
    }

	public XMLElement toXml() {
		final XMLElement xmlElement = new XMLElement(ELEMENT_NAME);
		xmlElement.setAttribute("type", getType());
		xmlElement.setAttribute("style", getStyle());
		if (getName() != null)
			xmlElement.setAttribute("name", getName());
		if (getLocale() != null)
			xmlElement.setAttribute("locale", getLocale().toString());
		xmlElement.setContent(getPattern());
		return xmlElement;
	}

	public String serialize() {
		return getType() + SERIALIZATION_SEPARATOR +  getStyle() + SERIALIZATION_SEPARATOR + TypeReference.encode(getPattern()); 
    }

	public static PatternFormat deserialize(String string) {
		final String[] tokens = string.split(SERIALIZATION_SEPARATOR, 3);
	    return FormatController.getController().createFormat(TypeReference.decode(tokens[2]), tokens[1], tokens[0]);
    }

	public boolean acceptsDate() {
	    return getType().equals(IFormattedObject.TYPE_DATE) || getPattern().equals(STANDARD_FORMAT_PATTERN);
    }
	
	public boolean acceptsNumber() {
		return getType().equals(IFormattedObject.TYPE_NUMBER) || getPattern().equals(STANDARD_FORMAT_PATTERN);
	}
	
	public boolean acceptsString() {
		return getType().equals(IFormattedObject.TYPE_STRING) || getPattern().equals(STANDARD_FORMAT_PATTERN);
	}

	abstract public Object formatObject(Object toFormat);

	@Override
    public int hashCode() {
		final String style = getStyle();
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
	    result = prime * result + ((style == null) ? 0 : style.hashCode());
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    PatternFormat other = (PatternFormat) obj;
	    if (pattern == null) {
		    if (other.pattern != null)
			    return false;
	    }
	    else if (!pattern.equals(other.pattern))
		    return false;
		final String style = getStyle();
	    if (style == null) {
		    if (other.getStyle() != null)
			    return false;
	    }
	    else if (!style.equals(other.getStyle()))
		    return false;
	    if (type == null) {
		    if (other.type != null)
			    return false;
	    }
	    else if (!type.equals(other.type))
		    return false;
	    return true;
    }

    @Override
    public String toString() {
        return pattern;
    }
    
    public boolean canFormat(Class<?> clazz){
    	return true;
    }
}
