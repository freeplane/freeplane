package org.freeplane.main.application;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

class ColoredIconCreator{
	final private Map<Color, Icon> coloredNoteIcons;
	private final Image originalImage;
    private final Color replacedColor;
	public ColoredIconCreator(Image originalImage, Color replacedColor) {
		super();
        this.replacedColor = replacedColor;
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
		if(! replacedColor.equals(newColor) && originalImage != null){
			final BufferedImage img = copy(originalImage); 
			int width = img.getWidth();
            for (int x = 0; x < width; x++) {
				int height = img.getHeight();
                for (int y = 0; y < height; y++) {
					final int rgb =  img.getRGB(x, y);
					if (shouldReplace(rgb))
						img.setRGB(x, y, 0xff000000| newRGB);
				}
			}
			return img;
		}
		else
			return originalImage;
	}

    private boolean shouldReplace(final int rgb) {
        return ((rgb >> 16) & 0xff) != 0;
   }
}