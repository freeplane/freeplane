package org.freeplane.features.styles;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class StyleExchange {
    private final MapModel sourceMap;
    private final MapModel targetMap;

    StyleExchange(MapModel sourceMap , final MapModel targetMap){
        this.sourceMap = sourceMap;
        this.targetMap = targetMap;
        
    }
	
    void replaceMapStylesAndAutomaticStyle() {
	    final ModeController modeController = Controller.getCurrentModeController();
		final IExtension oldStyleModel = targetMap.getRootNode().removeExtension(MapStyleModel.class);
		modeController.getExtension(MapStyle.class).onCreate(sourceMap);
		moveStyle(true);
		modeController.getExtension(AutomaticLayoutController.class).moveExtension(modeController, sourceMap, targetMap);
		modeController.getExtension(AutomaticEdgeColorHook.class).moveExtension(modeController, sourceMap, targetMap);
		LogicalStyleController.getController().refreshMap(targetMap);
		makeUndoable(oldStyleModel);
	}

    private void makeUndoable(final IExtension oldStyleModel) {
        final IExtension newStyleModel = targetMap.getRootNode().getExtension(MapStyleModel.class);
		IActor actor = new IActor() {
			@Override
			public void undo() {
				targetMap.getRootNode().putExtension(oldStyleModel);
			}

			@Override
			public String getDescription() {
				return "moveStyle";
			}

			@Override
			public void act() {
				targetMap.getRootNode().putExtension(newStyleModel);
			}
		};
		Controller.getCurrentModeController().execute(actor, targetMap);
    }
	
	   void moveStyle(boolean overwrite) {
	        final MapStyleModel source = sourceMap.getRootNode().removeExtension(MapStyleModel.class);
	        if(source == null)
	            return;
	        final IExtension undoHandler = targetMap.getExtension(IUndoHandler.class);
	        source.getStyleMap().putExtension(IUndoHandler.class, undoHandler);
	        final NodeModel targetRoot = targetMap.getRootNode();
	        final MapStyleModel target = MapStyleModel.getExtension(targetRoot);
	        if(target == null){
	            targetRoot.addExtension(source);
	        }
	        else{
	            target.setStylesFrom(source, overwrite);
	        }
	    }


}