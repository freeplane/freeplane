package org.docear.plugin.services;

import java.net.URL;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class ServicePreferences {
	
	ServicePreferences() {
		LogUtils.info("starting DocearBackupPreferences()");
		addPropertiesToOptionPanel();
	}
		
	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		MModeController modeController = (MModeController) Controller.getCurrentModeController();
		
		modeController.getOptionPanelBuilder().load(preferences);
	}
	
}
