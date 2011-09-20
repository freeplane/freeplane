package org.freeplane.main.application;

import java.net.URL;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class AddOnInstaller {
	
	/**
	 * Installs an add-on from the given location. Installation failures due to various reasons are communicated
	 * to the caller via a {@link AddOnInstallationException}s.
	 */
	public void install(final URL url) throws AddOnInstallationException {
		MFileManager.getController(Controller.getCurrentModeController());
	}
}
