package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ScriptingMenuUtils.LABEL_SCRIPTS_MENU;
import static org.freeplane.plugin.script.ScriptingMenuUtils.makeMenuTitle;
import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;
import static org.freeplane.plugin.script.ScriptingMenuUtils.parentLocation;
import static org.freeplane.plugin.script.ScriptingMenuUtils.scriptNameToMenuTitle;

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
    private final String parentLocation;

    ScriptingMenuContributor(ModeController modeController, ScriptingConfiguration configuration, String parentLocation) {
        this.menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder(MenuBuilder.class);
        this.configuration = configuration;
        this.parentLocation = parentLocation;
    }

    @Override
    public void updateMenus(ModeController modeController, MenuBuilder builder) {
        final String scriptsLocation = ScriptingConfiguration.getScriptsLocation(parentLocation);
        addSubMenu(parentLocation, scriptsLocation, TextUtils.getText(LABEL_SCRIPTS_MENU));
        registerScriptsForLocation(scriptsLocation);
    }

    private void registerScriptsForLocation(final String scriptsLocation) {
        if (configuration.getMenuTitleToPathMap().isEmpty()) {
            menuBuilder.addElement(scriptsLocation, new JMenuItem(noScriptsAvailableMessage()), 0);
        }
        else {
            for (final Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
                registerScriptForLocation(scriptsLocation, entry.getKey(), entry.getValue());
            }
        }
    }

    private void registerScriptForLocation(final String scriptsLocation, final String scriptName,
                                           final String scriptPath) {
        final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
        for (final ExecutionMode executionMode : metaData.getExecutionModes()) {
            final String title;
            String location = metaData.getMenuLocation(executionMode);
            if (location == null) {
                location = scriptsLocation + "/" + scriptName;
                title = scriptNameToMenuTitle(metaData.getScriptName());
            }
            else {
                title = getTitleForScript(location);
            }
            addSubMenu(parentLocation(location), location, title);
            addMenuItem(location, scriptName, scriptPath, executionMode, metaData);
        }
    }

    // location might be something like /menu_bar/edit/editGoodies.
    // Use the last path element for label lookup
    // Try to find text by 1. direct match or 2. with "addons." prefix or 3. use it verbatim
    private String getTitleForScript(final String location) {
        int index = location.lastIndexOf('/');
        final String lastKey = location.substring(index + 1);
        return TextUtils.getText(lastKey, TextUtils.getText("addons." + lastKey, lastKey));
    }

    private void addSubMenu(final String parentLocation, final String location, String menuTitle) {
        if (registeredLocations.add(location) && menuBuilder.get(location) == null) {
            final JMenu menuItem = new JMenu();
            MenuBuilder.setLabelAndMnemonic(menuItem, menuTitle);
            menuBuilder.addMenuItem(parentLocation, menuItem, location, MenuBuilder.AS_CHILD);
        }
    }

    private void addMenuItem(final String location, final String scriptName, final String scriptPath,
                             final ExecutionMode executionMode, ScriptMetaData metaData) {
        final String titleKey = metaData.getTitleKey(executionMode);
        if (registeredLocations.add(location + "/" + titleKey)) {
            menuBuilder.addAction(location, new ExecuteScriptAction(scriptName, makeMenuTitle(scriptName, titleKey),
                scriptPath, executionMode, metaData.cacheContent(), metaData.getPermissions()), MenuBuilder.AS_CHILD);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + parentLocation + ")";
    }
}
