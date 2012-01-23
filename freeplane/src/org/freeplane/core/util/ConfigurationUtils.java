package org.freeplane.core.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ConfigurationUtils {
	private static final String CONFIG_LIST_VALUE_SEPARATOR_STRICT = File.pathSeparator + File.pathSeparator;
	private static final String CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE = File.pathSeparator + '+';

    /** if not requireTwo one pathseparator suffices otherwise two are required. */
    public static List<String> decodeListValue(final String value, boolean requireTwo) {
        if (value.length() == 0)
            return Collections.emptyList();
        final String sep = requireTwo ? CONFIG_LIST_VALUE_SEPARATOR_STRICT : CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE;
        return Arrays.asList(value.split("\\s*" + sep + "\\s*"));
    }

	/** if not requireTwo one pathseparator suffices otherwise two are required. */
	public static String encodeListValue(final List<String> list, boolean requireTwo) {
		return StringUtils.join(list.iterator(), requireTwo ? CONFIG_LIST_VALUE_SEPARATOR_STRICT
		        : CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE);
	}

	public static File getLocalizedFile(final File baseDir, final String document, final String languageCode) {
        final File file;
    	final int extPosition = document.lastIndexOf('.');
    	if (extPosition != -1) {
    		final String map = document.substring(0, extPosition) + "_" + languageCode
    		        + document.substring(extPosition);
    		final File localFile = new File(baseDir, map);
    		if (localFile.canRead()) {
    			file = localFile;
    		}
    		else {
    			file = new File(baseDir, document);
    		}
    	}
    	else {
    		file = new File(baseDir, document);
    	}
        return file;
    }
}
