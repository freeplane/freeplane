package org.freeplane.core.ui.svgicons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.ImageIcon;

class CachingIcon implements Icon {

	private Supplier<ImageIcon> iconSupplier;
	private ImageIcon icon;
	private double scaleX = 0;
	private double scaleY = 0;
	private BufferedImage cachedImage;


	public CachingIcon(Supplier<ImageIcon> iconSupplier) {
		super();
		this.iconSupplier = iconSupplier;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		final Graphics2D g2 = (Graphics2D) g;
		if(g2.getRenderingHint(GraphicsHints.CACHE_ICONS) != Boolean.TRUE
				|| getIcon().getIconHeight() <= 0 || getIcon().getIconWidth() <= 0) {
			getIcon().paintIcon(c, g, x, y);
			return;
		}


		final AffineTransform transform = g2.getTransform();
		final double scaleX = transform.getScaleX();
		final double scaleY = transform.getScaleY();
		if(scaleX != this.scaleX || scaleY != this.scaleY || cachedImage == null) {
			final int scaledWidth = (int) (getIconWidth() * scaleX);
			final int scaledHeight = (int) (getIconHeight() * scaleY);
			if(scaledHeight <= 0 || scaledWidth <= 0)
				return;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			updateImage(scaledWidth, scaledHeight);
		}
		Graphics2D gg = (Graphics2D)g.create();
		gg.setTransform(AffineTransform.getTranslateInstance(x * scaleX + transform.getTranslateX(), y * scaleY  + transform.getTranslateY()));
		gg.drawImage(cachedImage, 0, 0, null);
		gg.dispose();
	}

	@Override
	public int getIconWidth() {
		return getIcon().getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return getIcon().getIconHeight();
	}

    private void updateImage(final int scaledWidth, final int scaledHeight) {
		cachedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = cachedImage.createGraphics();
        graphics.scale(scaleX, scaleY);
		getIcon().paintIcon(null, graphics, 0, 0);
	}

	public ImageIcon getImageIcon() {
		return getIcon();
	}

	private ImageIcon getIcon() {
		if(icon == null && iconSupplier != null) {
			icon = iconSupplier.get();
			iconSupplier = null;
		}
		return icon;
	}


}
