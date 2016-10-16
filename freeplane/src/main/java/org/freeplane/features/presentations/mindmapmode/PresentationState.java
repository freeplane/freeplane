package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.SLIDE_CHANGED;

import java.util.ArrayList;

import org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType;

public class PresentationState {
	private Presentation runningPresentation;
	private final ArrayList<PresentationStateChangeListener> presentationStateChangeListeners;
	private Slide currentSlide;
	
	public Slide getCurrentSlide() {
		return currentSlide;
	}

	public PresentationState() {
		super();
		this.runningPresentation = null;
		this.presentationStateChangeListeners = new ArrayList<>();
	}
	
	public Presentation getRunningPresentation() {
		return runningPresentation;
	}
	
	public void changeSlide(Presentation newPresentation) {
		final Slide newSlide;
		if(newPresentation != null) {
			newSlide = newPresentation.slides.getCurrentElement();
		} else
			newSlide = null;
		if(this.currentSlide != newSlide) {
			this.runningPresentation = newPresentation;
			currentSlide = newSlide;
			firePresentationStateChangedEvent(SLIDE_CHANGED);
		}
	}
	
	public void addPresentationStateListener(PresentationStateChangeListener presentationStateChangeListener) {
		this.presentationStateChangeListeners.add(presentationStateChangeListener);
	}

	public void removePresentationStateListener(PresentationStateChangeListener presentationStateChangeListener) {
		this.presentationStateChangeListeners.remove(presentationStateChangeListener);
	}

	private void firePresentationStateChangedEvent(EventType eventType) {
		for (PresentationStateChangeListener presentationStateChangeListener : presentationStateChangeListeners)
			presentationStateChangeListener.onPresentationStateChange(eventType.of(this));
	}

}
