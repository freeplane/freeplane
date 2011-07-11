package org.docear.plugin.backup;

import java.net.URL;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.features.mode.Controller;

public class BackupConfiguration {
	
	public BackupConfiguration() {
		ResourceBundles resBundle = ((ResourceBundles)Controller.getCurrentModeController().getController().getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}
		
		final URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		resBundle.addResources(resBundle.getLanguageCode(), res);
		
	}

	
}
