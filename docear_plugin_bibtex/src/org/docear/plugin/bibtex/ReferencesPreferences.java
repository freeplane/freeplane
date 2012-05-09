package org.docear.plugin.bibtex;

import java.net.URL;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class ReferencesPreferences {
    ReferencesPreferences() {
    	addPropertiesToOptionPanel();
    }

    private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null) throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller.getCurrentModeController();
	
		modeController.getOptionPanelBuilder().load(preferences);
    }

}
