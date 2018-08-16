package org.freeplane.features.explorer.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.explorer.CopySuggestedReferenceAction;
import org.freeplane.features.explorer.GlobalNodes;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.explorer.NodeAlias;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;

public class MMapExplorerController extends MapExplorerController{


	public static void install(ModeController modeController, TextController textController) {
		final MMapExplorerController explorer = new MMapExplorerController(modeController, textController);
		modeController.addExtension(MapExplorerController.class, explorer);
		modeController.addAction(new GloballyAccessibleNodeAction(explorer));
		modeController.addAction(new AssignAliasAction(explorer));
		modeController.addAction(new CopySuggestedReferenceAction(explorer));
		MapExplorerController.install(modeController);
	}


	private MMapExplorerController(ModeController modeController, TextController textController) {
		super(modeController);
	}

	public void makeGlobal(final NodeModel node, final boolean isGlobal) {
		if(isGlobal == isGlobal(node))
			return;
		final IActor actor = new IActor() {
			@Override
			public void act() {
				set(node, isGlobal);
			}

			@Override
			public void undo() {
				set(node, !isGlobal);
			}

			private void set(final NodeModel node, final boolean newValue) {
				final MapModel map = node.getMap();
				GlobalNodes.writeableOf(map).makeGlobal(node, newValue);
				final MapController mapController = modeController.getMapController();
				mapController.nodeChanged(node);
				fireGlobalNodeChangedEvent(map, mapController);
			}

			@Override
			public String getDescription() {
				return "setGlobal";
			}

		};
		modeController.execute(actor, node.getMap());
	}

	private void fireGlobalNodeChangedEvent(final MapModel map, final MapController mapController) {
		mapController.fireMapChanged(new MapChangeEvent(this, map, GLOBAL_NODES, null, null, false));
	}
	public void setAlias(final NodeModel node, final String alias) {
		final String oldAlias = NodeAlias.getAlias(node);
		if(oldAlias == alias || oldAlias != null && oldAlias.equals(alias))
			return;
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeAlias.setAlias(node, alias);
				final MapController mapController = modeController.getMapController();
				mapController.nodeChanged(node);
				if(isGlobal(node))
					fireGlobalNodeChangedEvent(node.getMap(), mapController);
			}

			@Override
			public void undo() {
				NodeAlias.setAlias(node, oldAlias);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setAlias";
			}

		};
		modeController.execute(actor, node.getMap());
	}
}
