package org.freeplane.plugin.formula;

import org.freeplane.core.util.TextUtils;

public class FormulaPluginUtils {

	public static String getFormulaText(String key) {
        return TextUtils.getText(getFormulaKey(key));
    }

	public static String getFormulaKey(String key) {
	    return "formula." + key;
    }
}
