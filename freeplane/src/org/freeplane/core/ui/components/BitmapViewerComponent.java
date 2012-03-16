/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import com.thebuzzmedia.imgscalr.Scalr;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
public class BitmapViewerComponent extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int hint;
	private BufferedImage cachedImage;
	private final URL url;
	private final Dimension originalSize;
	private int imageX;
	private int imageY;

	protected int getHint() {
		return hint;
	}

	public void setHint(final int hint) {
		this.hint = hint;
	}

	public BitmapViewerComponent(final URI uri) throws MalformedURLException, IOException {
		url = uri.toURL();
		cachedImage = ImageIO.read(url);
		originalSize = new Dimension(cachedImage.getWidth(), cachedImage.getHeight());
		hint = Image.SCALE_SMOOTH;
	}

	public Dimension getOriginalSize() {
		return new Dimension(originalSize);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		if (width == 0 || height == 0) {
			return;
		}
		if(cachedImage == null || cachedImage.getWidth() != width || cachedImage.getHeight() != height){
			BufferedImage image;
	        try {
		        image = ImageIO.read(url);
	        }
	        catch (IOException e) {
				super.paintComponent(g);
				return;
	        }
			final int imageWidth = image.getWidth();
			final int imageHeight = image.getHeight();
			if(imageWidth == 0 || imageHeight == 0){
				super.paintComponent(g);
				return;
			}
			final BufferedImage scaledImage = Scalr.resize(image, width,height);
			image.flush();
			final int scaledImageHeight = scaledImage.getHeight();
			final int scaledImageWidth = scaledImage.getWidth();
			if (scaledImageHeight > height) {
				imageX = 0;
				imageY = (height - scaledImageHeight) / 2;
			}
			else {
				imageX = (width - scaledImageWidth) / 2;
				imageY = 0;
			}
			cachedImage = scaledImage;
		}
		g.drawImage(cachedImage, imageX, imageY, null);
	}
}
