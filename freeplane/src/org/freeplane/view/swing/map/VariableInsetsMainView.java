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

abstract class VariableInsetsMainView extends ShapedMainView {
	private static final long serialVersionUID = 1L;
	private int zoomedVerticalInset;
	private int zoomedHorizontalInset;
	
	

	public VariableInsetsMainView() {
        zoomedVerticalInset = zoomedHorizontalInset = getMinimumHorizontalInset();
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

	abstract protected double getVerticalMarginFactor() ;
	
	abstract protected double getHorizontalMarginFactor();
	
	protected int getMinimumHorizontalInset(){
		return 0;
	}

	protected int getMinimumVerticalInset(){
		return 0;
	}

	protected Dimension getPreferredSizeWithoutMargin(int maximumWidth) {
		int scaledMaximumWidth = maximumWidth != Integer.MAX_VALUE ? (int)(maximumWidth / getHorizontalMarginFactor()) : maximumWidth;
		final int zoomedHorizontalInsetBackup = zoomedHorizontalInset;
		final int zoomedVerticalInsetBackup = zoomedVerticalInset;
		zoomedHorizontalInset  = getMinimumHorizontalInset();
		zoomedVerticalInset =  getMinimumVerticalInset();
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
		zoomedHorizontalInset = Math.max((int)(getZoom()*getMinimumHorizontalInset()), (width - preferredSize.width) / 2);
		zoomedVerticalInset = Math.max((int)(getZoom()*getMinimumVerticalInset()), (height - preferredSize.height) / 2);
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
