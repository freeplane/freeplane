package org.docear.plugin.core.listeners;

import java.util.Collection;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.core.resources.components.IPropertyControl;

public class PropertyLoadListener implements
		org.freeplane.core.resources.OptionPanelController.PropertyLoadListener {

	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OPEN_PREFERENCES);
	}

}
