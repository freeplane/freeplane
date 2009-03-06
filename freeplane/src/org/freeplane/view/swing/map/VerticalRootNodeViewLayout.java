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
import java.awt.Point;

import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 */
public class VerticalRootNodeViewLayout extends NodeViewLayoutAdapter {
	static private VerticalRootNodeViewLayout instance = null;
	public static boolean USE_COMMON_OUT_POINT_FOR_ROOT_NODE = ResourceController.getResourceController()
	    .getBooleanProperty(VerticalRootNodeViewLayout.USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING);
	private static final String USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING = "use_common_out_point_for_root_node";

	static VerticalRootNodeViewLayout getInstance() {
		if (VerticalRootNodeViewLayout.instance == null) {
			VerticalRootNodeViewLayout.instance = new VerticalRootNodeViewLayout();
		}
		return VerticalRootNodeViewLayout.instance;
	}

	public Point getMainViewInPoint(final NodeView view) {
		final Point centerPoint = (view.getMainView()).getCenterPoint();
		return centerPoint;
	}

	public Point getMainViewOutPoint(final NodeView view, final NodeView targetView, final Point destinationPoint) {
		final MainView mainView = view.getMainView();
		if (VerticalRootNodeViewLayout.USE_COMMON_OUT_POINT_FOR_ROOT_NODE) {
			if (targetView.isLeft()) {
				return mainView.getLeftPoint();
			}
			else {
				return mainView.getRightPoint();
			}
		}
		final Point p = new Point(destinationPoint);
		UITools.convertPointFromAncestor(view, p, mainView);
		final double nWidth = mainView.getWidth() / 2f;
		final double nHeight = mainView.getHeight() / 2f;
		final Point centerPoint = new Point((int) nWidth, (int) nHeight);
		double angle = Math.atan((p.y - centerPoint.y + 0f) / (p.x - centerPoint.x + 0f));
		if (p.x < centerPoint.x) {
			angle += Math.PI;
		}
		final Point out = new Point(centerPoint.x + (int) (Math.cos(angle) * nWidth), centerPoint.y
		        + (int) (Math.sin(angle) * nHeight));
		return out;
	}

	@Override
	protected void layout() {
		final int rightContentHeight = getChildContentHeight(false);
		int rightChildVerticalShift = getChildVerticalShift(false);
		final int leftContentHeight = getChildContentHeight(true);
		int leftChildVerticalShift = getChildVerticalShift(true);
		final int childHorizontalShift = getChildHorizontalShift();
		final int contentHeight = Math.max(rightContentHeight, leftContentHeight);
		final int x = Math.max(getSpaceAround(), -childHorizontalShift);
		if (getView().isContentVisible()) {
			getContent().setVisible(true);
			final Dimension contentPreferredSize = getContent().getPreferredSize();
			rightChildVerticalShift += (contentPreferredSize.height - rightContentHeight) / 2;
			leftChildVerticalShift += (contentPreferredSize.height - leftContentHeight) / 2;
			final int childVerticalShift = Math.min(rightChildVerticalShift, leftChildVerticalShift);
			final int y = Math.max(getSpaceAround(), -childVerticalShift);
			getContent().setBounds(x, y, contentPreferredSize.width, contentPreferredSize.height);
		}
		else {
			getContent().setVisible(false);
			final int childVerticalShift = Math.min(rightChildVerticalShift, leftChildVerticalShift);
			final int y = Math.max(getSpaceAround(), -childVerticalShift);
			getContent().setBounds(x, y, 0, contentHeight);
		}
		placeLeftChildren(leftChildVerticalShift);
		final int width1 = getView().getWidth();
		final int height1 = getView().getHeight();
		placeRightChildren(rightChildVerticalShift);
		final int width2 = getView().getWidth();
		final int height2 = getView().getHeight();
		getView().setSize(Math.max(width1, width2), Math.max(height1, height2));
	}

	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
		final NodeView movedView = view.getMovedView();
		final JComponent content = movedView.getContent();
		location.x = 0;
		location.y = -LISTENER_VIEW_WIDTH;
		UITools.convertPointToAncestor(content, location, view.getParent());
		view.setLocation(location);
		view.setSize(content.getWidth(), LISTENER_VIEW_WIDTH);
	}
}
