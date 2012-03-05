package org.docear.plugin.core.listeners;

import java.util.Collection;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.core.resources.components.IPropertyControl;

public class PropertyLoadListener implements
		org.freeplane.core.resources.OptionPanelController.PropertyLoadListener {

	@Override
	public void propertiesLoaded(Collection<IPropertyControl> properties) {
		DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.OPEN_PREFERENCES);
	}

}
