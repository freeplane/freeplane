package org.freeplane.core.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;

public class TextUtils {
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

	final static NumberFormatException numberFormatException = new NumberFormatException("text starts with space");
	
	public static Number toNumber(String text) throws NumberFormatException{
		if(text == null)
			return null;
		if(text.length() == 0 || Character.isWhitespace(text.charAt(0))) {
			throw numberFormatException;
		}
		try {
			return Long.valueOf(text);
		}
		catch (final NumberFormatException fne) {
		};
		return Double.valueOf(text);
	}

	public static boolean isNumber(String text) {
		if(text == null || text.length() == 0 || Character.isWhitespace(text.charAt(0))) {
			return false;
		}
		return NumberUtils.isNumber(text);
    }
	public static final Pattern DATE_REGEXP_PATTERN = Pattern.compile("\\d{4}(-?)\\d{2}(-?)\\d{2}" //
        + "(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?");
	public static Date toDateISO(String text){
        //        1         2         34            5         6   7        8           9
    	// \\d{4}(-?)\\d{2}(-?)\\d{2}(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?
    	final Matcher matcher = DATE_REGEXP_PATTERN.matcher(text);
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
    		SimpleDateFormat parser = new SimpleDateFormat(builder.toString());
    		ParsePosition pos = new ParsePosition(0);
    		Date date = parser.parse(text, pos);
    		if (date != null && pos.getIndex() == text.length()) {
    			return date;
    		}
    	}
		return null;
    }

	public static boolean isDateISO(String text) {
		if (text == null)
			return false;
		final Matcher matcher = DATE_REGEXP_PATTERN.matcher(text);
		return matcher.matches();
    }

	public static String toStringISO(Date date) {
    	return DateFormatUtils.formatUTC(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

	public static final DateFormat shortDateFormat;
	static {
		shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		shortDateFormat.setLenient(true);
	}
	
	public static Date toDateLocal(String text) {
		final ParsePosition parsePosition = new ParsePosition(0);
		Date date = shortDateFormat.parse(text, parsePosition);
		if(parsePosition.getErrorIndex() == -1 && parsePosition.getIndex() == text.length())
			return date;
		return null;
	}

	public static boolean isDateLocal(String text) {
		final ParsePosition parsePosition = new ParsePosition(0);
		shortDateFormat.parse(text, parsePosition);
		if(parsePosition.getErrorIndex() == -1 && parsePosition.getIndex() == text.length())
			return true;
		return false;
	}

	public static String toStringLocal(Date date) {
		return shortDateFormat.format(date);
	}
}
