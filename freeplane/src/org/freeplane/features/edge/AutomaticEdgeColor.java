package org.freeplane.features.edge;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;

public class AutomaticEdgeColor implements IExtension{
	public enum Rule {
		/*FOR_BRANCH(true), */ FOR_COLUMN(true), ON_BRANCH_CREATION(false);
		
		public final boolean isActiveOnCreation;
		public final boolean isDynamic;

		private Rule(boolean isDynamic) {
			this.isActiveOnCreation = ! isDynamic;
			this.isDynamic = isDynamic;
		} 
		};
	static final int MAXIMUM_COLOR_NUMBER = 12;
	private int colorCount; 
	final public Rule rule;
	public int getColorCount() {
    	return colorCount;
    }
	public AutomaticEdgeColor(Rule rule, int colorCount) {
	    super();
		this.rule = rule;
	    this.colorCount = colorCount;
    }

	Color nextColor() {
		int skippedColorNumber = 0;
		final ResourceController resourceController = ResourceController.getResourceController();
		if (colorCount >= MAXIMUM_COLOR_NUMBER) {
			colorCount = 0;
		}

		while (!resourceController.getBooleanProperty("use_auto_edge_color_" + colorCount)
		        && skippedColorNumber < MAXIMUM_COLOR_NUMBER) {
			colorCount++;
			if (colorCount >= MAXIMUM_COLOR_NUMBER)
				colorCount = 0;
			skippedColorNumber++;
		}

		if (skippedColorNumber < MAXIMUM_COLOR_NUMBER) {
			String colorSpec = resourceController.getProperty("auto_edge_color_" + colorCount);
			Color color = ColorUtils.stringToColor(colorSpec);
			colorCount++;
			return color;
		}
		else
			return Color.BLACK;

    }
}