package org.freeplane.plugin.configurationservice;

import org.freeplane.core.util.TextUtils;

public class FormulaUtils {

	public static String getFormulaText(String key) {
        return TextUtils.getText(getFormulaKey(key));
    }

	public static String getFormulaKey(String key) {
	    return "formula." + key;
    }
}
