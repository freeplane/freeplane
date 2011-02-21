package org.freeplane.core.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;

/** utilities for translations, conversions to/from number and dates etc.
 * In scripts available as "global variable" <code>textUtils</code>. */
public class TextUtils {
	private static HashMap<String, DateFormat> dateFormatCache = new HashMap<String, DateFormat>();

	public static String format(final String resourceKey, final Object... messageArguments) {
		final String text = TextUtils.getText(resourceKey);
		if (text == null)
			return null;
		final MessageFormat formatter = new MessageFormat(text);
		final Object[] processedArguments;
		if(text.startsWith("<html>")){
			processedArguments = new String[messageArguments.length];
			int i = 0;
			for(Object s : messageArguments){
				processedArguments[i++] = HtmlUtils.toXMLEscapedText(s.toString());
			}
		}
		else{
			processedArguments = messageArguments;
		}
		return formatter.format(processedArguments);
	}

	public static String getOptionalText(final String string) {
		return string == null ? null : TextUtils.getText(string);
	}

	public static String removeMnemonic(final String rawLabel) {
		return rawLabel.replaceFirst("&([^ ])", "$1");
	}

	/** Removes the "TranslateMe" sign from the end of not translated texts. */
	public static String removeTranslateComment(String inputString) {
		if (inputString != null && inputString.endsWith(ResourceBundles.POSTFIX_TRANSLATE_ME)) {
			inputString = inputString
			    .substring(0, inputString.length() - ResourceBundles.POSTFIX_TRANSLATE_ME.length());
		}
		return inputString;
	}

	public static NamedObject createTranslatedString(final String key) {
		final String fs = TextUtils.getText(key);
		return new NamedObject(key, fs);
	}

	public static String getText(final String key) {
		if (key == null) {
			return null;
		}
		return ((ResourceBundles) ResourceController.getResourceController().getResources()).getResourceString(key);
	}

	public static String getText(final String key, final String defaultString) {
		if (key == null) {
			return defaultString;
		}
		return ((ResourceBundles) ResourceController.getResourceController().getResources()).getResourceString(key,
		    defaultString);
	}

	public static String getOptionalTranslation(String text) {
		if(text.startsWith("%")){
			return getText(text.substring(1));
		}
		return text;
	}

	public static Number toNumber(final String text) throws NumberFormatException {
		try {
			return text == null ? null : Long.decode(text);
		}
		catch (NumberFormatException e) {
			// stupid FloatingDecimal.readJavaFormatString() trims the input string -> care for leading whitespace
			if (text.length() == 0 || Character.isWhitespace(text.charAt(0))) {
				throw new NumberFormatException("number '" + text + "' empty or starts with space");
			}
			return Double.valueOf(text);
		}
	}

	public static boolean isNumber(String text) {
		return NumberUtils.isNumber(text);
    }
	
	public static Date toDate(String text) {
		final Date date = toDateISO(text);
		return date == null ? toDateUser(text) : date;
	}
	
	public static boolean isDate(String text) {
		return isDateISO(text) || isDateUser(text);
	}

	public static final String ISO_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	public static final String ISO_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final Pattern ISO_DATE_TIME_REGEXP_PATTERN = Pattern.compile("\\d{4}(-?)\\d{2}(-?)\\d{2}" //
        + "(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?");

	public static Date toDateISO(String text) {
		//        1         2         34            5         6   7        8           9
		// \\d{4}(-?)\\d{2}(-?)\\d{2}(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?
		final Matcher matcher = ISO_DATE_TIME_REGEXP_PATTERN.matcher(text);
		if (matcher.matches()) {
			StringBuilder builder = new StringBuilder("yyyy");
			builder.append(matcher.group(1));
			builder.append("MM");
			builder.append(matcher.group(2));
			builder.append("dd");
			if (matcher.group(3) != null) {
				if (matcher.group(4) != null) {
					builder.append('\'');
					builder.append(matcher.group(4));
					builder.append('\'');
				}
				builder.append("HH");
				builder.append(matcher.group(5));
				builder.append("mm");
				if (matcher.group(7) != null) {
					builder.append(matcher.group(6));
					builder.append("ss");
				}
				if (matcher.group(8) != null) {
					builder.append(".SSS");
				}
				if (matcher.group(9) != null) {
					builder.append("Z");
				}
			}
			final String pattern = builder.toString();
			return parseDate(text, pattern);
		}
		return null;
	}

	private static Date parseDate(String text, final String pattern) {
	    DateFormat parser = dateFormatCache.get(pattern);
	    if (parser == null) {
	    	parser = new SimpleDateFormat(pattern);
	    	dateFormatCache.put(pattern, parser);
	    }
	    final ParsePosition pos = new ParsePosition(0);
	    final Date date = parser.parse(text, pos);
	    if (date != null && pos.getIndex() == text.length()) {
	    	return date;
	    }
		return null;
    }

	public static boolean isDateISO(String text) {
		if (text == null)
			return false;
		return ISO_DATE_TIME_REGEXP_PATTERN.matcher(text).matches();
	}

	public static String toStringISO(final Date date) {
		// use local timezone
    	return DateFormatUtils.format(date, ISO_DATE_TIME_FORMAT_PATTERN);
    }

	public static String toStringShortISO(final Date date) {
    	return DateFormatUtils.format(date, ISO_DATE_FORMAT_PATTERN);
    }
	
	public static Date toDateUser(String text) {
		return parseDate(text, ResourceController.getResourceController().getProperty("OptionPanel.date_format"));
	}

	public static boolean isDateUser(String text) {
		return text != null && toDateUser(text) != null;
	}
}
