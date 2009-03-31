package org.freeplane.core.resources;

import java.text.MessageFormat;

public class FpStringUtils {
	/**
	 * Example: expandPlaceholders("Hello $1.","Dolly"); => "Hello Dolly."
	 */
	static String expandPlaceholders(final String message, String s1) {
		String result = message;
		if (s1 != null) {
			s1 = s1.replaceAll("\\\\", "\\\\\\\\");
			result = result.replaceAll("\\$1", s1);
		}
		return result;
	}

	static String expandPlaceholders(final String message, final String s1, final String s2) {
		String result = message;
		if (s1 != null) {
			result = result.replaceAll("\\$1", s1);
		}
		if (s2 != null) {
			result = result.replaceAll("\\$2", s2);
		}
		return result;
	}

	public static String format(final String resourceKey, final Object[] messageArguments) {
		final MessageFormat formatter = new MessageFormat(FreeplaneResourceBundle.getText(resourceKey));
		final String stringResult = formatter.format(messageArguments);
		return stringResult;
	}

	public static String formatText(final String key, final String s1) {
		final String format = FreeplaneResourceBundle.getText(key);
		if (format == null) {
			return null;
		}
		return FpStringUtils.expandPlaceholders(format, s1);
	}

	public static String formatText(final String key, final String s1, final String s2) {
		final String format = FreeplaneResourceBundle.getText(key);
		if (format == null) {
			return null;
		}
		return FpStringUtils.expandPlaceholders(format, s1, s2);
	}

	public static String getOptionalText(final String string) {
		return string == null ? null : FreeplaneResourceBundle.getText(string);
	}

	public static String removeMnemonic(final String rawLabel) {
		return rawLabel.replaceFirst("&([^ ])", "$1");
	}

	/**
	 * Removes the "TranslateMe" sign from the end of not translated texts.
	 */
	// TODO ARCH rladstaetter 15.02.2009 method should have no need for existance! the build process should filter out resources not fit for production.
	public static String removeTranslateComment(String inputString) {
		if (inputString != null && inputString.endsWith(FreeplaneResourceBundle.POSTFIX_TRANSLATE_ME)) {
			inputString = inputString.substring(0, inputString.length()
			        - FreeplaneResourceBundle.POSTFIX_TRANSLATE_ME.length());
		}
		return inputString;
	}
}
