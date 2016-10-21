package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.SLIDE_CHANGED;

import java.util.ArrayList;

import org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType;

public class PresentationState {
	private Presentation currentPresentation;
	private final ArrayList<PresentationStateChangeListener> presentationStateChangeListeners;
	private Slide currentSlide;
	
	public Slide getCurrentSlide() {
		return currentSlide;
	}

	public PresentationState() {
		super();
		this.currentPresentation = null;
		this.presentationStateChangeListeners = new ArrayList<>();
	}
	
	public void changePresentation(Presentation newPresentation) {
		stopPresentation();
		if(currentPresentation != newPresentation){
			currentPresentation = newPresentation;
			firePresentationStateChangedEvent(SLIDE_CHANGED);
		}
	}

	public void showSlide() {
		Slide newSlide = currentPresentation.slides.getCurrentElement();
		if (currentSlide != newSlide) {
			currentSlide = newSlide;
			firePresentationStateChangedEvent(SLIDE_CHANGED);
		}
		if (currentSlide != null)
			currentSlide.apply();
	}

	public void stopPresentation() {
		if (currentSlide != null) {
			currentSlide = null;
			firePresentationStateChangedEvent(SLIDE_CHANGED);
			Slide.ALL_NODES.apply();
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

	public boolean isPresentationRunning() {
		return currentSlide != null;
	}

	public void showNextSlide() {
		NamedElementCollection<Slide> slides = currentPresentation.slides;
		final int currentElementIndex = slides.getCurrentElementIndex();
		slides.selectCurrentElement(currentElementIndex + 1);
		showSlide();
	}

	public void showPreviousSlide() {
		NamedElementCollection<Slide> slides = currentPresentation.slides;
		final int currentElementIndex = slides.getCurrentElementIndex();
		slides.selectCurrentElement(currentElementIndex - 1);
		showSlide();
	}

	public boolean canShowNextSlide() {
		if (currentPresentation == null)
			return false;
		NamedElementCollection<Slide> slides = currentPresentation.slides;
		return slides.getSize() != 0 && slides.getCurrentElementIndex() < slides.getSize() - 1;
	}

	public boolean canShowCurrentSlide() {
		if (currentPresentation == null)
			return false;
		NamedElementCollection<Slide> slides = currentPresentation.slides;
		return slides.getCurrentElement() != null;
	}

	public boolean canShowPreviousSlide() {
		if (currentPresentation == null)
			return false;
		NamedElementCollection<Slide> slides = currentPresentation.slides;
		return slides.getCurrentElementIndex() > 0;
	}

	void changeSlide() {
		firePresentationStateChangedEvent(SLIDE_CHANGED);
	}
}
