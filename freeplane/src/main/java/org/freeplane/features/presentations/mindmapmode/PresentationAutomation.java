package org.freeplane.features.presentations.mindmapmode;

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;
import org.freeplane.view.swing.map.MapView;

class PresentationAutomation implements PresentationStateChangeListener{

	static final String SWITCH_TO_FULL_SCREEN_PROPERTY = "presentation.switchToFullScreen";
	static final String SWITCH_TO_PRESENTATION_MODE_PROPERTY = "presentation.switchToPresentationMode";
	private boolean isPresentationRunning;
	private final PresentationKeyEventDispatcher[] dispatchers;
	

	PresentationAutomation(PresentationState state, PresentationKeyEventDispatcher... dispatchers) {
		super();
		this.isPresentationRunning = false;
		this.dispatchers = dispatchers;
	}

	@Override
	public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
		if (presentationStateChangeEvent.eventType == PresentationStateChangeEvent.EventType.PLAYING_STATE_CHANGED) {
			isPresentationRunning = presentationStateChangeEvent.presentationState.isPresentationRunning();
			updateDispatcherState();
			updateFullScreenMode();
			updatePresentationMode();
		}
	}

	private void updateFullScreenMode() {
		if(ResourceController.getResourceController().getBooleanProperty(SWITCH_TO_FULL_SCREEN_PROPERTY)){
			final FrameController viewController = (FrameController) Controller.getCurrentController().getViewController();
			viewController.setFullScreen(isPresentationRunning);

		}
	}
	
	private void updatePresentationMode() {
		if(ResourceController.getResourceController().getBooleanProperty(SWITCH_TO_PRESENTATION_MODE_PROPERTY)){
			final JComponent mapViewComponent = getMapViewComponent();
			if(mapViewComponent != null) {
				mapViewComponent.putClientProperty(MapView.PRESENTATION_MODE_ENABLED, isPresentationRunning);
			}
		}
	}

	private JComponent getMapViewComponent() {
		final JComponent mapViewComponent = (JComponent) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		return mapViewComponent;
	}

	private void updateDispatcherState() {
		for(PresentationKeyEventDispatcher dispatcher : dispatchers) {
			if(isPresentationRunning)
				dispatcher.activate();
			else
				dispatcher.deactivate();
		}
	}
	
}
