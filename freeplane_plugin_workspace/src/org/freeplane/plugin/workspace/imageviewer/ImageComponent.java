package org.freeplane.plugin.workspace.imageviewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JComponent;

public class ImageComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	private Image image = null;
	private boolean scaled = false;
	private Dimension size = null;
	private Insets insets = new Insets(0, 0, 0, 0);

	ImageComponent(Image image, boolean scaled) {
		this.image = image;
		this.scaled = scaled;
	}

	void setImage(Image image) {
		this.image = image;
		revalidate();
	}

	public void paint(Graphics g) {
		super.paint(g);
		insets = getInsets(insets);
		size = getSize(size);

		double scalex = (double) size.getWidth() / image.getWidth(this);
		double scaley = (double) size.getHeight() / image.getHeight(this);
		double scale = Math.min(scalex, scaley);

		if (image == null)
			return;
		if (scaled)
			g.drawImage(image, insets.left, insets.top, (int) (image.getWidth(this) * scale) - insets.left - insets.right,
					(int) (image.getWidth(this) * scale) - insets.top - insets.bottom, this);
		else
			g.drawImage(image, insets.left, insets.top, this);
	}

	public Dimension getMinimumSize() {
		int imgw = 32, imgh = 32;
		if (image != null) {
			imgw = image.getWidth(this);
			imgh = image.getHeight(this);
		}
		insets = getInsets(insets);
		return new Dimension(insets.left + Math.max(32, imgw / 10) + insets.right, insets.top + Math.max(32, imgh / 10)
				+ insets.bottom);
	}

	public Dimension getPreferredSize() {
		int imgw = 32, imgh = 32;
		if (image != null) {
			imgw = image.getWidth(this);
			imgh = image.getHeight(this);
		}
		insets = getInsets(insets);
		return new Dimension(insets.left + imgw + insets.right, insets.top + imgh + insets.bottom);
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

}
