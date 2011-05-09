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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

/**
 * @author Volker Boerchers
 */
public class FormatController implements IExtension {
	private static final String RESOURCES_NUMBER_FORMAT = "number_format";
	private static final String RESOURCES_DATETIME_FORMAT = "datetime_format";
	private static final String RESOURCES_DATE_FORMAT = "date_format";
	private static final String FORMATS_XML = "formats.xml";
	private static final String ROOT_ELEMENT = "formats";
	private String pathToFile;
	private static List<PatternFormat> dateFormats = new ArrayList<PatternFormat>();
	private static List<PatternFormat> numberFormats = new ArrayList<PatternFormat>();
	private static List<PatternFormat> stringFormats = new ArrayList<PatternFormat>();
	private static boolean formatsLoaded;
	private static SimpleDateFormat defaultDateFormat;
	private static SimpleDateFormat defaultDateTimeFormat;
	private static HashMap<String, SimpleDateFormat> dateFormatCache = new HashMap<String, SimpleDateFormat>();
	private static DecimalFormat defaultNumberFormat;
	private static HashMap<String, DecimalFormat> numberFormatCache = new HashMap<String, DecimalFormat>();

	static {
		MModeController modeController = (MModeController) Controller.getCurrentModeController();
		modeController.getOptionPanelBuilder().addValidator(new IValidator() {
			public ValidationResult validate(Properties properties) {
				final ValidationResult result = new ValidationResult();
				try {
	                createDateFormat(properties.getProperty(RESOURCES_DATE_FORMAT));
                }
                catch (Exception e) {
                	result.addError(TextUtils.getText("OptionPanel.validate_invalid_date_format"));
                }
                try {
                	createDefaultDateTimeFormat(properties.getProperty(RESOURCES_DATETIME_FORMAT));
                }
                catch (Exception e) {
                	result.addError(TextUtils.getText("OptionPanel.validate_invalid_datetime_format"));
                }
                try {
                	getDecimalFormat(properties.getProperty(RESOURCES_NUMBER_FORMAT));
                }
                catch (Exception e) {
                	result.addError(TextUtils.getText("OptionPanel.validate_invalid_number_format"));
                }
				return result;
			}
		});
	}

	public FormatController() {
		pathToFile = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator
		        + FORMATS_XML;
		initPatternFormats();
	}

	private void initPatternFormats() {
		if (formatsLoaded)
			return;
		try {
			loadFormats();
			if (numberFormats.isEmpty() && dateFormats.isEmpty() && stringFormats.isEmpty()) {
				addStandardFormats();
				saveFormatsNoThrow();
			}
			formatsLoaded = true;
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.getText("formats_not_loaded"));
		}
	}

	private void addStandardFormats() {
		String number = IFormattedObject.TYPE_NUMBER;
		numberFormats.add(PatternFormat.createPatternFormat("#.####", PatternFormat.STYLE_DECIMAL, number,
		    "default number"));
		numberFormats.add(PatternFormat.createPatternFormat("#.00", PatternFormat.STYLE_DECIMAL, number, "decimal"));
		numberFormats.add(PatternFormat.createPatternFormat("#", PatternFormat.STYLE_DECIMAL, number, "integer"));
		numberFormats.add(PatternFormat.createPatternFormat("#.##%", PatternFormat.STYLE_DECIMAL, number, "percent"));
		String dType = IFormattedObject.TYPE_DATE;
		final String dStyle = PatternFormat.STYLE_DATE;
		dateFormats.add(createLocalPattern("short date", SimpleDateFormat.SHORT, null));
		dateFormats.add(createLocalPattern("medium date", SimpleDateFormat.MEDIUM, null));
		dateFormats.add(createLocalPattern("short datetime", SimpleDateFormat.SHORT, SimpleDateFormat.SHORT));
		dateFormats.add(createLocalPattern("medium datetime", SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT));
		dateFormats.add(PatternFormat.createPatternFormat("yyyy-MM-dd", dStyle, dType, "short iso date"));
		dateFormats.add(PatternFormat.createPatternFormat("yyyy-MM-dd HH:mm", dStyle, dType, "long iso date"));
		dateFormats.add(PatternFormat.createPatternFormat(FormattedDate.ISO_DATE_TIME_FORMAT_PATTERN, dStyle, dType,
		    "full iso date"));
	}

	private PatternFormat createLocalPattern(String name, int dateStyle, Integer timeStyle) {
		final SimpleDateFormat simpleDateFormat = (SimpleDateFormat) (timeStyle == null ? SimpleDateFormat
		    .getDateInstance(dateStyle) : SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle));
		final String dStyle = PatternFormat.STYLE_DATE;
		final String dType = IFormattedObject.TYPE_DATE;
		final String locale = Locale.getDefault().toString();
		return PatternFormat.createPatternFormat(simpleDateFormat.toPattern(), dStyle, dType, name, locale);
	}

	void loadFormats() throws Exception {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader reader = new StdXMLReader(new BufferedInputStream(new FileInputStream(pathToFile)));
			parser.setReader(reader);
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector<XMLElement> formats = loader.getChildren();
			for (XMLElement elem : formats) {
				final String type = elem.getAttribute("type", null);
				final String style = elem.getAttribute("style", null);
				final String name = elem.getAttribute("name", null);
				final String locale = elem.getAttribute("locale", null);
				final String content = elem.getContent();
				if (StringUtils.isEmpty(type) || StringUtils.isEmpty(style) || StringUtils.isEmpty(content)) {
					throw new RuntimeException("wrong format in " + pathToFile
					        + ": none of the following must be empty: type=" + type + ", style=" + style
					        + ", element content=" + content);
				}
				else {
					final PatternFormat format = PatternFormat.createPatternFormat(content, style, type);
					format.setName(name);
					format.setLocale(locale);
					if (type.equals(IFormattedObject.TYPE_DATE)) {
						dateFormats.add(format);
					}
					else if (type.equals(IFormattedObject.TYPE_NUMBER)) {
						numberFormats.add(format);
					}
					else if (type.equals(IFormattedObject.TYPE_STRING)) {
						stringFormats.add(format);
					}
					else {
						throw new RuntimeException("unknown type in " + pathToFile + ": type=" + type + ", style="
						        + style + ", element content=" + content);
					}
				}
			}
		}
		catch (final IOException e) {
			LogUtils.warn("error parsing " + pathToFile, e);
		}
	}

	private void saveFormatsNoThrow() {
		try {
			saveFormats(getAllFormats());
		}
		catch (final Exception e) {
			LogUtils.warn("cannot create " + pathToFile, e);
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
		final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + sep //
		        + "<!-- 'type' selects the kind of data the formatter is intended to format. -->"
		        + sep //
		        + "<!-- 'style' selects the formatter implementation: -->"
		        + sep //
		        + "<!--   - 'date': http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html -->"
		        + sep //
		        + "<!--   - 'decimal': http://download.oracle.com/javase/6/docs/api/java/text/DecimalFormat.html -->"
		        + sep //
		        + "<!--   - 'formatter': http://download.oracle.com/javase/6/docs/api/java/util/Formatter.html -->"
		        + sep //
		        + "<!--   - 'name': a informal name, a comment that's not visible in the app -->" + sep //
		        + "<!--   - 'locale': the name of the locale, only set for locale dependent format codes -->" + sep;
		for (PatternFormat patternFormat : formats) {
			saver.addChild(patternFormat.toXml());
		}
		final Writer writer = new FileWriter(pathToFile);
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

	/** returns a matching IFormattedObject if possible and if formatString is not-null.
	 * Removes format if formatString is null. */
	public static Object format(final Object obj, final String formatString) {
		try {
			final PatternFormat format = formatString == null ? null : PatternFormat.guessPatternFormat(formatString);
			// logging for invalid pattern is done in guessPatternFormat()
			if (obj == null)
				return obj;
			final Object toFormat = (obj instanceof IFormattedObject) ? ((IFormattedObject) obj).getObject() : obj;
			if (format == null)
				return toFormat;
			if (format.acceptsDate() && toFormat instanceof Date) {
				return new FormattedDate((Date) toFormat, formatString);
			}
			else if (format.acceptsNumber()) {
				final Number number = toFormat instanceof Number ? (Number) toFormat : TextUtils.toNumber(HtmlUtils
				    .htmlToPlain(toFormat.toString()));
				return new FormattedNumber(number, formatString, format.formatObject(number).toString());
			}
			else {
				return new FormattedObject(toFormat, format);
			}
		}
		catch (Exception e) {
			LogUtils.warn("cannot format " + obj.toString() + " with " + formatString + ": " + e.getMessage());
			return obj;
		}
	}

	public static Format getDefaultFormat(String type) {
		if (type.equals(IFormattedObject.TYPE_DATE))
			return getDefaultDateFormat();
		else if (type.equals(IFormattedObject.TYPE_DATETIME))
			return getDefaultDateTimeFormat();
		else if (type.equals(IFormattedObject.TYPE_NUMBER))
			return getDefaultNumberFormat();
		else
			throw new IllegalArgumentException("unknown format style");
	}

	public static SimpleDateFormat getDefaultDateFormat() {
		if (defaultDateFormat != null)
			return defaultDateFormat;
		final ResourceController resourceController = ResourceController.getResourceController();
		String datePattern = resourceController.getProperty(RESOURCES_DATE_FORMAT);
		defaultDateFormat = createDateFormat(datePattern);
		resourceController.addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if (propertyName.equals(RESOURCES_DATE_FORMAT))
					defaultDateFormat = createDateFormat(newValue);
			}
		});
		return defaultDateFormat;
	}

	private static SimpleDateFormat createDateFormat(final String datePattern) {
		final Integer style = getDateStyle(datePattern);
		if (style != null)
			return (SimpleDateFormat) DateFormat.getDateInstance(style);
		else
			return new SimpleDateFormat(datePattern);
	}

	public static SimpleDateFormat getDefaultDateTimeFormat() {
		if (defaultDateTimeFormat != null)
			return defaultDateTimeFormat;
		final ResourceController resourceController = ResourceController.getResourceController();
		String datetimePattern = resourceController.getProperty(RESOURCES_DATETIME_FORMAT);
		defaultDateTimeFormat = createDefaultDateTimeFormat(datetimePattern);
		resourceController.addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if (propertyName.equals(RESOURCES_DATETIME_FORMAT)) {
					defaultDateTimeFormat = createDefaultDateTimeFormat(newValue);
				}
			}
		});
		return defaultDateTimeFormat;
	}

	private static SimpleDateFormat createDefaultDateTimeFormat(String datetimePattern) {
		final String[] styles = datetimePattern.split("\\s*,\\s*");
		if (styles.length == 2 && getDateStyle(styles[0]) != null && getDateStyle(styles[1]) != null)
			return (SimpleDateFormat) DateFormat.getDateTimeInstance(getDateStyle(styles[0]), getDateStyle(styles[1]));
		else
			return new SimpleDateFormat(datetimePattern);
	}

	private static Integer getDateStyle(final String string) {
		if (string.equals("SHORT"))
			return DateFormat.SHORT;
		if (string.equals("MEDIUM"))
			return DateFormat.MEDIUM;
		if (string.equals("LONG"))
			return DateFormat.LONG;
		if (string.equals("FULL"))
			return DateFormat.FULL;
		return null;
	}
	

	public static DecimalFormat getDefaultNumberFormat() {
		if (defaultNumberFormat != null)
			return defaultNumberFormat;
	    final ResourceController resourceController = ResourceController.getResourceController();
	    defaultNumberFormat = getDecimalFormat(resourceController.getProperty(RESOURCES_NUMBER_FORMAT));
		resourceController.addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if (propertyName.equals(RESOURCES_NUMBER_FORMAT)) {
					defaultNumberFormat = getDecimalFormat(newValue);
				}
			}
		});
	    return defaultNumberFormat;
    }

	/** @param pattern either a string (see {@link DecimalFormat}) or null for a default formatter. */
	public static DecimalFormat getDecimalFormat(final String pattern) {
		DecimalFormat format = numberFormatCache.get(pattern);
		if (format == null) {
			format = (DecimalFormat) ((pattern == null) ? getDefaultNumberFormat()
			        : new DecimalFormat(pattern));
			numberFormatCache.put(pattern, format);
		}
		return format;
	}

	public static SimpleDateFormat getDateFormat(String pattern) {
	    SimpleDateFormat parser = dateFormatCache.get(pattern);
        if (parser == null) {
        	parser = new SimpleDateFormat(pattern);
        	dateFormatCache.put(pattern, parser);
        }
	    return parser;
    }
}
