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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
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
 * @author Volker Boerchers
 */
public class ScannerController implements IExtension {
	private static final String SCANNER_XML = "scanner.xml";
	private static final String ROOT_ELEMENT = "scanners";
	private String pathToFile;
	private Scanner selectedScanner;
	private static List<Scanner> scanners = new ArrayList<Scanner>();
	private static boolean fileLoaded;

	public ScannerController() {
		pathToFile = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator
		        + SCANNER_XML;
		initScanners();
		selectScanner(Locale.getDefault());
	}

	public void selectScanner(final Locale locale) {
		selectedScanner = findScanner(locale);
	}

	public Object parse(String string) {
		return selectedScanner.parse(string);
	}

	private Scanner findScanner(final Locale locale) {
		final String localeAsString = locale.toString();
		Scanner countryScanner = null;
		Scanner defaultScanner = null;
		for (Scanner scanner : scanners) {
			if (scanner.localeMatchesExactly(localeAsString))
				return scanner;
			else if (localeAsString.contains("_") && scanner.countryMatches(localeAsString))
				countryScanner = scanner;
			else if (scanner.isDefault())
				defaultScanner = scanner;
		}
		return countryScanner == null ? defaultScanner : countryScanner;
	}

	private void initScanners() {
		if (fileLoaded)
			return;
		try {
			loadScanners();
			if (scanners.isEmpty()) {
				addStandardScanners();
				saveScannersNoThrow();
			}
			fileLoaded = true;
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.getText("scanners_not_loaded"));
		}
	}

	private void addStandardScanners() {
		scanners.add(createEnglishScanner());
		scanners.add(createGermanScanner());
	}

	private Scanner createEnglishScanner() {
		final Scanner s = new Scanner(new String[] { "en" }, true);
		s.setFirstChars("+-0123456789.");
		final String tNumber = IFormattedObject.TYPE_NUMBER;
		final String tDate = IFormattedObject.TYPE_DATETIME;
		final Locale loc = new Locale("en");
		s.addParser(Parser.createParser(Parser.STYLE_DECIMAL, tNumber, null, loc, "supports locale specific numbers"));
		// number literals are a subset of english localized decimal parser
		// s.addParser(Parser.createParser(Parser.STYLE_NUMBERLITERAL, tNumber, null, loc, "numbers like 12345.12"));
		s.addParser(Parser.createParser(Parser.STYLE_ISODATE, tDate, null, loc, "ISO reader for date and date/time"));
		s.addParser(Parser.createParser(Parser.STYLE_DATE, tDate, "M/d", loc, "completes date with current year"));
		s.addParser(Parser.createParser(Parser.STYLE_DATE, tDate, "M/d/y", loc, "parses 4/21/11 or 4/21/2011"));
		return s;
	}

	private Scanner createGermanScanner() {
		final Scanner s = new Scanner(new String[] { "de", "hr" }, false);
		s.setFirstChars("+-0123456789,.");
		final String tNumber = IFormattedObject.TYPE_NUMBER;
		final String tDate = IFormattedObject.TYPE_DATETIME;
		final Locale loc = new Locale("de");
		s.addParser(Parser.createParser(Parser.STYLE_DATE, tDate, "d.M", loc, "completes date with current year"));
		s.addParser(Parser.createParser(Parser.STYLE_DATE, tDate, "d.M.y", loc, "parses 21.4.11 or 21.4.2011"));
		s.addParser(Parser.createParser(Parser.STYLE_DECIMAL, tNumber, null, loc,
		    "uses comma as decimal separator: 1.234,12"));
		s.addParser(Parser.createParser(Parser.STYLE_ISODATE, tDate, null, loc, "ISO reader for date and date/time"));
		s.addParser(Parser.createParser(Parser.STYLE_NUMBERLITERAL, tNumber, null, loc,
		    "support dot as decimal separator (if nothing else matches)"));
		return s;
	}

	void loadScanners() throws Exception {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader reader = new StdXMLReader(new BufferedInputStream(new FileInputStream(pathToFile)));
			parser.setReader(reader);
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector<XMLElement> scannerElements = loader.getChildren();
			for (XMLElement elem : scannerElements) {
				scanners.add(parseScanner(elem));
			}
			boolean haveDefault = false;
			for (Scanner scanner : scanners) {
				if (scanner.isDefault()) {
					if (haveDefault)
						LogUtils.warn(pathToFile + ": multiple scanners are marked as default - fix that!");
					else
						haveDefault = true;
				}
			}
			if (haveDefault)
				LogUtils.warn(pathToFile + ": no scanner is marked as default - fix that!");
		}
		catch (final IOException e) {
			LogUtils.warn("error parsing " + pathToFile, e);
		}
	}

	private Scanner parseScanner(XMLElement elem) {
		final String locales = elem.getAttribute("locale", "");
		final String isDefault = elem.getAttribute("default", "false");
		if (StringUtils.isEmpty(locales)) {
			throw new RuntimeException("wrong scanner in " + pathToFile
			        + ": none of the following must be empty: locales=" + locales + ".");
		}
		final Scanner scanner = new Scanner(locales.trim().split(","), Boolean.parseBoolean(isDefault));
		final Locale locale = new Locale(scanner.getLocales().get(0));
		for (XMLElement child : elem.getChildren()) {
			if (child.getName().equals("checkfirstchar")) {
				final String chars = elem.getAttribute("chars", "");
				final boolean disabled = Boolean.parseBoolean(elem.getAttribute("disabled", "false"));
				if (!disabled)
					scanner.setFirstChars(chars);
			}
			else if (child.getName().equals("parser")) {
				scanner.addParser(parseParser(child, locale));
			}
		}
		return scanner;
	}

	private Parser parseParser(XMLElement elem, Locale locale) {
		final String type = elem.getAttribute("type", null);
		final String style = elem.getAttribute("style", null);
		final String format = elem.getAttribute("format", null);
		final String comment = elem.getAttribute("comment", null);
		return Parser.createParser(style, type, format, locale, comment);
	}

	private void saveScannersNoThrow() {
		try {
			saveScanners(scanners);
		}
		catch (final Exception e) {
			LogUtils.warn("cannot save create " + pathToFile, e);
		}
	}

	private void saveScanners(final List<Scanner> scanners) throws IOException {
		final XMLElement saver = new XMLElement();
		saver.setName(ROOT_ELEMENT);
		final String sep = System.getProperty("line.separator");
		final String description = commentLines("Description:" //
		    , "" //
		    , "<scanner> Scanners are locale dependent. If there is no scanner for" //
		    , "the selected locale the scanner marked with default=\"true\" is choosen." //
		    , " 'locales': A comma-separated list of locale names." //
		    , "   The locale is selected via Preferences -> Environment -> Language" //
		    , "   It's a pattern like 'en' (generic English) or 'en_US'" //
		    , "   (English/USA). Use the more general two-letter form if appropriate." //
		    , " 'default': Set to \"true\" for only one locale. The standard is 'en'." //
		    , "" //
		    , "<checkfirstchar> allows to enable a fast check for the first input" //
		    , "character. If the first input character is not contained in the string" //
		    , "given in attribute 'chars' no further attempts are made to parse the" //
		    , "input as a number or date." //
		    , "Do not use this option if you have have scanner formats that can" //
		    , "recognize arbitrary text at the beginning of the pattern. To disable" //
		    , "this check omit <checkfirstchar> or add the attribute disabled=\"true\"." //
		    , " 'chars': A string of characters that may start data." //
		    , "" //
		    , "<type> selects the kind of data the scanner should recognize." //
		    , " 'style' selects the formatter implementation:" //
		    , "  - \"isodate\": flexible ISO date reader for strings like 2011-04-29 22:31:21" //
		    , "    Only creates datetimes if time part is given, so no differentiation" //
		    , "    between date and date/time is necessary." //
		    , "  - \"date\": a special format for dates; needs attribute 'format'. See" //
		    , "    http://download.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html" //
		    , "  - \"numberliteral\": parses Java float or integral number literals only, with" //
		    , "    a dot as decimal separator and no thousands separator. See" //
		    , "    http://en.wikibooks.org/wiki/Java_Programming/Literals/Numeric_Literals/Floating_Point_Literals" //
		    , "  - \"decimal\": a special format for numbers; needs attribute 'format'. See" //
		    , "    http://download.oracle.com/javase/6/docs/api/java/text/DecimalFormat.html" //
		    , " 'format': The format code of a \"date\" or \"decimal\" scanner." //
		    , " 'comment': Inline comment, not used by the application.");
		final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + sep + description;
		for (Scanner scanner : scanners) {
			saver.addChild(scanner.toXml());
		}
		final Writer writer = new FileWriter(pathToFile);
		final XMLWriter xmlWriter = new XMLWriter(writer);
		xmlWriter.addRawContent(header);
		xmlWriter.write(saver, true);
		writer.close();
	}

	private String commentLines(String... comments) {
		StringBuilder builder = new StringBuilder(comments.length * 100);
		for (String comment : comments) {
			builder.append(String.format("<!-- %-71s -->%n", comment));
		}
		return builder.toString();
	}
}
