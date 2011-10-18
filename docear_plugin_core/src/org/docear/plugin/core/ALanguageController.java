package org.docear.plugin.core;

import java.io.File;
import java.net.URL;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.features.mode.Controller;

public abstract class ALanguageController {
	private static final String DEFAULT_LANGUAGE = "en";
	
	public ALanguageController() {
		ResourceBundles resBundle = ((ResourceBundles)Controller.getCurrentModeController().getController().getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = DEFAULT_LANGUAGE;
		}
		
		URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		if (res == null) {
			lang = DEFAULT_LANGUAGE;
			res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		}
		
		System.out.println("DEBUG res: "+res);
		if (res == null) {
			return;
		}
		File f = new File(res.getPath());
		if (!f.exists()) {
			lang = DEFAULT_LANGUAGE;
			res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		}
				
		resBundle.addResources(resBundle.getLanguageCode(), res);
	}
}
