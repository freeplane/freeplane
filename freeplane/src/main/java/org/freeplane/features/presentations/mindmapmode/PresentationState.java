package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.PLAYING_STATE_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.SLIDE_CHANGED;

import java.util.ArrayList;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType;

public class PresentationState {
	private NamedElementCollection<Presentation> presentations;
	private Presentation currentPresentation;
	private final ArrayList<PresentationStateChangeListener> presentationStateChangeListeners;
	private Slide currentSlide;
	private boolean highlightsNodes;
	private boolean combinesAllPresentations;
	private float zoomFactor;
	
	protected void setCombinesAllPresentations(boolean combinesAllPresentations) {
		if(this.combinesAllPresentations != combinesAllPresentations){
			this.combinesAllPresentations = combinesAllPresentations;
			if(presentations != null)
				firePresentationStateChangedEvent(EventType.COLLECTION_SIZE_CHANGED);
		}
	}

	public PresentationState() {
		super();
		this.combinesAllPresentations = false;
		this.currentPresentation = null;
		this.presentationStateChangeListeners = new ArrayList<>();
		this.zoomFactor = 1f;
	}
	
	public void changePresentation(CollectionChangedEvent<Presentation> event) {
		presentations = event.collection;
		Presentation presentation = presentations != null ? presentations.getCurrentElement() : null;
		if(currentPresentation != presentation){
			currentPresentation = presentation;
			if(presentation == null)
				stopPresentation();
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
		if (currentSlide != null) {
			if(! isPresentationAlreadyRunning){
				zoomFactor = Controller.getCurrentController().getMapViewManager().getZoom();
			}
			currentSlide.apply(getPresentationZoomFactor());
		}
	}

	private float getPresentationZoomFactor() {
		return usesMapZoom() ? zoomFactor : 1f;
	}

	private boolean usesMapZoom() {
		return ResourceController.getResourceController().getBooleanProperty("presentation.zoom");
	}

	public void stopPresentation() {
		if (currentSlide != null) {
			currentSlide = null;
			firePresentationStateChangedEvent(PLAYING_STATE_CHANGED);
			Slide.ALL_NODES.apply(1f);
			if(usesMapZoom())
				Controller.getCurrentController().getMapViewManager().setZoom(zoomFactor);
			zoomFactor = 1f;
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
		if(currentPresentationHasNextSlide()) {
			NamedElementCollection<Slide> slides = currentPresentation.slides;
			final int currentElementIndex = slides.getCurrentElementIndex();
			slides.selectCurrentElement(currentElementIndex + 1);
		} else if (combinesAllPresentations()) {
			final int followingNotEmptyPresentationIndex = findFollowingNotEmptyPresentationIndex();
			if (followingNotEmptyPresentationIndex != -1) {
				presentations.selectCurrentElement(followingNotEmptyPresentationIndex);
				showFirstSlide();
			}
		}
	}

	public void showLastSlide() {
		if(canShowNextSlide()) {
			NamedElementCollection<Slide> slides = currentPresentation.slides;
			slides.selectCurrentElement(slides.getSize() - 1);
		}
	}

	public void showPreviousSlide() {
		if(currentPresentationHasPreviousSlide()) {
			NamedElementCollection<Slide> slides = currentPresentation.slides;
			final int currentElementIndex = slides.getCurrentElementIndex();
			slides.selectCurrentElement(currentElementIndex - 1);
		} else if (combinesAllPresentations()) {
			final int previousNotEmptyPresentationIndex = findPreviousNotEmptyPresentationIndex();
			if (previousNotEmptyPresentationIndex != -1) {
				presentations.selectCurrentElement(previousNotEmptyPresentationIndex);
				showLastSlide();
			}
		}
	}

	public void showFirstSlide() {
		if(canShowPreviousSlide()) {
			NamedElementCollection<Slide> slides = currentPresentation.slides;
			slides.selectCurrentElement(0);
		}
	}

	public boolean currentPresentationHasNextSlide() {
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

	public boolean currentPresentationHasPreviousSlide() {
		if (currentPresentation == null)
			return false;
		NamedElementCollection<Slide> slides = currentPresentation.slides;
		return slides.getCurrentElementIndex() > 0;
	}


	public boolean canShowPreviousSlide() {
		return currentPresentationHasPreviousSlide() || combinesAllPresentations() && anyPreviousPresentationIsNotEmpty();
	}

	private boolean anyPreviousPresentationIsNotEmpty() {
		return findPreviousNotEmptyPresentationIndex() != -1;
	}

	private boolean anyFollowingPresentationIsNotEmpty() {
		return findFollowingNotEmptyPresentationIndex() != -1;
	}

	private int findPreviousNotEmptyPresentationIndex() {
		if(currentPresentation == null)
			return -1;
		final int currentPresentationIndex = presentations.getCurrentElementIndex();
		for (int i = currentPresentationIndex - 1; i >= 0 ; i--) {
			if(presentations.getElement(i).slides.getSize() > 0)
				return i;
		}
		return -1;
	}

	private int findFollowingNotEmptyPresentationIndex() {
		if(currentPresentation == null)
			return -1;
		final int currentPresentationIndex = presentations.getCurrentElementIndex();
		for (int i = currentPresentationIndex + 1; i < presentations.getSize(); i++) {
			if(presentations.getElement(i).slides.getSize() > 0)
				return i;
		}
		return -1;
	}
	private boolean combinesAllPresentations() {
		return combinesAllPresentations;
	}

	public boolean canShowFirstSlide() {
		return currentPresentationHasPreviousSlide();
	}

	public boolean canShowNextSlide() {
		return currentPresentationHasNextSlide() || combinesAllPresentations() && anyFollowingPresentationIsNotEmpty();
	}

	public boolean canShowLastSlide() {
		return currentPresentationHasNextSlide();
	}

	void changeSlide() {
		if(isPresentationRunning())
			showPresentation();
		else
			firePresentationStateChangedEvent(SLIDE_CHANGED);
	}

	public boolean shouldHighlightNodeContainedOnSlide(NodeModel node) {
		return  ! isPresentationRunning() && highlightsNodes && canShowCurrentSlide() && currentPresentation.slides.getCurrentElement().isNodeVisible(node);
	}
	
	public boolean shouldHighlightNodeFoldedOnSlide(NodeModel node) {
		return ! isPresentationRunning() && highlightsNodes && canShowCurrentSlide() && currentPresentation.slides.getCurrentElement().isNodeFolded(node);
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
