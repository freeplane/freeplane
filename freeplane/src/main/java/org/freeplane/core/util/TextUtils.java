package org.freeplane.core.util;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.format.FormatController;

/** utilities for translations, conversions to/from number and dates etc.
 * In scripts available as "global variable" <code>textUtils</code>. */
public class TextUtils {
    // from http://lists.xml.org/archives/xml-dev/200108/msg00891.html
    // but make scheme mandatory
    private static final String URI_REGEXP = "([a-zA-Z][0-9a-zA-Z+\\-\\.]+:" //
            + "/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?";
    private static Pattern uriPattern = Pattern.compile(URI_REGEXP);

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
				processedArguments[i++] = HtmlUtils.toHTMLEscapedText(s.toString());
			}
		}
		else{
			processedArguments = messageArguments;
		}
		return formatter.format(processedArguments);
	}

	public static String getOptionalText(final String string) {
		return string == null ? null : TextUtils.getRawText(string);
	}

	public static String getOptionalText(final String string, final String defaultValue) {
		return string == null ? null : TextUtils.getRawText(string, defaultValue);
	}

	public static String removeMnemonic(final String rawLabel) {
		final int pos = rawLabel.indexOf('&');
		if(pos == -1)
			return rawLabel;
		final int length = rawLabel.length();
		StringBuilder sb = new StringBuilder(length);
		sb.append(rawLabel.subSequence(0, pos));
		sb.append(rawLabel.subSequence(pos + 1, length));
		return sb.toString();
		
	}

	/** Removes the "TranslateMe" sign from the end of not translated texts. */
	public static String removeTranslateComment(String inputString) {
		if (inputString != null && inputString.endsWith(ResourceBundles.POSTFIX_TRANSLATE_ME)) {
			inputString = inputString
			    .substring(0, inputString.length() - ResourceBundles.POSTFIX_TRANSLATE_ME.length());
		}
		return inputString;
	}

	public static TranslatedObject createTranslatedString(final String key) {
		final String fs = TextUtils.getText(key);
		return new TranslatedObject(key, fs);
	}

	public static String getText(final String key) {
		final String text = getRawText(key);
		if(text == null)
			return text;
		return removeMnemonic(text);
	}

	public static String getRawText(final String key) {
		if (key == null) {
			return null;
		}
		return getLanguageResources().getResourceString(key);
	}

	public static String getOriginalRawText(final String key) {
		if (key == null) {
			return null;
		}
		return getLanguageResources().getOriginalString(key);
	}

	public static String getText(final String key, final String defaultString) {
		final String text = getRawText(key, defaultString);
		if(text == null)
			return text;
		return removeMnemonic(text);
	}
	public static String getRawText(final String key, final String defaultString) {
		if (key == null) {
			return defaultString;
		}
		return getLanguageResources().getResourceString(key, defaultString);
	}

	private static ResourceBundles getLanguageResources() {
		return (ResourceBundles) ResourceController.getResourceController().getResources();
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
		if (isEmpty(text))
			return false;
		final char first = text.charAt(0);
		if (Character.isLetter(first) || Character.isSpaceChar(first))
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

	/** in opposite to the URI make scheme mandatory. */
	public static boolean matchUriPattern(String text) {
        return text.length() > 0 && uriPattern.matcher(text).matches();
    }

	/** accessor for scripts. */
	public DecimalFormat getDefaultNumberFormat() {
		return FormatController.getController().getDefaultNumberFormat();
	}
	
	/** accessor for scripts. */
	public SimpleDateFormat getDefaultDateFormat() {
		return FormatController.getController().getDefaultDateFormat();
	}
	
	/** accessor for scripts. */
	public SimpleDateFormat getDefaultDateTimeFormat() {
		return FormatController.getController().getDefaultDateTimeFormat();
	}

	/** Shortcut for scripting: Copies <code>string</code> to the system clipboard. */
	public static void copyToClipboard(String string) {
	    ClipboardController.getController().setClipboardContents(string);
    }
	
	/** Shortcut for scripting: Copies <code>html</code> with mimetype text/html to the system clipboard. */
	public static void copyHtmlToClipboard(String html) {
		ClipboardController.getController().setClipboardContentsToHtml(html);
	}
}
