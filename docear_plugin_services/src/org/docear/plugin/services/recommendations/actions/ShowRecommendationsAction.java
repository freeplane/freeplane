package org.docear.plugin.services.recommendations.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

@EnabledAction(checkOnPopup = true)
public class ShowRecommendationsAction extends AFreeplaneAction {
	public final static String TYPE = "ShowRecommendationsAction";

	private static final long serialVersionUID = 1L;

	public ShowRecommendationsAction() {
		super(TYPE);
	}

	public void setEnabled() {
		if (ServiceController.getController().isRecommendationsAllowed() && CommunicationsController.getController().getUserName() != null ) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		ModeController modeController = Controller.getCurrentController().getModeController(DocearRecommendationsModeController.MODENAME);
		Controller.getCurrentController().selectMode(modeController);

	}

}
