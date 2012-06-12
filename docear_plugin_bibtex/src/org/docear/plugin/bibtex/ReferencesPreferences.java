package org.docear.plugin.bibtex;

import java.net.URL;

import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class ReferencesPreferences {
    ReferencesPreferences(ModeController modeController) {
    	addPropertiesToOptionPanel(modeController);
    }

    private void addPropertiesToOptionPanel(ModeController modeController) {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null) throw new RuntimeException("cannot open preferences");
		if(modeController instanceof MModeController) {
			MModeController mController = (MModeController) modeController; 
		
			mController.getOptionPanelBuilder().load(preferences);
		}
    }

}
