package org.freeplane.features.presentations.mindmapmode;


public class PresentationStateChangeEvent {
	
	enum EventType{
		SLIDE_CHANGED, HIGNLIGHTING_CHANGED;
		public PresentationStateChangeEvent of(PresentationState state){
			return new PresentationStateChangeEvent(this, state);
		}
	}
	
	public final PresentationState presentationState;
	public final EventType eventType;

	public PresentationStateChangeEvent(EventType eventType, PresentationState presentationStateModel) {
		super();
		this.eventType = eventType;
		this.presentationState = presentationStateModel;
	}

}
