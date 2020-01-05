package org.freeplane.features.filter.hidden;

import org.freeplane.core.enumeration.NodeEnumerationAttributeHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class HiddenNodeContoller {

	public static void registerModeSpecificActions(MModeController controller) {
		controller.addAction(new HideNodeAction());
		controller.addAction(new ShowHiddenNodesAction());
	}

	public static void install(ModeController controller) {
		final ReadManager readManager = controller.getMapController().getReadManager();
		final WriteManager writeManager = controller.getMapController().getWriteManager();
		new NodeEnumerationAttributeHandler<NodeVisibility>(NodeVisibility.class).registerBy(readManager, writeManager);
		new NodeEnumerationAttributeHandler<NodeVisibilityConfiguration>(NodeVisibilityConfiguration.class).registerBy(readManager, writeManager);
		registerStateIconProvider(controller);
	}

	private static UIIcon hiddenNodeIcon;

	private static void registerStateIconProvider(ModeController modeController) {
		modeController.getExtension(IconController.class).addStateIconProvider(new IStateIconProvider() {
			@Override
			public UIIcon getStateIcon(NodeModel node) {
				if (node.getExtension(NodeVisibility.class) != NodeVisibility.HIDDEN) {
					return null;
				}
				if (hiddenNodeIcon == null) {
					hiddenNodeIcon = IconStoreFactory.ICON_STORE.getUIIcon("hidden.svg");
				}
				return hiddenNodeIcon;
			}

			@Override
			public boolean mustIncludeInIconRegistry() {
				return true;
			}
		});
	}
}