package org.freeplane.features.edge;

import java.awt.Color;
import java.util.List;

public class EdgeColorConfiguration{
	private static final int ROOT_COLOR_INDEX = 0;
	private static final int FIRST_CYCLIC_COLOR_INDEX = ROOT_COLOR_INDEX + 1;
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
		else if(FIRST_CYCLIC_COLOR_INDEX == colors.size())
			return EdgeController.STANDARD_EDGE_COLOR;
		else
			return colors.get(FIRST_CYCLIC_COLOR_INDEX + (colorCounter - FIRST_CYCLIC_COLOR_INDEX) % (colors.size() - FIRST_CYCLIC_COLOR_INDEX)); 
	}
}
