package org.docear.plugin.services.recommendations.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnPopup = true)
public class ShowRecommendationsAction extends AFreeplaneAction {
	public final static String TYPE = "ShowRecommendationsAction";
	
	private static final long serialVersionUID = 1L;

	public ShowRecommendationsAction() {
		super(TYPE);
	}
	
	public void setEnabled() {
		if(CommunicationsController.getController().getUserName() == null || !ServiceController.getController().isRecommendationsAllowed()) {
			setEnabled(false);
		}
		else {
			setEnabled(true);
		}		
	}

	public void actionPerformed(ActionEvent e) {
		Controller.getCurrentController().selectMode(DocearRecommendationsModeController.MODENAME);
	}

}
