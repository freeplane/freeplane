package org.docear.plugin.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;

import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class ServicePreferences {	

	ServicePreferences(ModeController modeController) {		
		addPropertiesToOptionPanel(modeController);
	}
		
	private void addPropertiesToOptionPanel(ModeController modeController) {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open preferences");
		if(modeController instanceof MModeController) {
			((MModeController) modeController).getOptionPanelBuilder().load(preferences);
		}
		
		final OptionPanelController optionController = Controller.getCurrentController().getOptionPanelController();
				
		optionController.addPropertyLoadListener(new PropertyLoadListener() {
			public void propertiesLoaded(Collection<IPropertyControl> properties) {
				for(IPropertyControl property : properties){
					if(property != null && property.getName() != null && property.getName().equalsIgnoreCase(CommunicationsController.DOCEAR_USE_PROXY)){
						((IPropertyControl) optionController.getPropertyControl(CommunicationsController.DOCEAR_PROXY_HOST)).setEnabled(((BooleanProperty)property).getBooleanValue());			
						((IPropertyControl) optionController.getPropertyControl(CommunicationsController.DOCEAR_PROXY_PORT)).setEnabled(((BooleanProperty)property).getBooleanValue());						
					}
				}
				((BooleanProperty) optionController.getPropertyControl(CommunicationsController.DOCEAR_USE_PROXY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								((IPropertyControl) optionController.getPropertyControl(CommunicationsController.DOCEAR_PROXY_HOST)).setEnabled(Boolean.parseBoolean(evt.getNewValue().toString()));			
								((IPropertyControl) optionController.getPropertyControl(CommunicationsController.DOCEAR_PROXY_PORT)).setEnabled(Boolean.parseBoolean(evt.getNewValue().toString()));								
							}
						});				
			}
		});
	
	}
	
}
