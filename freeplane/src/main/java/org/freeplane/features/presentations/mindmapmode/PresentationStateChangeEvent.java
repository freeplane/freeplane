package org.freeplane.features.presentations.mindmapmode;


public class PresentationStateChangeEvent {
	
	enum EventType{
		SLIDE_CHANGED, HIGNLIGHTING_CHANGED, PLAYING_STATE_CHANGED;
		public PresentationStateChangeEvent of(PresentationState state){
			return new PresentationStateChangeEvent(this, state);
		}
	}
	
	public final PresentationState presentationState;
	public final EventType eventType;

	public PresentationStateChangeEvent(EventType eventType, PresentationState presentationState) {
		super();
		this.eventType = eventType;
		this.presentationState = presentationState;
	}

}
