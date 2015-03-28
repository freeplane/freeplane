package org.freeplane.plugin.script;

import java.text.MessageFormat;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;

public class ScriptingMenuUtils {
    static final String LABEL_AVAILABLE_MODES_TOOLTIP = "ExecuteScript.available_modes_tooltip";
    static final String LABEL_NO_SCRIPTS_AVAILABLE = "ExecuteScripts.noScriptsAvailable";
    static final String LABEL_SCRIPT = "ExecuteScript.script";
    static final String LABEL_SCRIPTS_MENU = "ExecuteScripts.text";

    public static String parentLocation(String location) {
        return location.replaceFirst("/[^/]*$", "");
    }

    public static String getMenuItemTitle(ScriptMetaData metaData, ExecutionMode executionMode) {
        final String titleKey = metaData.getTitleKey(executionMode);
        final String scriptName = metaData.getScriptName();
        final String translation = TextUtils.getText(titleKey, titleKey.replace('_', ' '));
        return translation.contains("{0}") ? MessageFormat.format(translation, scriptNameToMenuItemTitle(scriptName))
                : translation;
    }

    /** menuTitle may either be a scriptName or a translation key. */
    public static String scriptNameToMenuItemTitle(final String scriptName) {
        final String translation = TextUtils.getText(scriptName, null);
        // convert CamelCase to Camel Case
        return translation != null ? translation : scriptName.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public static String noScriptsAvailableMessage() {
        return "<html><body><em>" + TextUtils.getText(ScriptingMenuUtils.LABEL_NO_SCRIPTS_AVAILABLE)
                + "</em></body></html>";
    }

    public static String getMenuTitle(ScriptMetaData metaData, ExecutionMode executionMode) {
        final String specialLocation = metaData.getMenuLocation(executionMode);
        if (specialLocation == null) {
            return scriptNameToMenuItemTitle(metaData.getScriptName());
        }
        else {
            return getTitleForLocation(specialLocation);
        }
    }

    // location might be something like /menu_bar/edit/editGoodies.
    // Use the last path element for label lookup
    // Try to find text by 1. direct match or 2. with "addons." prefix or 3. use it verbatim
    public static String getTitleForLocation(final String location) {
        int index = location.lastIndexOf('/');
        final String lastKey = location.substring(index + 1);
        return TextUtils.getText(lastKey, TextUtils.getText("addons." + lastKey, lastKey));
    }
}
