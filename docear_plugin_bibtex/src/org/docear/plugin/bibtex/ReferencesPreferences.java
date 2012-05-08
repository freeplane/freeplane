package org.docear.plugin.bibtex;

import java.net.URL;

import org.docear.plugin.bibtex.listeners.PropertiesActionListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class ReferencesPreferences {
    ReferencesPreferences() {
	Controller.getCurrentController().getOptionPanelController().addButtonListener(new PropertiesActionListener());
	addPropertiesToOptionPanel();
    }

    private void addPropertiesToOptionPanel() {
	final URL preferences = this.getClass().getResource("preferences.xml");
	if (preferences == null) throw new RuntimeException("cannot open preferences");
	MModeController modeController = (MModeController) Controller.getCurrentModeController();

	modeController.getOptionPanelBuilder().load(preferences);
    }

}
