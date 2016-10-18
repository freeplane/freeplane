package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class PresentationController implements IExtension{
	private final PresentationState presentationState;
	private final PresentationEditorController presentationEditorController;
	
	public static void install(final ModeController modeController) {
		final PresentationController presentationController = new PresentationController();
		presentationController.registerActions(modeController);
		presentationController.addMapSelectionListener(modeController);
		new PresentationBuilder().register(modeController.getMapController());
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add("Presentations", presentationController.createPanel());

	}

	private PresentationController() {
		presentationState = new PresentationState();
		presentationEditorController = new PresentationEditorController(presentationState);
	}

	private void registerActions(ModeController modeController) {
		modeController.addAction(new ShowCurrentSlideAction(presentationState));
		modeController.addAction(new ShowPreviousSlideAction(presentationState));
		modeController.addAction(new ShowNextSlideAction(presentationState));
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

@SelectableAction
class ShowCurrentSlideAction extends AFreeplaneAction {
	private PresentationState presentationState;

	public ShowCurrentSlideAction(PresentationState presentationState) {
		super("ShowCurrentSlideAction");
		this.presentationState = presentationState;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (presentationState.isPresentationRunning())
			presentationState.stopPresentation();
		else if (presentationState.canShowCurrentSlide())
			presentationState.showSlide();
		setSelected(presentationState.isPresentationRunning());
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}
}

class ShowNextSlideAction extends AFreeplaneAction {
	private final PresentationState presentationState;

	public ShowNextSlideAction(PresentationState presentationState) {
		super("ShowNextSlideAction");
		this.presentationState = presentationState;
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				setEnabled(ShowNextSlideAction.this.presentationState.canShowNextSlide());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (presentationState.canShowNextSlide())
			presentationState.showNextSlide();
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}

}

class ShowPreviousSlideAction extends AFreeplaneAction {
	private PresentationState presentationState;

	public ShowPreviousSlideAction(PresentationState presentationState) {
		super("ShowPreviousSlideAction");
		this.presentationState = presentationState;
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				setEnabled(ShowPreviousSlideAction.this.presentationState.canShowPreviousSlide());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (presentationState.canShowPreviousSlide())
			presentationState.showPreviousSlide();
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}
}
