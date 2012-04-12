package org.docear.plugin.services.listeners;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;

public class DocearEventListener implements IDocearEventListener {

	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LICENSES_ACCEPTED) {
			if (DocearController.getController().isLicenseDialogNecessary()) 
			{
				DocearAllowUploadChooserAction.showDialog(false);
			}
		}
	}


}
