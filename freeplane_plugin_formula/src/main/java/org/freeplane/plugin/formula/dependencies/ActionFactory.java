package org.freeplane.plugin.formula.dependencies;

import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.ModeController;

public class ActionFactory {
	public static void createActions(final ModeController modeController) {
		final LinkController linkController = modeController.getExtension(LinkController.class);
		modeController.addAction(new TracePrecedentsAction(linkController));
		modeController.addAction(new TraceDependentsAction(linkController));
		modeController.addAction(new ClearDependenciesAction());
	}
}
