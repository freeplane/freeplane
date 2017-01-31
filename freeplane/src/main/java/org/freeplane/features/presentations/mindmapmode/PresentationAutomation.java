package org.freeplane.features.presentations.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;

class PresentationAutomation implements PresentationStateChangeListener, IFreeplanePropertyListener{

	static final String PROCESS_UP_DOWN_KEYS_PROPERTY = "presentation.processesUpDownKeys";
	static final String SWITCH_TO_FULL_SCREEN_PROPERTY = "presentation.switchToFullScreen";
	static final String SWITCH_TO_PRESENTATION_MODE_PROPERTY = "presentation.switchToPresentationMode";
	private boolean processUpDownKeys;
	private boolean isPresentationRunning;
	private final UpDownKeyEventDispatcher dispatcher;
	

	PresentationAutomation(PresentationState state, UpDownKeyEventDispatcher dispatcher, //
			boolean processUpDownKeys) {
		super();
		this.processUpDownKeys = processUpDownKeys;
		this.isPresentationRunning = false;
		this.dispatcher = dispatcher;
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
				mapViewComponent.putClientProperty("presentation_mode", isPresentationRunning);
			}
		}
	}

	private JComponent getMapViewComponent() {
		final JComponent mapViewComponent = (JComponent) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		return mapViewComponent;
	}

	private void updateDispatcherState() {
		if (processUpDownKeys) {
			if(isPresentationRunning)
				dispatcher.activate();
			else
				dispatcher.deactivate();
		}
	}
	
	@Override
	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if(PROCESS_UP_DOWN_KEYS_PROPERTY.equals(propertyName) && isPresentationRunning){
			if(Boolean.parseBoolean(newValue))
				dispatcher.activate();
			else
				dispatcher.deactivate();
		}
	}

}
