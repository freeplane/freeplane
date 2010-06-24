package org.freeplane.core.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ConfigurationUtils {
	private static final String CONFIG_LIST_VALUE_SEPARATOR = File.pathSeparator + File.pathSeparator;

	public  static List<String> decodeListValue(final String value) {
        final String[] values = value.split(CONFIG_LIST_VALUE_SEPARATOR);
        return Arrays.asList(values);
    }

	public static String encodeListValue(final List<String> list) {
		return StringUtils.join(list.toArray(), CONFIG_LIST_VALUE_SEPARATOR);
    }
}
