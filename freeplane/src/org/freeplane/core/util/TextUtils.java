package org.freeplane.core.util;

import java.text.MessageFormat;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;

/** utilities for translations, conversions to/from number and dates etc.
 * In scripts available as "global variable" <code>textUtils</code>. */
public class TextUtils {
	public static String format(final String resourceKey, final Object... messageArguments) {
		final String text = TextUtils.getText(resourceKey);
		if (text == null)
			return null;
		MessageFormat formatter;
        try {
            formatter = new MessageFormat(text);
        }
        catch (IllegalArgumentException e) {
            LogUtils.severe("wrong format " + text + " for property " + resourceKey, e);
            return text;
        }
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
		if("".equals(text))
			return false;
		final char first = text.charAt(0);
		if(Character.isLetter(first) || Character.isSpaceChar(first))
			return false;
		try {
	        Double.parseDouble(text);
	        return true;
        }
        catch (NumberFormatException e) {
        	return false;
        }
    }

	public static boolean isEmpty(final String str) {
	    return str == null || str.length() == 0;
	}
}
