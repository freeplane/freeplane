package org.freeplane.core.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ColoredIconCreator{
	final private Map<Color, Icon> coloredNoteIcons;
	private final int replacedColorRGB;
	private final Image originalImage;
	public ColoredIconCreator(Image originalImage, Color replacedColor) {
		super();
		replacedColorRGB = 0xffffff & replacedColor.getRGB();
		coloredNoteIcons  = new HashMap<Color, Icon>();
		this.originalImage = originalImage;
	}
	
	private BufferedImage copy(Image img)
	{
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();
	    return bimage;
	}
	
	public Icon createColoredIcon(Color iconColor) {
		Icon icon = coloredNoteIcons.get(iconColor);
		if(icon == null && originalImage != null){
			icon = new ImageIcon(createColoredImage(iconColor));
			coloredNoteIcons.put(iconColor, icon);
		}
		return icon;
	}

	public Image createColoredImage(Color newColor) {
		final int newRGB = 0xffffff & newColor.getRGB();
		if(replacedColorRGB != newRGB && originalImage != null){
			final BufferedImage img = copy(originalImage); 
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					final int rgb =  img.getRGB(x, y);
					if ((0xffffff &rgb) == replacedColorRGB)
						img.setRGB(x, y, 0xff000000 & rgb| newRGB);
				}
			}
			return img;
		}
		else
			return originalImage;
	}
}