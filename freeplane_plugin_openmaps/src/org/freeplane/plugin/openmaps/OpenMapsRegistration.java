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
		final OpenMapsNodeHook nodeHook = new OpenMapsNodeHook();
		if (modeController.getModeName() == "MindMap") {
			modeController.addAction(new InsertOpenMapsAction(nodeHook));
			modeController.addAction(new RemoveOpenMapsAction(nodeHook));
			modeController.addAction(new ViewOpenMapsAction(nodeHook));
		}
	}

}
