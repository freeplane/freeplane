/*
 * Preview Dialog - A Preview Dialog for your Swing Applications Copyright (C)
 * 2003 Jens Kaiser. Written by: 2003 Jens Kaiser <jens.kaiser@web.de> This
 * program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details. You should have received a copy of
 * the GNU Library General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 */
package org.freeplane.features.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;

import org.freeplane.core.util.LogUtils;
import org.freeplane.view.swing.map.MapView;

class Preview extends JComponent {
	final private static double MINIMUM_ZOOM_FACTOR = 0.1;
	
	private static final long serialVersionUID = 1L;
	private Graphics2D imageGraphics;
	protected int index = 0;
	private BufferedImage previewPageImage = null;
	private final PrintController printController;
	private final Printable view;
	protected double zoom = 0.0;

	public Preview(final PrintController printController, final Printable view, Dimension size) {
		this.printController = printController;
		this.view = view;
		final PageFormat format = getPageFormat();
		this.zoom = Math.min(size.getHeight() / format.getHeight(), size.getWidth() / format.getWidth());
		resize();
	}

	public void changeZoom(final double zoom) {
		this.zoom = Math.max(Preview.MINIMUM_ZOOM_FACTOR, this.zoom * zoom);
		resize();
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	private PageFormat getPageFormat() {
		return printController.getPageFormat();
	}

	private int getPageHeight(final PageFormat format) {
		return (int) (format.getHeight() * zoom);
	}

	public int getPageIndex() {
		return index;
	}

	private int getPageWidth(final PageFormat format) {
		return (int) (format.getWidth() * zoom);
	}

	public void moveIndex(final int indexStep) {
		final int newIndex = index + indexStep;
		if (newIndex >= 0) {
			index = newIndex;
			previewPageImage = null;
		}
	}

	@Override
	public void paint(final Graphics g) {
		try {
			final Graphics2D g2d = (Graphics2D) g;
			final PageFormat format = getPageFormat();
			paintPaper(g, format);
			if (previewPageImage == null) {
				previewPageImage = (BufferedImage) createImage(getPageWidth(format) - 1, getPageHeight(format) - 1);
				imageGraphics = previewPageImage.createGraphics();
				imageGraphics.scale(zoom, zoom);
				if(view instanceof MapView)
					((MapView)view).preparePrinting();
				while (Printable.NO_SUCH_PAGE == view.print(imageGraphics, format, index) && index > 0) {
					index --;
				}
				if(view instanceof MapView)
					((MapView)view).endPrinting();
			}
			g2d.drawImage(previewPageImage, 0, 0, this);
		}
		catch (final PrinterException e) {
			LogUtils.severe(e);
		}
	}

	protected void paintPaper(final Graphics g, final PageFormat format) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getPageWidth(format), getPageHeight(format));
		g.setColor(Color.black);
		g.drawRect(0, 0, getPageWidth(format) - 1, getPageHeight(format) - 1);
	}

	public void resize() {
		final PageFormat pageFormat = getPageFormat();
		int width = getPageWidth(pageFormat);
		int height = getPageHeight(pageFormat);
		setPreferredSize(new Dimension(width, height));
		previewPageImage = null;
		revalidate();
	}
}
