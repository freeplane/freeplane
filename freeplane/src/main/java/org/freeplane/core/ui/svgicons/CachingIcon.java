package org.freeplane.core.ui.svgicons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

class CachingIcon implements Icon {
	
	final private Icon icon;
	private double scaleX = 0;
	private double scaleY = 0;
	private BufferedImage cachedImage;
	

	public CachingIcon(Icon icon) {
		super();
		this.icon = icon;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2 = (Graphics2D) g;
		if(g2.getRenderingHint(GraphicsHints.CACHE_ICONS) != Boolean.TRUE) {
			icon.paintIcon(c, g, x, y);
			return;
		}
		
		
		final AffineTransform transform = g2.getTransform();
		final double scaleX = transform.getScaleX();
		final double scaleY = transform.getScaleY();
		if(scaleX != this.scaleX || scaleY != this.scaleY || cachedImage == null) {
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			updateImage();
		}
		Graphics2D gg = (Graphics2D)g.create();
		gg.setTransform(AffineTransform.getTranslateInstance(x * scaleX + transform.getTranslateX(), y * scaleY  + transform.getTranslateY()));
		gg.drawImage(cachedImage, 0, 0, null);
		gg.dispose();
	}

	public int getIconWidth() {
		return icon.getIconWidth();
	}

	public int getIconHeight() {
		return icon.getIconHeight();
	}
	
    private void  updateImage()
    {
        cachedImage = new BufferedImage((int) (getIconWidth() * scaleX), (int) (getIconHeight() * scaleY), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = cachedImage.createGraphics();
        graphics.scale(scaleX, scaleY);
		icon.paintIcon(null, graphics, 0, 0);
    }

	
}
