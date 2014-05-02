package org.freeplane.plugin.script;

import java.text.MessageFormat;

import org.freeplane.core.util.TextUtils;

public class ScriptingMenuUtils {
    static final String LABEL_NO_SCRIPTS_AVAILABLE = "ExecuteScripts.noScriptsAvailable";
    static final String LABEL_SCRIPTS_MENU = "ExecuteScripts.text";

    public static String parentLocation(String location) {
        return location.replaceFirst("/[^/]*$", "");
    }

    public static String makeMenuTitle(final String scriptName, final String titleKey) {
        final String translation = TextUtils.getText(titleKey, titleKey.replace('_', ' '));
        return translation.contains("{0}") ? MessageFormat.format(translation, scriptNameToMenuTitle(scriptName))
                : translation;
    }

    /** menuTitle may either be a scriptName or a translation key. */
    public static String scriptNameToMenuTitle(final String scriptName) {
        final String translation = TextUtils.getText(scriptName, null);
        // convert CamelCase to Camel Case
        return translation != null ? translation : scriptName.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public static String noScriptsAvailableMessage() {
        return "<html><body><em>" + TextUtils.getText(ScriptingMenuUtils.LABEL_NO_SCRIPTS_AVAILABLE)
                + "</em></body></html>";
    }
}
