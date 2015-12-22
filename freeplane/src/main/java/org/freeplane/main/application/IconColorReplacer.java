package org.freeplane.main.application;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freeplane.core.ui.ColoredIconCreator;
import org.freeplane.core.util.ColorUtils;

public class IconColorReplacer {
	final private List<Image> originalIconImages;
	final Map<Color, List<Image>> derivedImages;
	public IconColorReplacer(List<Image> iconImages) {
		this.originalIconImages = iconImages;
		derivedImages = new HashMap<Color, List<Image>>();
		derivedImages.put(original, iconImages);
	}
	private static final Color original = ColorUtils.stringToColor("#E6D837");
	private static final Color red = ColorUtils.stringToColor("#ff0000");
	private static final Color blue = ColorUtils.stringToColor("#0099ff");
	private static final Color green = ColorUtils.stringToColor("#009900");
	private static final Color purple = ColorUtils.stringToColor("#cc00cc");
	private static final Color orange =  ColorUtils.stringToColor("#ff6600");
	static private Color[] colors = new Color[]{
			original, red, blue, green, purple, orange
	};
	private int currentColorIndex = 0;
	
	public List<Image> getNextIconImages() {
		currentColorIndex++;
		currentColorIndex %= colors.length;
		Color requiredColor = colors[currentColorIndex];
		List<Image> coloredImages = derivedImages.get(requiredColor);
		if(coloredImages != null)
			return coloredImages;
		coloredImages = new ArrayList<Image>(originalIconImages.size());
		for(Image originalImage: originalIconImages){
			ColoredIconCreator coloredIconCreator = new ColoredIconCreator(originalImage, original);
			Image coloredImage = coloredIconCreator.createColoredImage(requiredColor);
			coloredImages.add(coloredImage);
		}
		derivedImages.put(requiredColor, coloredImages);
		return coloredImages;
	}
}
