package org.freeplane.plugin.formula.dependencies;

import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.ModeController;

public class ActionFactory {
	public static void createActions(ModeController modeController){
		LinkController linkController = modeController.getExtension(LinkController.class);
		modeController.addAction(new TracePrecedenceAction(linkController));
		modeController.addAction(new ClearDependenciesAction(linkController));
	}
}
