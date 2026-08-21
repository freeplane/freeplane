package org.freeplane.features.treetable;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class TreeTableController implements IExtension {

	public static TreeTableController getController() {
		return Controller.getCurrentModeController().getExtension(TreeTableController.class);
	}

	public static void install(final TreeTableController treeTableController) {
		Controller.getCurrentModeController().addExtension(TreeTableController.class, treeTableController);
	}

	public TreeTableController() {
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		new TreeTableBuilder().registerBy(mapController.getReadManager(), mapController.getWriteManager());
	}
}
