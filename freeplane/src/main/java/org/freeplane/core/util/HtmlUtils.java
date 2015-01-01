/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utilities for conversion from/to HTML and XML used in Freeplane: In scripts available
 * as "global variable" <code>htmlUtils</code>.
 */
public class HtmlUtils {
	public static class IndexPair {
		final public boolean mIsTag;
		final public int originalEnd;
		final public int originalStart;
		final public int pureTextEnd;
		final public int pureTextStart;

		public IndexPair(final int pOriginalStart, final int pOriginalEnd, final int pPureTextStart,
		                 final int pPureTextEnd, final boolean pIsTag) {
			super();
			originalStart = pOriginalStart;
			originalEnd = pOriginalEnd;
			pureTextStart = pPureTextStart;
			pureTextEnd = pPureTextEnd;
			mIsTag = pIsTag;
		}

		@Override
		public String toString() {
			final StringBuilder buffer = new StringBuilder();
			buffer.append("[IndexPair:");
			buffer.append(" originalStart: ");
			buffer.append(originalStart);
			buffer.append(" originalEnd: ");
			buffer.append(originalEnd);
			buffer.append(" pureTextStart: ");
			buffer.append(pureTextStart);
			buffer.append(" pureTextEnd: ");
			buffer.append(pureTextEnd);
			buffer.append(" is a tag: ");
			buffer.append(mIsTag);
			buffer.append("]");
			return buffer.toString();
		}
	}

	private static final Pattern FIND_TAGS_PATTERN = Pattern.compile("([^<]*)(<[^>]+>)");
	private static final Pattern HTML_PATTERN = Pattern.compile("(?s)^\\s*<\\s*html[^>]*>.*", Pattern.CASE_INSENSITIVE);
	private static Pattern[] PATTERNS;
	private static HtmlUtils sInstance = new HtmlUtils();
	private static final Pattern SLASHED_TAGS_PATTERN = Pattern.compile("<((" + "br|area|base|basefont|"
	        + "bgsound|button|col|colgroup|embed|hr" + "|img|input|isindex|keygen|link|meta"
	        + "|object|plaintext|spacer|wbr" + ")(\\s[^>]*)?)/>");
	private static final Pattern TAGS_PATTERN = Pattern.compile("(?s)<[^><]*>");

	public static HtmlUtils getInstance() {
		return HtmlUtils.sInstance;
	}

	/** equivalent to htmlToPlain(text, strictHTMLOnly=true, removeNewLines=true)
	 * @see #htmlToPlain(String, boolean, boolean) */
	public static String htmlToPlain(final String text) {
		return HtmlUtils.htmlToPlain(text, /* strictHTMLOnly= */true, /* removeNewLines= */true);
	}

	/** equivalent to htmlToPlain(text, strictHTMLOnly, removeNewLines=true)
	 * @see #htmlToPlain(String, boolean, boolean) */
	public static String htmlToPlain(final String text, final boolean strictHTMLOnly) {
		return htmlToPlain(text, strictHTMLOnly, /* removeNewLines= */true);
	}

	/** removes html markup and entities, partly and where appropriate by replacing it by plaintext equivalents like 
	 * &lt;li&gt; -> '*'.
	 * @param strictHTMLOnly if true does nothing unless the text starts with &lt;html&gt;
	 * @param removeNewLines set to false to keep all blank lines. */
	public static String htmlToPlain(final String text, final boolean strictHTMLOnly, final boolean removeNewLines) {
		if (strictHTMLOnly && !HtmlUtils.isHtmlNode(text)) {
			return text;
		}
		if (PATTERNS == null) {
			PATTERNS = new Pattern[] { 
					Pattern.compile("(?ims)>[\n\t]+"), 
					Pattern.compile("(?ims)[\n\t ]+"), 
			        Pattern.compile("(?ims)<br[^>]*>"), 
			        Pattern.compile("(?ims)<p[^>]*>\\s+"),
			        Pattern.compile("(?ims)<div[^>]*>\\s+"), 
			        Pattern.compile("(?ims)<tr[^>]*>\\s+"),
			        Pattern.compile("(?ims)<dt[^>]*>"), 
			        Pattern.compile("(?ims)<dd[^>]*>"),
			        Pattern.compile("(?ims)<td[^>]*>"), 
			        Pattern.compile("(?ims)<[uo]l[^>]*>"),
			        Pattern.compile("(?ims)<li[^>]*>"), 
			        Pattern.compile("(?ims) *</[^>]*>"),
			        Pattern.compile("(?ims)<[^/][^>]*> *"), 
			        Pattern.compile("^\n+"), 
			        Pattern.compile("(?ims)&lt;"),
			        Pattern.compile("(?ims)&gt;"), 
			        Pattern.compile("(?ims)&quot;"), 
			        Pattern.compile("(?ims)&nbsp;"),
			        Pattern.compile("(?ims)&amp;"),
			        Pattern.compile("(?ims)[ \t]+\n") };
		}
		String intermediate = text;
		int i = 0;
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll(">");
		if (removeNewLines)
			intermediate = PATTERNS[i++].matcher(intermediate).replaceAll(" ");
		else
			i++;
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n   ");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll(" ");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n   * ");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("");
		if (removeNewLines)
			intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("");
		else
			i++;
		intermediate = intermediate.trim();
		intermediate = HtmlUtils.unescapeHTMLUnicodeEntity(intermediate);
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("<");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll(">");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\"");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll(" ");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("&");
		intermediate = PATTERNS[i++].matcher(intermediate).replaceAll("\n");
		intermediate = intermediate.replace('\u00a0', ' ');
		return intermediate;
	}

	public static boolean isHtmlNode(final String text) {
		for (int i = 0; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if (ch == '<') {
				break;
			}
			if (!Character.isWhitespace(ch) || i == text.length()) {
				return false;
			}
		}
		return HtmlUtils.HTML_PATTERN.matcher(text).matches();
	}

	/** transforms {@code &, <, >, \n} and whitespace by their HTML counterpart and
	 * encloses the whole text in {@code <html><body><p>...</p></body></html>}. */
	public static String plainToHTML(final String text) {
		char myChar;
		final String textTabsExpanded = text.replaceAll("\t", "         ");
		final StringBuilder result = new StringBuilder(textTabsExpanded.length());
		final int lengthMinus1 = textTabsExpanded.length() - 1;
		result.append("<html><body><p>");
		for (int i = 0; i < textTabsExpanded.length(); ++i) {
			myChar = textTabsExpanded.charAt(i);
			switch (myChar) {
				case '&':
					result.append("&amp;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case ' ':
					if (i > 0 && i < lengthMinus1 && textTabsExpanded.charAt(i - 1) > 32
					        && textTabsExpanded.charAt(i + 1) > 32) {
						result.append(' ');
					}
					else {
						result.append("&nbsp;");
					}
					break;
				case '\n':
					result.append("</p>\n<p>");
					break;
				default:
					result.append(myChar);
			}
		}
		result.append("</p></body></html>");
		return result.toString();
	}

	public static String removeAllTagsFromString(final String text) {
		return HtmlUtils.TAGS_PATTERN.matcher(text).replaceAll("");
	}

	/**
	 * Removes all tags (<..>) from a string if it starts with "<html>..." to
	 * make it compareable.
	 */
	public static String removeHtmlTagsFromString(final String text) {
		if (HtmlUtils.isHtmlNode(text)) {
			return HtmlUtils.removeAllTagsFromString(text);
		}
		else {
			return text;
		}
	}

	public static String toXMLEscapedText(final String text) {
		final int len = text.length();
		final StringBuilder result = new StringBuilder(len);
		char myChar;
		for (int i = 0; i < len; ++i) {
			myChar = text.charAt(i);
			switch (myChar) {
				case '&':
					result.append("&amp;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '"':
					result.append("&quot;");
					break;
				default:
					result.append(myChar);
			}
		}
		return result.toString();
	}

	public static String toXMLEscapedTextExpandingWhitespace(String text) {
		text = text.replaceAll("\t", "         ");
		final int len = text.length();
		final StringBuilder result = new StringBuilder(len);
		char myChar;
		for (int i = 0; i < len; ++i) {
			myChar = text.charAt(i);
			switch (myChar) {
				case '&':
					result.append("&amp;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '"':
					result.append("&quot;");
					break;
				case ' ':
					if (i > 0 && i < len - 1 && text.charAt(i - 1) > 32 && text.charAt(i + 1) > 32) {
						result.append(' ');
					}
					else {
						result.append("&nbsp;");
					}
					break;
				default:
					result.append(myChar);
			}
		}
		return result.toString();
	}

	public static String toXMLUnescapedText(final String text) {
		return text.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");
	}

	public static String unescapeHTMLUnicodeEntity(final String text) {
		final StringBuilder resultBuilder = new StringBuilder(text.length());
		final StringBuilder entity = new StringBuilder();
		boolean readingEntity = false;
		char myChar;
		for (int i = 0; i < text.length(); ++i) {
			myChar = text.charAt(i);
			if (readingEntity) {
				if (myChar == ';') {
					if (entity.charAt(0) == '#') {
						try {
							final char c;
							if (entity.charAt(1) == 'x') {
								c = (char) Integer.parseInt(entity.substring(2), 16);
							}
							else {
								c = (char) Integer.parseInt(entity.substring(1), 10);
							}
							if (c >= ' ' || c == '\t' || c == '\r' || c == '\n') {
								resultBuilder.append(c);
							}
							else {
								resultBuilder.append(' ');
							}
						}
						catch (final NumberFormatException e) {
							resultBuilder.append('&').append(entity).append(';');
						}
					}
					else {
						resultBuilder.append('&').append(entity).append(';');
					}
					entity.setLength(0);
					readingEntity = false;
				}
				else {
					entity.append(myChar);
				}
			}
			else {
				if (myChar == '&') {
					readingEntity = true;
				}
				else {
					resultBuilder.append(myChar);
				}
			}
		}
		if (entity.length() > 0) {
			resultBuilder.append('&').append(entity);
		}
		final String result = resultBuilder.toString();
		return result;
	}

	public static String unicodeToHTMLUnicodeEntity(final String text) {
		/*
		 * Heuristic reserve for expansion : factor 1.2
		 */
		StringBuilder result = null;
		int intValue;
		char myChar;
		for (int i = 0; i < text.length(); ++i) {
			myChar = text.charAt(i);
			intValue = text.charAt(i);
			if (intValue < 32 || intValue > 126) {
				if(result == null){
					 result = new StringBuilder((int) (text.length() * 1.2));
					 result.append(text.subSequence(0, i));
				}
				result.append("&#x").append(Integer.toString(intValue, 16)).append(';');
			}
			else if(result != null){
				result.append(myChar);
			}
		}
		if(result != null)
			return result.toString();
		return text;
	}

	/**
	 *
	 */
	private HtmlUtils() {
		super();
	}

	/**
	 * @return the maximal index i such that pI is mapped to i by removing all
	 *         tags from the original input.
	 */
	public static int getMaximalOriginalPosition(final int pI, final ArrayList<IndexPair> pListOfIndices) {
		for (int i = pListOfIndices.size() - 1; i >= 0; --i) {
			final IndexPair pair =  pListOfIndices.get(i);
			if (pI >= pair.pureTextStart) {
				if (!pair.mIsTag) {
					return pair.originalStart + pI - pair.pureTextStart;
				}
				else {
					return pair.originalEnd;
				}
			}
		}
		throw new IllegalArgumentException("Position " + pI + " not found.");
	}

	public static int getMinimalOriginalPosition(final int pI, final ArrayList<IndexPair> pListOfIndices) {
	for (final IndexPair pair : pListOfIndices) {
			if (pI >= pair.pureTextStart && pI <= pair.pureTextEnd) {
				return pair.originalStart + pI - pair.pureTextStart;
			}
		}
		throw new IllegalArgumentException("Position " + pI + " not found.");
	}

	/**
	 * Replaces text in node content without replacing tags. fc, 19.12.06: This
	 * method is very difficult. If you have a simplier method, please supply
	 * it. But look that it complies with FindTextTests!!!
	 */
	public static String getReplaceResult(final Pattern pattern, final String text, final String replacement) {
		return new HtmlReplacer().getReplaceResult(pattern, replacement, text);
	}
	static class HtmlReplacer{
		private ArrayList<IndexPair> splittedStringList;
		private String stringWithoutTags;

		public String getReplaceResult(final Pattern pattern, final String replacement, final String text) {
			final String unescapedText = unescapeHTMLUnicodeEntity(text);
			initialize(unescapedText);
			final Matcher matcher = pattern.matcher(stringWithoutTags);
			if (! matcher.find()) {
				return unescapedText;
			}
			final StringBuilder sbResult = new StringBuilder();
			int pureTextPosition = 0;
			final Iterator<IndexPair> indexPairs = splittedStringList.iterator();
			IndexPair pair = null;
			for(;;){
				final int mStart = matcher.start();
				final int mEnd = matcher.end();
				
				if(pair == null){
					for(pair = indexPairs.next();pair.pureTextEnd <= mStart;pair = indexPairs.next()){
						if(pair.mIsTag || pureTextPosition <= pair.pureTextStart){
							sbResult.append(unescapedText, pair.originalStart, pair.originalEnd);
						}
						else if(pureTextPosition <= pair.pureTextEnd){
							sbResult.append(unescapedText, pair.originalStart + pureTextPosition - pair.pureTextStart, pair.originalEnd);
						}
					}
					if(pureTextPosition < pair.pureTextStart){
						pureTextPosition = pair.pureTextStart;
					}
				}

				sbResult.append(unescapedText, 
					pair.originalStart + pureTextPosition - pair.pureTextStart, 
					pair.originalStart + mStart - pair.pureTextStart);
				appendReplacement(sbResult, matcher, replacement);
				pureTextPosition = mEnd;
				
				if(matcher.find()){
					if(matcher.start() >= pair.pureTextEnd){
						if(mEnd < pair.pureTextEnd){
							sbResult.append(unescapedText, pair.originalStart + pureTextPosition - pair.pureTextStart, pair.originalEnd);
							pureTextPosition = pair.pureTextEnd;
						}
						pair = null;
					}
					continue;
				}
				for(;;){
					if(pureTextPosition <= pair.pureTextEnd){
						sbResult.append(unescapedText, pair.originalStart + pureTextPosition - pair.pureTextStart, unescapedText.length());
						return sbResult.toString();
					}
					if(pair.mIsTag){
						sbResult.append(unescapedText, pair.originalStart, pair.originalEnd);
					}
					pair = indexPairs.next();
				}
			}

		}
		
		private void initialize(final String text) {
			splittedStringList = new ArrayList<IndexPair>();
			stringWithoutTags = null;
			{
				final StringBuffer sb = new StringBuffer();
				final Matcher matcher = FIND_TAGS_PATTERN.matcher(text);
				int lastMatchEnd = 0;
				while (matcher.find()) {
					final String textWithoutTag = matcher.group(1);
					int replStart = sb.length();
					matcher.appendReplacement(sb, "$1");
					IndexPair indexPair;
					if (textWithoutTag.length() > 0) {
						indexPair = new IndexPair(lastMatchEnd, matcher.end(1), replStart, sb.length(), false);
						lastMatchEnd = matcher.end(1);
						splittedStringList.add(indexPair);
					}
					replStart = sb.length();
					indexPair = new IndexPair(lastMatchEnd, matcher.end(2), sb.length(), sb.length(), true);
					lastMatchEnd = matcher.end(2);
					splittedStringList.add(indexPair);
				}
				final int replStart = sb.length();
				matcher.appendTail(sb);
				if (sb.length() != replStart) {
					final IndexPair indexPair = new IndexPair(lastMatchEnd, text.length(), replStart, sb.length(), false);
					splittedStringList.add(indexPair);
				}
				stringWithoutTags = sb.toString();
			}
		}

		private void appendReplacement(final StringBuilder sbResult, final Matcher matcher, final String replacement) {
			int cursor = 0;
			while (cursor < replacement.length()) {
				char nextChar = replacement.charAt(cursor);
				if (nextChar == '\\') {
					cursor++;
					nextChar = replacement.charAt(cursor);
					sbResult.append(nextChar);
					cursor++;
				}
				else if (nextChar == '$') {
					// Skip past $
					cursor++;
					// The first number is always a group
					int refNum = (int) replacement.charAt(cursor) - '0';
					if ((refNum < 0) || (refNum > 9))
						throw new IllegalArgumentException("Illegal group reference");
					cursor++;
					// Capture the largest legal group string
					boolean done = false;
					while (!done) {
						if (cursor >= replacement.length()) {
							break;
						}
						int nextDigit = replacement.charAt(cursor) - '0';
						if ((nextDigit < 0) || (nextDigit > 9)) { // not a number
							break;
						}
						int newRefNum = (refNum * 10) + nextDigit;
						if (matcher.groupCount() < newRefNum) {
							done = true;
						}
						else {
							refNum = newRefNum;
							cursor++;
						}
					}
					// Append group
					if (matcher.group(refNum) != null)
						sbResult.append(matcher.group(refNum));
				}
				else {
					sbResult.append(nextChar);
					cursor++;
				}
			}
		}
	}

	/**
	 * @return true, if well formed XML.
	 */
	public static boolean isWellformedXml(final String xml) {
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			factory.newSAXParser().parse(new InputSource(new StringReader(xml)), new DefaultHandler());
			return true;
		}
		catch (final SAXParseException e) {
			LogUtils.warn("XmlParseError on line " + e.getLineNumber() + " of " + xml, e);
		}
		catch (final Exception e) {
			LogUtils.severe("XmlParseError", e);
		}
		return false;
	}

	public static String toHtml(final String xhtmlText) {
		return HtmlUtils.SLASHED_TAGS_PATTERN.matcher(xhtmlText).replaceAll("<$1>");
	}

	public static String toXhtml(String htmlText) {
		if (!HtmlUtils.isHtmlNode(htmlText)) {
			return null;
		}
		final StringReader reader = new StringReader(htmlText);
		final StringWriter writer = new StringWriter();
		try {
			XHTMLWriter.html2xhtml(reader, writer);
			final String resultXml = writer.toString();
			if (!HtmlUtils.isWellformedXml(resultXml)) {
				return HtmlUtils.toXMLEscapedText(htmlText);
			}
			return resultXml;
		}
		catch (final IOException e) {
			LogUtils.severe(e);
		}
		catch (final BadLocationException e) {
			LogUtils.severe(e);
		}
		htmlText = htmlText.replaceAll("<", "&gt;");
		htmlText = htmlText.replaceAll(">", "&lt;");
		return htmlText;
	}

	public static int endOfText(final String html) {
		int bodyEndPos = html.lastIndexOf("</body>");
		if (bodyEndPos == -1) {
			bodyEndPos = html.lastIndexOf("</BODY>");
		}
		if (bodyEndPos == -1) {
			bodyEndPos = html.lastIndexOf("</html>");
		}
		if (bodyEndPos == -1) {
			bodyEndPos = html.lastIndexOf("</HTML>");
		}
		if (bodyEndPos == -1) {
			bodyEndPos = html.length();
		}
		return bodyEndPos;
	}

	static public String combineTextWithExceptionInfo(final String text, final Exception ex) {
		final String escaped = HtmlUtils.toXMLEscapedText(text).replaceAll("\n", "<br>\n");
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append(ex.getClass().getSimpleName());
		sb.append("<br>\n");
		sb.append(ex.getMessage());
		sb.append("<br>\n");
		sb.append(escaped);
		final String string = sb.toString();
		return string;
	}

	public static String element(final String name, final String content) {
		return HtmlUtils.element(name, null, content);
	}

	public static String element(final String name, final Map<String, String> attributes, final String content) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<").append(name).append(HtmlUtils.toAttributeString(attributes)).append(">");
		if (content != null && content.length() > 0) {
			builder.append(content);
		}
		return builder.append("</").append(name).append(">").toString();
	}

	private static String toAttributeString(final Map<String, String> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		for (final Map.Entry<String, String> entry : attributes.entrySet()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(entry.getKey());
			builder.append("=\"");
			builder.append(entry.getKey());
			builder.append('"');
		}
		return builder.toString();
	}

	public static String extractRawBody(final String text) {
		int start = text.indexOf("<body>");
		final int textBegin;
		if (start != -1)
			textBegin = start + "<body>".length();
		else{
			start = text.indexOf("</head>");
			if (start != -1){
				textBegin = start+ "</head>".length();
			}
			else {
				start = text.indexOf("<html>");
				textBegin = start+ "<html>".length();
			}
		}
		int end = text.indexOf("</body>", textBegin);
		if (end == -1){
			end = text.indexOf("</html>", textBegin);
			if (end == -1){
				end = text.length();
			}
		}
		return text.substring(textBegin, end).trim();
	}

	/** Gets the string URL of an existing link, or null if none. */
	public static String getURLOfExistingLink(HTMLDocument doc, int pos) {
	    //setIgnoreActions(true);      
	    final Element linkElement = HtmlUtils.getCurrentLinkElement(doc, pos);
	    final boolean foundLink = (linkElement != null);
	    if (!foundLink) {
	        return null;
	    }
	    final AttributeSet elemAttrs = linkElement.getAttributes();
	    final Object linkAttr = elemAttrs.getAttribute(HTML.Tag.A);
	    final Object href = ((AttributeSet) linkAttr).getAttribute(HTML.Attribute.HREF);
	    return href != null ? href.toString() : null;
	}

	public static Element getCurrentLinkElement(HTMLDocument doc, int pos) {
	    Element element2 = null;
	    Element element = doc.getCharacterElement(pos);
	    Object linkAttribute = null; //elem.getAttributes().getAttribute(HTML.Tag.A);
	    Object href = null;
	    while (element != null && linkAttribute == null) {
	        element2 = element;
	        linkAttribute = element.getAttributes().getAttribute(HTML.Tag.A);
	        if (linkAttribute != null) {
	            href = ((AttributeSet) linkAttribute).getAttribute(HTML.Attribute.HREF);
	        }
	        element = element.getParentElement();
	    }
	    if (linkAttribute != null && href != null) {
	        return element2;
	    }
	    else {
	        return null;
	    }
	}

	public static boolean isEmpty(String newText) {
		return ! (newText.contains("<img") || newText.contains("<table")) 
				&& htmlToPlain(newText).equals("");
    }

	public static String toHTMLEscapedText(String s) {
		return toXMLEscapedText(s).replaceAll("\n", "<br>\n");
	}

	private static Pattern htmlBodyPattern = Pattern.compile("^\\s*(?:<html>|<body>)+\\s*(.*?)"
            + "\\s*(?:</body>|</html>)+\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Join arbitrary texts to html. Plain text arguments will be transformed via
     * {@link #plainToHTML(String)}, i.e. newlines and other special characters will
     * be translated to their HTML counterpart and wrapped in a paragraph (&lt;p&gt;&lt;/p&gt;).
     * <pre>{@code
     *   // plain + html -> <html><body><p>text1</p>text2</body></html>
     *   HtmlUtils.join("text1", "", "<html><body>text2</body></html>");
     *   // insert an empty paragraph (<p></p>) between two strings:
     *   HtmlUtils.join("text1", "", "text2");
     *   // this will insert two paragraphs:
     *   HtmlUtils.join("text1", "\n", "text2");
     * }</pre>
     * @param texts either html (starting with <HTML> or <html>) or plain text.
     * @return html
     */
    public static String join(String... texts) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");
        for (int i = 0; i < texts.length; i++) {
            String string = texts[i];
            if (i > 0)
                builder.append('\n');
            if (!isHtmlNode(string))
                string = plainToHTML(string);
            builder.append(htmlBodyPattern.matcher(string).replaceFirst("$1"));
        }
        builder.append("</body></html>");
        return builder.toString();
    }
}
