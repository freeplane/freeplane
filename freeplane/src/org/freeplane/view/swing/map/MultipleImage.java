/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;

public class MultipleImage extends ImageIcon {
	private boolean isDirty;
	final private Vector mImages = new Vector();
	private double zoomFactor = 1;

	public MultipleImage(final double zoom) {
		zoomFactor = zoom;
		isDirty = true;
	};

	public void addImage(final ImageIcon image) {
		mImages.add(image);
		setImage(image.getImage());
		isDirty = true;
	};

	@Override
	public int getIconHeight() {
		int myY = 0;
		for (int i = 0; i < mImages.size(); i++) {
			final int otherHeight = ((ImageIcon) mImages.get(i)).getIconHeight();
			if (otherHeight > myY) {
				myY = otherHeight;
			}
		}
		return (int) (myY * zoomFactor);
	};

	@Override
	public int getIconWidth() {
		int myX = 0;
		for (int i = 0; i < mImages.size(); i++) {
			myX += ((ImageIcon) mImages.get(i)).getIconWidth();
		}
		return (int) (myX * zoomFactor);
	}

	@Override
	public Image getImage() {
		if (!isDirty) {
			return super.getImage();
		}
		final int w = getIconWidth();
		final int h = getIconHeight();
		if (w == 0 || h == 0) {
			return null;
		}
		final BufferedImage outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = outImage.createGraphics();
		double myX = 0;
		for (final Iterator i = mImages.iterator(); i.hasNext();) {
			final ImageIcon currentIcon = (ImageIcon) i.next();
			final double pwidth = (currentIcon.getIconWidth() * zoomFactor);
			final AffineTransform inttrans = AffineTransform.getScaleInstance(zoomFactor,
			    zoomFactor);
			g.drawImage(currentIcon.getImage(), inttrans, null);
			g.translate(pwidth, 0);
			myX += pwidth;
		}
		g.dispose();
		setImage(outImage);
		isDirty = false;
		return super.getImage();
	}

	public int getImageCount() {
		return mImages.size();
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		if (getImage() != null) {
			super.paintIcon(c, g, x, y);
		}
	}
};
