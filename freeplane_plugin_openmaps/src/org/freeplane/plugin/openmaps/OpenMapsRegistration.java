package org.freeplane.plugin.openmaps;

import org.freeplane.features.icon.IconClickedEvent;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconMouseListener;
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
			ViewOpenMapsAction viewOpenMapsAction = new ViewOpenMapsAction(nodeHook);
			modeController.addAction(viewOpenMapsAction);
			modeController.getExtension(IconController.class).addIconMouseListener(new IconMouseListener() {
				
				public void uiIconClicked(IconClickedEvent event) {
					if (event.getUIIcon().getName().equals(OpenMapsNodeHook.ICON_NAME))
						nodeHook.viewCurrentlySelectedLocation(event.getNode());
				}
			});
		}
	}

}
