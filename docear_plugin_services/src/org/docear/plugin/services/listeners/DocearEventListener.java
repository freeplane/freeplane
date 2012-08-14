package org.docear.plugin.services.listeners;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.services.communications.CommunicationsController;

public class DocearEventListener implements IDocearEventListener {

	public void handleEvent(DocearEvent event) {
		if (event.getType() == DocearEventType.APPLICATION_CLOSING) {
			ServiceController.getController().shutdown();
		}
		else if (event.getType() == DocearEventType.FINISH_THREADS) {
			ServiceController.getController().finishThreads();
		}
		else if (event.getType() == DocearEventType.SHOW_LICENSES) {
			DocearAllowUploadChooserAction.showDialog(true);
		}
		else if( CommunicationsController.CONNECTION_BAR_CLICKED.equals(event.getSource()) ) {
			DocearAllowUploadChooserAction.showDialog(false);
		}
	}


}
