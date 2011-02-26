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
package org.freeplane.features.common.format;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.time.FastDateFormat;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

/**
 * @author Dimitry Polivaev
 */
public class FormatController implements IExtension {
	private static final String FORMATS_XML = "formats.xml";
	private static final String ROOT_ELEMENT = "formats";
	private String pathToFormatsFile;
	private List<PatternFormat> dateFormats = new ArrayList<PatternFormat>();
	private List<PatternFormat> numberFormats = new ArrayList<PatternFormat>();
	private List<PatternFormat> stringFormats = new ArrayList<PatternFormat>();

	public FormatController() {
		pathToFormatsFile = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator
		        + FORMATS_XML;
		initFormats();
	}

	private void initFormats() {
		try {
			loadFormats();
			if (numberFormats.isEmpty() && dateFormats.isEmpty() && stringFormats.isEmpty()) {
				addStandardFormats();
				saveFormats();
			}
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.getText("formats_not_loaded"));
		}
	}

	private void addStandardFormats() {
		String numberType = PatternFormat.TYPE_NUMBER;
		numberFormats.add(PatternFormat.createPatternFormat("%.0f", PatternFormat.STYLE_FORMATTER, numberType));
		numberFormats.add(PatternFormat.createPatternFormat("%.2f", PatternFormat.STYLE_FORMATTER, numberType));
		String dateType = PatternFormat.TYPE_DATE;
		dateFormats.add(PatternFormat.createPatternFormat("yyyy-MM-dd", PatternFormat.STYLE_DATE, dateType));
		dateFormats.add(PatternFormat.createPatternFormat("yyyy-MM-dd HH:mm:ss", PatternFormat.STYLE_DATE, dateType));
		dateFormats.add(PatternFormat.createPatternFormat(TextUtils.ISO_DATE_TIME_FORMAT_PATTERN,
		    PatternFormat.STYLE_DATE, dateType));
		final int style = FastDateFormat.MEDIUM;
		final FastDateFormat localDate = FastDateFormat.getDateInstance(style, null, null);
		final FastDateFormat localDateTime = FastDateFormat.getDateTimeInstance(style, style, null, null);
		dateFormats.add(PatternFormat.createPatternFormat(localDate.getPattern(), PatternFormat.STYLE_DATE, dateType));
		dateFormats.add(PatternFormat.createPatternFormat(localDateTime.getPattern(), PatternFormat.STYLE_DATE,
		    dateType));
	}

	void loadFormats() throws Exception {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader reader = new StdXMLReader(new BufferedInputStream(new FileInputStream(pathToFormatsFile)));
			parser.setReader(reader);
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector<XMLElement> formats = loader.getChildren();
			for (XMLElement xmlElement : formats) {
				final String type = xmlElement.getAttribute("type", "");
				final String style = xmlElement.getAttribute("style", "");
				final String content = xmlElement.getContent();
				if (nullOrEmpty(type) || nullOrEmpty(style) || nullOrEmpty(content)) {
					throw new RuntimeException("wrong format in " + pathToFormatsFile
					        + ": none of the following must be empty: type=" + type + ", style=" + style
					        + ", element content=" + content);
				}
				else {
					final PatternFormat format = PatternFormat.createPatternFormat(content, style, type);
					if (type.equals(PatternFormat.TYPE_DATE)) {
						dateFormats.add(format);
					}
					else if (type.equals(PatternFormat.TYPE_NUMBER)) {
						numberFormats.add(format);
					}
					else if (type.equals(PatternFormat.TYPE_STRING)) {
						stringFormats.add(format);
					}
					else {
						throw new RuntimeException("unknown type in " + pathToFormatsFile + ": type=" + type
						        + ", style=" + style + ", element content=" + content);
					}
				}
			}
		}
		catch (final FileNotFoundException e) {
		}
		catch (final AccessControlException e) {
		}
	}

	private boolean nullOrEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public void saveFormats() {
		try {
			saveFormats(getAllFormats());
		}
		catch (final Exception e) {
			LogUtils.warn(e);
		}
	}

	public ArrayList<PatternFormat> getAllFormats() {
	    final ArrayList<PatternFormat> formats = new ArrayList<PatternFormat>(numberFormats);
	    formats.addAll(dateFormats);
	    formats.addAll(stringFormats);
	    return formats;
    }

	private void saveFormats(final List<PatternFormat> formats) throws IOException {
		final XMLElement saver = new XMLElement();
		saver.setName(ROOT_ELEMENT);
		final String sep = System.getProperty("line.separator");
		final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + sep //
		        + "<!-- 'type' selects the kind of data the formatter is intended to format. -->" + sep //
		        + "<!-- 'style' selects the formatter implementation: -->" + sep //
		        + "<!-- 'formatter': http://download.oracle.com/javase/6/docs/api/java/util/Formatter.html -->" + sep //
		        + "<!-- 'date': http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html -->" + sep;
		for (PatternFormat patternFormat : formats) {
			patternFormat.toXml(saver);
		}
		final Writer writer = new FileWriter(pathToFormatsFile);
		final XMLWriter xmlWriter = new XMLWriter(writer);
		xmlWriter.addRawContent(header);
		xmlWriter.write(saver, true);
		writer.close();
	}

	public List<PatternFormat> getDateFormats() {
		return dateFormats;
	}

	public List<PatternFormat> getNumberFormats() {
		return numberFormats;
	}
	
	public List<PatternFormat> getStringFormats() {
		return stringFormats;
	}

	public List<String> getAllPatterns() {
		final ArrayList<PatternFormat> formats = getAllFormats();
		ArrayList<String> result = new ArrayList<String>(formats.size());
		for (PatternFormat patternFormat : formats) {
			result.add(patternFormat.getPattern());
        }
	    return result;
    }
}
