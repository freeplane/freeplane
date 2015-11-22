/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.SwingConstants;

import org.freeplane.features.nodelocation.LocationModel;

abstract class OvalMainView extends ShapedMainView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int zoomedVerticalInset;
	private int zoomedHorizontalInset;

	
	public OvalMainView() {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        zoomedVerticalInset = zoomedHorizontalInset = 0;
    }

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
	}

	protected Point getConnectorPointAtTheOvalBorder(Point p) {
		final double nWidth = this.getWidth() / 2f;
    	final double nHeight = this.getHeight() / 2f;
    	int dx = Math.max(Math.abs(p.x -  this.getWidth()/2), getNodeView().getZoomed(LocationModel.HGAP));
    	if(p.x < this.getWidth()/2)
    		dx = -dx;
    	double angle = Math.atan((p.y - nHeight) / dx);
    	if (dx < 0) {
    		angle += Math.PI;
    	}
    	final Point out = new Point((int) ((1f + Math.cos(angle)) * nWidth), (int) ((1f + Math.sin(angle)) * nHeight));
		return out;
	}
	
	@Override
	public Dimension getPreferredSize(int minimumWidth, int maximumWidth) {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize(minimumWidth, maximumWidth);
		}
		final Dimension prefSize = getPreferredSizeWithoutMargin(maximumWidth);
		prefSize.width = (int) Math.ceil(prefSize.width *getHorizontalMarginFactor());
		prefSize.height = (int) Math.ceil(prefSize.height *getVerticalMarginFactor());
		if(prefSize.width < minimumWidth)
			prefSize.width = minimumWidth;
		return prefSize;
	}

	protected double getVerticalMarginFactor() {
		return (double) 1.5;
	}

	protected double getHorizontalMarginFactor() {
		return 1.4;
	}
	
	protected Dimension getPreferredSizeWithoutMargin(int maximumWidth) {
		int scaledMaximumWidth = maximumWidth != Integer.MAX_VALUE ? (int)(maximumWidth / getHorizontalMarginFactor()) : maximumWidth;
		final int zoomedHorizontalInsetBackup = zoomedHorizontalInset;
		final int zoomedVerticalInsetBackup = zoomedVerticalInset;
		zoomedHorizontalInset = zoomedVerticalInset = 0;
		final Dimension prefSize;
		try{
			prefSize = super.getPreferredSize(0, scaledMaximumWidth);
		}
		finally {
			zoomedHorizontalInset = zoomedHorizontalInsetBackup;
			zoomedVerticalInset = zoomedVerticalInsetBackup;
		}
		return prefSize;
	}

	@Override
	public Insets getZoomedInsets() {
		int topInset = getZoomedVerticalInset();
		int leftInset = getZoomedHorizontalInset();
		return new Insets(topInset, leftInset, topInset, leftInset);
	}

	protected int getZoomedVerticalInset() {
		return zoomedVerticalInset;
	}

	protected int getZoomedHorizontalInset() {
		return zoomedHorizontalInset;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		Dimension preferredSize = getPreferredSizeWithoutMargin(width);
		super.setBounds(x, y, width, height);
		zoomedHorizontalInset = Math.max(0, width - preferredSize.width) / 2;
		zoomedVerticalInset = Math.max(0, height - preferredSize.height) / 2;
	}

	@Override
	public Insets getInsets() {
		Insets insets = getZoomedInsets();
		float zoom = getZoom();
		if(zoom != 1f) {
			insets.left /= zoom;
			insets.right /= zoom;
			insets.top /= zoom;
			insets.bottom /= zoom;
		}
		return insets;
	}

	@Override
	public Insets getInsets(Insets insets) {
		return getInsets();
	}
	
	
	@Override
	public Point getConnectorPoint(Point p) {
		return USE_COMMON_OUT_POINT_FOR_ROOT_NODE || ! getNodeView().isRoot() ? 
				super.getConnectorPoint(p) : getConnectorPointAtTheOvalBorder(p);
	}

}
