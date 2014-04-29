package org.freeplane.plugin.script;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;

class ScriptingMenuContributor implements IMenuContributor {
    private final MenuBuilder menuBuilder;
    private final ScriptingConfiguration configuration;
    private final HashSet<String> registeredLocations = new HashSet<String>();

    ScriptingMenuContributor(ModeController modeController, ScriptingConfiguration configuration) {
        this.menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder(MenuBuilder.class);
        this.configuration = configuration;
    }

    @Override
    public void updateMenus(ModeController modeController, MenuBuilder builder) {
        for (final String scriptsParentLocation : ScriptingConfiguration.getScriptsParentLocations()) {
            final String scriptsLocation = ScriptingConfiguration.getScriptsLocation(scriptsParentLocation);
            addSubMenu(scriptsParentLocation, scriptsLocation, TextUtils.getText("ExecuteScripts.text"));
            registeredLocations.add(scriptsLocation);
            if (configuration.getMenuTitleToPathMap().isEmpty()) {
                final String message = "<html><body><em>" + TextUtils.getText("ExecuteScripts.noScriptsAvailable")
                        + "</em></body></html>";
                menuBuilder.addElement(scriptsLocation, new JMenuItem(message), 0);
            }
            registerScriptsForLocation(scriptsLocation);
        }
    }

    private void registerScriptsForLocation(final String scriptsLocation) {
        for (final Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
            registerScriptForLocation(scriptsLocation, entry.getKey(), entry.getValue());
        }
    }

    private void registerScriptForLocation(final String scriptsLocation, final String scriptName,
                                           final String scriptPath) {
        final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
        // in the worst case three actions will cache a script - should not matter that much since it's unlikely
        // that one script is used in multiple modes by the same user
        for (final ExecutionMode executionMode : metaData.getExecutionModes()) {
            final String titleKey;
            final String scriptLocation;
            String location = metaData.getMenuLocation(executionMode);
            // FIXME: reduce code duplication (VB)
            if (location == null) {
                location = scriptsLocation + "/" + scriptName;
                if (!registeredLocations.contains(location)) {
                    final String parentMenuTitle = pimpMenuTitle(metaData.getScriptName());
                    addSubMenu(parentLocation(location), location, parentMenuTitle);
                    registeredLocations.add(location);
                }
                titleKey = metaData.getTitleKey(executionMode);
                scriptLocation = location + "/" + titleKey;
            }
            else {
                if (!registeredLocations.contains(location)) {
                    addSubMenu(parentLocation(location), location, getMenuTitle(location));
                    registeredLocations.add(location);
                }
                titleKey = metaData.getTitleKey(executionMode);
                scriptLocation = location + "/" + titleKey;
            }
            if (!registeredLocations.contains(scriptLocation)) {
                addMenuItem(location, scriptName, scriptPath, executionMode, titleKey, metaData);
                registeredLocations.add(scriptLocation);
            }
        }
    }

    // location might be something like /menu_bar/edit/editGoodies
    private String getMenuTitle(final String location) {
        int index = location.lastIndexOf('/');
        final String lastKey = location.substring(index + 1);
        return TextUtils.getText(lastKey, TextUtils.getText("addons." + lastKey, lastKey));
    }

    private String parentLocation(String location) {
        return location.replaceFirst("/[^/]*$", "");
    }

    private void addSubMenu(final String parentLocation, final String location, String menuTitle) {
        if (menuBuilder.get(location) == null) {
            final JMenu menuItem = new JMenu();
            MenuBuilder.setLabelAndMnemonic(menuItem, menuTitle);
            menuBuilder.addMenuItem(parentLocation, menuItem, location, MenuBuilder.AS_CHILD);
        }
    }

    private void addMenuItem(final String location, final String scriptName, final String scriptPath,
                             final ExecutionMode executionMode, final String titleKey, ScriptMetaData metaData) {
        final String translation = TextUtils.getText(titleKey, titleKey.replace('_', ' '));
        final String menuName = translation.contains("{0}") ? MessageFormat.format(translation,
            pimpMenuTitle(scriptName)) : translation;
        menuBuilder.addAction(location, new ExecuteScriptAction(scriptName, menuName, scriptPath, executionMode,
            metaData.cacheContent(), metaData.getPermissions()), MenuBuilder.AS_CHILD);
    }

    /** menuTitle may either be a scriptName or a translation key. */
    private String pimpMenuTitle(final String menuTitle) {
        final String translation = TextUtils.getText(menuTitle, null);
        // convert CamelCase to Camel Case
        return translation != null ? translation : menuTitle.replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}
