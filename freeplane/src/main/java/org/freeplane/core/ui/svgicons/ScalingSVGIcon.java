package org.freeplane.core.ui.svgicons;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.kitfox.svg.app.beans.SVGIcon;

class ScalingSVGIcon extends SVGIcon {

	private static final long serialVersionUID = 1L;
	private static final double scaleX;
	private static final double scaleY;
	static {
		AffineTransform defaultTransform = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
		scaleX = defaultTransform.getScaleX();
		scaleY = defaultTransform.getScaleY();
	}
	@Override
	public Image getImage() {
		BufferedImage bi = new BufferedImage(
				(int)(getIconWidth() * scaleX),
				(int)(getIconHeight() * scaleY), 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) bi.getGraphics();
		paintIcon(null, graphics, 0, 0);
		return bi;
	}

}
