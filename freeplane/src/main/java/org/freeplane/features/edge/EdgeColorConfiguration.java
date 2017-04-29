package org.freeplane.features.edge;

import java.awt.Color;
import java.util.List;

public class EdgeColorConfiguration{
	private static final int FIRST_CYCLIC_STYLE_LEVEL = 1;
	public List<Color> colors;

	public EdgeColorConfiguration(List<Color> colors) {
		super();
		this.colors = colors;
	}

	public boolean areEdgeColorsAvailable() {
		return ! colors.isEmpty();
	}

	public Color getEdgeColor(int colorCounter) {
		if(colorCounter < colors.size())
			return colors.get(colorCounter);
		else
			return colors.get(FIRST_CYCLIC_STYLE_LEVEL + (colorCounter - FIRST_CYCLIC_STYLE_LEVEL) % (colors.size() - FIRST_CYCLIC_STYLE_LEVEL)); 
	}
}
