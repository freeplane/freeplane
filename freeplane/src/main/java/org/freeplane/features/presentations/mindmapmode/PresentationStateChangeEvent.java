package org.freeplane.features.presentations.mindmapmode;


public class PresentationStateChangeEvent {
	
	enum EventType{
		SLIDE_CHANGED, HIGNLIGHTING_CHANGED;
		public PresentationStateChangeEvent of(PresentationStateModel state){
			return new PresentationStateChangeEvent(this, state);
		}
	}
	
	public final PresentationStateModel presentationState;
	public final EventType eventType;

	public PresentationStateChangeEvent(EventType eventType, PresentationStateModel presentationStateModel) {
		super();
		this.eventType = eventType;
		this.presentationState = presentationStateModel;
	}

}
