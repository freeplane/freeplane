package org.freeplane.plugin.script;

import static org.freeplane.plugin.script.ScriptingMenuUtils.LABEL_SCRIPTS_MENU;
import static org.freeplane.plugin.script.ScriptingMenuUtils.getMenuTitle;
import static org.freeplane.plugin.script.ScriptingMenuUtils.getMenuItemTitle;
import static org.freeplane.plugin.script.ScriptingMenuUtils.noScriptsAvailableMessage;
import static org.freeplane.plugin.script.ScriptingMenuUtils.parentLocation;

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
    // the location containing the list of scripts
    private String scriptsLocation;

    ScriptingMenuContributor(ModeController modeController, ScriptingConfiguration configuration, String parentLocation) {
        this.menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder(MenuBuilder.class);
        this.configuration = configuration;
        this.parentLocation = parentLocation;
        scriptsLocation = ScriptingConfiguration.getScriptsLocation(parentLocation);
    }

    @Override
    public void updateMenus(ModeController modeController, MenuBuilder builder) {
        addSubMenu(parentLocation, scriptsLocation, TextUtils.getText(LABEL_SCRIPTS_MENU));
        registerScripts();
    }

    private void registerScripts() {
        if (configuration.getMenuTitleToPathMap().isEmpty()) {
            menuBuilder.addElement(scriptsLocation, new JMenuItem(noScriptsAvailableMessage()), 0);
        }
        else {
            for (final Entry<String, String> entry : configuration.getMenuTitleToPathMap().entrySet()) {
                registerScript(entry.getKey(), entry.getValue());
            }
        }
    }

    private void registerScript(final String scriptName, final String scriptPath) {
        final ScriptMetaData metaData = configuration.getMenuTitleToMetaDataMap().get(scriptName);
        for (final ExecutionMode executionMode : metaData.getExecutionModes()) {
            final String location = getLocation(scriptName, metaData, executionMode);
            addSubMenu(parentLocation(location), location, getMenuTitle(metaData, executionMode));
            addMenuItem(location, scriptName, scriptPath, executionMode, metaData);
        }
    }

    private String getLocation(String scriptName, ScriptMetaData metaData, ExecutionMode executionMode) {
        final String specialLocation = metaData.getMenuLocation(executionMode);
        return (specialLocation == null) ? scriptsLocation + "/" + scriptName : specialLocation;
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
        if (registeredLocations.add(location + "/" + metaData.getTitleKey(executionMode))) {
            final ExecuteScriptAction action = new ExecuteScriptAction(scriptName, getMenuItemTitle(metaData,
                executionMode), scriptPath, executionMode, metaData.cacheContent(), metaData.getPermissions());
            menuBuilder.addAction(location, action, MenuBuilder.AS_CHILD);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + parentLocation + ")";
    }
}
