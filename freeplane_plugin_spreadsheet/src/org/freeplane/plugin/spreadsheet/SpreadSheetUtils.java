package org.freeplane.plugin.spreadsheet;

import org.freeplane.core.util.TextUtils;

public class SpreadSheetUtils {

	public static String getSpreadSheetText(String key) {
        return TextUtils.getText(getSpreadSheetKey(key));
    }

	public static String getSpreadSheetKey(String key) {
	    return "spreadsheet." + key;
    }
}
