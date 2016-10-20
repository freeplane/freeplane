package org.freeplane.features.presentations.mindmapmode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.MapExtensions;

public class MapPresentations implements IExtension{
	static {
		MapExtensions.registerMapExtension(MapPresentations.class);
	}
	
	public final NamedElementCollection<Presentation> presentations;

	public MapPresentations(NamedElementFactory<Presentation> factory) {
		super();
		this.presentations = new NamedElementCollection<>(factory);
	}
	
}
