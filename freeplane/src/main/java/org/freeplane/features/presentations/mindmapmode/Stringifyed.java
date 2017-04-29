package org.freeplane.features.presentations.mindmapmode;

public class Stringifyed<T extends NamedElement>{
	public final T element;

	public Stringifyed(T element) {
		this.element = element;
	}

	@Override
	public String toString() {
		return element.getName();
	}
	
	public Stringifyed<T> valueOf(String newName){
		element.setName(newName);
		return this;
	}
}
