package org.freeplane.features.presentations;

public class SlideModel implements NamedElement{
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SlideModel(String name) {
		super();
		this.name = name;
	}
	
}
