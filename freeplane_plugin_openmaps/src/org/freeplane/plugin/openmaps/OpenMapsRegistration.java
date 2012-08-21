package org.freeplane.plugin.openmaps;

import org.freeplane.features.mode.ModeController;

/**
 * @author Blair Archibald 
 */
public class OpenMapsRegistration {
	
	public OpenMapsRegistration(ModeController modeController) { 
		if (modeController.getModeName() == "MindMap") {
			//Possibly employ a factory method here.
			modeController.addAction(new InsertOpenMapsAction());
		}
	}

}
