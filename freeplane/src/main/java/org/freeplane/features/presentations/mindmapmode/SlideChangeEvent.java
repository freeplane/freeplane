package org.freeplane.features.presentations.mindmapmode;

public class SlideChangeEvent {
	public final Slide slideModel;

	public SlideChangeEvent(Slide slideModel) {
		super();
		this.slideModel = slideModel;
	}

	public static SlideChangeEvent of(Slide slideModel) {
		return new SlideChangeEvent(slideModel);
	}

}
