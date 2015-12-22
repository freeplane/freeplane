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

import java.awt.Dimension;
import java.awt.Insets;

import org.freeplane.features.nodestyle.ShapeConfigurationModel;

abstract class VariableInsetsMainView extends ShapedMainView {
	private static final long serialVersionUID = 1L;
	private int zoomedVerticalInset;
	private int zoomedHorizontalInset;
	
	public VariableInsetsMainView(ShapeConfigurationModel shapeConfiguration) {
		super(shapeConfiguration);
        zoomedVerticalInset = zoomedHorizontalInset = getMinimumHorizontalInset();
	}
	
	protected boolean areInsetsFixed() {
		return false;
	}


	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		final Dimension prefSize = getPreferredRectangleSizeWithoutMargin(getMaximumWidth());
		final double widthWithMargin = Math.max(prefSize.width*getHorizontalMarginFactor(), prefSize.width + getZoom() * getMinimumHorizontalInset());
		prefSize.width =  limitWidth((int) Math.ceil(widthWithMargin));
		prefSize.height = (int) Math.ceil(Math.max(prefSize.height *getVerticalMarginFactor(), prefSize.height + getZoom() * getMinimumVerticalInset()));
		return prefSize;
	}
	
	abstract protected double getVerticalMarginFactor() ;
	
	abstract protected double getHorizontalMarginFactor();
	
	protected int getMinimumHorizontalInset(){
		return getShapeConfiguration().getHorizontalMargin().toBaseUnitsRounded();
	}

	protected int getMinimumVerticalInset(){
		return getShapeConfiguration().getVerticalMargin().toBaseUnitsRounded();
	}

	protected Dimension getPreferredRectangleSizeWithoutMargin(int maximumWidth) {
		int scaledMaximumWidth = maximumWidth != Integer.MAX_VALUE ? (int)(maximumWidth / getHorizontalMarginFactor()) : maximumWidth;
		final int zoomedHorizontalInsetBackup = zoomedHorizontalInset;
		final int zoomedVerticalInsetBackup = zoomedVerticalInset;
		final float zoom = getZoom();
		zoomedHorizontalInset  = (int) (zoom * getMinimumHorizontalInset());
		zoomedVerticalInset =  (int) (zoom * getMinimumVerticalInset());
		final int oldMinimumWidth = getMinimumWidth();
		final int oldMaximumWidth = getMaximumWidth();
		final Dimension prefSize;
		try{
			this.setMinimumWidth(0);
			this.setMaximumWidth(scaledMaximumWidth);
			prefSize = super.getPreferredSize();
			prefSize.width -= zoomedHorizontalInset;
			prefSize.height -= zoomedVerticalInset;
		}
		finally {
			zoomedHorizontalInset = zoomedHorizontalInsetBackup;
			zoomedVerticalInset = zoomedVerticalInsetBackup;
			setMaximumWidth(oldMaximumWidth);
			setMinimumWidth(oldMinimumWidth);
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
		final int oldMinimumWidth = getMinimumWidth();
		setMinimumWidth(0);
		Dimension preferredRectangleSize = getPreferredRectangleSizeWithoutMargin(getMaximumWidth());
		final Dimension preferredSize = getPreferredSize();
		setMinimumWidth(oldMinimumWidth);
		super.setBounds(x, y, width, height);
		zoomedHorizontalInset = (Math.min(preferredSize.width, width) - preferredRectangleSize.width) / 2;
		zoomedVerticalInset = (Math.min(preferredSize.height, height) - preferredRectangleSize.height) / 2;
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
}
