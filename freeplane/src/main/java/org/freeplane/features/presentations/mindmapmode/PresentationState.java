package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.*;

import java.util.ArrayList;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType;

public class PresentationState {
	private Presentation currentPresentation;
	private final ArrayList<PresentationStateChangeListener> presentationStateChangeListeners;
	private Slide currentSlide;
	private boolean highlightsNodes;
	
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

	public void showPresentation() {
		final boolean isPresentationAlreadyRunning = isPresentationRunning();
		Slide newSlide = currentPresentation.slides.getCurrentElement();
		if (currentSlide != newSlide) {
			currentSlide = newSlide;
			firePresentationStateChangedEvent(isPresentationAlreadyRunning ? SLIDE_CHANGED : PLAYING_STATE_CHANGED);
		}		
		if (currentSlide != null)
			currentSlide.apply();
	}

	public void stopPresentation() {
		if (currentSlide != null) {
			currentSlide = null;
			firePresentationStateChangedEvent(PLAYING_STATE_CHANGED);
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
		if(canShowNextSlide()) {
			NamedElementCollection<Slide> slides = currentPresentation.slides;
			final int currentElementIndex = slides.getCurrentElementIndex();
			slides.selectCurrentElement(currentElementIndex + 1);
		}
	}

	public void showPreviousSlide() {
		if(canShowPreviousSlide()) {
			NamedElementCollection<Slide> slides = currentPresentation.slides;
			final int currentElementIndex = slides.getCurrentElementIndex();
			slides.selectCurrentElement(currentElementIndex - 1);
		}
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
		if(isPresentationRunning())
			showPresentation();
		else
			firePresentationStateChangedEvent(SLIDE_CHANGED);
	}

	public boolean shouldHighlightNodeContainedOnSlide(NodeModel node) {
		return  ! isPresentationRunning() && highlightsNodes && canShowCurrentSlide()  && currentPresentation.slides.getCurrentElement().isNodeVisible(node);
	}
	
	public boolean shouldHighlightNodeFoldedOnSlide(NodeModel node) {
		return ! isPresentationRunning() && highlightsNodes && canShowCurrentSlide()  && currentPresentation.slides.getCurrentElement().isNodeFolded(node);
	}

	public boolean highlightsNodes() {
		return highlightsNodes;
	}

	public void setHighlightsNodes(boolean highlightsNodes) {
		if(this.highlightsNodes != highlightsNodes) {
			this.highlightsNodes = highlightsNodes;
			firePresentationStateChangedEvent(SLIDE_CHANGED);
		}
	}

}
