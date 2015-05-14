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
        // -1: don't discard trailing empty strings
        return Arrays.asList(value.split("\\s*" + sep + "\\s*", -1));
    }

	/** if not requireTwo one pathseparator suffices otherwise two are required. */
	public static String encodeListValue(final List<String> list, boolean requireTwo) {
		return StringUtils.join(list.iterator(), requireTwo ? CONFIG_LIST_VALUE_SEPARATOR_STRICT
		        : CONFIG_LIST_VALUE_SEPARATOR_ONE_OR_MORE);
	}

	public static File getLocalizedFile(final File[] baseDirs, final String document, final String languageCode) {
    	final int extPosition = document.lastIndexOf('.');
    	final String localizedDocument;
    	if (extPosition != -1) {
    		localizedDocument = document.substring(0, extPosition) + "_" + languageCode + document.substring(extPosition);
    	}
    	else{
    		localizedDocument = document;
    	}
    	for(File baseDir : baseDirs){
    		if(baseDir != null){
    			final File localFile = new File(baseDir, localizedDocument);
    			if (localFile.canRead()) {
    				return localFile;
    			}
    			final File file = new File(baseDir, document);
    			if (file.canRead()) {
    				return file;
    			}
    		}
    	}
        return null;
    }
}
