package org.freeplane.features.presentations;

public class PresentationModel implements NamedElement{
	private String name;
	public final CollectionModel<SlideModel> slides;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PresentationModel(String name) {
		super();
		this.name = name;
		slides = new CollectionModel<>(SlideModel.class);
	}
	
}
