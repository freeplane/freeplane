package org.freeplane.features.fpsearch;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanel;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.features.mode.mindmapmode.MModeController;

class ShowPreferenceItemAction extends AbstractAction {
    private PreferencesItem preferencesItem;

    ShowPreferenceItemAction(PreferencesItem preferencesItem) {
        super("Show");
        this.preferencesItem = preferencesItem;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        System.out.format("Showing preferences item: %s\n", preferencesItem);
        showPrefsDialog();
    }

    private void showPrefsDialog()
    {
        OptionPanelBuilder optionPanelBuilder = new OptionPanelBuilder();
        final ResourceController resourceController = ResourceController.getResourceController();
        URL preferences = resourceController.getResource("/xml/preferences.xml");
        optionPanelBuilder.load(preferences);
        ShowPreferencesAction showPreferencesAction = MModeController.createShowPreferencesAction(optionPanelBuilder, this.preferencesItem);
        int uniqueId = new Long(System.currentTimeMillis()).intValue();
        showPreferencesAction.actionPerformed(
                new ActionEvent(this, uniqueId, OptionPanel.OPTION_PANEL_RESOURCE_PREFIX + preferencesItem.tab));
    }

}
