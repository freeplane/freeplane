package org.freeplane.core.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ConfigurationUtils {
	private static final String CONFIG_LIST_VALUE_SEPARATOR_STRICT = File.pathSeparator + File.pathSeparator;
	private static final String CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE = File.pathSeparator + '+';

	/** if not requireTwo one pathseparator suffices otherwise two are required. */
	public static List<String> decodeListValue(final String value, boolean requireTwo) {
		final String[] values = value.length() == 0 ? new String[0] : value
		    .split(requireTwo ? CONFIG_LIST_VALUE_SEPARATOR_STRICT : CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE);
		return Arrays.asList(values);
	}

	/** if not requireTwo one pathseparator suffices otherwise two are required. */
	public static String encodeListValue(final List<String> list, boolean requireTwo) {
		return StringUtils.join(list.toArray(), requireTwo ? CONFIG_LIST_VALUE_SEPARATOR_STRICT
		        : CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE);
	}
}
