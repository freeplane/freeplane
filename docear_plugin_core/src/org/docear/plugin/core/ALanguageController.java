package org.docear.plugin.core;

import java.net.URL;
import java.util.Collection;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.mode.Controller;

public abstract class ALanguageController {
	private static final String DEFAULT_LANGUAGE = "en";
	
	public ALanguageController() {
		setLanguage();
		
		final OptionPanelController optionController = Controller.getCurrentController().getOptionPanelController();
		
		optionController.addPropertyLoadListener(new PropertyLoadListener() {			
			public void propertiesLoaded(Collection<IPropertyControl> properties) {
				setLanguage();
			}
		});
		
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if(propertyName.equalsIgnoreCase("language")){
					setLanguage();
				}
			}
		});
	}

	public void setLanguage() {
		ResourceBundles resBundle = ((ResourceBundles)Controller.getCurrentController().getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = DEFAULT_LANGUAGE;
		}
		
		URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		if (res == null) {
			lang = DEFAULT_LANGUAGE;
			res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		}
		
		if (res == null) {
			return;
		}
		
//		File f = new File(res.getPath());
//		if (!f.exists()) {
//			lang = DEFAULT_LANGUAGE;
//			res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
//		}
				
		resBundle.addResources(resBundle.getLanguageCode(), res);
	}
	
	
}
