package org.freeplane.features.presentations;

import java.awt.Component;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.presentations.mindmapmode.PresentationEditorController;

public class PresentationController implements IExtension{
	private final PresentationStateModel presentationStateModel;
	private final PresentationEditorController presentationEditorController;
	
	public static void install(final ModeController modeController) {
		final PresentationController presentationController = new PresentationController(modeController);
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add("Presentations", presentationController.createPanel());

	}
	private PresentationController(ModeController modeController) {
		presentationStateModel = new PresentationStateModel();
		presentationEditorController = new PresentationEditorController(presentationStateModel);
		addMapSelectionListener(modeController);
	}
	
	private void addMapSelectionListener(final ModeController modeController) {
		IMapSelectionListener mapSelectionListener = new IMapSelectionListener() {
			
			@Override
			public void beforeMapChange(MapModel oldMap, MapModel newMap) {
			}
			
			@Override
			public void afterMapChange(MapModel oldMap, MapModel newMap) {
				if(newMap != null && Controller.getCurrentModeController() == modeController)
					presentationEditorController.setPresentations(MapPresentations.getPresentations(newMap).presentations);
				else
					presentationEditorController.setPresentations(null);
			}
		};
		modeController.getController().getMapViewManager().addMapSelectionListener(mapSelectionListener);
	}
	public Component createPanel() {
		return new JAutoScrollBarPane(presentationEditorController.createPanel());
	}
	

}
