package org.freeplane.features.presentations;

public class SlideChangeEvent {
	public final SlideModel slideModel;

	public SlideChangeEvent(SlideModel slideModel) {
		super();
		this.slideModel = slideModel;
	}

	public static SlideChangeEvent of(SlideModel slideModel) {
		return new SlideChangeEvent(slideModel);
	}

}
