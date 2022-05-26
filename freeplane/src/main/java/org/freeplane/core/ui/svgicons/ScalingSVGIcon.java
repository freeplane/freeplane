package org.freeplane.core.ui.svgicons;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.kitfox.svg.app.beans.SVGIcon;

class ScalingSVGIcon extends SVGIcon {

	private static final long serialVersionUID = 1L;
	private static final AffineTransform defaultTransform = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
	@Override
	public Image getImage() {
		double scaleX = defaultTransform.getScaleX();
		double scaleY = defaultTransform.getScaleY();
		BufferedImage bi = new BufferedImage(
				(int)(getIconWidth() * scaleX),
				(int)(getIconHeight() * scaleY), 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) bi.getGraphics();
		paintIcon(null, graphics, 0, 0);
		return bi;
	}

}
