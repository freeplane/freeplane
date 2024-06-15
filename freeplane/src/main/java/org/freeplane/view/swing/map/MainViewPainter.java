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
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.view.swing.map.MainView.ConnectorLocation;

abstract class MainViewPainter{

    private static final Rectangle EMPTY_RECTANGLE = new Rectangle();
    MainView mainView;
    MainViewPainter(MainView mainView){
        this.mainView = mainView;

    }
    int getMainViewHeightWithFoldingMark(boolean onlyFolded) {
        int height = mainView.getHeight();
        NodeView nodeView = mainView.getNodeView();
        if (nodeView.usesHorizontalLayout() &&  (! onlyFolded || nodeView.isFolded())) {
            height += 2 * mainView.getZoomedFoldingMarkHalfWidth();
        }
        return height;
    }
    int getMainViewWidthWithFoldingMark(boolean onlyFolded) {
        int width = mainView.getWidth();
        final NodeView nodeView = mainView.getNodeView();
        if (! nodeView.usesHorizontalLayout() && (! onlyFolded || nodeView.isFolded())) {
            width += mainView.getZoomedFoldingMarkHalfWidth() * 3;
        }
        return width;
    }

	int getSingleChildShift() {
		return 0;
	}

	Point getConnectorPoint(@SuppressWarnings("unused") Point relativeLocation,
	        ConnectorLocation connectorLocation) {
		return connectorLocation.pointSupplier.apply(this);
    }

    Point getCenterPoint() {
        return new Point(mainView.getWidth()/2, mainView.getHeight()/2);
    }

	abstract Point getLeftPoint();

	abstract Point getRightPoint();

    Point getTopPoint() {
        return new Point(mainView.getWidth()/2, 0);
    }

    Point getBottomPoint() {
        return new Point(mainView.getWidth()/2, mainView.getHeight());
    }

	abstract NodeGeometryModel getShapeConfiguration();

	abstract void paintBackground(final Graphics2D graphics, final Color color);

	void paintComponent(final Graphics graphics) {
		mainView.paintComponentDefault(graphics);
	}

	void paintDecoration(final NodeView nodeView, final Graphics2D g) {
		mainView.drawModificationRect(g);
		mainView.paintDragRectangle(g);
		paintFoldingMark(nodeView, g);
        boolean isMinimized = mainView.isShortened();
        boolean shouldPaintCloneMarker = nodeView.getNode().isCloneNode() && mainView.shouldPaintCloneMarker(nodeView);
		if (isMinimized) {
        	FoldingMark.SHORTENED.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, shouldPaintCloneMarker ? 0.6 : 0, 7./3, 5./3));
        }
		if (shouldPaintCloneMarker){
			if (nodeView.getNode().isCloneTreeRoot()) {
				if (nodeView.getNode().getChildren().stream().anyMatch(NodeModel::isCloneTreeNode))
					FoldingMark.CLONE.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, isMinimized ? -0.6 : 0, 2.4, 2.5));
				else
					FoldingMark.CLONE.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, isMinimized ? -0.6 : 0, 2.4, 1.8));
            } else if (nodeView.getNode().isCloneTreeNode())
				FoldingMark.CLONE.draw(g, nodeView, mainView.decorationMarkBounds(nodeView, isMinimized ? -0.6 : 0, 1.5, 2.5));
		}
	}

	void paintFoldingMark(final NodeView nodeView, final Graphics2D g) {
		if (! mainView.hasChildren())
			return;
		final MapView map = mainView.getMap();
		final MapController mapController = map.getModeController().getMapController();
		final FoldingMark markType = mainView.foldingMarkType(mapController, nodeView);
		boolean drawsControls = mainView.getMouseArea() != MouseArea.OUT && ! map.isPrinting();
		if(markType == FoldingMark.FOLDING_CIRCLE_UNFOLDED && ! drawsControls)
		    return;
		Rectangle markBounds = getFoldingRectangleBounds(nodeView, drawsControls);
		(drawsControls || markType != FoldingMark.FOLDING_CIRCLE_FOLDED ? markType : FoldingMark.FOLDING_CIRCLE_UNFOLDED)
			.draw(g, nodeView, markBounds);
	}

	Rectangle getFoldingRectangleBounds(final NodeView nodeView, boolean drawsControls) {
        final int width = drawsControls ? Math.max(mainView.getZoomedFoldingSwitchMinWidth(),
                mainView.getZoomedFoldingMarkHalfWidth() * 2) : mainView.getZoomedFoldingMarkHalfWidth() * 2;
		final int halfWidth = width / 2;
		final Point p;
		if(! drawsControls && ! nodeView.isFolded())
		    return EMPTY_RECTANGLE;
		if(nodeView.usesHorizontalLayout()) {
		    if(nodeView.isTopOrLeft()) {
		        p = getTopPoint();
                p.y -= halfWidth;
		    }
		    else {
                p = getBottomPoint();
                p.y += halfWidth;
		    }
		}
		else {
		    p = mainView.getNodeView().paintsChildrenOnTheLeft() ? getLeftPoint() : getRightPoint();
		    if (p.x <= 0) {
		        p.x -= halfWidth;
		    }
		    else {
		        p.x += halfWidth;
		    }
		}
		Rectangle markBounds = new Rectangle(p.x - halfWidth, p.y-halfWidth, halfWidth*2, halfWidth*2);
        return markBounds;
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
