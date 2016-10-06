package org.freeplane.features.presentations;


public class PresentationStateChangeEvent {
	public final PresentationStateModel presentationModel;

	public PresentationStateChangeEvent(PresentationStateModel presentationStateModel) {
		super();
		this.presentationModel = presentationStateModel;
	}

	public static PresentationStateChangeEvent of(PresentationStateModel presentationStateModel) {
		return new PresentationStateChangeEvent(presentationStateModel);
	}

}
