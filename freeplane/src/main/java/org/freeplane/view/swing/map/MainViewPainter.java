/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General License for more details.
 *
 *  You should have received a copy of the GNU General License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import org.freeplane.features.map.MapController;
import org.freeplane.features.nodestyle.NodeGeometryModel;

abstract class MainViewPainter{

	MainView mainView;
	MainViewPainter(MainView mainView){
		this.mainView = mainView;

	}
	int getMainViewHeightWithFoldingMark() {
		return mainView.getHeight();
	}
	/** get width including folding symbol */
	int getMainViewWidthWithFoldingMark() {
		int width = mainView.getWidth();
		final NodeView nodeView = mainView.getNodeView();
		if (nodeView.isFolded()) {
			width += mainView.getZoomedFoldingSymbolHalfWidth() * 3;
		}
		return width;
	}

	int getSingleChildShift() {
		return 0;
	}

    Point getConnectorPoint(Point relativeLocation) {
        if(relativeLocation.x > mainView.getWidth())
            return getRightPoint();
        if(relativeLocation.x < 0)
            return getLeftPoint();
        if(relativeLocation.y > mainView.getHeight()){
            final Point bottomPoint = mainView.getBottomPoint();
            bottomPoint.y = mainView.getNodeView().getContent().getHeight();
			return bottomPoint;
        }
        if(relativeLocation.y <0)
            return mainView.getTopPoint();
        return getCenterPoint();
    }

    Point getCenterPoint() {
        return new Point(mainView.getWidth()/2, mainView.getHeight()/2);
    }

	abstract Point getLeftPoint();

	abstract Point getRightPoint();

	abstract NodeGeometryModel getShapeConfiguration();

	abstract void paintBackground(final Graphics2D graphics, final Color color);

	void paintComponent(final Graphics graphics) {
		mainView.paintComponentDefault(graphics);
	}

	void paintDecoration(final NodeView nodeView, final Graphics2D g) {
		mainView.drawModificationRect(g);
		mainView.paintDragRectangle(g);
		paintFoldingMark(nodeView, g);
        if (mainView.isShortened()) {
        	FoldingMark.SHORTENED.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, 7./3, 5./3));
        } else if (mainView.shouldPaintCloneMarker(nodeView)){
			if (nodeView.getModel().isCloneTreeRoot())
				FoldingMark.CLONE.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, 2, 2.5));
			else if (nodeView.getModel().isCloneTreeNode())
				FoldingMark.CLONE.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, 1.5, 2.5));
		}
	}

	void paintFoldingMark(final NodeView nodeView, final Graphics2D g) {
		if (! mainView.hasChildren())
			return;
		final MapView map = mainView.getMap();
		final MapController mapController = map.getModeController().getMapController();
		final FoldingMark markType = mainView.foldingMarkType(mapController, nodeView);
		if(mainView.getMouseArea() != MouseArea.OUT && ! map.isPrinting()){
			final int width = Math.max(MainView.FOLDING_CIRCLE_WIDTH, mainView.getZoomedFoldingSymbolHalfWidth() * 2);
			final Point p = mainView.getNodeView().isLeft() ? getLeftPoint() : getRightPoint();
			if(p.y + width/2 > mainView.getHeight())
				p.y = mainView.getHeight() - width;
			else
				p.y -= width/2;
			if(nodeView.isLeft())
				p.x -= width;
			final FoldingMark foldingCircle;
			if(markType.equals(FoldingMark.UNFOLDED)) {
				if(nodeView.hasHiddenChildren())
					foldingCircle = FoldingMark.FOLDING_CIRCLE_HIDDEN_CHILD;
				else
					foldingCircle = FoldingMark.FOLDING_CIRCLE_UNFOLDED;
            }
			else{
				foldingCircle = FoldingMark.FOLDING_CIRCLE_FOLDED;
			}
            foldingCircle.draw(g, nodeView, new Rectangle(p.x, p.y, width, width));
		}
		else{
			final int halfWidth = mainView.getZoomedFoldingSymbolHalfWidth();
			final Point p = mainView.getNodeView().isLeft() ? getLeftPoint() : getRightPoint();
			if (p.x <= 0) {
				p.x -= halfWidth;
			}
			else {
				p.x += halfWidth;
			}
			markType.draw(g, nodeView, new Rectangle(p.x - halfWidth, p.y-halfWidth, halfWidth*2, halfWidth*2));
		}
	}

	boolean areInsetsFixed() {
		return true;
	}

	Insets getZoomedInsets() {
		return mainView.getDefaultZoomedInsets();
	}

	Insets getInsets() {
		return mainView.getDefaultInsets();
	}

	Insets getInsets(Insets insets) {
		return mainView.getDefaultInsets(insets);
	}

	Dimension getPreferredSize() {
		return mainView.getDefaultPreferredSize();
	}

	void setBounds(int x, int y, int width, int height) {
		mainView.setBoundsDefault(x, y, width, height);
	}
 }
