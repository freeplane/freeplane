package org.freeplane.features.edge;

import java.awt.Color;
import java.util.List;

public class EdgeColorConfiguration{
	public List<Color> colors;

	public EdgeColorConfiguration(List<Color> colors) {
		super();
		this.colors = colors;
	}

	public boolean areEdgeColorsAvailable() {
		return ! colors.isEmpty();
	}

	public Color getEdgeColor(int colorCounter) {
		return colors.get((colorCounter-1) % colors.size()); 
	}
}
