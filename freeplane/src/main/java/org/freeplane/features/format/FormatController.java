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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLWriter;

/**
 * @author Volker Boerchers
 */
public class FormatController implements IExtension, IFreeplanePropertyListener {
	private static final String RESOURCES_NUMBER_FORMAT = "number_format";
	private static final String RESOURCES_DATETIME_FORMAT = "datetime_format";
	private static final String RESOURCES_DATE_FORMAT = "date_format";
	private static final String FORMATS_XML = "formats.xml";
	private static final String ROOT_ELEMENT = "formats";
	private String pathToFile;
	private Locale locale;
	private List<PatternFormat> specialFormats = new ArrayList<PatternFormat>();
	private List<PatternFormat> dateFormats = new ArrayList<PatternFormat>();
	private List<PatternFormat> numberFormats = new ArrayList<PatternFormat>();
	private List<PatternFormat> stringFormats = new ArrayList<PatternFormat>();
	private boolean formatsLoaded;
	private SimpleDateFormat defaultDateFormat;
	private SimpleDateFormat defaultDateTimeFormat;
	private HashMap<String, SimpleDateFormat> dateFormatCache = new HashMap<String, SimpleDateFormat>();
	private DecimalFormat defaultNumberFormat;
	private HashMap<String, DecimalFormat> numberFormatCache = new HashMap<String, DecimalFormat>();
    static private boolean firstError = true;

	public IValidator createValidator (){
	    return new IValidator() {
			public ValidationResult validate(Properties properties) {
				final ValidationResult result = new ValidationResult();
				try {
                    createDateFormat(properties.getProperty(RESOURCES_DATE_FORMAT));
                    if (properties.getProperty(RESOURCES_DATE_FORMAT).isEmpty())
                        throw new Exception();
                }
                catch (Exception e) {
                	result.addError(TextUtils.getText("OptionPanel.validate_invalid_date_format"));
                }
                try {
                    createDefaultDateTimeFormat(properties.getProperty(RESOURCES_DATETIME_FORMAT));
                    if (properties.getProperty(RESOURCES_DATETIME_FORMAT).isEmpty())
                        throw new Exception();
                }
                catch (Exception e) {
                	result.addError(TextUtils.getText("OptionPanel.validate_invalid_datetime_format"));
                }
                try {
                    getDecimalFormat(properties.getProperty(RESOURCES_NUMBER_FORMAT));
                    if (properties.getProperty(RESOURCES_NUMBER_FORMAT).isEmpty())
                        throw new Exception();
                }
                catch (Exception e) {
                	result.addError(TextUtils.getText("OptionPanel.validate_invalid_number_format"));
                }
				return result;
			}
		};
	}

	public FormatController() {
		final String freeplaneUserDirectory = ResourceController.getResourceController().getFreeplaneUserDirectory();
		// applets have no user directory and no file access anyhow
		pathToFile = freeplaneUserDirectory == null ? null : freeplaneUserDirectory + File.separator + FORMATS_XML;
		locale = FormatUtils.getFormatLocaleFromResources();
		initPatternFormats();
        final ResourceController resourceController = ResourceController.getResourceController();
        resourceController.addPropertyChangeListener(this);
	}

	public static FormatController getController() {
		return getController(Controller.getCurrentController());
	}

	public static FormatController getController(Controller controller) {
		return (FormatController) controller.getExtension(FormatController.class);
	}
	
	public static void install(final FormatController formatController) {
		Controller.getCurrentController().addExtension(FormatController.class, formatController);
		Controller.getCurrentController().addOptionValidator(formatController.createValidator());
	}

	private void initPatternFormats() {
		if (formatsLoaded)
			return;
		specialFormats.add(PatternFormat.getStandardPatternFormat());
		specialFormats.add(PatternFormat.getIdentityPatternFormat());
		try {
			if (pathToFile != null)
				loadFormats();
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			if(firstError){
			    firstError = false;
			    UITools.errorMessage(TextUtils.getText("formats_not_loaded"));
			}
		}
        if (numberFormats.isEmpty() && dateFormats.isEmpty() && stringFormats.isEmpty()) {
            addStandardFormats();
            if (pathToFile != null)
                saveFormatsNoThrow();
        }
        formatsLoaded = true;
	}

	private void addStandardFormats() {
		String number = IFormattedObject.TYPE_NUMBER;
		numberFormats.add(createFormat("#0.####", PatternFormat.STYLE_DECIMAL, number,
		    "default number", locale));
		numberFormats.add(createFormat("#.00", PatternFormat.STYLE_DECIMAL, number, "decimal", locale));
		numberFormats.add(createFormat("#", PatternFormat.STYLE_DECIMAL, number, "integer", locale));
		numberFormats.add(createFormat("#.##%", PatternFormat.STYLE_DECIMAL, number, "percent", locale));
		String dType = IFormattedObject.TYPE_DATE;
		final String dStyle = PatternFormat.STYLE_DATE;
		dateFormats.add(createLocalPattern("short date", SimpleDateFormat.SHORT, null));
		dateFormats.add(createLocalPattern("medium date", SimpleDateFormat.MEDIUM, null));
		dateFormats.add(createLocalPattern("short datetime", SimpleDateFormat.SHORT, SimpleDateFormat.SHORT));
		dateFormats.add(createLocalPattern("medium datetime", SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT));
		dateFormats.add(createFormat("yyyy-MM-dd", dStyle, dType, "short iso date", locale));
		dateFormats.add(createFormat("yyyy-MM-dd HH:mm", dStyle, dType, "long iso date", locale));
		dateFormats.add(createFormat(FormattedDate.ISO_DATE_TIME_FORMAT_PATTERN, dStyle, dType,
		    "full iso date", locale));
		dateFormats.add(createFormat("HH:mm", dStyle, dType, "time", locale));
	}

	private PatternFormat createLocalPattern(String name, int dateStyle, Integer timeStyle) {
		final SimpleDateFormat simpleDateFormat = (SimpleDateFormat) (timeStyle == null ? SimpleDateFormat
		    .getDateInstance(dateStyle, locale) : SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle, locale));
		final String dStyle = PatternFormat.STYLE_DATE;
		final String dType = IFormattedObject.TYPE_DATE;
		return createFormat(simpleDateFormat.toPattern(), dStyle, dType, name, locale);
	}

	private void loadFormats() throws Exception {
		BufferedInputStream inputStream = null;
		final File configXml = new File(pathToFile);
		if (!configXml.exists()) {
			LogUtils.info(pathToFile + " does not exist yet");
			return;
		}
		try {
			final IXMLParser parser = XMLLocalParserFactory.createLocalXMLParser();
			inputStream = new BufferedInputStream(new FileInputStream(configXml));
			final IXMLReader reader = new StdXMLReader(inputStream);
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
					throw new RuntimeException("wrong format in " + configXml
					        + ": none of the following must be empty: type=" + type + ", style=" + style
					        + ", element content=" + content);
				}
				else {
					final PatternFormat format = createFormat(content, style, type, name,
					    (locale == null ? null : new Locale(locale)));
					if (type.equals(IFormattedObject.TYPE_DATE)) {
						dateFormats.add(format);
					}
					else if (type.equals(IFormattedObject.TYPE_NUMBER)) {
						numberFormats.add(format);
					}
					else if (type.equals(IFormattedObject.TYPE_STRING)) {
						stringFormats.add(format);
					}
					else if (type.equals(PatternFormat.TYPE_STANDARD)) {
					    // ignore this pattern that crept in in some 1.2 beta version
					}
					else {
						throw new RuntimeException("unknown type in " + configXml + ": type=" + type + ", style="
						        + style + ", element content=" + content);
					}
				}
			}
		}
		catch (final IOException e) {
			LogUtils.warn("error parsing " + configXml, e);
		}
		finally {
			FileUtils.silentlyClose(inputStream);
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

	public void addPatternFormat(PatternFormat format){
		specialFormats.add(format);
	}
	public ArrayList<PatternFormat> getAllFormats() {
		final ArrayList<PatternFormat> formats = new ArrayList<PatternFormat>();
		formats.addAll(specialFormats);
		formats.addAll(numberFormats);
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
            if (!patternFormat.getType().equals(PatternFormat.TYPE_IDENTITY)
                    && !patternFormat.getType().equals(PatternFormat.TYPE_STANDARD)) {
                saver.addChild(patternFormat.toXml());
            }
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

	/** @deprecated use getAllFormats() instead. */
	public List<String> getAllPatterns() {
		final ArrayList<PatternFormat> formats = getAllFormats();
		ArrayList<String> result = new ArrayList<String>(formats.size());
		for (PatternFormat patternFormat : formats) {
			result.add(patternFormat.getPattern());
		}
		return result;
	}

	/** returns a matching {@link IFormattedObject} if possible and if <a>formatString</a> is not-null.
	 * Otherwise <a>defaultObject</a> is returned.
	 * Removes format if <a>formatString</a> is null. */
	public static Object format(final Object obj, final String formatString, final Object defaultObject) {
		try {
			final PatternFormat format = PatternFormat.guessPatternFormat(formatString);
			// logging for invalid pattern is done in guessPatternFormat()
            if (obj == null)
                return obj;
			Object toFormat = extractObject(obj);
			if (format == null || format == PatternFormat.getIdentityPatternFormat())
				return toFormat;
			if (toFormat instanceof String) {
			    final String string = (String) toFormat;
                if (string.startsWith("=")) {
			        return new FormattedFormula(string, formatString);
			    }
			    else {
		            final ScannerController scannerController = ScannerController.getController();
		            if (scannerController != null)
		                toFormat = scannerController.parse(string);
			    }
            }
            if (format.acceptsDate() && toFormat instanceof Date) {
				return new FormattedDate((Date) toFormat, formatString);
			}
            else if (format.acceptsNumber() && toFormat instanceof Number) {
                return new FormattedNumber((Number) toFormat, formatString, format.formatObject(toFormat).toString());
            }
            else {
				return new FormattedObject(toFormat, format);
			}
		}
		catch (Exception e) {
            // Be quiet, just like Excel does...
            // LogUtils.warn("cannot format '" + StringUtils.abbreviate(obj.toString(), 20) + "' of type "
            //               + obj.getClass().getSimpleName() + " with " + formatString + ": " + e.getMessage());
			return defaultObject;
		}
	}

    private static Object extractObject(final Object obj) {
        return (obj instanceof IFormattedObject) ? ((IFormattedObject) obj).getObject() : obj;
    }

    /** returns a matching IFormattedObject if possible and if formatString is not-null.
     * Otherwise <a>obj</a> is returned.
     * Removes format if formatString is null. */
    public static Object format(final Object obj, final String formatString) {
        return format(obj, formatString, obj);
    }

    public static Object formatUsingDefault(final Object object) {
        if (object instanceof Date)
            return format(object, FormatController.getController().getDefaultDateTimeFormat().toPattern());
        if (object instanceof Number)
            return format(object, FormatController.getController().getDefaultNumberFormat().toPattern());
        return object;
    }

	public Format getDefaultFormat(String type) {
		if (type.equals(IFormattedObject.TYPE_DATE))
			return getDefaultDateFormat();
		else if (type.equals(IFormattedObject.TYPE_DATETIME))
			return getDefaultDateTimeFormat();
		else if (type.equals(IFormattedObject.TYPE_NUMBER))
			return getDefaultNumberFormat();
		else
			throw new IllegalArgumentException("unknown format style");
	}

	public SimpleDateFormat getDefaultDateFormat() {
		if (defaultDateFormat != null)
			return defaultDateFormat;
		final ResourceController resourceController = ResourceController.getResourceController();

		// DateFormatParser cannot handle empty date format!
		fixEmptyDataFormatProperty(resourceController, RESOURCES_DATE_FORMAT, "SHORT");

		String datePattern = resourceController.getProperty(RESOURCES_DATE_FORMAT);
		defaultDateFormat = createDateFormat(datePattern);
		return defaultDateFormat;
	}

	/**
	 * Fix old invalid values (empty data format properties) on startup.
	 * For new configurations, this is forced by the Validator on top of this file!
	 * @param resourceController
	 * @param resourceProperty
	 * @param defaultValue
	 */
	private void fixEmptyDataFormatProperty(final ResourceController resourceController,
			final String resourceProperty, final String defaultValue)
	{
		if (resourceController.getProperty(resourceProperty).isEmpty()) {
			resourceController.setProperty(resourceProperty, defaultValue);
		}
	}

	private static SimpleDateFormat createDateFormat(final String datePattern) {
		final Integer style = getDateStyle(datePattern);
		if (style != null)
			return (SimpleDateFormat) DateFormat.getDateInstance(style, FormatUtils.getFormatLocaleFromResources());
		else
			return new SimpleDateFormat(datePattern, FormatUtils.getFormatLocaleFromResources());
	}

	public SimpleDateFormat getDefaultDateTimeFormat() {
		if (defaultDateTimeFormat != null)
			return defaultDateTimeFormat;
		final ResourceController resourceController = ResourceController.getResourceController();

		// DateFormatParser cannot handle empty date format!
		fixEmptyDataFormatProperty(resourceController, RESOURCES_DATETIME_FORMAT, "SHORT,SHORT");

		String datetimePattern = resourceController.getProperty(RESOURCES_DATETIME_FORMAT);
		defaultDateTimeFormat = createDefaultDateTimeFormat(datetimePattern);
		return defaultDateTimeFormat;
	}

	private SimpleDateFormat createDefaultDateTimeFormat(String datetimePattern) {
		final String[] styles = datetimePattern.split("\\s*,\\s*");
		if (styles.length == 2 && getDateStyle(styles[0]) != null && getDateStyle(styles[1]) != null)
			return (SimpleDateFormat) DateFormat.getDateTimeInstance(getDateStyle(styles[0]), getDateStyle(styles[1]),
			    FormatUtils.getFormatLocaleFromResources());
		else
			return getDateFormat(datetimePattern);
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

	public DecimalFormat getDefaultNumberFormat() {
		if (defaultNumberFormat != null)
			return defaultNumberFormat;
	    final ResourceController resourceController = ResourceController.getResourceController();

		// an empty number format does not make sense!
		fixEmptyDataFormatProperty(resourceController, RESOURCES_NUMBER_FORMAT, "#0.####");

		defaultNumberFormat = getDecimalFormat(resourceController.getProperty(RESOURCES_NUMBER_FORMAT));
	    return defaultNumberFormat;
    }

	/** @param pattern either a string (see {@link DecimalFormat}) or null for a default formatter. */
	public DecimalFormat getDecimalFormat(final String pattern) {
		DecimalFormat format = numberFormatCache.get(pattern);
		if (format == null) {
			format = (DecimalFormat) ((pattern == null) ? getDefaultNumberFormat()
			        : new DecimalFormat(pattern, new DecimalFormatSymbols(FormatUtils.getFormatLocaleFromResources())));
			numberFormatCache.put(pattern, format);
		}
		return format;
	}

	public SimpleDateFormat getDateFormat(String pattern) {
	    SimpleDateFormat parser = dateFormatCache.get(pattern);
        if (parser == null) {
        	parser = new SimpleDateFormat(pattern, FormatUtils.getFormatLocaleFromResources());
        	dateFormatCache.put(pattern, parser);
        }
	    return parser;
    }

    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if (propertyName.equals(RESOURCES_DATE_FORMAT)) {
            defaultDateFormat = createDateFormat(newValue);
            final ScannerController scannerController = ScannerController.getController();
            if (scannerController != null)
                scannerController.addParsersForStandardFormats();
        }
        else if (propertyName.equals(RESOURCES_DATETIME_FORMAT)) {
            defaultDateTimeFormat = createDefaultDateTimeFormat(newValue);
            final ScannerController scannerController = ScannerController.getController();
            if (scannerController != null)
                scannerController.addParsersForStandardFormats();
        }
        else if (propertyName.equals(RESOURCES_NUMBER_FORMAT)) {
            defaultNumberFormat = getDecimalFormat(newValue);
        }
        else if (FormatUtils.equalsFormatLocaleName(propertyName)) {
            locale = FormatUtils.getFormatLocaleFromResources();
        }
    }

	public List<PatternFormat> getSpecialFormats() {
		return specialFormats;
	}

	public PatternFormat createFormat(String pattern, String style, String type) {
		for(PatternFormat specialFormat : specialFormats)
			if (pattern.equals(specialFormat.getPattern()))
				return specialFormat;
	    if (style.equals(PatternFormat.STYLE_DATE))
			return new DatePatternFormat(pattern);
		else if (style.equals(PatternFormat.STYLE_FORMATTER))
			return new FormatterPatternFormat(pattern, type);
		else if (style.equals(PatternFormat.STYLE_DECIMAL))
			return new DecimalPatternFormat(pattern);
		else
			throw new IllegalArgumentException("unknown format style");
	}

	public PatternFormat createFormat(final String pattern, final String style, final String type,
	                                                final String name, final Locale locale) {
		final PatternFormat format = createFormat(pattern, style, type, name);
		format.setLocale(locale);
		return format;
	}

	public PatternFormat createFormat(final String pattern, final String style, final String type,
	                                                final String name) {
		final PatternFormat format = createFormat(pattern, style, type);
		format.setName(name);
		return format;
	}

}
