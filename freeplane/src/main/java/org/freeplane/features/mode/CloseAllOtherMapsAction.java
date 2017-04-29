package org.freeplane.features.mode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;

@SuppressWarnings("serial")
class CloseAllOtherMapsAction extends AFreeplaneAction{

	public CloseAllOtherMapsAction() {
		super("CloseAllOtherMapsAction");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final MapModel mapToKeepOpen = controller.getMap();
		controller.closeAllMaps(mapToKeepOpen);
	}

}
