package org.freeplane.plugin.openmaps;

import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.openmaps.actions.InsertOpenMapsAction;
import org.freeplane.plugin.openmaps.actions.RemoveOpenMapsAction;
import org.freeplane.plugin.openmaps.actions.ViewOpenMapsAction;

/**
 * @author Blair Archibald 
 */
public class OpenMapsRegistration {
	
	public OpenMapsRegistration(ModeController modeController) { 
		if (modeController.getModeName() == "MindMap") {
			modeController.addAction(new InsertOpenMapsAction());
			modeController.addAction(new RemoveOpenMapsAction());
			modeController.addAction(new ViewOpenMapsAction());
		}
	}

}
