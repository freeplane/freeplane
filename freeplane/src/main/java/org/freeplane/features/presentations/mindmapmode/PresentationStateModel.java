package org.freeplane.features.presentations.mindmapmode;

import java.util.ArrayList;

public class PresentationStateModel {
	private PresentationModel runningPresentation;
	private final ArrayList<PresentationStateChangeListener> presentationStateChangeListeners;
	
	public PresentationStateModel() {
		super();
		this.runningPresentation = null;
		this.presentationStateChangeListeners = new ArrayList<>();
	}
	
	public PresentationModel getRunningPresentation() {
		return runningPresentation;
	}
	public void setRunningPresentation(PresentationModel runningPresentation) {
		if(this.runningPresentation != runningPresentation) {
			this.runningPresentation = runningPresentation;
			firePresentationStateChangeEvent();
		}
	}
	
	public void addPresentationStateListener(PresentationStateChangeListener presentationStateChangeListener) {
		this.presentationStateChangeListeners.add(presentationStateChangeListener);
	}

	public void removePresentationStateListener(PresentationStateChangeListener presentationStateChangeListener) {
		this.presentationStateChangeListeners.remove(presentationStateChangeListener);
	}

	private void firePresentationStateChangeEvent() {
		for (PresentationStateChangeListener presentationStateChangeListener : presentationStateChangeListeners)
			presentationStateChangeListener.onPresentationStateChange(PresentationStateChangeEvent.of(this));
	}

}
